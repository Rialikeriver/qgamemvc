package Pack_1;

import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.geometry.Side;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.application.Platform;
import java.util.List;
import java.util.Optional;

public class QController {
    private QModel model;
    private QView view;

    private Pack_1.profile.Session session;
    private Pack_1.profile.UserManager userManager;

    private Timeline timer;
    private int secondsRemaining;

    public QController(QModel model, QView view,
                       Pack_1.profile.Session session,
                       Pack_1.profile.UserManager userManager) {
        this.model = model;
        this.view = view;
        this.session = session;
        this.userManager = userManager;

        setupTimer();
        attachEvents();
        updateDisplay();
    }

    private void setupTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            view.getTimerLabel().setText(secondsRemaining + "s");

            if (secondsRemaining <= 5) {
                view.getTimerLabel().setStyle("-fx-font-size: 40px; -fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
            }

            if (secondsRemaining <= 0) handleTimeOut();
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    private void startCountdown() {
        timer.stop();
        secondsRemaining = 20;
        view.getTimerLabel().setText("20s");
        view.getTimerLabel().setStyle("-fx-font-size: 40px; -fx-text-fill: #d4af37;");
        timer.play();
    }

    private void handleAnswer(String label) {
        timer.stop();

        Question currentQ = model.getCurrentQuestion();
        Answer selected = currentQ.getAnswers().stream()
                .filter(a -> a.getLabel().equalsIgnoreCase(label))
                .findFirst().orElse(null);

        boolean isCorrect = (selected != null && (selected.isCorrect() || model.isEntangled(selected)));

        if (isCorrect) {
            model.nextQuestion();

            if (session.hasUser()) {
                userManager.updateUserProgress(
                        session.getCurrentUser(),
                        model.getCurrentQuestionIndex(),
                        model.isSuperpositionUsed(),
                        model.isEntanglementUsed(),
                        model.getCurrentCashMoney()
                );
            }

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

    private void handleFailure(String message) {
        view.getBtnA().setDisable(true);
        view.getBtnB().setDisable(true);
        view.getBtnC().setDisable(true);
        view.getBtnD().setDisable(true);
        view.getSuperpositionBtn().setDisable(true);
        view.getEntanglementBtn().setDisable(true);

        view.getQuestionLabel().setText(message);
        view.getTimerLabel().setText("--");
        view.getTimerLabel().setStyle("-fx-text-fill: gray; -fx-font-size: 40px;");

        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void attachEvents() {
        view.getBtnA().setOnAction(e -> handleAnswer("A"));
        view.getBtnB().setOnAction(e -> handleAnswer("B"));
        view.getBtnC().setOnAction(e -> handleAnswer("C"));
        view.getBtnD().setOnAction(e -> handleAnswer("D"));
        view.getSuperpositionBtn().setOnAction(e -> handleSuperposition());
        view.getEntanglementBtn().setOnAction(e -> handleEntanglement());
        view.getMenuDiamond().setOnAction(e -> showSettingsMenu());
    }

    private void handleSuperposition() {
        List<Answer> toDisable = model.applySuperposition();
        if (toDisable != null) {
            view.disableAnswers(toDisable);
            view.getSuperpositionBtn().setDisable(true);
            model.setSuperpositionUsed(true);

            if (session.hasUser()) {
                userManager.updateUserProgress(
                        session.getCurrentUser(),
                        model.getCurrentQuestionIndex(),
                        model.isSuperpositionUsed(),
                        model.isEntanglementUsed(),
                        model.getCurrentCashMoney()
                );
            }
        }
    }

    private void handleEntanglement() {
        model.applyEntanglement();
        view.getEntanglementBtn().setDisable(true);
        model.setEntanglementUsed(true);

        if (session.hasUser()) {
            userManager.updateUserProgress(
                    session.getCurrentUser(),
                    model.getCurrentQuestionIndex(),
                    model.isSuperpositionUsed(),
                    model.isEntanglementUsed(),
                    model.getCurrentCashMoney()
            );
        }
    }

    private void updateDisplay() {
        Question q = model.getCurrentQuestion();
        if (q != null) {
            view.updateQuestion(q.getQuestionText(), q.getAnswers());
            view.updateLadderHighlight(model.getCurrentQuestionIndex());
            view.resetButtons();

            view.getSuperpositionBtn().setDisable(model.isSuperpositionUsed());
            view.getEntanglementBtn().setDisable(model.isEntanglementUsed());

            startCountdown();
        } else {
            timer.stop();
            view.getQuestionLabel().setText("CONGRATULATIONS! YOU ARE A QUANTUM MILLIONAIRE!");
        }
    }

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

    private void handleGameOver() {
        if (session.hasUser()) {
            userManager.recordGameResult(session.getCurrentUser(), model);
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");

        if (model.isPlayerWon()) {
            alert.setHeaderText("You Won!");
        } else {
            alert.setHeaderText("Game Over");
        }

        alert.setContentText("You earned: $" + model.getMoneyEarned());

        ButtonType playAgain = new ButtonType("Play Again");
        ButtonType quit = new ButtonType("Quit");

        alert.getButtonTypes().setAll(playAgain, quit);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == playAgain) {
            model.resetGame();
            updateDisplay();
        }
    }
}