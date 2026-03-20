package Pack_1;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javax.swing.*;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin-facing view for managing the full question bank. This screen displays
 * all questions in a styled list and provides controls for refreshing, adding,
 * editing, and deleting entries. It serves as the UI layer for
 * {@link QModeSelectionController} and interacts with the model through
 * controller callbacks.
 *
 * <p>The view uses JavaFX for layout and rendering, but the question editor
 * dialog is implemented using Swing components for simplicity. The editor
 * allows administrators to modify question text, tier, answers, and correctness
 * flags before returning a fully constructed {@link Question} instance.</p>
 */
public class QModeSelectionView extends BorderPane {

    private Label titleLabel;
    private ListView<Question> questionListView;
    private Button addBtn, editBtn, deleteBtn, refreshBtn;

    /**
     * Builds the question manager UI and initializes all controls.
     */
    public QModeSelectionView() {
        setupUI();
    }

    /**
     * Configures layout, styling, list cell formatting, and action buttons.
     */
    private void setupUI() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        this.setPadding(new Insets(20));

        titleLabel = new Label("️ ADMIN PANEL - Question Manager");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        this.setTop(titleLabel);

        questionListView = new ListView<>();
        questionListView.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        questionListView.setPrefHeight(500);

        questionListView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question q, boolean empty) {
                super.updateItem(q, empty);
                if (empty || q == null) {
                    setText(null);
                    setStyle("-fx-control-inner-background: transparent;");
                } else {
                    setText(String.format("Tier %d | %s", q.getTier(), q.getQuestionText()));
                    setStyle("-fx-control-inner-background: #2b1055; -fx-text-fill: white;");
                }
            }
        });

        HBox buttonBox = new HBox(
                15,
                refreshBtn = new Button("Refresh"),
                addBtn = new Button("Add Question"),
                editBtn = new Button("Edit"),
                deleteBtn = new Button("Delete")
        );
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        buttonBox.getChildren().forEach(n -> n.getStyleClass().add("answer-btn"));

        this.setCenter(new VBox(20, questionListView, buttonBox));
    }

    /**
     * Opens a Swing-based editor dialog for creating or modifying a question.
     * Returns a new {@link Question} instance if the user confirms, or null if
     * cancelled or invalid.
     */
    public Question showQuestionEditor(Question existing) {
        JTextField txtText = new JTextField(existing != null ? existing.getQuestionText() : "", 20);
        JTextField txtTier = new JTextField(existing != null ? String.valueOf(existing.getTier()) : "1", 5);

        JTextField[] ansTexts = new JTextField[4];
        JRadioButton[] correctRadios = new JRadioButton[4];
        ButtonGroup group = new ButtonGroup();

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Question Text:")); panel.add(txtText);
        panel.add(new JLabel("Tier (1-15):")); panel.add(txtTier);

        for (int i = 0; i < 4; i++) {
            ansTexts[i] = new JTextField(existing != null ? existing.getAnswers().get(i).getText() : "");
            correctRadios[i] = new JRadioButton("Correct");

            if (existing != null && existing.getAnswers().get(i).isCorrect()) {
                correctRadios[i].setSelected(true);
            }

            group.add(correctRadios[i]);
            panel.add(new JLabel("Answer " + (char)('A' + i) + ":")); panel.add(ansTexts[i]);
            panel.add(new JLabel("Correct?")); panel.add(correctRadios[i]);
        }

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Question Editor",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                List<Answer> answers = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Answer a = new Answer();
                    a.setLabel(String.valueOf((char)('A' + i)));
                    a.setText(ansTexts[i].getText());
                    a.setCorrect(correctRadios[i].isSelected());
                    answers.add(a);
                }

                int tier = Integer.parseInt(txtTier.getText());
                String id = (existing != null)
                        ? existing.getId()
                        : String.format("T%02d-Q%d", tier, System.currentTimeMillis() % 1000);

                return new Question(id, tier, txtText.getText(), answers);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid Input!");
            }
        }
        return null;
    }

    // Getters for controller wiring
    public ListView<Question> getQuestionListView() { return questionListView; }
    public Button getAddBtn() { return addBtn; }
    public Button getEditBtn() { return editBtn; }
    public Button getDeleteBtn() { return deleteBtn; }
    public Button getRefreshBtn() { return refreshBtn; }
}
