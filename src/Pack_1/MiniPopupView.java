package Pack_1;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A compact, centered popup used for confirmations, warnings, and small
 * messages. The popup dims the background, displays a gold title, a bold
 * header, a message body, and one or two action buttons depending on the
 * caller's needs. The inner card is styled as a small floating panel to
 * distinguish it from full‑screen overlays.
 *
 * <p>This view is intentionally minimal and reusable across the application
 * for quick prompts such as "Are you sure?", "Overwrite save?", or "Exit
 * game?". Controllers attach behavior to the exposed buttons.</p>
 */
public class MiniPopupView extends StackPane {

    // Action buttons (OK / Cancel)
    private final Button primaryBtn;
    private final Button secondaryBtn;

    /**
     * Builds a centered popup card with the given title, header, message,
     * and an optional secondary button. The background is dimmed to draw
     * focus to the popup content.
     */
    public MiniPopupView(String title, String header, String message, boolean showSecondary) {

        // Full-screen dimmer
        setStyle("-fx-background-color: rgba(0,0,0,0.55);");

        // Title (gold)
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 36px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");

        // Header (white, bold)
        Label headerLbl = new Label(header);
        headerLbl.setStyle("-fx-font-size: 26px; -fx-text-fill: white; -fx-font-weight: bold;");
        headerLbl.setWrapText(true);
        headerLbl.setMaxWidth(450);

        // Message (white)
        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-font-size: 22px; -fx-text-fill: white;");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(450);

        // Buttons
        primaryBtn = new Button("OK");
        primaryBtn.getStyleClass().add("answer-btn");

        secondaryBtn = new Button("Cancel");
        secondaryBtn.getStyleClass().add("answer-btn");
        secondaryBtn.setVisible(showSecondary);

        // Inner card (the actual popup)
        VBox card = new VBox(20, titleLbl, headerLbl, msgLbl, primaryBtn);
        if (showSecondary) card.getChildren().add(secondaryBtn);

        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: rgba(0,0,0,0.85);" +
                "-fx-padding: 30;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: #d4af37;" +
                "-fx-border-width: 2;"
        );

        // Center the card
        getChildren().add(card);
        setAlignment(card, Pos.CENTER);
    }

    // Accessors for controller wiring
    public Button getPrimaryBtn() { return primaryBtn; }
    public Button getSecondaryBtn() { return secondaryBtn; }
}
