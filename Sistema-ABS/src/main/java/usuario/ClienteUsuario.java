package usuario;

import controllers.UsuarioController;
import java.io.*;
import java.net.Socket;

/**
 * Cliente de rede do Painel de Controle do Usuário.
 */
public class ClienteUsuario implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String host;
    private int porta;
    private UsuarioController controller;
    private volatile boolean rodando = true;

    public ClienteUsuario(String host, int porta, UsuarioController controller) {
        this.host = host;
        this.porta = porta;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(host, porta);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Identificação inicial para o servidor central
            out.println("USUARIO_CONTROLE");

            while (rodando) {
                String msg = in.readLine();
                if (msg == null) break;

                processarMensagem(msg);
            }
        } catch (IOException e) {
            System.err.println("Erro na conexão do Usuário: " + e.getMessage());
        }
    }

    /**
     * Decompõe a mensagem de atualização vinda do servidor e reflete na interface.
     * Protocolo: UPDATE:RODA:VELOCIDADE:SENSOR_ATIVO:ATUADOR_ATIVO
     */
    private void processarMensagem(String msg) {
        String[] partes = msg.split(":");
        if (partes[0].equals("UPDATE")) {
            String roda = partes[1];
            double vel = Double.parseDouble(partes[2]);
            boolean sAtivo = Boolean.parseBoolean(partes[3]);
            boolean aAtivo = Boolean.parseBoolean(partes[4]);

            controller.atualizarVelocidade(roda, vel);
            controller.atualizarStatusSensor(roda, sAtivo);
            controller.atualizarStatusAtuador(roda, aAtivo);
        }
    }

    /** Envia a escolha de frenagem manual selecionada no ComboBox para o servidor */
    public void enviarComandoFrenagem(String roda, String valor) {
        if (out != null) {
            out.println("COMANDO:" + roda + ":" + valor.replace("%", ""));
        }
    }
}