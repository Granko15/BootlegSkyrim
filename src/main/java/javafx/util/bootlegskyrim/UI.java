package javafx.util.bootlegskyrim;

import javafx.beans.property.DoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;


public class UI {

    World world;

    ImageView quest = new ImageView(new Image("file:UI/quest.png"));

    ImageView deathScreen = new ImageView(new Image("file:UI/death_screen.png"));

    ImageView winningScreen = new ImageView(new Image("file:UI/winning_screen.png"));

    ImageView coinBag = new ImageView(new Image("file:UI/coinBag.png"));

    ImageView detected = new ImageView(new Image("file:UI/detected.png"));

    ImageView hidden = new ImageView(new Image("file:UI/hidden.png"));

    ImageView levelUp = new ImageView(new Image("file:UI/level_up.png"));

    Rectangle healthBar;

    Text coins = new Text(1825,100, "0");

    Text level = new Text(1550,100, String.format("Lvl: %d",1));

    /**
     * nastaví všetky on screen itemy
     * @param world
     */
    UI(World world){
        this.world = world;
        coinBag.setX(1700);
        coinBag.setY(25);
        detected.setX(900);
        detected.setY(500);

        hidden.setX(900);
        hidden.setY(500);
        updateHealthBar(50, 50);
        coins.setFont(new Font(50));
        coins.setFill(Color.GOLD);
        level.setFont(new Font(50));
        level.setFill(Color.GOLD);
    }

    /**
     * prepočítava veľkosť health baru podľa hráčových aktuálnych a maximálnych životov
     * a zároveň vytvorí už nový health bar so správnymi rozmermi
     * @param currentHp
     * @param maxHealth
     */
    public void updateHealthBar(double currentHp, double maxHealth){
        double percentage = currentHp/maxHealth;
        double width = percentage * 300;
        healthBar = new Rectangle(width, 15, Color.RED);
        healthBar.setX(50);
        healthBar.setY(25);
    }
}
