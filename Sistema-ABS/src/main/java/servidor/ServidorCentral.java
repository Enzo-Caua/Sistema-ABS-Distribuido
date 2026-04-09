package servidor;

import controllers.ServidorController;
import javafx.application.Application;
import sensores.ServidorUDP;
import telas.ServidorApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Núcleo do sistema. Gerencia conexões TCP, recebe dados UDP e orquestra a lógica do ABS.
 */
public class ServidorCentral {
    public static final CountDownLatch guiPronta = new CountDownLatch(1);
    private static ServidorController controller;

    /** Monitora o tempo do último sinal recebido de cada sensor para detectar quedas */
    private static final Map<String, Long> sensoresAtivos = new ConcurrentHashMap<>();

    private static final List<GestorUsuario> usuariosConectados = new CopyOnWriteArrayList<>();
    private static RegistroAtuadores registroAtuadores = new RegistroAtuadores();
    private static final Map<String, Integer> frenagensManuais = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new Thread(() -> Application.launch(ServidorApp.class, args)).start();
        new Thread(() -> {
            try {
                guiPronta.await();
                controller = ServidorApp.getController();
                iniciarWatchdog();
                executarServicos();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    /**
     * Thread Watchdog: Verifica periodicamente se algum sensor parou de enviar dados.
     * Caso o sensor fique inativo, envia um comando de RESET para o atuador correspondente.
     */
    private static void iniciarWatchdog() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    long agora = System.currentTimeMillis();

                    sensoresAtivos.forEach((posicao, ultimoSinal) -> {
                        if (agora - ultimoSinal > 3000) {
                            controller.atualizarStatusSensor(posicao, false);
                            sensoresAtivos.remove(posicao);

                            // Se o sensor caiu e o usuário não está travando a roda manualmente, reseta o atuador
                            if (getFrenagemManual(posicao) == 0) {
                                registroAtuadores.enviarComando(posicao, "RESET");
                            }

                            broadcastParaUsuarios("UPDATE:" + posicao + ":0.0:false:false");
                        }
                    });
                } catch (Exception e) {}
            }
        }).start();
    }

    /** Inicializa todos os serviços de rede (UDP, TCP e Descoberta de IP) */
    private static void executarServicos() {
        int portaUDP = 55000;
        int portaCentral = 60000;

        LogicaABS logica = new LogicaABS();

        GestorIP.iniciarAnuncioServidor();
        new Thread(new ServidorUDP(portaUDP, portaCentral)).start();

        // Gerenciamento de Conexões TCP (Atuadores e Usuários)
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(portaCentral)) {
                System.out.println("[TCP] Aguardando conexões de Atuadores e Usuários...");
                while (true) {
                    Socket cliente = ss.accept();

                    new Thread(() -> {
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                            String identificacao = in.readLine();

                            if (identificacao != null) {
                                identificacao = identificacao.trim();

                                if (identificacao.equals("USUARIO_CONTROLE")) {
                                    GestorUsuario gu = new GestorUsuario(cliente, in, registroAtuadores);
                                    adicionarUsuario(gu);
                                    if (controller != null) controller.atualizarStatusUsuario(true);
                                    new Thread(gu).start();
                                } else {
                                    // Se não for usuário, identifica como atuador pelo nome da roda
                                    new Thread(new GestorAtuadores(cliente, registroAtuadores, controller, identificacao)).start();
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Erro ao processar conexão TCP.");
                        }
                    }).start();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }).start();

        // Recebimento de dados processados pelo ServidorUDP para monitoramento e lógica ABS
        try (DatagramSocket socketRecebe = new DatagramSocket(60000)) {
            byte[] buffer = new byte[2048];
            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                socketRecebe.receive(pacote);
                String rawData = new String(pacote.getData(), 0, pacote.getLength()).trim();

                for (String leitura : rawData.split(";")) {
                    if (leitura.contains(":")) {
                        String posicaoOriginal = leitura.split(":")[0].trim();
                        String rodaNormalizada = normalizarNomeParaUsuario(posicaoOriginal);

                        sensoresAtivos.put(rodaNormalizada, System.currentTimeMillis());
                        controller.atualizarStatusSensor(rodaNormalizada, true);
                    }
                }
                logica.processar(rawData, registroAtuadores);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void adicionarUsuario(GestorUsuario gu) { usuariosConectados.add(gu); }
    public static void removerUsuario(GestorUsuario gu) { usuariosConectados.remove(gu); }

    /** Envia uma mensagem para todos os painéis de usuário conectados */
    public static void broadcastParaUsuarios(String mensagem) {
        for (GestorUsuario gu : usuariosConectados) {
            gu.enviarAtualizacao(mensagem);
        }
    }

    private static String normalizarNomeParaUsuario(String nome) {
        if (nome.contains("Dianteira Esquerda") || nome.contains("RDE")) return "DIANTEIRA_ESQ";
        if (nome.contains("Dianteira Direita") || nome.contains("RDD"))  return "DIANTEIRA_DIR";
        if (nome.contains("Traseira Esquerda") || nome.contains("RTE"))   return "TRASEIRA_ESQ";
        if (nome.contains("Traseira Direita") || nome.contains("RTD"))    return "TRASEIRA_DIR";
        return nome.toUpperCase().replace(" ", "_");
    }

    public static int getFrenagemManual(String roda) { return frenagensManuais.getOrDefault(roda, 0); }
    public static void setFrenagemManual(String roda, int nivel) { frenagensManuais.put(roda, nivel); }
}