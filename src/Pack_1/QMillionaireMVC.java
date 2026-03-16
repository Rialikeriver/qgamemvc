package Pack_1;

import Database.QuestionLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.List;

public class QMillionaireMVC extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        QSplash splash = new QSplash(() -> {
            VBox modeBox = new VBox(40);
            modeBox.setAlignment(Pos.CENTER);
            modeBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
            
            Label title = new Label("SELECT GAME MODE");
            title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
            
            Button adminBtn = new Button(" ADMIN MODE\nManage Questions");
            adminBtn.getStyleClass().addAll("answer-btn");
            adminBtn.setPrefSize(400, 120);
            adminBtn.setStyle("-fx-font-size: 20px;");
            
            Button userBtn = new Button("PLAYER MODE\nPlay Quantum Millionaire");
            userBtn.getStyleClass().addAll("answer-btn");
            userBtn.setPrefSize(400, 120);
            userBtn.setStyle("-fx-font-size: 20px;");
            
            modeBox.getChildren().addAll(title, adminBtn, userBtn);
            
            Scene modeScene = new Scene(modeBox, 1280, 720);
            addCSS(modeScene);
            primaryStage.setTitle("Quantum Millionaire - Select Mode");
            primaryStage.setScene(modeScene);
            
            adminBtn.setOnAction(e -> showAdminScreen(primaryStage));
            userBtn.setOnAction(e -> showGameScreen(primaryStage));
            
            primaryStage.show();
        });
        splash.show();
    }
    
    private void showAdminScreen(Stage primaryStage) {
        QModeSelectionModel adminModel = new QModeSelectionModel();
        QModeSelectionView adminView = new QModeSelectionView();
        new QModeSelectionController(adminModel, adminView); 
        
        Scene adminScene = new Scene(adminView, 1280, 720);
        addCSS(adminScene);
        primaryStage.setTitle("Quantum Millionaire - Admin Panel");
        primaryStage.setScene(adminScene);
    }
    
    private void showGameScreen(Stage primaryStage) {
        List<Question> questions = QuestionLoader.loadQuestions();
        QModel gameModel = new QModel(questions);
        QView gameView = new QView();
        new QController(gameModel, gameView);  
        
        Scene gameScene = new Scene(gameView, 1280, 720);
        addCSS(gameScene);
        primaryStage.setTitle("Quantum Millionaire - Game");
        primaryStage.setScene(gameScene);
    }
    
    private void addCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/Pack_1/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS Path Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) { launch(args); }
}