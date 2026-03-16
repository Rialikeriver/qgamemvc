package Pack_1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class QModeSelectionView extends BorderPane {
    private Label titleLabel;
    private ListView<Question> questionListView;
    private Button addBtn, editBtn, deleteBtn, refreshBtn;
    
    public QModeSelectionView() {
        setupUI();
    }
    
    private void setupUI() {
        this.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        this.setPadding(new Insets(20));
        
        // Title
        titleLabel = new Label("🛠️ ADMIN PANEL - Question Manager");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        titleLabel.setPadding(new Insets(20));
        this.setTop(titleLabel);
        
        // Question List
        questionListView = new ListView<>();
        questionListView.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white;");
        questionListView.setPrefHeight(500);
        
        // ListCell for the questions
        questionListView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question q, boolean empty) {
                super.updateItem(q, empty);
                if (empty || q == null) {
                    setText(null);
                    setStyle("-fx-control-inner-background: transparent;");
                } else {
                    String display = String.format("Q%d: %s\n%s", 
                        q.getTier(), q.getQuestionText().substring(0, Math.min(60, q.getQuestionText().length())), 
                        "...");
                    setText(display);
                    setStyle("-fx-control-inner-background: rgba(43,16,85,0.8); -fx-text-fill: white;");
                }
            }
        });
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        
        refreshBtn = new Button("Refresh");
        addBtn = new Button("Add Question");
        editBtn = new Button("Edit");
        deleteBtn = new Button("Delete");
        
        refreshBtn.getStyleClass().addAll("answer-btn");
        addBtn.getStyleClass().addAll("answer-btn");
        editBtn.getStyleClass().addAll("answer-btn");
        deleteBtn.getStyleClass().addAll("answer-btn");
        
        buttonBox.getChildren().addAll(refreshBtn, addBtn, editBtn, deleteBtn);
        
        VBox centerBox = new VBox(20, questionListView, buttonBox);
        centerBox.setAlignment(Pos.CENTER);
        this.setCenter(centerBox);
    }
    
    // Getters for the Controller
    public ListView<Question> getQuestionListView() { return questionListView; }
    public Button getAddBtn() { return addBtn; }
    public Button getEditBtn() { return editBtn; }
    public Button getDeleteBtn() { return deleteBtn; }
    public Button getRefreshBtn() { return refreshBtn; }
}