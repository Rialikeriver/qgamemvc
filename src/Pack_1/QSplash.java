package Pack_1;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Splash screen class for Ria's Quantum Millionaire.
 * Extends Stage to act as a standalone window.
 */
public class QSplash extends Stage {
    
    public QSplash(Runnable onComplete) {
        // 1. Window Style: Removes the top bar (minimize/close buttons)
        this.initStyle(StageStyle.UNDECORATED);

        // 2. UI Elements
        Label title = new Label("RIA'S QUANTUM MILLIONAIRE");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        
        Label loading = new Label("Initializing Quantum States...");
        loading.setStyle("-fx-text-fill: white;");

        // 3. Layout Container
        VBox layout = new VBox(20, title, loading);
        layout.setAlignment(Pos.CENTER);
        // Dark purple theme to match your game background
        layout.setStyle("-fx-background-color: #1a0b2e; -fx-border-color: #d4af37; -fx-border-width: 2;");
        
        // 4. Scene setup
        this.setScene(new Scene(layout, 500, 300));
        this.centerOnScreen();

        // 5. Timer: 3 seconds as required by Assignment 4
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            this.close();     // Close the splash
            onComplete.run(); // Call back to the Launcher to start the main game
        });
        delay.play();
    }
}