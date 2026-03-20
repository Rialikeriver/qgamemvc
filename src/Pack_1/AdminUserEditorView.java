package Pack_1;

import Pack_1.profile.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * JavaFX view for editing all aspects of a user's profile in admin mode.
 * This screen exposes identity, progression, money statistics, win/loss
 * records, and lifeline usage, along with reset and save actions. The layout
 * uses a central form grid and a bottom action bar styled to match the
 * application's theme.
 *
 * <p>Fields are grouped visually and logically to mirror the structure of
 * the {@link User} model. The controller layer reads values from these
 * fields, applies validation, and persists changes through {@code UserManager}.</p>
 */
public class AdminUserEditorView extends BorderPane {

    // Identity
    private final TextField usernameField;

    // Progression
    private final TextField currentTierField;
    private final TextField highestTierField;

    // Money
    private final TextField lastGameMoneyField;
    private final TextField totalMoneyField;

    // Win/loss
    private final TextField gamesWonField;
    private final TextField gamesLostField;

    // Lifelines (session + lifetime)
    private final CheckBox superUsedBox;
    private final CheckBox entUsedBox;
    private final CheckBox interfUsedBox;
    private TextField lifelinesUsedField;
    private final TextField totalLifelinesField;

    // Action buttons
    private final Button saveBtn;
    private final Button cancelBtn;
    private final Button resetProgressBtn;
    private final Button resetLifelinesBtn;
    private final Button resetStatsBtn;

    /**
     * Builds the full admin user editor UI, pre-populating all fields with
     * the given user's current values. The layout is a grid of labeled fields
     * followed by a bottom action bar containing reset and save controls.
     */
    public AdminUserEditorView(User user) {
        setStyle("-fx-background-color: linear-gradient(to bottom, #1a0b2e, #000022);");
        setPadding(new Insets(20));

        Label title = new Label("EDIT USER: " + user.getUsername());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #d4af37;");
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        int row = 0;

        // Identity
        usernameField = new TextField(user.getUsername());

        // Progression
        currentTierField = new TextField(String.valueOf(user.getCurrentTier()));
        highestTierField = new TextField(String.valueOf(user.getHighestTierReached()));

        // Money
        lastGameMoneyField = new TextField(String.valueOf(user.getLastGameMoney()));
        totalMoneyField = new TextField(String.valueOf(user.getTotalMoneyEarned()));

        // Win/loss
        gamesWonField = new TextField(String.valueOf(user.getGamesWon()));
        gamesLostField = new TextField(String.valueOf(user.getGamesLost()));

        // Lifelines
        superUsedBox = new CheckBox("Superposition used");
        entUsedBox = new CheckBox("Entanglement used");
        interfUsedBox = new CheckBox("Interference used");
        superUsedBox.setSelected(user.isSuperpositionUsed());
        entUsedBox.setSelected(user.isEntanglementUsed());
        interfUsedBox.setSelected(user.isInterferenceUsed());

        lifelinesUsedField = new TextField(String.valueOf(user.getLifelinesUsed()));
        totalLifelinesField = new TextField(String.valueOf(user.getTotalLifelinesUsed()));

        // Grid layout
        grid.add(new Label("Username:"), 0, row); grid.add(usernameField, 1, row++);

        grid.add(new Label("Current Tier (0-14):"), 0, row); grid.add(currentTierField, 1, row++);
        grid.add(new Label("Highest Tier:"), 0, row); grid.add(highestTierField, 1, row++);

        grid.add(new Label("Last Game Money:"), 0, row); grid.add(lastGameMoneyField, 1, row++);
        grid.add(new Label("Total Money Earned:"), 0, row); grid.add(totalMoneyField, 1, row++);

        grid.add(new Label("Games Won:"), 0, row); grid.add(gamesWonField, 1, row++);
        grid.add(new Label("Games Lost:"), 0, row); grid.add(gamesLostField, 1, row++);

        grid.add(superUsedBox, 0, row, 2, 1); row++;
        grid.add(entUsedBox, 0, row, 2, 1); row++;
        grid.add(interfUsedBox, 0, row, 2, 1); row++;

        grid.add(new Label("Current Game Lifelines Used:"), 0, row); grid.add(lifelinesUsedField, 1, row++);
        grid.add(new Label("Total Lifelines Used:"), 0, row); grid.add(totalLifelinesField, 1, row++);

        // Buttons
        saveBtn = new Button("Save");
        cancelBtn = new Button("Cancel");
        resetProgressBtn = new Button("Reset Progress");
        resetLifelinesBtn = new Button("Reset Lifelines");
        resetStatsBtn = new Button("Reset Lifetime Stats");

        saveBtn.getStyleClass().add("answer-btn");
        cancelBtn.getStyleClass().add("answer-btn");
        resetProgressBtn.getStyleClass().add("answer-btn");
        resetLifelinesBtn.getStyleClass().add("answer-btn");
        resetStatsBtn.getStyleClass().add("answer-btn");

        HBox buttons = new HBox(15, resetProgressBtn, resetLifelinesBtn, resetStatsBtn, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20));

        setCenter(grid);
        setBottom(buttons);
    }

    // Getters for controller wiring
    public TextField getUsernameField() { return usernameField; }
    public TextField getCurrentTierField() { return currentTierField; }
    public TextField getHighestTierField() { return highestTierField; }
    public TextField getLastGameMoneyField() { return lastGameMoneyField; }
    public TextField getTotalMoneyField() { return totalMoneyField; }
    public TextField getGamesWonField() { return gamesWonField; }
    public TextField getGamesLostField() { return gamesLostField; }
    public CheckBox getSuperUsedBox() { return superUsedBox; }
    public CheckBox getEntUsedBox() { return entUsedBox; }
    public CheckBox getInterfUsedBox() { return interfUsedBox; }
    public TextField getLifelinesUsedField() { return lifelinesUsedField; }
    public TextField getTotalLifelinesField() { return totalLifelinesField; }

    public Button getSaveBtn() { return saveBtn; }
    public Button getCancelBtn() { return cancelBtn; }
    public Button getResetProgressBtn() { return resetProgressBtn; }
    public Button getResetLifelinesBtn() { return resetLifelinesBtn; }
    public Button getResetStatsBtn() { return resetStatsBtn; }
}
