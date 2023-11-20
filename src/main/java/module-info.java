module javafx.util.bootlegskyrim {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;
    requires jim3dsModelImporterJFX;

    opens javafx.util.bootlegskyrim to javafx.fxml;
    exports javafx.util.bootlegskyrim;
}