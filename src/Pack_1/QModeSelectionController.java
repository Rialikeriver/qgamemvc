package Pack_1;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class QModeSelectionController {
    private QModeSelectionModel model;
    private QModeSelectionView view;
    private Stage primaryStage;
    
    public QModeSelectionController(QModeSelectionModel model, QModeSelectionView view) {
    	        this.model = model;
    	        this.view = view;
    	        loadQuestionsToView();
    	        attachEvents();
    	        }
    
    private void loadQuestionsToView() {
        view.getQuestionListView().setItems(
            FXCollections.observableArrayList(model.getQuestions())
        );
        System.out.println("Loaded " + model.getQuestionCount() + " questions to ListView");
    }
    
    private void attachEvents() {
        // Refresh
        view.getRefreshBtn().setOnAction(e -> {
            model = new QModeSelectionModel();
            loadQuestionsToView();
        });
        
        //STILL GOTTA ADD THESE FEATURES --> Add, Edit, Delete (save back to JSON) they're not functional rn
        view.getAddBtn().setOnAction(e -> System.out.println("Add clicked"));
        view.getEditBtn().setOnAction(e -> {
            Question selected = view.getQuestionListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("Edit: " + selected.getId());
            }
        });
        view.getDeleteBtn().setOnAction(e -> {
            Question selected = view.getQuestionListView().getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("Delete: " + selected.getId());
            }
        });
    }
}