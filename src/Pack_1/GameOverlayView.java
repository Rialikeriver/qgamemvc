package Pack_1;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class GameOverlayView extends StackPane {

    private final Button primaryBtn;
    private final Button secondaryBtn;

    public GameOverlayView(String title, String subtitle, String amountRaw, boolean isWin) {

        setStyle("-fx-background-color: rgba(0,0,0,0.75);");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 72px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");

        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setStyle("-fx-font-size: 32px; -fx-text-fill: white;");
        subtitleLbl.setWrapText(true);
        subtitleLbl.setAlignment(Pos.CENTER);
        subtitleLbl.setMaxWidth(800);

        // Format money with commas
        int amountInt = 0;
        try {
            amountInt = Integer.parseInt(amountRaw.replace("$", "").replace(",", ""));
        } catch (Exception ignored) {}

        String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(amountInt);
        String moneyText = "$" + formattedAmount;

        VBox box = new VBox(40);
        box.setAlignment(Pos.CENTER);

        if (isWin) {
            Label earnedLbl = new Label("You earned:");
            earnedLbl.setStyle("-fx-font-size: 40px; -fx-text-fill: white;");

            Label amountLbl = new Label(moneyText);
            amountLbl.setStyle("-fx-font-size: 72px; -fx-text-fill: #d4af37; -fx-font-weight: bold;");

            box.getChildren().addAll(titleLbl, subtitleLbl, earnedLbl, amountLbl);
            addConfetti();
        } else {
            Label lossLbl = new Label("You earned " + moneyText);
            lossLbl.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");
            lossLbl.setWrapText(true);
            lossLbl.setMaxWidth(800);
            lossLbl.setAlignment(Pos.CENTER);

            box.getChildren().addAll(titleLbl, subtitleLbl, lossLbl);
        }

        primaryBtn = new Button("Play Again");
        secondaryBtn = new Button("Quit");

        primaryBtn.getStyleClass().add("answer-btn");
        secondaryBtn.getStyleClass().add("answer-btn");

        box.getChildren().addAll(primaryBtn, secondaryBtn);

        getChildren().add(box);
    }


    private void addConfetti() {
        Random r = new Random();

        for (int i = 0; i < 80; i++) {
            Circle c = new Circle(6, Color.hsb(r.nextInt(360), 1, 1));
            c.setTranslateX(r.nextInt(1280) - 640);
            c.setTranslateY(-400);

            getChildren().add(c);

            Duration duration = Duration.seconds(2 + r.nextDouble());
            KeyValue kv = new KeyValue(c.translateYProperty(), 400 + r.nextInt(200));
            KeyFrame kf = new KeyFrame(duration, kv);

            Timeline t = new Timeline(kf);
            t.setCycleCount(1);
            t.play();
        }
    }

    public Button getPrimaryBtn() { return primaryBtn; }
    public Button getSecondaryBtn() { return secondaryBtn; }
}
