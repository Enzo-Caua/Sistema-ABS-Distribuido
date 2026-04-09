package servidor;

import controllers.ServidorController;
import java.io.*;
import java.net.Socket;

/**
 * Representa o manipulador de conexão no servidor para um cliente Atuador específico.
 */
public class GestorAtuadores implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private String nomeRoda;
    private RegistroAtuadores registro;
    private ServidorController controller;

    public GestorAtuadores(Socket s, RegistroAtuadores registro, ServidorController controller, String nomeRodaPreDefinido) {
        this.socket = s;
        this.registro = registro;
        this.controller = controller;
        this.nomeRoda = nomeRodaPreDefinido;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Tenta obter o nome da roda caso não tenha sido passado via construtor
            if (this.nomeRoda == null || this.nomeRoda.isEmpty()) {
                String linhaNome = in.readLine();
                if (linhaNome != null) this.nomeRoda = linhaNome.trim();
            }

            if (this.nomeRoda != null) {
                registro.registrar(nomeRoda, this);
                if (controller != null) controller.atualizarStatusAtuador(nomeRoda, true);

                String velocidadeCalculada;
                // Escuta o retorno do atuador (a velocidade real após aplicação do freio)
                while ((velocidadeCalculada = in.readLine()) != null) {
                    try {
                        double vel = Double.parseDouble(velocidadeCalculada);

                        // Notifica todos os painéis de usuário sobre a velocidade real atualizada
                        ServidorCentral.broadcastParaUsuarios("UPDATE:" + nomeRoda + ":" + vel + ":true:true");
                    } catch (NumberFormatException e) {
                        // Ignora formatos de mensagem inválidos
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ATUADOR] Conexão encerrada: " + nomeRoda);
        } finally {
            if (nomeRoda != null) {
                registro.remover(nomeRoda);
                if (controller != null) {
                    controller.atualizarStatusAtuador(nomeRoda, false);
                }
            }
            fecharSocket();
        }
    }

    private void fecharSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Envia um comando de frenagem (velocidade;nível) para o cliente físico */
    public void enviarComando(String msg) {
        if (out != null) out.println(msg);
    }
}