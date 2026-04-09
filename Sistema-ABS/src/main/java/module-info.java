module pbl.sistemaabs {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens telas to javafx.fxml;

    exports telas;
    exports controllers;
    opens controllers to javafx.fxml;
}