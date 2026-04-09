package atuadores;

import controllers.AtuadoresController;
import java.io.*;
import java.net.Socket;

/**
 * Cliente de rede responsável pela comunicação TCP entre o atuador e o servidor central.
 */
public class ClienteAtuadores implements Runnable {

    private final AtuadorFreio atuador;
    private final String host;
    private final int porta;
    private final AtuadoresController controller;
    private volatile boolean rodando = true;
    private Socket socket;

    public ClienteAtuadores(AtuadorFreio atuador, String host, int porta, AtuadoresController controller) {
        this.atuador = atuador;
        this.host = host;
        this.porta = porta;
        this.controller = controller;
    }

    /**
     * Fecha as conexões de rede, limpa a interface gráfica e encerra as threads internas.
     */
    public void encerrar() {
        this.rodando = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) { }
        this.atuador.parar();

        if (controller != null) {
            controller.limparDados(atuador.getRoda());
        }
    }

    @Override
    public void run() {
        // Inicia a thread de simulação do atuador
        new Thread(atuador).start();
        try {
            this.socket = new Socket(host, porta);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // O primeiro envio identifica qual roda este cliente controla
            out.println(atuador.getRoda());

            while (rodando) {
                String mensagemRecebida = in.readLine();
                if (mensagemRecebida == null) break;

                // Comando especial enviado pelo servidor quando um sensor é desconectado
                if (mensagemRecebida.equals("RESET")) {
                    if (controller != null) {
                        controller.limparDados(atuador.getRoda());
                    }
                    continue;
                }

                try {
                    // Protocolo esperado: "velocidade;nivelFrenagem"
                    String[] partes = mensagemRecebida.split(";");
                    double velocidadeAtual = Double.parseDouble(partes[0]);
                    int nivelAtuacao = Integer.parseInt(partes[1]);

                    // Processa a redução de velocidade localmente
                    double novaVelocidade = atuador.aplicarFreio(velocidadeAtual, nivelAtuacao);

                    // Atualiza a visualização no JavaFX
                    if (controller != null) {
                        controller.atualizarDados(atuador.getRoda(), novaVelocidade, nivelAtuacao);
                    }

                    // Reporta a velocidade real resultante de volta para o servidor
                    out.println(novaVelocidade);
                } catch (Exception e) {
                    if (rodando) System.err.println("[" + atuador.getRoda() + "] Erro processando dados.");
                }
            }
        } catch (IOException e) {
            if (rodando) System.err.println("[" + atuador.getRoda() + "] Conexão encerrada.");
        } finally {
            encerrar();
        }
    }

    public AtuadorFreio getAtuador() {
        return atuador;
    }
}