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
	private Pack_1.profile.UserManager userManager;
	private Pack_1.profile.Session session;
	private ProfileController profileController;

    @Override
    public void start(Stage primaryStage) {
    	userManager = new Pack_1.profile.UserManager(
    	        new Pack_1.profile.JsonUserStore(),
    	        new Pack_1.profile.JsonStatsStore()
    	);
    	session = new Pack_1.profile.Session();
    	profileController = new ProfileController(userManager);
    	
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
            userBtn.setOnAction(e -> showPlayerMenu(primaryStage));
            
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
    	// You may later use session.getCurrentUser() to restore progress
        List<Question> questions = QuestionLoader.loadQuestions();
        QModel gameModel = new QModel(questions);

        // Restore progress if a user is logged in
        if (session.hasUser()) {
            Pack_1.profile.User u = session.getCurrentUser();
            
            gameModel.resetGame();			// 0's out.
            // currentTier == how many questions they’ve cleared
            for (int i = 0; i < u.getCurrentTier(); i++) {
                gameModel.nextQuestion();
            }
            
            // Set lifelines based on their use state
            gameModel.setSuperpositionUsed(u.isSuperpositionUsed());
            gameModel.setEntanglementUsed(u.isEntanglementUsed());
            //gameModel.setThirdLifeLineUsed(u.isThirdLifeLineUsed());
        }
        QView gameView = new QView();
        new QController(gameModel, gameView, session, userManager);
        
        Scene gameScene = new Scene(gameView, 1280, 720);
        addCSS(gameScene);
        primaryStage.setTitle("Quantum Millionaire - Game");
        primaryStage.setScene(gameScene);
    }
    
    private void showPlayerMenu(Stage primaryStage) {
        PlayerMenuView view = new PlayerMenuView();

        view.getNewGameBtn().setOnAction(e -> showNewProfileScreen(primaryStage));
        view.getLoadGameBtn().setOnAction(e -> showLoadProfileScreen(primaryStage));
        view.getQuitBtn().setOnAction(e -> primaryStage.close());

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setScene(scene);
    }

    private void showNewProfileScreen(Stage primaryStage) {
        NewProfileView view = new NewProfileView();

        profileController.wireNewProfileScreen(
                view,
                user -> {
                    session.setCurrentUser(user);
                    showGameScreen(primaryStage);
                },
                () -> showPlayerMenu(primaryStage)
        );

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setScene(scene);
    }

    private void showLoadProfileScreen(Stage primaryStage) {
        ProfileSelectionView view = new ProfileSelectionView();

        profileController.wireProfileSelectionScreen(
                view,
                user -> {
                    session.setCurrentUser(user);
                    showGameScreen(primaryStage);
                },
                () -> showPlayerMenu(primaryStage),
                () -> showNewProfileScreen(primaryStage)
        );

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setScene(scene);
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