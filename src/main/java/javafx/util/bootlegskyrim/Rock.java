package javafx.util.bootlegskyrim;

import com.interactivemesh.jfx.importer.tds.TdsModelImporter;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;

public class Rock{

    World world;
    Group g;
    TdsModelImporter tdsImporter;
    int posX;
    int posZ;
    int posY;

    /**
     * vytvorí 3d objekt skaly a pridá ho do groupy vo World.java
     * @param world
     * @param posX
     * @param posY
     * @param posZ
     */
    Rock(World world, int posX, int posY,  int posZ){
        this.world = world;
        this.posX = posX;
        this.posZ = posZ;
        this.posY = posY;

        this.tdsImporter = new TdsModelImporter();
        tdsImporter.read("rock/rock.3DS");
        this.g = (Group) tdsImporter.getImport()[0];
        MeshView node = (MeshView) g.getChildren().get(0);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image("file:textures/blocks/cobblestone.png"));
        node.setMaterial(material);

        Scale s = new Scale(0.5,0.5,0.5);
        node.getTransforms().addAll(s);
        node.setTranslateX(posX);
        node.setTranslateY(posY);
        node.setTranslateZ(posZ);
        world.group3d.getChildren().addAll(node);
    }
}
