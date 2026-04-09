package telas;

import controllers.AtuadoresController;
import atuadores.MainAtuadores;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AtuadoresApp extends Application {

    // Ponto de acesso para o Main conseguir o controller
    public static AtuadoresController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/telas/Atuadores.fxml"));
        Parent root = loader.load();

        // Guarda a referência do controller criado pelo FXML
        controller = loader.getController();

        stage.setScene(new Scene(root));
        Image iconeLogo = new Image(getClass().getResourceAsStream("/imagens/IconeVelocimetro.png"));
        stage.getIcons().add(iconeLogo);
        stage.setResizable(false);
        stage.setTitle("Sistema ABS - Painel de Atuadores");

        stage.setOnCloseRequest(event -> {
            MainAtuadores.clientesGlobais.forEach(c -> c.encerrar());
            Platform.exit();
            System.exit(0);
        });

        stage.show();
        MainAtuadores.guiPronta.countDown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}