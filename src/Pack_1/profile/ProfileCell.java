package Pack_1.profile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Custom {@link ListCell} implementation for rendering {@link User} objects in a
 * profile selection list.
 *
 * <p>This cell displays a compact profile card containing the user's avatar,
 * username, last played date, tier, current game progress, lifeline usage,
 * total money earned, and win/loss record. The layout is structured into
 * multiple columns to keep related statistics grouped and visually balanced.</p>
 *
 * <p>The cell is fully styled using JavaFX nodes and CSS classes, allowing the
 * surrounding UI to apply theme-specific styling (e.g., the {@code profile-card}
 * style class). All content is rebuilt on each update to ensure accurate
 * reflection of the user's current state.</p>
 */
public class ProfileCell extends ListCell<User> {

    /**
     * Updates the visual representation of the cell based on the given user.
     *
     * <p>If the cell is empty or the user is {@code null}, the graphic is cleared.
     * Otherwise, a structured profile card is constructed containing:</p>
     *
     * <ul>
     *   <li>An avatar icon</li>
     *   <li>The username in highlighted styling</li>
     *   <li>Last played date and current tier</li>
     *   <li>Current game money and lifelines used</li>
     *   <li>Total money earned and win/loss statistics</li>
     * </ul>
     *
     * <p>The layout uses a combination of {@link HBox} and {@link VBox} containers
     * to create a three-column statistics row beneath the username. All labels are
     * styled for readability against the game's UI theme.</p>
     *
     * @param user  the user associated with this cell
     * @param empty whether the cell should be rendered as empty
     */
    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);

        if (empty || user == null) {
            setGraphic(null);
            return;
        }

        // Avatar
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 32px;");
        avatar.setMinWidth(50);
        avatar.setAlignment(Pos.CENTER);

        // Username
        Label name = new Label(user.getUsername());
        name.setStyle("-fx-font-size: 18px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");

        // Last played
        String lastPlayed = (user.getLastPlayed() == null)
                ? "Never played"
                : user.getLastPlayed().toString();

        Label lastPlayedLbl = new Label("Last Played: " + lastPlayed);
        lastPlayedLbl.setStyle("-fx-text-fill: white;");

        // Tier
        Label tierLbl = new Label("Current Tier: " + (user.getCurrentTier() + 1));
        tierLbl.setStyle("-fx-text-fill: white;");

        VBox colA = new VBox(2, lastPlayedLbl, tierLbl);

        // Current game money
        Label currentMoneyLbl = new Label("Current Game: $" + user.getLastGameMoney());
        currentMoneyLbl.setStyle("-fx-text-fill: white;");

        // Lifelines
        Label lifelinesLbl = new Label("Lifelines Used: " + user.getLifelinesUsed());
        lifelinesLbl.setStyle("-fx-text-fill: white;");

        VBox colB = new VBox(2, currentMoneyLbl, lifelinesLbl);

        // Total money + wins/losses
        Label totalMoneyLbl = new Label("Total Money: $" + user.getTotalMoneyEarned());
        totalMoneyLbl.setStyle("-fx-text-fill: white;");

        Label winsLossLbl = new Label("Won: " + user.getGamesWon() + "  Lost: " + user.getGamesLost());
        winsLossLbl.setStyle("-fx-text-fill: white;");

        VBox colC = new VBox(2, totalMoneyLbl, winsLossLbl);

        // Three-column layout
        HBox statsRow = new HBox(40, colA, colB, colC);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        // Stack name + stats
        VBox textBox = new VBox(6, name, statsRow);

        // Final card layout
        HBox root = new HBox(15, avatar, textBox);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("profile-card");

        setGraphic(root);
    }
}
