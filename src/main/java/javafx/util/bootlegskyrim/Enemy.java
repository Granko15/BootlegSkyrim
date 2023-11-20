package javafx.util.bootlegskyrim;

import com.interactivemesh.jfx.importer.tds.TdsModelImporter;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import java.io.File;
import java.util.Random;

public class Enemy extends Thread {

    World world;
    Group g;
    boolean alive = true;
    TdsModelImporter tdsImporter = new TdsModelImporter();
    MeshView node;
    PhongMaterial material = new PhongMaterial();
    int posX;
    int posZ;
    int posY;
    int level;
    int hp;
    int attackDamage;
    int oldPlayerHp;
    int id;
    boolean nearPlayer = false;
    String musicFile = "monster/growl.wav";
    Media sound = new Media(new File(musicFile).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(sound);

    double volume = 0.1;


    Enemy(World world, int posX, int posY,  int posZ, int id){
        this.world = world;
        this.posX = posX;
        this.posZ = posZ;
        this.posY = posY;
        this.id = id;
        this.oldPlayerHp = this.world.player.health;
        this.level = this.world.player.level;
        this.hp = level * 2+5;
        this.attackDamage = level*2;
        mediaPlayer.setVolume(volume);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        tdsImporter.read("monster/flesh_creature.3DS");
        this.g = (Group) tdsImporter.getImport()[0];
        node = (MeshView) g.getChildren().get(0);

        material.setDiffuseMap(new Image("file:monster/flesh_diffuse_1024.jpg"));
        node.setMaterial(material);

        node.setTranslateX(posX);
        node.setTranslateY(posY);
        node.setTranslateZ(posZ);
        node.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));
        world.group3d.getChildren().addAll(node);
    }

    /**
     * spustí thread s monštrom a beží pokiaľ je nažive
     */
    @Override
    public void run() {
        while (alive) {
            try {
                if (hp <= 0) alive = false;
                if (checkArea(300)) {
                    this.world.player.inAttackRange = checkArea(50);
                    turnToPlayer();
                    moveTowardsPlayer();
                    mediaPlayer.play();
                    if (nearPlayer) {
                        if (oldPlayerHp >= this.world.player.health) {
                            attack();
                            oldPlayerHp = this.world.player.health;
                        }
                    }
                }else {
                    if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)){
                        mediaPlayer.stop();
                    }
                    randomMovement();
                }
                Thread.sleep(100 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)){
            mediaPlayer.stop();
        }
    }

    /**
     * funkcia mala simulovať náhodný pohyb postavy, pokiaľ hráč nie je v blízkosti,
     * avšak spôsobovala chybovosť aplikácie a preto som ju nakoniec nepoužil
     */
    public void randomMovement(){
        Random random = new Random();
        double dx = Math.sin(Math.toRadians(random.nextInt(1,180)));
        double dz = Math.cos(Math.toRadians(random.nextInt(1,180)));
        this.world.group3d.getChildren().get(id).translateZProperty().set(this.world.group3d.getChildren().get(id).getTranslateZ() + dz);
        this.world.group3d.getChildren().get(id).translateXProperty().set(this.world.group3d.getChildren().get(id).getTranslateX() + dx);

    }

    public double distance(
            double x1,
            double y1,
            double x2,
            double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    /**
     * kontrola či monštrum vidí hráča
     * @param distance
     * @return true ak vidí, false ak nie
     */
    public boolean checkArea(int distance){
        var c = this.world.camera.getBoundsInParent();
        var n = node.getBoundsInParent();
        return distance(c.getCenterX(), c.getCenterZ(), n.getCenterX(), n.getCenterZ()) < distance;
    }

    /**
     * kontrola kolízie s hráčom
     * @return true ak kolízia nastala, false ak nenastala
     */
    public boolean checkCollision(){
        var c = this.world.camera.getBoundsInParent();
        var n = node.getBoundsInParent();
        return c.intersects(n);
    }

    /**
     * hýbe monštrum smerom k hráčovej aktuálnej polohe v priestore
     */
    public void moveTowardsPlayer(){
        double dx = this.world.camera.getTranslateX() - this.world.group3d.getChildren().get(id).getTranslateX();
        double dz = this.world.camera.getTranslateZ() - this.world.group3d.getChildren().get(id).getTranslateZ();
        double oldZ = this.world.group3d.getChildren().get(id).getTranslateZ();
        double oldX = this.world.group3d.getChildren().get(id).getTranslateX();
        double hyp = Math.sqrt(dx*dx + dz*dz);
        dx /= hyp;
        dz /= hyp;

        this.world.group3d.getChildren().get(id).translateZProperty().set(this.world.group3d.getChildren().get(id).getTranslateZ() + dz*5);
        this.world.group3d.getChildren().get(id).translateXProperty().set(this.world.group3d.getChildren().get(id).getTranslateX() + dx*5);
        nearPlayer = false;

        if(checkCollision()){
            this.world.group3d.getChildren().get(id).translateZProperty().set(oldZ);
            this.world.group3d.getChildren().get(id).translateXProperty().set(oldX);
            nearPlayer = true;
        }
    }

    public void attack(){
        this.world.player.health -= attackDamage;
    }

    /**
     * otáča monštrum okolo svojej osi Z
     */
    public void turnToPlayer(){
        this.world.group3d.getChildren().get(id).getTransforms().add(new Rotate(-90, Rotate.Z_AXIS));
    }

}
