package Pack_1;

import Database.QuestionLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Entry point and top‑level coordinator for the Quantum Millionaire application.
 * This class initializes persistent stores, manages the active user session,
 * and routes between all major screens (splash, mode selection, admin tools,
 * profile management, and gameplay). It acts as the central navigation hub
 * for the entire MVC architecture.
 *
 * <p>The controller constructs views on demand, wires them to their respective
 * controllers, and applies the shared CSS theme. It also restores mid‑game
 * progress when a returning player resumes a session.</p>
 */
public class QMillionaireMVC extends Application {

    private Pack_1.profile.UserManager userManager;
    private Pack_1.profile.Session session;
    private ProfileController profileController;

    /**
     * Initializes persistent stores, session state, and shows the splash screen.
     * After the splash completes, the user is taken to the mode selection screen.
     */
    /**
     * Initializes persistent stores, session state, and shows the splash screen.
     * After the splash completes, the user is taken to the mode selection screen.
     */
    @Override
    public void start(Stage primaryStage) {
        userManager = new Pack_1.profile.UserManager(
                new Pack_1.profile.JsonUserStore(),
                new Pack_1.profile.JsonStatsStore()
        );
        session = new Pack_1.profile.Session();
        profileController = new ProfileController(userManager);

        // When the splash screen ends, we call showModeSelection
        QSplash splash = new QSplash(() -> {
            showModeSelection(primaryStage); 
            primaryStage.show();
        });

        splash.show();
    }
    

    /**
     * Rebuilds and displays the mode selection screen. Used when returning
     * from admin or player menus.
     */
    private void showModeSelection(Stage primaryStage) {
        VBox modeBox = new VBox(30); // Tightened spacing for 3 buttons
        modeBox.setAlignment(Pos.CENTER);
        modeBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");

        Label title = new Label("SELECT GAME MODE");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");

        // --- EXISTING BUTTONS ---
        Button adminBtn = createModeButton("ADMIN MODE\nManage Questions");
        Button userBtn = createModeButton("PLAYER MODE\nPlay Quantum Millionaire");

        // --- NEW MULTIPLAYER BUTTON ---
        Button multiBtn = createModeButton("MULTIPLAYER MODE\nConnect & Chat");
        multiBtn.setStyle("-fx-font-size: 20px; -fx-border-color: #00ff00;"); // Green border to distinguish it

        modeBox.getChildren().addAll(title, adminBtn, userBtn, multiBtn);

        Scene modeScene = new Scene(modeBox, 1280, 720);
        addCSS(modeScene);

        adminBtn.setOnAction(e -> showAdminScreen(primaryStage));
        userBtn.setOnAction(e -> showPlayerMenu(primaryStage));
        
        // Wire the new button to the Network Setup
        multiBtn.setOnAction(e -> {
            if (!session.hasUser()) {
                showLoadProfileScreen(primaryStage, true);  // ← return to multiplayer
            } else {
                showNetworkSetup(primaryStage);
            }
        });

        primaryStage.setScene(modeScene);
    }
    /**
     * Shows the admin panel with options to manage questions or users.
     */
    private void showAdminScreen(Stage primaryStage) {
        AdminMenuView view = new AdminMenuView();

        view.getManageQuestionsBtn().setOnAction(e -> showQuestionManager(primaryStage));
        view.getManageUsersBtn().setOnAction(e -> showAdminUserList(primaryStage));
        view.getBackBtn().setOnAction(e -> showModeSelection(primaryStage));

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setTitle("Quantum Millionaire - Admin Panel");
        primaryStage.setScene(scene);
    }

    /**
     * Opens the question manager (admin mode).
     */
    private void showQuestionManager(Stage primaryStage) {
        QModeSelectionModel adminModel = new QModeSelectionModel();
        QModeSelectionView adminView = new QModeSelectionView();
        new QModeSelectionController(adminModel, adminView);

        Scene adminScene = new Scene(adminView, 1280, 720);
        addCSS(adminScene);
        primaryStage.setTitle("Quantum Millionaire - Admin Panel");
        primaryStage.setScene(adminScene);
    }

    /**
     * Starts or resumes a game session. If a user is logged in, their progress
     * (tier and lifelines) is restored before gameplay begins.
     */
    private void showGameScreen(Stage primaryStage) {
        List<Question> questions = QuestionLoader.loadQuestions();
        QModel gameModel = new QModel(questions);

        if (session.hasUser()) {
            Pack_1.profile.User u = session.getCurrentUser();

            gameModel.resetGame();
            for (int i = 0; i < u.getCurrentTier(); i++) {
                gameModel.nextQuestion();
            }

            gameModel.setSuperpositionUsed(u.isSuperpositionUsed());
            gameModel.setEntanglementUsed(u.isEntanglementUsed());
            gameModel.setInterferenceUsed(u.isInterferenceUsed());
        }

        QView gameView = new QView();
        new QController(gameModel, gameView, session, userManager);

        Scene gameScene = new Scene(gameView, 1280, 720);
        addCSS(gameScene);
        primaryStage.setTitle("Quantum Millionaire - Game");
        primaryStage.setScene(gameScene);
    }


    /**
     * Shows the player menu. If returnToMultiplayer is true, loading a profile
     * will return to the multiplayer setup instead of starting a single-player game.
     */
    private void showPlayerMenu(Stage primaryStage, boolean returnToMultiplayer) {
        PlayerMenuView view = new PlayerMenuView();

        // New profile always starts a new single-player game
        view.getNewGameBtn().setOnAction(e -> showNewProfileScreen(primaryStage));

        // Load profile returns to either multiplayer or single-player
        view.getLoadGameBtn().setOnAction(e -> 
            showLoadProfileScreen(primaryStage, returnToMultiplayer)
        );

        view.getQuitBtn().setOnAction(e -> primaryStage.close());

        // Back always returns to mode selection
        view.getBackBtn().setOnAction(e -> showModeSelection(primaryStage));

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setScene(scene);
    }

    /**
     * Shows the player menu with options to start a new profile, load a profile,
     * quit, or return to mode selection.
     */
    private void showPlayerMenu(Stage primaryStage) {
        showPlayerMenu(primaryStage, false);   // default: NOT returning to multiplayer
    }


    /**
     * Shows the admin user list and wires admin‑specific callbacks.
     */
    private void showAdminUserList(Stage primaryStage) {
        ProfileSelectionView view = new ProfileSelectionView();
        AdminUserController controller = new AdminUserController(userManager);

        controller.wireUserList(
                view,
                user -> showAdminUserEditor(primaryStage, user),
                () -> showAdminScreen(primaryStage),
                () -> showAdminNewUser(primaryStage)
        );

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setTitle("Admin - Manage Users");
        primaryStage.setScene(scene);
    }

    /**
     * Shows the admin "create new user" screen.
     */
    private void showAdminNewUser(Stage primaryStage) {
        NewProfileView view = new NewProfileView();

        profileController.wireNewProfileScreen(
                view,
                user -> showAdminUserList(primaryStage),
                () -> showAdminUserList(primaryStage)
        );

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setTitle("Admin - New User");
        primaryStage.setScene(scene);
    }

    /**
     * Shows the new profile creation screen for players.
     */
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

    /**
     * Shows the profile selection screen for players.
     */
    private void showLoadProfileScreen(Stage primaryStage, boolean returnToMultiplayer) {
        ProfileSelectionView view = new ProfileSelectionView();

        profileController.wireProfileSelectionScreen(
            view,
            user -> {
                session.setCurrentUser(user);

                if (returnToMultiplayer) {
                    showNetworkSetup(primaryStage);
                } else {
                    showGameScreen(primaryStage);
                }
            },
            () -> {
                if (returnToMultiplayer) {
                    showModeSelection(primaryStage);
                } else {
                    showPlayerMenu(primaryStage);
                }
            },
            () -> showNewProfileScreen(primaryStage)
        );

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setScene(scene);
    }


    /**
     * Shows the admin user editor for modifying an existing profile.
     */
    private void showAdminUserEditor(Stage primaryStage, Pack_1.profile.User user) {
        AdminUserEditorView view = new AdminUserEditorView(user);

        view.getSaveBtn().setOnAction(e -> {
            try {
                user.setUsername(view.getUsernameField().getText().trim());
                user.setCurrentTier(Integer.parseInt(view.getCurrentTierField().getText()));
                user.setHighestTierReached(Integer.parseInt(view.getHighestTierField().getText()));
                user.setLastGameMoney(Integer.parseInt(view.getLastGameMoneyField().getText()));
                user.setTotalMoneyEarned(Integer.parseInt(view.getTotalMoneyField().getText()));
                user.setGamesWon(Integer.parseInt(view.getGamesWonField().getText()));
                user.setGamesLost(Integer.parseInt(view.getGamesLostField().getText()));
                user.setSuperpositionUsed(view.getSuperUsedBox().isSelected());
                user.setEntanglementUsed(view.getEntUsedBox().isSelected());
                user.setInterferenceUsed(view.getInterfUsedBox().isSelected());
                user.setLifelinesUsed(Integer.parseInt(view.getLifelinesUsedField().getText()));
                user.setTotalLifelinesUsed(Integer.parseInt(view.getTotalLifelinesField().getText()));

                userManagerSaveAll();
                showAdminUserList(primaryStage);
            } catch (NumberFormatException ex) {
                // Optional: add error label or dialog
            }
        });

        view.getCancelBtn().setOnAction(e -> showAdminUserList(primaryStage));

        view.getResetProgressBtn().setOnAction(e -> {
            user.setCurrentTier(0);
            user.setLastGameMoney(0);
            view.getCurrentTierField().setText("0");
            view.getLastGameMoneyField().setText("0");
        });

        view.getResetLifelinesBtn().setOnAction(e -> {
            user.setSuperpositionUsed(false);
            user.setEntanglementUsed(false);
            user.setInterferenceUsed(false);
            user.setLifelinesUsed(0);

            view.getSuperUsedBox().setSelected(false);
            view.getEntUsedBox().setSelected(false);
            view.getInterfUsedBox().setSelected(false);
            view.getLifelinesUsedField().setText("0");
        });

        view.getResetStatsBtn().setOnAction(e -> {
            user.setTotalMoneyEarned(0);
            user.setGamesWon(0);
            user.setGamesLost(0);
            user.setHighestTierReached(0);
            user.setTotalLifelinesUsed(0);

            view.getTotalMoneyField().setText("0");
            view.getGamesWonField().setText("0");
            view.getGamesLostField().setText("0");
            view.getHighestTierField().setText("0");
            view.getTotalLifelinesField().setText("0");
        });

        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setTitle("Admin - Edit User");
        primaryStage.setScene(scene);
    }

    /**
     * Saves all user data through the user manager.
     */
    private void userManagerSaveAll() {
        userManager.saveAllUsers();
    }

    /**
     * Applies the shared CSS theme to a scene.
     */
    private void addCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/Pack_1/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS Path Error: " + e.getMessage());
        }
    }

  

    /**
     * Helper to keep button styling consistent
     */
    private Button createModeButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().addAll("answer-btn");
        b.setPrefSize(400, 100);
        b.setAlignment(Pos.CENTER);
        b.setTextAlignment(TextAlignment.CENTER);
        return b;
    }

    /**
     * NEW METHOD: Shows the Network Connection screen
     */
    private void showNetworkSetup(Stage primaryStage) {

        // Create the view
        Network.MP_ConnectionView view = new Network.MP_ConnectionView();

        // Get the real profile name from the active session
        String playerName = session.getCurrentUser().getUsername();

        // Create the controller (patched version requires Stage)
        new Network.MP_ConnectionController(view, playerName, primaryStage);

        // Back button returns to mode selection
        view.getBackBtn().setOnAction(e -> showModeSelection(primaryStage));

        // Show the screen
        Scene scene = new Scene(view, 1280, 720);
        addCSS(scene);
        primaryStage.setTitle("Quantum Millionaire - Network Setup");
        primaryStage.setScene(scene);
    }



    
    
    public static void main(String[] args) {
        launch(args);
    }
}
