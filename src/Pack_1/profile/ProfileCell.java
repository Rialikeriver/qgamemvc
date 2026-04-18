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
 * total money earned, win/loss record, and multiplayer statistics.</p>
 */
public class ProfileCell extends ListCell<User> {

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

        // Total money (single-player)
        Label totalMoneyLbl = new Label("Total Money: $" + user.getTotalMoneyEarned());
        totalMoneyLbl.setStyle("-fx-text-fill: white;");

        // Wins/losses (single-player)
        Label winsLossLbl = new Label("Won: " + user.getGamesWon() + "  Lost: " + user.getGamesLost());
        winsLossLbl.setStyle("-fx-text-fill: white;");

        // Multiplayer stats — placed directly under wins/losses
        Label mpMoneyLbl = new Label("MP Money: $" + user.getMpMoneyEarned());
        mpMoneyLbl.setStyle("-fx-text-fill: white;");

        Label mpWinsLossLbl = new Label("MP Won: " + user.getMpGamesWon() + "  Lost: " + user.getMpGamesLost());
        mpWinsLossLbl.setStyle("-fx-text-fill: white;");

        VBox colC = new VBox(2,
                totalMoneyLbl,
                winsLossLbl
        );
        
        VBox colD = new VBox(2,
                mpMoneyLbl,
                mpWinsLossLbl
        );

        // TFour-column layout
        HBox statsRow = new HBox(40, colA, colB, colC, colD);
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
