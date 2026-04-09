package telas;

import controllers.UsuarioController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import servidor.GestorIP;
import usuario.ClienteUsuario;

import java.util.concurrent.CountDownLatch;

public class UsuarioApp extends Application {

    private static UsuarioController controller;

    // CountDownLatch para sincronizar o início da lógica de rede com a interface pronta
    public static final CountDownLatch guiPronta = new CountDownLatch(1);

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/telas/Usuario.fxml"));
        Parent root = loader.load();

        // Guarda a referência do controller para que a classe de comunicação possa atualizar a tela
        controller = loader.getController();

        stage.setScene(new Scene(root));

        // Configuração estética seguindo o padrão do sistema
        try {
            Image iconeLogo = new Image(getClass().getResourceAsStream("/imagens/IconeVelocimetro.png"));
            stage.getIcons().add(iconeLogo);
        } catch (Exception e) {
            System.err.println("Aviso: Ícone não encontrado.");
        }

        stage.setResizable(false);
        stage.setTitle("Sistema ABS - Painel de Controle do Usuário");

        // Garante que o processo seja encerrado totalmente ao fechar a janela
        stage.setOnCloseRequest(event -> {
            System.out.println("Encerrando Painel do Usuário...");
            Platform.exit();
            System.exit(0);
        });

        stage.show();

        // Libera qualquer thread que esteja aguardando a GUI carregar
        guiPronta.countDown();

        // No final do start() de UsuarioApp:
        new Thread(() -> {
            try {
                String ip = GestorIP.descobrirIpServidor();
                ClienteUsuario cliente = new ClienteUsuario(ip, 60000, controller);
                controller.setClienteRede(cliente);
                new Thread(cliente).start();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    /**
     * Permite que as classes de rede obtenham o controller para atualizar
     * as velocidades e os status de conexão em tempo real.
     */
    public static UsuarioController getController() {
        return controller;
    }

    public static void main(String[] args) {
        launch(args);
    }
}