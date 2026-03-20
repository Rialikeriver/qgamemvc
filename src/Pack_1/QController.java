package Pack_1;

import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.geometry.Side;
import javafx.geometry.NodeOrientation;
import javafx.application.Platform;
import java.util.List;
import javafx.animation.Animation;

import Pack_1.profile.Session;
import Pack_1.profile.UserManager;

/**
 * Main controller for the gameplay loop. Handles:
 * <ul>
 *   <li>question progression and answer evaluation</li>
 *   <li>lifeline activation and UI updates</li>
 *   <li>countdown timer and pulse animation</li>
 *   <li>game over and failure states</li>
 *   <li>language switching</li>
 *   <li>mid‑game and end‑game persistence through {@link UserManager}</li>
 * </ul>
 *
 * <p>The controller coordinates the {@link QModel} (game state), the
 * {@link QView} (UI), and the active {@link Session}. It updates the view
 * whenever the model changes and ensures user progress is saved at the
 * appropriate times.</p>
 */
public class QController {

    private QModel model;
    private QView view;

    private Session session;
    private UserManager userManager;

    private Timeline timer;
    private Timeline pulseAnimation;
    private int secondsRemaining;

    /**
     * Creates a new gameplay controller and initializes timers, animations,
     * event handlers, and the initial display.
     */
    public QController(QModel model, QView view,
                       Session session,
                       UserManager userManager) {
        this.model = model;
        this.view = view;
        this.session = session;
        this.userManager = userManager;

        setupTimer();
        setupPulseAnimation();
        attachEvents();
        updateDisplay();
    }

    /**
     * Configures the 20‑second countdown timer and its visual state transitions.
     * Handles warning/critical color changes and triggers timeout behavior.
     */
    private void setupTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            view.getTimerLabel().setText(secondsRemaining + "s");

            Label t = view.getTimerLabel();
            t.getStyleClass().setAll("timer-base");

            if (secondsRemaining > 5) {
                pulseAnimation.stop();
            }
            else if (secondsRemaining > 2) {
                t.getStyleClass().add("timer-warning");
                pulseAnimation.setRate(1.0);
                if (pulseAnimation.getStatus() != Animation.Status.RUNNING) pulseAnimation.play();
            }
            else {
                t.getStyleClass().add("timer-critical");
                pulseAnimation.setRate(1.5);
                if (pulseAnimation.getStatus() != Animation.Status.RUNNING) pulseAnimation.play();
            }

            if (secondsRemaining <= 0) handleTimeOut();
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Creates the pulsing animation used during low‑time warnings.
     */
    private void setupPulseAnimation() {
        pulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(view.getTimerLabel().scaleXProperty(), 1.0),
                new KeyValue(view.getTimerLabel().scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(250),
                new KeyValue(view.getTimerLabel().scaleXProperty(), 1.12),
                new KeyValue(view.getTimerLabel().scaleYProperty(), 1.12)
            ),
            new KeyFrame(Duration.millis(500),
                new KeyValue(view.getTimerLabel().scaleXProperty(), 1.0),
                new KeyValue(view.getTimerLabel().scaleYProperty(), 1.0)
            )
        );
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Starts a fresh 20‑second countdown for each question.
     */
    private void startCountdown() {
        timer.stop();
        secondsRemaining = 20;
        view.getTimerLabel().setText("20s");
        view.getTimerLabel().setStyle("-fx-font-size: 40px; -fx-text-fill: #d4af37;");
        timer.play();
    }

    private void resumeCountdown() {
        if (timer != null) timer.play();
    }

    /**
     * Evaluates the selected answer, updates the model, and handles success or failure.
     */
    private void handleAnswer(String label) {
        timer.stop();

        Question currentQ = model.getCurrentQuestion();
        Answer selected = currentQ.getAnswers().stream()
                .filter(a -> a.getLabel().equalsIgnoreCase(label))
                .findFirst().orElse(null);

        boolean isCorrect = (selected != null && (selected.isCorrect() || model.isEntangled(selected)));

        if (isCorrect) {
            model.nextQuestion();
            updateProgressInStore();

            if (model.isGameOver()) {
                updateDisplay();
                handleGameOver();
            } else {
                updateDisplay();
            }

        } else {
            handleFailure("WRONG ANSWER! YOUR JOURNEY ENDS HERE.");
        }
    }

    private void handleTimeOut() {
        timer.stop();
        handleFailure("TIME'S OVER.");
    }

    /**
     * Handles all failure states: disables UI, records results, and shows the
     * game‑over overlay for losses.
     */
    private void handleFailure(String message) {
        view.getBtnA().setDisable(true);
        view.getBtnB().setDisable(true);
        view.getBtnC().setDisable(true);
        view.getBtnD().setDisable(true);
        view.getSuperpositionBtn().setDisable(true);
        view.getEntanglementBtn().setDisable(true);
        view.getInterferenceBtn().setDisable(true);

        timer.stop();
        view.getTimerLabel().setText("--");
        view.getTimerLabel().setStyle("-fx-text-fill: gray; -fx-font-size: 40px;");

        // Record failure result (guaranteed money, loss, lifelines, totals)
        if (session.hasUser()) {
            userManager.recordGameResult(session.getCurrentUser(), model);
        }

        GameOverlayView overlay = new GameOverlayView(
                "GAME OVER",
                "YOUR JOURNEY ENDS HERE",
                String.valueOf(model.getGuaranteedMoney()),
                false
        );

        view.showOverlay(overlay);

        overlay.getPrimaryBtn().setOnAction(e -> {
            view.hideOverlay(overlay);
            model.resetGame();
            updateDisplay();
        });

        overlay.getSecondaryBtn().setOnAction(e -> {
            if (session.hasUser()) {
                userManager.finalizeGame(session.getCurrentUser(), model);
            }
            model.resetGame();
            Platform.exit();
        });
    }

    /**
     * Attaches all UI event handlers for answers, lifelines, and settings.
     */
    private void attachEvents() {
        view.getBtnA().setOnAction(e -> handleAnswer("A"));
        view.getBtnB().setOnAction(e -> handleAnswer("B"));
        view.getBtnC().setOnAction(e -> handleAnswer("C"));
        view.getBtnD().setOnAction(e -> handleAnswer("D"));
        view.getSuperpositionBtn().setOnAction(e -> handleSuperposition());
        view.getEntanglementBtn().setOnAction(e -> handleEntanglement());
        view.getInterferenceBtn().setOnAction(e -> handleInterference());
        view.getMenuDiamond().setOnAction(e -> showSettingsMenu());
    }

    /**
     * Handles the Interference lifeline and displays the suggestion popup.
     */
    private void handleInterference() {
        timer.stop();

        Answer suggested = model.applyInterference();
        if (suggested != null) {
            model.setInterferenceUsed(true);

            MiniPopupView popup = new MiniPopupView(
                    "Quantum Interference",
                    "Probability Wave Measured",
                    "Constructive Interference suggests: " + suggested.getLabel(),
                    false
            );

            view.showOverlay(popup);
            view.getInterferenceBtn().setDisable(true);
            updateProgressInStore();

            popup.getPrimaryBtn().setOnAction(e -> {
                view.hideOverlay(popup);
                resumeCountdown();
            });
        }
    }

    /**
     * Handles the Superposition lifeline (disables two answers).
     */
    private void handleSuperposition() {
        List<Answer> toDisable = model.applySuperposition();
        if (toDisable != null) {
            view.disableAnswers(toDisable);
            view.getSuperpositionBtn().setDisable(true);
            model.setSuperpositionUsed(true);
            updateProgressInStore();
        }
    }

    /**
     * Handles the Entanglement lifeline (marks two answers as linked).
     */
    private void handleEntanglement() {
        model.applyEntanglement();
        view.getEntanglementBtn().setDisable(true);
        model.setEntanglementUsed(true);
        updateProgressInStore();
    }

    /**
     * Saves mid‑game progress (tier, lifelines, money) for the active user.
     */
    private void updateProgressInStore() {
        if (session.hasUser()) {
            userManager.updateUserProgress(
                    session.getCurrentUser(),
                    model.getCurrentQuestionIndex(),
                    model.isSuperpositionUsed(),
                    model.isEntanglementUsed(),
                    model.isInterferenceUsed(),
                    model.getLastEarnedMoney()
            );
        }
    }

    /**
     * Updates the UI for the current question, lifeline states, ladder highlight,
     * earnings, and restarts the countdown.
     */
    private void updateDisplay() {
        Question q = model.getCurrentQuestion();
        if (q != null) {
            view.updateQuestion(q.getQuestionText(), q.getAnswers());
            view.updateLadderHighlight(model.getCurrentQuestionIndex());
            view.resetButtons();

            view.getSuperpositionBtn().setDisable(model.isSuperpositionUsed());
            view.getEntanglementBtn().setDisable(model.isEntanglementUsed());
            view.getInterferenceBtn().setDisable(model.isInterferenceUsed());

            view.getEarningsLabel().setText("$" + model.getLastEarnedMoney());

            startCountdown();
        } else {
            timer.stop();
            view.getQuestionLabel().setText("CONGRATULATIONS! YOU ARE A QUANTUM MILLIONAIRE!");
        }
    }

    /**
     * Shows the language/settings menu and reloads questions when switching languages.
     */
    private void showSettingsMenu() {
        ContextMenu settingsMenu = new ContextMenu();
        Menu langMenu = new Menu("Languages");
        MenuItem en = new MenuItem("English");
        MenuItem fa = new MenuItem("Farsi");

        langMenu.getItems().addAll(en, fa);

        fa.setOnAction(e -> {
            List<Question> farsi = Database.QuestionLoader.loadQuestions("BeMillionaireQuestionsfa.json");
            if (farsi != null && !farsi.isEmpty()) {
                model.setQuestions(farsi);
                model.resetGame();
                view.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                updateDisplay();
            }
        });

        en.setOnAction(e -> {
            List<Question> english = Database.QuestionLoader.loadQuestions("BeMillionaireQuestions.json");
            if (!english.isEmpty()) {
                model.setQuestions(english);
                model.resetGame();
                view.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                updateDisplay();
            }
        });

        settingsMenu.getItems().add(langMenu);
        settingsMenu.show(view.getMenuDiamond(), Side.BOTTOM, 0, 0);
    }

    /**
     * Handles the win state: records results, shows the overlay, and wires
     * Play Again / Quit actions.
     */
    private void handleGameOver() {
        timer.stop();

        if (session.hasUser()) {
            userManager.recordGameResult(session.getCurrentUser(), model);
        }

        GameOverlayView overlay = new GameOverlayView(
                model.isPlayerWon() ? "YOU WON!" : "YOU HAVE FAILED!",
                model.isPlayerWon() ? "CONGRATULATIONS!" : "BE DESTITUTE",
                String.valueOf(model.getLastEarnedMoney()),
                model.isPlayerWon()
        );

        view.showOverlay(overlay);

        overlay.getPrimaryBtn().setOnAction(e -> {
            view.hideOverlay(overlay);
            model.resetGame();
            updateDisplay();
        });

        overlay.getSecondaryBtn().setOnAction(e -> {
            if (session.hasUser()) {
                userManager.finalizeGame(session.getCurrentUser(), model);
            }
            model.resetGame();
            Platform.exit();
        });
    }
}
