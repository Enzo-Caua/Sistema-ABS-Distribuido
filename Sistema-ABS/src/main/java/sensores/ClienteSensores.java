package sensores;

import java.io.IOException;
import java.net.*;

/**
 * Cliente responsável por enviar dados de velocidade via UDP para o Servidor Central.
 */
public class ClienteSensores implements Runnable {

    private final SensorVelocidade sensor;
    private final String host;
    private final int porta;
    private volatile boolean rodando = true;

    public ClienteSensores(SensorVelocidade sensor, String host, int porta) {
        this.sensor = sensor;
        this.host = host;
        this.porta = porta;
    }

    /** Para a transmissão de rede e a simulação física do sensor */
    public void encerrar() {
        this.rodando = false;
        this.sensor.parar();
    }

    @Override
    public void run() {
        // Inicia a lógica física de variação de velocidade
        new Thread(sensor).start();

        try (DatagramSocket clienteUDP = new DatagramSocket()) {
            InetAddress endereco = InetAddress.getByName(host);

            while (rodando) {
                double velocidade = sensor.getVelocidade();
                velocidade = Math.round(velocidade * 10.0) / 10.0;

                // Mensagem formatada para o Servidor UDP: "Posição: Valor km/h"
                String mensagem = sensor.getPosicao() + ": " + velocidade + " km/h";
                byte[] buffer = mensagem.getBytes();

                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, endereco, porta);
                clienteUDP.send(pacote);

                // Frequência de atualização dos sensores
                Thread.sleep(2000);
            }
        } catch (IOException | InterruptedException e) {
            if (rodando) e.printStackTrace();
        }
        System.out.println("Thread de Rede [" + sensor.getPosicao() + "] finalizada.");
    }

    public SensorVelocidade getSensor() {
        return sensor;
    }
}