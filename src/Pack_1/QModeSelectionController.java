package Pack_1;

import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Controller for the admin question‑management screen. This class wires the
 * {@link QModeSelectionView} to the underlying {@link QModeSelectionModel},
 * enabling administrators to refresh, add, edit, and delete questions.
 *
 * <p>All long‑running operations (opening the editor dialog, saving updates)
 * are executed on background threads, with UI updates marshalled back onto
 * the JavaFX application thread. The controller ensures the list view always
 * reflects the current state of the question store.</p>
 */
public class QModeSelectionController {

    private QModeSelectionModel model;
    private QModeSelectionView view;

    /**
     * Creates a controller, initializes the list view, and attaches all event handlers.
     */
    public QModeSelectionController(QModeSelectionModel model, QModeSelectionView view) {
        this.model = model;
        this.view = view;
        refreshUI();
        attachEvents();
    }

    /**
     * Reloads the question list from the model into the view.
     */
    private void refreshUI() {
        view.getQuestionListView().setItems(
                FXCollections.observableArrayList(model.getQuestions())
        );
    }

    /**
     * Attaches handlers for refresh, add, edit, and delete actions.
     * Add/Edit operations run in background threads to avoid blocking the UI.
     */
    private void attachEvents() {

        // Refresh list
        view.getRefreshBtn().setOnAction(e -> refreshUI());

        // Add new question
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

        // Edit selected question
        view.getEditBtn().setOnAction(e -> {
            Question selected = view.getQuestionListView()
                                    .getSelectionModel()
                                    .getSelectedItem();
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

        // Delete selected question
        view.getDeleteBtn().setOnAction(e -> {
            Question selected = view.getQuestionListView()
                                    .getSelectionModel()
                                    .getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Delete selected question?",
                        ButtonType.YES,
                        ButtonType.NO
                );

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
