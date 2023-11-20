package javafx.util.bootlegskyrim;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 *                          ╔═╡The Witcher: The Curse of Brokilon╞═╗
 *      Vitajte vo svete Zaklínača, hra predstavuje jeden konkrétny kontrakt pre zaklínača.
 *      Treba vyčistiť Brokilon od monštier, ktoré sa vďaka neschopnosti dryád,
 *      veľmi rozmnožili.
 *
 *      Na začiatku hry máš popísané detaily kontraktu. Stlačením myši ich dáš preč.
 *      Nepriatelia sa spawnujú na náhodných miestach. Keď zabiješ jedného spawne sa ďalší.
 *      Kurzor ti napovie, či ťa nepriateľ vidí alebo nie.
 *      Keď ťa monštrum spozoruje, začne sa točiť a rozbehne sa proti tebe.
 *      Pozor, každé monštrum vie chodiť cez stromy a skaly, ty však nie!
 *      Za každého nepriateľa dostaneš mince. Ak ich nazbieraš dostatočne veľa, vyhrávaš.
 *      Pozor, nepriateľia ťa vedia aj zraňovať. Preto keď sa ti minú životy, prehrávaš.
 *      Po zabití 2 nepriateľov dostaneš novú úroveň, zvýši sa ti sila útoku a vyliečiš sa o 5 životov.
 *      Nech je tvoj strieborný meč ostrý a nepriatelia grambľavý, lovu zdar!
 *
 *      HINT: Máš o niečo väčší dosah útoku ako monštrá, tak to využi ;)
 *
 *      Cieľ: Nazbierať 8 a viac goldov.
 *
 *      ovládanie:
 *          WASD - pohyb
 *          ťahanie myšou - pohyb kamery
 *          J (podržanie) - zobraz kontrakt
 *          SPACE - útok
 *
 *          CREATIVE MODE(iba pre cheaterov a tých čo si chcú pozrieť mapu zvrchu):
 *              R,F - lietanie hore/dolu
 *
 *       P.S.: Občas hra spadne kvôli funkcii checkCameraCollision(), kvôli "missing case in transform state switch"
 *              čo som nevedel zistiť, čo znamená. Hru treba iba reštartovať a modliť sa :D
 *
 *       created by: Matúš Granec, 2022
 */

public class World extends Application {
    AnchorPane topLayer2D = new AnchorPane();

    Scene scene = new Scene(topLayer2D, 1920, 1080, true);

    Group group3d = new Group();
    SubScene bottomLayer3D = new SubScene(group3d, 1920, 1080, true, SceneAntialiasing.BALANCED);

    UI myUi = new UI(this);

    Camera camera = new PerspectiveCamera(true);

    String musicFile = "sounds/Back_On_The_Path.mp3";
    Media sound = new Media(new File(musicFile).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(sound);
    double volume = 0.05;

    String levelUpFile = "sounds/level_up.mp3";
    Media levelUpSound = new Media(new File(levelUpFile).toURI().toString());
    MediaPlayer levelUpPlayer = new MediaPlayer(levelUpSound);

    Player player = new Player(this);
    int oldPlayerLevel = player.level;
    Enemy enemy;
    int enemyId;

    boolean won = false;
    boolean lost = false;

    List<Pair<Integer,Integer>> freeSpace = new ArrayList<>();

    private final char[][] map = new char[30][30];

    List<String> allLines;

    {
        try {
            allLines = Files.readAllLines(Paths.get("world.txt"));
            for (int i = 0; i < allLines.size(); i++) {
                for (int j = 0; j < allLines.get(i).length(); j++){
                    map[i][j] = allLines.get(i).charAt(j);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Nastaví celú scénu a spustí potrebné veci (player, enemy)
     * @param stage nastaví hlavné pódium aplikácie
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        topLayer2D.getChildren().add(bottomLayer3D);
        topLayer2D.getChildren().add(myUi.quest);
        topLayer2D.getChildren().add(myUi.coins);
        topLayer2D.getChildren().add(myUi.coinBag);
        topLayer2D.getChildren().add(myUi.level);
        topLayer2D.getChildren().add(myUi.healthBar);
        topLayer2D.getChildren().add(player.swordNeutralView);

        mediaPlayer.volumeProperty().set(volume);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();

        levelUpPlayer.setVolume(0.5);

        bottomLayer3D.setFill(Color.rgb(10, 18, 29));
        bottomLayer3D.setCamera(camera);
        camera.setTranslateX(400);
        camera.setTranslateZ(50);
        camera.setFarClip(5000);

        loadMap();
        setEvents();
        player.start();
        spawnEnemy();

        stage.setTitle("╔═╡The Witcher: The Curse of Brokilon╞═╗");
        stage.setScene(scene);
        stage.show();

    }

    /**
     * načíta mapu zo vstupného súboru
     * B - okraj
     * T - najprv hlina, potom strom
     * . - tráva
     * R - najprv hlina, potom skala
     */
    private void loadMap() {
        int Z = 0;
        for (char[] chars : map) {
            int X = 0;
            for (int j = 0; j < map[0].length; j++) {
                switch (chars[j]) {
                    case 'B' -> {
                        createBox(X, Z, "file:wall/dark_forest.jpg");
                    }
                    case 'c' -> {
                        createFloorTile(X, Z, "file:textures/blocks/cobblestone.png");
                    }
                    case '.' -> {
                        freeSpace.add(new Pair<>(X, Z));
                        createFloorTile(X, Z, "file:textures/blocks/grass_carried.png");
                    }
                    default -> createFloorTile(X, Z, "file:textures/blocks/dirt.png");
                }
                X += 50;
            }
            Z += 50;
        }
        Z = 0;
        for (char[] chars : map) {
            int X = 0;
            for (int j = 0; j < map[0].length; j++) {
                switch (chars[j]) {
                    case 'T' -> {
                        new Tree(this, X, 10, Z);
                    }
                    case 'R' -> {
                        new Rock(this, X, 10, Z);
                    }
                }
                X += 50;
            }
            Z += 50;
        }
    }

    /**
     *
     * @param X pozícia na x-ovej osi
     * @param Z pozícia na z-ovej osi
     * @param path cesta k textúre
     */
    private void createBox(int X, int Z, String path) {
        Box box = new Box(50, 200,50);
        box.setTranslateX(X);
        box.setTranslateZ(Z);
        box.setTranslateY(-70);
        PhongMaterial boxMaterial = new PhongMaterial();
        boxMaterial.setDiffuseMap(new Image(path));
        box.setMaterial(boxMaterial);
        group3d.getChildren().add(box);
    }

    /**
     * @param X pozícia na x-ovej osi
     * @param Z pozícia na z-ovej osi
     * @param path cesta k textúre
     */
    private void createFloorTile(int X, int Z, String path) {
        Box box = new Box(50,1,50);
        box.setTranslateX(X);
        box.setTranslateZ(Z);
        box.setTranslateY(25);
        PhongMaterial floorMaterial = new PhongMaterial();
        floorMaterial.setDiffuseMap(new Image(path));
        box.setMaterial(floorMaterial);
        group3d.getChildren().add(box);
    }

    /**
     * spawn enemy na náhodnom voľnom políčku
     */
    public void spawnEnemy(){
        enemyId = group3d.getChildren().size();
        int randomIndex = new Random().nextInt(freeSpace.size()-1);
        this.enemy = new Enemy(this, freeSpace.get(randomIndex).getKey(), 3, freeSpace.get(randomIndex).getValue(), enemyId);
        this.enemy.start();
    }

    /**
     * @return vráti true ak som narazil do objektu a false ak nie
     */
    boolean checkCameraCollision() {
        var c = camera.getBoundsInParent();
        for (Node n: group3d.getChildren()) {
            if (n instanceof Box) {
                Box b = (Box) n;
                if (b.getBoundsInParent().intersects(c)) {
                    return true;
                }
            }
            if( ("pine4m_pine-trunk").equals(n.idProperty().getValue()) ||
                ("flesh_crea_01 - Default").equals(n.idProperty().getValue()) ||
                ("Rock_Default").equals(n.idProperty().getValue())){
                if (n.getBoundsInParent().intersects(c)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  nastaví key bindings a ovládanie hráča/kamery
     *  a zabaezpečuje kontrolu eventov ako update HP, zranenie od monštra
     *  level up a zbieranie goldov
     */
    private void setEvents() {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case R -> camera.translateYProperty().set(camera.getTranslateY() - 2);
                case F -> camera.translateYProperty().set(camera.getTranslateY() + 2);
                case SPACE -> {
                    if(player.alive){
                        topLayer2D.getChildren().remove(player.swordNeutralView);
                        if (!topLayer2D.getChildren().contains(player.swordAttackView)) {
                            topLayer2D.getChildren().add(player.swordAttackView);
                        }
                        player.slashSoundPlayer.play();
                        if(player.inAttackRange){
                            if (enemy != null && !enemy.alive) {
                                killEnemy();
                                updateLevel();
                                spawnEnemy();
                            }
                            player.attack();
                        }
                        updateHealthBar();
                        updateCoins();
                    }
                }
                case J ->{
                    if(player.alive){
                        if(!topLayer2D.getChildren().contains(myUi.quest)) {
                            topLayer2D.getChildren().add(myUi.quest);
                        }
                        topLayer2D.getChildren().remove(myUi.hidden);
                        topLayer2D.getChildren().remove(myUi.detected);
                    }
                }
                default -> {

                    addCursor();
                    updateHealthBar();
                    if(!won) {
                        win();
                    }
                    if (!player.alive){
                        killPlayer();
                    }
                    updateCoins();
                    player.setDirection(event);
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case SPACE -> {
                    if(player.alive) {
                        topLayer2D.getChildren().remove(player.swordAttackView);
                        topLayer2D.getChildren().add(player.swordNeutralView);
                        player.slashSoundPlayer.stop();
                        updateHealthBar();
                        updateCoins();
                    }

                }
                case J -> {
                    topLayer2D.getChildren().remove(myUi.quest);
                }
                default -> {
                    updateHealthBar();
                    updateCoins();
                    if(!won) {
                        win();
                    }
                    if (!player.alive && !lost){
                        killPlayer();
                    }
                    addCursor();
                    player.stopMovement();
                }
            }
        });

        camera.getTransforms().add(new Rotate(0, 0,0,0, Rotate.Y_AXIS));
        camera.getTransforms().add(new Rotate(0, 0,0,0, Rotate.X_AXIS));

        scene.setOnMousePressed(event -> {

            if(!"sounds/Geralt_Of_Rivia.mp3".equals(musicFile) &&
                    !"sounds/Drink_Up_Theres_More!.mp3".equals(musicFile)){
                mediaPlayer.stop();
                musicFile = "sounds/Geralt_Of_Rivia.mp3";
                sound = new Media(new File(musicFile).toURI().toString());
                mediaPlayer = new MediaPlayer(sound);
                volume = 0.1;
                mediaPlayer.setVolume(volume);
                mediaPlayer.play();
            }

            if(!won) {
                win();
            }

            if (!player.alive && !lost){
                killPlayer();
            }

            updateHealthBar();

            topLayer2D.getChildren().remove(myUi.quest);
            topLayer2D.getChildren().remove(myUi.levelUp);
            addCursor();

            player.mousePressed = true;
            player.lastMouseX = event.getSceneX();
            player.lastMouseY = event.getSceneY();
        });

        scene.setOnMouseReleased(event -> {
            if(!won) {
                win();
            }
            if (!player.alive && !lost){
                killPlayer();
            }
            player.mousePressed = false;
                });

        scene.setOnMouseDragged(event -> {
            if(!won) {
                win();
            }

            if (!player.alive && !lost){
                killPlayer();
            }

            player.mouseDragged(event);
        });
    }

    /**
     * pridá kurzor na obrazovku podľa toho či ma monštrum vidí alebo nie
     */
    public void addCursor(){
        if(enemy != null) {
            if (!topLayer2D.getChildren().contains(myUi.hidden) && !enemy.checkArea(300)) {
                topLayer2D.getChildren().remove(myUi.detected);
                topLayer2D.getChildren().add(myUi.hidden);
            }
            if (!topLayer2D.getChildren().contains(myUi.detected) && enemy.checkArea(300)) {
                topLayer2D.getChildren().remove(myUi.hidden);
                topLayer2D.getChildren().add(myUi.detected);
            }
        }
    }

    /**
     * pozrie či hráč je nažive a či má dostatok goldov, ak áno tak skončí
     * hra a objaví sa winning screen
     */
    public void win(){
        if(player.alive && player.gold >= 8){
            won = true;
            killEnemy();
            mediaPlayer.stop();
            musicFile = "sounds/Drink_Up_Theres_More!.mp3";
            sound = new Media(new File(musicFile).toURI().toString());
            mediaPlayer = new MediaPlayer(sound);
            volume = 0.1;
            mediaPlayer.setVolume(volume);
            mediaPlayer.play();
            topLayer2D.getChildren().remove(myUi.levelUp);
            topLayer2D.getChildren().remove(myUi.detected);
            topLayer2D.getChildren().remove(myUi.hidden);
            topLayer2D.getChildren().add(myUi.winningScreen);
        }
    }

    /**
     * kontroluje stav goldov a updatuje ich počet na obrazovke
     */
    public void updateCoins(){
        topLayer2D.getChildren().remove(myUi.coins);
        myUi.coins.setText(""+player.gold);
        topLayer2D.getChildren().add(myUi.coins);
    }

    /**
     * kontroluje stav životov a updatuje ich na obrazovke
     */
    public void updateHealthBar(){
        topLayer2D.getChildren().remove(myUi.healthBar);
        myUi.updateHealthBar(player.health,player.maxHealth);
        topLayer2D.getChildren().add(myUi.healthBar);
    }

    /**
     * kontroluje stav levelov a updatuje ich počet na obrazovke
     */
    public void updateLevel(){
        if(oldPlayerLevel != player.level){
            if(!topLayer2D.getChildren().contains(myUi.levelUp)){
                topLayer2D.getChildren().add(myUi.levelUp);
                levelUpPlayer.play();
                oldPlayerLevel = player.level;
                topLayer2D.getChildren().remove(myUi.level);
                myUi.level.setText(String.format("Lvl: %d",player.level));
                topLayer2D.getChildren().add(myUi.level);
            }
        }else {
            topLayer2D.getChildren().remove(myUi.levelUp);
        }
    }

    /**
     * ak hráč ubral nepriateľovi dostatok životov, nepriateľ zmizne a hráčovi
     * sa pridajú zabitia a goldy
     */
    public void killEnemy(){
        player.recentKills++;
        player.kills++;
        player.gold++;
        if(enemy != null) {
            group3d.getChildren().remove(group3d.getChildren().get(enemy.id));
            enemy.stop();
        }
        this.enemy = null;
    }

    /**
     * ak hráč stratí všetky životy, tak hra končí a objaví sa death screenwww
     */
    public void killPlayer(){
        lost = true;
        topLayer2D.getChildren().remove(player.swordNeutralView);
        topLayer2D.getChildren().remove(player.swordAttackView);
        topLayer2D.getChildren().remove(myUi.hidden);
        topLayer2D.getChildren().remove(myUi.detected);
        if(!topLayer2D.getChildren().contains(myUi.deathScreen)){
            topLayer2D.getChildren().add(myUi.deathScreen);
        }
        killEnemy();
        player.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}