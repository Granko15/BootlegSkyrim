package javafx.util.bootlegskyrim;

import com.interactivemesh.jfx.importer.tds.TdsModelImporter;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class Tree{

    World world;
    Group g;
    TdsModelImporter tdsImporter;
    int posX;
    int posZ;
    int posY;

    /**
     * vytvorí 3d objekt stromu a pridá ho do groupy vo World.java
     * @param world
     * @param posX
     * @param posY
     * @param posZ
     */
    Tree(World world, int posX, int posY,  int posZ){
        this.world = world;
        this.posX = posX;
        this.posZ = posZ;
        this.posY = posY;

        this.tdsImporter = new TdsModelImporter();
        tdsImporter.read("tree/tree1.3ds");
        this.g = (Group) tdsImporter.getImport()[0];
        MeshView node = (MeshView) g.getChildren().get(0);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image("file:textures/blocks/stripped_dark_oak_log.png"));
        node.setMaterial(material);

        Scale s = new Scale(3,0.5,3);
        node.getTransforms().addAll(s);
        node.setTranslateX(posX);
        node.setTranslateY(posY);
        node.setTranslateZ(posZ);
        node.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));
        world.group3d.getChildren().addAll(node);
    }
}
