package Pack_1;

import java.util.List;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.geometry.Side;
import javafx.geometry.NodeOrientation;
public class QController {
    private QModel model;
    private QView view;

    public QController(QModel model, QView view) {
        this.model = model;
        this.view = view;
        attachEvents();
        updateDisplay();
    }

    private void attachEvents() {
        view.getBtnA().setOnAction(e -> handleAnswer("A"));
        view.getBtnB().setOnAction(e -> handleAnswer("B"));
        view.getBtnC().setOnAction(e -> handleAnswer("C"));
        view.getBtnD().setOnAction(e -> handleAnswer("D"));
        view.getSuperpositionBtn().setOnAction(e -> handleSuperposition());
        view.getMenuDiamond().setOnAction(e -> showSettingsMenu());
        view.getEntanglementBtn().setOnAction(e -> handleEntanglement());
    }
        // New Handler
        private void handleEntanglement() {
            model.applyEntanglement();
            System.out.println("Quantum Entanglement Active: One wrong answer is now linked to reality.");
            view.getEntanglementBtn().setDisable(true);
        
    }

    private void showSettingsMenu() {
        ContextMenu settingsMenu = new ContextMenu();

        // 1. Languages Submenu
        Menu langMenu = new Menu("Languages");
        MenuItem en = new MenuItem("English");
        MenuItem fa = new MenuItem("فارسی (Farsi)");
        langMenu.getItems().addAll(en, fa);

        // --- Logic to apply language switch ---
        
     // Inside showSettingsMenu() in QController.java

     // Inside fa.setOnAction(...)
        fa.setOnAction(e -> {
            List<Question> farsiQuestions = Database.QuestionLoader.loadQuestions("BeMillionaireQuestionsfa.json");
            
            // Check if list is NOT null and NOT empty
            if (farsiQuestions != null && !farsiQuestions.isEmpty()) {
                model.setQuestions(farsiQuestions);
                model.resetGame(); // Start from Q1 in the new language
                view.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                updateDisplay();
            }
        });

        en.setOnAction(e -> {
            List<Question> englishQuestions = Database.QuestionLoader.loadQuestions("BeMillionaireQuestions.json");
            if (!englishQuestions.isEmpty()) {
                model.setQuestions(englishQuestions);
                view.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                updateDisplay();
            }
        });

        // 2. Look and Feel Submenu
        Menu styleMenu = new Menu("Look and Feel");
        MenuItem modern = new MenuItem("Modern");
        MenuItem classic = new MenuItem("Classic");
        styleMenu.getItems().addAll(modern, classic);

        // 3. Color Themes Submenu
        Menu colorMenu = new Menu("Color Themes");
        MenuItem normal = new MenuItem("Default");
        MenuItem deuteranopia = new MenuItem("Deuteranopia");
        MenuItem tritanopia = new MenuItem("Tritanopia");
        colorMenu.getItems().addAll(normal, deuteranopia, tritanopia);

        settingsMenu.getItems().addAll(langMenu, styleMenu, colorMenu);
        settingsMenu.show(view.getMenuDiamond(), Side.BOTTOM, 0, 0);

        // Theme Actions
        deuteranopia.setOnAction(e -> view.applyTheme("theme-deuteranopia"));
        tritanopia.setOnAction(e -> view.applyTheme("theme-tritanopia"));
        normal.setOnAction(e -> view.applyTheme("")); 
        modern.setOnAction(e -> view.applyTheme("modern-style"));
        classic.setOnAction(e -> view.applyTheme("classic-style"));
        
    }

    private void handleAnswer(String label) {
        Question currentQ = model.getCurrentQuestion();
        Answer selected = currentQ.getAnswers().stream()
                .filter(a -> a.getLabel().equalsIgnoreCase(label))
                .findFirst().orElse(null);

        // Quantum Logic: Check if it's actually correct OR entangled
        boolean isTechnicallyCorrect = (selected != null && selected.isCorrect());
        boolean isQuantumEntangled = model.isEntangled(selected);

        if (isTechnicallyCorrect || isQuantumEntangled) {
            if (isQuantumEntangled) System.out.println("Entanglement Success!");
            model.nextQuestion();
            updateDisplay();
        } else {
            System.out.println("Wavefunction Collapsed - Game Over.");
        }
    }

    private void handleSuperposition() {
        List<Answer> toDisable = model.applySuperposition();
        if (toDisable != null) {
            view.disableAnswers(toDisable); 
            // Optional: disable the lifeline button so it can't be used twice
            view.getSuperpositionBtn().setDisable(true);
        }
    }

    private void updateDisplay() {
        Question q = model.getCurrentQuestion();
        if (q != null) {
            view.updateQuestion(q.getQuestionText(), q.getAnswers());
            view.updateLadderHighlight(model.getCurrentQuestionIndex()); 
            view.resetButtons();
            
            // Re-enable lifelines for the new question
            view.getSuperpositionBtn().setDisable(false);
            view.getEntanglementBtn().setDisable(false); 
        }
    }
}