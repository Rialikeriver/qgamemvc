package Pack_1;

import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class QModeSelectionController {
    private QModeSelectionModel model;
    private QModeSelectionView view;
    
    public QModeSelectionController(QModeSelectionModel model, QModeSelectionView view) {
        this.model = model;
        this.view = view;
        refreshUI();
        attachEvents();
    }
    
    private void refreshUI() {
        view.getQuestionListView().setItems(FXCollections.observableArrayList(model.getQuestions()));
    }
    
    private void attachEvents() {
        view.getRefreshBtn().setOnAction(e -> refreshUI());

        view.getAddBtn().setOnAction(e -> {
            new Thread(() -> {
                Question newQ = view.showQuestionEditor(null);
                if (newQ != null) {
                    Platform.runLater(() -> {
                        model.getQuestions().add(newQ);
                        model.saveAll();
                        refreshUI();
                    });
                }
            }).start();
        });

        view.getEditBtn().setOnAction(e -> {
            Question selected = view.getQuestionListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                new Thread(() -> {
                    Question updated = view.showQuestionEditor(selected);
                    if (updated != null) {
                        Platform.runLater(() -> {
                            int index = model.getQuestions().indexOf(selected);
                            model.getQuestions().set(index, updated);
                            model.saveAll();
                            refreshUI();
                        });
                    }
                }).start();
            }
        });

        view.getDeleteBtn().setOnAction(e -> {
            Question selected = view.getQuestionListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected question?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        model.getQuestions().remove(selected);
                        model.saveAll();
                        refreshUI();
                    }
                });
            }
        });
    }
}