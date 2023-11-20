package javafx.util.bootlegskyrim;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

import java.io.File;
import java.util.ArrayList;

public class Player extends Thread {
    private enum Dir {FORWARD, BACKWARD, LEFT, RIGHT, STOP}

    private Dir direction = Dir.STOP;
    private final World world;

    double angle = 0;
    boolean mousePressed = false;
    boolean alive = true;
    double lastMouseX;
    double lastMouseY;
    boolean inAttackRange = false;
    int maxHealth;
    int health;
    int attackDamage;
    int level;
    int kills;
    int recentKills;
    int movementSpeed = 4;
    int gold;

    ImageView swordNeutralView = new ImageView(new Image("file:sword/aerondight.png"));
    ImageView swordAttackView = new ImageView(new Image("file:sword/aerondight_attack.png"));

    String musicFile = "sword/slash.wav";
    Media sound = new Media(new File(musicFile).toURI().toString());
    MediaPlayer slashSoundPlayer = new MediaPlayer(sound);

    Player(World world) {
        swordNeutralView.setX(1200);
        swordNeutralView.setY(300);
        swordAttackView.setX(1200);
        swordAttackView.setY(300);
        kills = 0;
        gold = 0;
        health = 50;
        level = 1;
        maxHealth = 50;
        attackDamage = 2;

        slashSoundPlayer.setVolume(0.1);

        this.world = world;
    }

    /**
     * spustí thread s hráčom a zastaví sa pokiaľ hráč nie je nažive
     */
    @Override
    public void run() {
        while (alive) {
            try {
                if (health <= 0) alive = false;
                movement();
                levelUp();
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * zvýší hráčov level ak zabil 2 nepriateľov po sebe
     * a vylieči hráča o 5 životov
     * a pridá hráčovi silu útoku a goldy
     */
    void levelUp(){
        if(recentKills == 2) {
            level++;
            if (maxHealth - health <= 10){
                health += maxHealth - health;
            }
            if (maxHealth - health > 10){
                health +=5;
            }
            gold += 2;
            recentKills = 0;
            attackDamage+=2;
        }
    }

    /**
     * nastaví smer pohybu hráča podľa klávesy
     * @param event
     */
    void setDirection(KeyEvent event) {
        switch (event.getCode()) {
            case W -> direction = Dir.FORWARD;
            case S -> direction = Dir.BACKWARD;
            case A -> direction = Dir.LEFT;
            case D -> direction = Dir.RIGHT;
        }
    }

    /**
     * hráč zautočí a uberie monštru HP
     */
    void attack() {
        if (this.world.enemy != null) {
            Enemy enemy = this.world.enemy;
            enemy.hp -= attackDamage;
        }
    }

    /**
     * nastavuje a smeruje kameru pri stlačení a ťahaní myši
     * @param event
     */
    void mouseDragged(MouseEvent event) {
        if (mousePressed) {
            var transforms = new ArrayList<>(world.camera.getTransforms());
            world.camera.getTransforms().clear();
            for (Transform t: transforms) {
                Rotate rotate = (Rotate) t;
                float MOUSE_SENSITIVITY = 0.1f;
                if (rotate.getAxis().equals(Rotate.Y_AXIS)) {
                    rotate.setAngle(rotate.getAngle() + (event.getSceneX() - lastMouseX) * MOUSE_SENSITIVITY);
                    angle = rotate.getAngle();
                } else {
                    rotate.setAngle(rotate.getAngle() - (event.getSceneY() - lastMouseY) * MOUSE_SENSITIVITY);
                }
                world.camera.getTransforms().add(rotate);
            }
        }
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    void stopMovement() {
        direction = Dir.STOP;
    }

    /**
     * hýbe hráča podľa nasataveného smeru a uhla
     */
    private void movement() {
        if (direction == Dir.STOP) return;
        double dx = Math.sin(Math.toRadians(angle));
        double dz = Math.cos(Math.toRadians(angle));
        double oldZ = world.camera.getTranslateZ();
        double oldX = world.camera.getTranslateX();
        switch (direction) {
            case FORWARD -> {
                world.camera.translateZProperty().set(world.camera.getTranslateZ() + dz * movementSpeed);
                world.camera.translateXProperty().set(world.camera.getTranslateX() + dx * movementSpeed);
            }
            case BACKWARD -> {
                world.camera.translateZProperty().set(world.camera.getTranslateZ() - dz * movementSpeed);
                world.camera.translateXProperty().set(world.camera.getTranslateX() - dx * movementSpeed);
            }
            case LEFT -> {
                world.camera.translateZProperty().set(world.camera.getTranslateZ() + dx * movementSpeed);
                world.camera.translateXProperty().set(world.camera.getTranslateX() - dz * movementSpeed);
            }
            case RIGHT -> {
                world.camera.translateZProperty().set(world.camera.getTranslateZ() - dx * movementSpeed);
                world.camera.translateXProperty().set(world.camera.getTranslateX() + dz * movementSpeed);
            }
        }
        if (world.checkCameraCollision()) {
            world.camera.translateZProperty().set(oldZ);
            world.camera.translateXProperty().set(oldX);
        }
    }
}