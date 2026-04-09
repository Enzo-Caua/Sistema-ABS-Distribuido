package sensores;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agregador de dados dos sensores. Recebe pacotes individuais e agrupa-os
 * em uma "rodada" antes de enviar para a lógica central do ABS.
 */
public class ServidorUDP implements Runnable {

    private final int portaEscuta;
    private final int portaCentral;
    private final Map<String, Double> leiturasRodada = new ConcurrentHashMap<>();
    private final int TOTAL_SENSORES = 4;

    public ServidorUDP(int portaEscuta, int portaCentral) {
        this.portaEscuta = portaEscuta;
        this.portaCentral = portaCentral;
    }

    @Override
    public void run() {
        try (DatagramSocket socketUDP = new DatagramSocket(portaEscuta)) {
            System.out.println("[UDP] Agregador rodando.");

            byte[] bufferRec = new byte[256];
            InetAddress enderecoLocal = InetAddress.getByName("localhost");

            while (true) {
                DatagramPacket pacoteRecebido = new DatagramPacket(bufferRec, bufferRec.length);
                socketUDP.receive(pacoteRecebido);

                String mensagem = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());

                try {
                    String[] partes = mensagem.split(": ");
                    String idSensor = partes[0];
                    double velocidade = Double.parseDouble(partes[1].split(" ")[0].replace(",", "."));

                    // Se o sensor já enviou um dado, encerramos a rodada atual e iniciamos uma nova
                    if (leiturasRodada.containsKey(idSensor)) {
                        enviarDadosParaCentral(enderecoLocal, socketUDP);
                    }

                    leiturasRodada.put(idSensor, velocidade);

                    // Se coletamos dados das 4 rodas, envia o bloco completo para a central
                    if (leiturasRodada.size() == TOTAL_SENSORES) {
                        enviarDadosParaCentral(enderecoLocal, socketUDP);
                    }

                } catch (Exception e) {
                    System.err.println("[UDP] Erro ao processar: " + mensagem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compacta todas as leituras acumuladas em uma única string e envia via UDP para a Central.
     */
    private void enviarDadosParaCentral(InetAddress endereco, DatagramSocket socket) throws IOException {
        if (leiturasRodada.isEmpty()) return;

        // Formato da mensagem: RODA1:VEL;RODA2:VEL;...
        StringBuilder sb = new StringBuilder();
        leiturasRodada.forEach((id, vel) -> {
            sb.append(id).append(":").append(vel).append(";");
        });

        byte[] bufferEnvio = sb.toString().getBytes();
        DatagramPacket pacoteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, endereco, portaCentral);
        socket.send(pacoteEnvio);

        System.out.println("[UDP] Dados brutos da rodada enviados para a Central.");
        leiturasRodada.clear();
    }
}