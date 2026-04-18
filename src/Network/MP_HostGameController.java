package Network;

import Pack_1.QModel;
import Pack_1.Question;
import Pack_1.Answer;
import Database.QuestionLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Pack_1.profile.Session;
import Pack_1.profile.UserManager;

/**
 * Host-side multiplayer game controller.
 * - Owns a QModel and drives the question/timer loop.
 * - Evaluates correctness per player.
 * - Tracks per-player total money (sum of full question values).
 * - Tracks tier hits for ladder pips.
 * - Broadcasts QUESTION, TIMER, BOARD, WIN, LIFELINE, SCOREBOARD_TIMER.
 */
public class MP_HostGameController {

	private static final int QUESTION_TIME = 40;
	private static final int SCOREBOARD_TIME = 30;

	private final MP_QView mpView;
	private final MP_Server server;
	private final MP_Client client;
	private final String hostNamed;

	private final QModel model;
	private Timeline timer;
	private int secondsRemaining;

	private boolean superpositionUsed = false;
	private boolean entanglementUsed = false;
	private boolean interferenceUsed = false;
	private String entangledLabel = null;

	private Timeline scoreboardTimer;
	private int scoreboardSecondsRemaining = SCOREBOARD_TIME;
	private final Set<String> continueClicks = new HashSet<>();

	private final Map<String, String> currentAnswers = new ConcurrentHashMap<>();
	private final Map<String, Boolean> playerStatus = new ConcurrentHashMap<>();

	private final Map<String, Integer> playerEarnings = new HashMap<>();

	private final Map<Integer, List<String>> tierHits = new HashMap<>();

	private final UserManager userManager;
	private final Session session;

	private final Runnable returnToNetworkSetup;

	public MP_HostGameController(MP_QView mpView,
			MP_Server server,
			MP_Client client,
			String hostName,
			List<String> initialPlayers,
			UserManager userManager,
			Session session,
			Runnable returnToNetworkSetup) {

		this.mpView = mpView;
		this.server = server;
		this.client = client;
		this.hostName = (hostName == null || hostName.isBlank()) ? "Host" : hostName;

		this.userManager = userManager;
		this.session = session;
		this.returnToNetworkSetup = returnToNetworkSetup;

		mpView.getMenuDiamond().setOnAction(e -> showSettingsMenu());

		List<Question> questions = QuestionLoader.loadQuestions("BeMillionaireQuestions.json");
		this.model = new QModel(questions);

		if (initialPlayers != null && !initialPlayers.isEmpty()) {
			for (String p : initialPlayers) {
				playerStatus.put(p, true);
				playerEarnings.put(p, 0);
			}
		} else {
			playerStatus.put(this.hostName, true);
			playerEarnings.put(this.hostName, 0);
		}

		setupLocalBindings();
		setupTimer();
		startNewQuestion();
	}

	// -------------------------------------------------------------------------
	// NETWORK MESSAGE HANDLING
	// -------------------------------------------------------------------------

	public void handleNetworkMessage(String type, String sender, String payload) {
		switch (type) {
		case MP_Protocol.ANSWER -> handlePlayerAnswer(sender, payload);
		case MP_Protocol.READY  -> {
			playerStatus.put(sender, true);
			playerEarnings.putIfAbsent(sender, 0);
		}
		case MP_Protocol.LIFELINE -> handleLifelineRequest(sender, payload);
		case MP_Protocol.CONTINUE -> continueClicks.add(sender);
		case MP_Protocol.JOIN -> {
			playerStatus.putIfAbsent(sender, true);
			playerEarnings.putIfAbsent(sender, 0);
		}
		case MP_Protocol.LEAVE -> {
			playerStatus.put(sender, false);
		}
		default -> {}
		}
	}

	// -------------------------------------------------------------------------
	// LOCAL UI BINDINGS
	// -------------------------------------------------------------------------

	private void setupLocalBindings() {
		mpView.getBtnA().setOnAction(e -> handleLocalAnswer("A"));
		mpView.getBtnB().setOnAction(e -> handleLocalAnswer("B"));
		mpView.getBtnC().setOnAction(e -> handleLocalAnswer("C"));
		mpView.getBtnD().setOnAction(e -> handleLocalAnswer("D"));
	}

	private void setupTimer() {
		timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			secondsRemaining--;

			mpView.getSharedTimerLabel().setText(secondsRemaining + "s");

			client.send(MP_Protocol.format(
					MP_Protocol.TIMER,
					hostName,
					String.valueOf(secondsRemaining)
					));

			if (secondsRemaining <= 0) {
				timer.stop();
				handleTimeOut();
			}
		}));
		timer.setCycleCount(Timeline.INDEFINITE);
	}

	private void resetLifelinesForNewQuestion() {
		// Lifelines are per-game, not per-question.
	}

	// -------------------------------------------------------------------------
	// QUESTION LOOP
	// -------------------------------------------------------------------------

	private void startNewQuestion() {
		currentAnswers.clear();
		resetLifelinesForNewQuestion();

		if (scoreboardTimer != null) {
			scoreboardTimer.stop();
			scoreboardTimer = null;
		}

		Question q = model.getCurrentQuestion();
		if (q == null) {
			handleGameOver();
			return;
		}

		mpView.updateQuestion(q.getQuestionText(), q.getAnswers());
		mpView.updateLadderHighlight(model.getCurrentQuestionIndex());
		mpView.resetButtons();

		secondsRemaining = QUESTION_TIME;
		mpView.getSharedTimerLabel().setText(QUESTION_TIME + "s");
		timer.playFromStart();

		client.send(MP_Protocol.format(
				MP_Protocol.QUESTION,
				hostName,
				serializeQuestion(q)
				));
	}

	private String serializeQuestion(Question q) {
		StringBuilder sb = new StringBuilder();

		sb.append(model.getCurrentQuestionIndex()).append("|")
		.append(q.getTier()).append("|")
		.append(q.getQuestionText().replace("|", "/")).append("|");

		q.getAnswers().forEach(a -> {
			sb.append(a.getLabel()).append(":")
			.append(a.getText().replace("|", "/").replace(":", "-"))
			.append("|");
		});

		return sb.toString();
	}

	private void handleLocalAnswer(String label) {
		lockLocalSelection(label);
		handlePlayerAnswer(hostName, label);
	}

	private void lockLocalSelection(String label) {
		Button a = mpView.getBtnA();
		Button b = mpView.getBtnB();
		Button c = mpView.getBtnC();
		Button d = mpView.getBtnD();

		a.getStyleClass().remove("answer-selected");
		b.getStyleClass().remove("answer-selected");
		c.getStyleClass().remove("answer-selected");
		d.getStyleClass().remove("answer-selected");

		a.setDisable(true);
		b.setDisable(true);
		c.setDisable(true);
		d.setDisable(true);

		switch (label.toUpperCase()) {
		case "A" -> a.getStyleClass().add("answer-selected");
		case "B" -> b.getStyleClass().add("answer-selected");
		case "C" -> c.getStyleClass().add("answer-selected");
		case "D" -> d.getStyleClass().add("answer-selected");
		}
	}

	public void handlePlayerAnswer(String playerName, String label) {
		if (!playerStatus.getOrDefault(playerName, true)) return;
		currentAnswers.put(playerName, label);
	}

	private void handleTimeOut() {
		Map<String, Boolean> correctness = evaluateAnswers();

		int tierIndex = model.getCurrentQuestionIndex();
		int value = 0;
		if (tierIndex >= 0 && tierIndex < QModel.LADDER_VALUES.length) {
			String moneyStr = QModel.LADDER_VALUES[tierIndex];
			value = parseMoney(moneyStr);
		}

		for (var entry : correctness.entrySet()) {
			String player = entry.getKey();
			boolean isCorrect = entry.getValue();
			if (isCorrect) {
				int old = playerEarnings.getOrDefault(player, 0);
				playerEarnings.put(player, old + value);

				tierHits.computeIfAbsent(tierIndex, k -> new ArrayList<>());
				if (!tierHits.get(tierIndex).contains(player)) {
					tierHits.get(tierIndex).add(player);
				}
			}
		}

		int hostTotal = playerEarnings.getOrDefault(hostName, 0);
		mpView.updateEarnings(hostTotal);

		mpView.updatePlayerNameCards(new ArrayList<>(playerStatus.keySet()), playerEarnings);
		mpView.updateLadderMarkers(tierHits);

		broadcastBoard(correctness);
		showScoreboard(correctness);
	}

	private int parseMoney(String s) {
		String cleaned = s.replace("$", "").replace(",", "").trim();
		try {
			return Integer.parseInt(cleaned);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private Map<String, Boolean> evaluateAnswers() {
		Map<String, Boolean> correctness = new HashMap<>();

		Question q = model.getCurrentQuestion();
		Answer correct = q.getCorrectAnswer();
		String correctLabel = correct.getLabel().toUpperCase();

		for (String player : playerStatus.keySet()) {

			String ans = currentAnswers.get(player);

			if (entanglementUsed && ans != null && ans.equalsIgnoreCase(entangledLabel)) {
				correctness.put(player, false);
				continue;
			}

			boolean isCorrect = ans != null && ans.equalsIgnoreCase(correctLabel);
			correctness.put(player, isCorrect);
		}

		return correctness;
	}

	private void broadcastBoard(Map<String, Boolean> correctnessMap) {
		StringBuilder sb = new StringBuilder();

		for (String player : playerStatus.keySet()) {
			boolean alive = playerStatus.get(player);
			boolean correct = correctnessMap.getOrDefault(player, false);
			int money = playerEarnings.getOrDefault(player, 0);

			sb.append(player)
			.append(":")
			.append(alive ? "alive" : "dead")
			.append(":")
			.append(correct ? "correct" : "wrong")
			.append(":")
			.append(money)
			.append("|");
		}

		StringBuilder tiersSb = new StringBuilder();
		for (var entry : tierHits.entrySet()) {
			int idx = entry.getKey();
			List<String> players = entry.getValue();
			if (players == null || players.isEmpty()) continue;

			tiersSb.append(idx)
			.append("=")
			.append(String.join(",", players))
			.append(";");
		}

		sb.append("TIERS:").append(tiersSb);

		client.send(MP_Protocol.format(
				MP_Protocol.BOARD,
				hostName,
				sb.toString()
				));
	}

	// -------------------------------------------------------------------------
	// LIFELINES
	// -------------------------------------------------------------------------

	private void handleLifelineRequest(String sender, String payload) {
		String[] parts = payload.split("\\|");
		if (parts.length < 2) return;
		if (!"REQUEST".equalsIgnoreCase(parts[0])) return;

		String type = parts[1].toUpperCase();

		switch (type) {
		case "SUPERPOSITION" -> applySuperposition(sender);
		case "ENTANGLEMENT"  -> applyEntanglement(sender);
		case "INTERFERENCE"  -> applyInterference(sender);
		}
	}

	private void broadcastLifeline(String type, String data, String usedBy) {
		String payload = type + "|" + data + "|" + usedBy;
		client.send(MP_Protocol.format(
				MP_Protocol.LIFELINE,
				hostName,
				payload
				));
	}

	public void applySuperposition(String usedBy) {
		if (superpositionUsed) return;
		superpositionUsed = true;

		Question q = model.getCurrentQuestion();
		List<Answer> wrong = q.getWrongAnswers();
		Collections.shuffle(wrong);

		int count = Math.min(wrong.size(), new Random().nextInt(3) + 1);

		List<String> labels = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			labels.add(wrong.get(i).getLabel());
		}

		broadcastLifeline("SUPERPOSITION", String.join(",", labels), usedBy);

		mpView.disableAnswers(wrong.subList(0, count));
		mpView.getSuperpositionBtn().setDisable(true);
		mpView.appendChat("★ " + usedBy + " used SUPERPOSITION");
	}

	public void applyEntanglement(String usedBy) {
		if (entanglementUsed) return;
		entanglementUsed = true;

		Question q = model.getCurrentQuestion();
		List<Answer> wrong = q.getWrongAnswers();
		Answer chosen = wrong.get(new Random().nextInt(wrong.size()));

		entangledLabel = chosen.getLabel();

		broadcastLifeline("ENTANGLEMENT", entangledLabel, usedBy);

		mpView.getEntanglementBtn().setDisable(true);
		mpView.appendChat("★ " + usedBy + " used ENTANGLEMENT");
	}

	public void applyInterference(String usedBy) {
		if (interferenceUsed) return;
		interferenceUsed = true;

		Question q = model.getCurrentQuestion();
		Random r = new Random();

		String revealedLabel;
		if (r.nextDouble() < 0.5) {
			revealedLabel = q.getCorrectAnswer().getLabel();
		} else {
			List<Answer> wrong = q.getWrongAnswers();
			revealedLabel = wrong.get(r.nextInt(wrong.size())).getLabel();
		}

		broadcastLifeline("INTERFERENCE", revealedLabel, usedBy);

		mpView.getInterferenceBtn().setDisable(true);
		mpView.appendChat("★ " + usedBy + " used INTERFERENCE (reveals: " + revealedLabel + ")");
	}

	// -------------------------------------------------------------------------
	// GAME OVER & SCOREBOARD
	// -------------------------------------------------------------------------

	private void handleGameOver() {
		if (timer != null) {
			timer.stop();
		}
		if (scoreboardTimer != null) {
			scoreboardTimer.stop();
			scoreboardTimer = null;
		}

		mpView.getBtnA().setDisable(true);
		mpView.getBtnB().setDisable(true);
		mpView.getBtnC().setDisable(true);
		mpView.getBtnD().setDisable(true);

		mpView.getSuperpositionBtn().setDisable(true);
		mpView.getEntanglementBtn().setDisable(true);
		mpView.getInterferenceBtn().setDisable(true);

		int max = 0;
		for (int v : playerEarnings.values()) {
			if (v > max) max = v;
		}

		List<String> winners = new ArrayList<>();
		for (var e : playerEarnings.entrySet()) {
			if (e.getValue() == max) {
				winners.add(e.getKey());
			}
		}

		for (var entry : playerEarnings.entrySet()) {
			String player = entry.getKey();
			int money = entry.getValue();
			boolean won = winners.contains(player);

			userManager.findUser(player).ifPresent(u -> {
				userManager.recordMultiplayerResult(u, won, money);
			});
		}

		String payload = String.join(",", winners);

		client.send(MP_Protocol.format(
				MP_Protocol.WIN,
				hostName,
				payload
				));

		mpView.appendChat("*** Game Over — Winner(s): " + payload + " ***");

		// ⭐ Host winner overlay (same style as client)
		MP_ScoreboardOverlayView overlay = new MP_ScoreboardOverlayView(false);
		VBox list = overlay.getPlayerListBox();

		Label lbl = new Label("Winner(s): " + (payload.isEmpty() ? "None" : payload));
		lbl.setStyle("-fx-text-fill: #d4af37; -fx-font-size: 22px; -fx-font-weight: bold;");
		list.getChildren().add(lbl);

		overlay.getCountdownLabel().setText("Returning to Network Setup...");
		overlay.getContinueBtn().setDisable(true);

		mpView.getChildren().add(overlay);

		Timeline exitTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
			if (returnToNetworkSetup != null) {
				returnToNetworkSetup.run();
			}
		}));
		exitTimer.setCycleCount(1);
		exitTimer.play();
	}

	private void showScoreboard(Map<String, Boolean> correctness) {

		mpView.getChildren().removeIf(node -> node instanceof MP_ScoreboardOverlayView);

		if (scoreboardTimer != null) {
			scoreboardTimer.stop();
			scoreboardTimer = null;
		}

		MP_ScoreboardOverlayView overlay = new MP_ScoreboardOverlayView(true);
		VBox list = overlay.getPlayerListBox();

		for (String player : correctness.keySet()) {
			boolean alive = playerStatus.get(player);
			boolean correct = correctness.get(player);
			int money = playerEarnings.getOrDefault(player, 0);

			Label lbl = new Label(
					player + " — " +
							(correct ? "CORRECT" : "WRONG") +
							" — $" + money
					);

			String hex = mpView.getPlayerColor(player);

			lbl.setStyle(
					"-fx-text-fill: " + hex + ";" +
							"-fx-font-size: 18px;"
					);

			list.getChildren().add(lbl);
		}

		overlay.getCountdownLabel().setText("Next question in " + SCOREBOARD_TIME + "s");

		scoreboardSecondsRemaining = SCOREBOARD_TIME;
		continueClicks.clear();

		scoreboardTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

			scoreboardSecondsRemaining--;

			overlay.getCountdownLabel().setText(
					"Next question in " + scoreboardSecondsRemaining + "s"
					);

			client.send(MP_Protocol.format(
					MP_Protocol.SCOREBOARD_TIMER,
					hostName,
					String.valueOf(scoreboardSecondsRemaining)
					));

			boolean allClicked = continueClicks.size() >= playerStatus.size();

			if (scoreboardSecondsRemaining <= 0 || allClicked) {
				scoreboardTimer.stop();
				mpView.getChildren().remove(overlay);

				// ⭐ Correct final-question logic:
				// We must check the index BEFORE calling nextQuestion().
				int justFinishedIndex = model.getCurrentQuestionIndex();

				// Advance to the next question
				model.nextQuestion();

				// If we just finished question index 14 (the 15th question), the game is over.
				boolean finalQuestionJustFinished = (justFinishedIndex >= 14);

				if (finalQuestionJustFinished || model.getCurrentQuestion() == null) {
					Platform.runLater(this::handleGameOver);
				} else {
					Platform.runLater(this::startNewQuestion);
				}

			}

		}));

		scoreboardTimer.setCycleCount(Timeline.INDEFINITE);
		scoreboardTimer.play();

		mpView.getChildren().add(overlay);

		overlay.getContinueBtn().setOnAction(e -> {
			continueClicks.add(hostName);
			overlay.getContinueBtn().setDisable(true);
		});
	}

	// -------------------------------------------------------------------------
	// SETTINGS MENU
	// -------------------------------------------------------------------------

	private void showSettingsMenu() {
		ContextMenu settingsMenu = new ContextMenu();

		Menu themeMenu = new Menu("Themes & Accessibility");

		MenuItem modern = new MenuItem("Modern Style");
		MenuItem classic = new MenuItem("Classic Style");
		MenuItem deuteranopia = new MenuItem("Deuteranopia (Red-Green)");
		MenuItem tritanopia = new MenuItem("Tritanopia (Blue-Yellow)");

		themeMenu.getItems().addAll(
				modern,
				classic,
				new SeparatorMenuItem(),
				deuteranopia,
				tritanopia
				);

		modern.setOnAction(e -> mpView.applyTheme("modern-style"));
		classic.setOnAction(e -> mpView.applyTheme("classic-style"));
		deuteranopia.setOnAction(e -> mpView.applyTheme("theme-deuteranopia"));
		tritanopia.setOnAction(e -> mpView.applyTheme("theme-tritanopia"));

		settingsMenu.getItems().add(themeMenu);

		settingsMenu.show(mpView.getMenuDiamond(), Side.BOTTOM, 0, 0);
	}
}
