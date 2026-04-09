package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Gerencia a comunicação entre o servidor central e o painel de controle do usuário.
 */
public class GestorUsuario implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    private PrintWriter out;
    private final RegistroAtuadores registroAtuadores;
    private volatile boolean rodando = true;

    public GestorUsuario(Socket socket, BufferedReader in, RegistroAtuadores registroAtuadores) {
        this.socket = socket;
        this.in = in;
        this.registroAtuadores = registroAtuadores;
    }

    @Override
    public void run() {
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("[USUARIO] Painel de controle do usuário conectado com sucesso.");

            String linha;
            while (rodando && (linha = in.readLine()) != null) {
                // Processa comandos enviados pelo painel de controle (ex: "COMANDO:RODA:NIVEL")
                if (linha.startsWith("COMANDO:")) {
                    String[] partes = linha.split(":");
                    if (partes.length >= 3) {
                        String roda = partes[1];
                        int nivelFrenagem = Integer.parseInt(partes[2]);

                        // Salva a intenção do usuário no ServidorCentral para ser processada na próxima rodada do ABS
                        ServidorCentral.setFrenagemManual(roda, nivelFrenagem);

                        System.out.println("[USUARIO] Configuração persistida para " + roda + ": " + nivelFrenagem + "%");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[USUARIO] Conexão com o usuário perdida.");
        } finally {
            fechar();
        }
    }

    /** Envia pacotes de atualização de estado (velocidade e conexão) para o painel do usuário */
    public void enviarAtualizacao(String mensagem) {
        if (out != null) {
            out.println(mensagem);
        }
    }

    /** Finaliza a conexão e atualiza o status visual no servidor */
    public void fechar() {
        this.rodando = false;
        ServidorCentral.removerUsuario(this);

        if (telas.ServidorApp.getController() != null) {
            telas.ServidorApp.getController().atualizarStatusUsuario(false);
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}