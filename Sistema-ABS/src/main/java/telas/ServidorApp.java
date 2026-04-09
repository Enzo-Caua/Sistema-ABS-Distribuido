package telas;

import controllers.ServidorController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import servidor.ServidorCentral;

public class ServidorApp extends Application {
    private static ServidorController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/telas/Servidor.fxml"));
        Parent root = loader.load();

        // Guarda a referência do controller para o ServidorCentral usar
        controller = loader.getController();

        stage.setScene(new Scene(root));
        Image iconeLogo = new Image(getClass().getResourceAsStream("/imagens/IconeVelocimetro.png"));
        stage.getIcons().add(iconeLogo);
        stage.setResizable(false);
        stage.setTitle("Sistema ABS - Servidor Central");

        stage.setOnCloseRequest(event -> {
            System.out.println("Encerrando Servidor...");
            Platform.exit();
            System.exit(0);
        });

        stage.show();

        // Avisa ao ServidorCentral que a GUI está pronta
        ServidorCentral.guiPronta.countDown();
    }

    public static ServidorController getController() {
        return controller;
    }
}