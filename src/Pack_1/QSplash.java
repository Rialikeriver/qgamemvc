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
 * Splash screen for Quantum Millionaire. This lightweight stage appears briefly
 * at application startup, displaying the game title and a loading message before
 * transitioning automatically to the main mode‑selection screen.
 *
 * <p>The splash uses an undecorated window, a centered layout, and a short
 * timed delay (3 seconds) as required by the assignment specification. When the
 * delay completes, the splash closes itself and invokes the provided callback
 * to continue application initialization.</p>
 */
public class QSplash extends Stage {

    /**
     * Creates a splash window and schedules its automatic dismissal.
     *
     * @param onComplete a callback executed after the splash closes
     */
    public QSplash(Runnable onComplete) {

        // Remove window decorations (title bar, close/minimize buttons)
        this.initStyle(StageStyle.UNDECORATED);

        // Title label
        Label title = new Label("Who wnats to be a millionaire - Ria and Taylor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");

        // Loading message
        Label loading = new Label("Initializing Quantum States...");
        loading.setStyle("-fx-text-fill: white;");

        // Layout container
        VBox layout = new VBox(20, title, loading);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1a0b2e; -fx-border-color: #d4af37; -fx-border-width: 2;");

        // Scene setup
        this.setScene(new Scene(layout, 500, 300));
        this.centerOnScreen();

        // 3‑second delay before continuing to the main application
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            this.close();
            onComplete.run();
        });
        delay.play();
    }
}
