package telas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sensores.MainSensores;

public class SensoresApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/telas/Sensores.fxml"));
        stage.setScene(new Scene(root));

        Image iconeLogo = new Image(getClass().getResourceAsStream("/imagens/IconeVelocimetro.png"));
        stage.getIcons().add(iconeLogo);
        stage.setResizable(false);
        stage.setTitle("Sistema ABS - Sensores de Velocidades");

        // --- ESTE É O PONTO CHAVE ---
        stage.setOnCloseRequest(event -> {
            System.out.println("Janela fechada. Encerrando todos os sensores...");

            // 1. Chama o encerramento de cada sensor ativo
            MainSensores.clientesGlobais.forEach(cliente -> {
                cliente.encerrar();
            });

            // 2. Finaliza a plataforma JavaFX
            Platform.exit();

            // 3. Força a saída do sistema para garantir que as threads parem
            System.exit(0);
        });

        stage.show();

        MainSensores.guiPronta.countDown();
    }

    public static void main(String[] args) {
        launch();
    }
}