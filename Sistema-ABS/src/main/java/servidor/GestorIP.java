package servidor;

import java.net.*;
import java.util.Scanner;

/**
 * Utilitário para descoberta automática de IP do servidor na rede local usando UDP Broadcast.
 */
public class GestorIP {
    private static final int DISCOVERY_PORT = 65000;
    private static final String REQUEST_MSG = "DISCOVER_ABS_SERVER_REQUEST";
    private static final String RESPONSE_MSG = "DISCOVER_ABS_SERVER_RESPONSE";

    /** Inicia um servidor de escuta para responder a pedidos de descoberta */
    public static void iniciarAnuncioServidor() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                byte[] buffer = new byte[256];
                System.out.println("[DISCOVERY] Ouvindo requisições de descoberta...");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (message.equals(REQUEST_MSG)) {
                        byte[] responseBytes = RESPONSE_MSG.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBytes, responseBytes.length,
                                packet.getAddress(), packet.getPort()
                        );
                        socket.send(responsePacket);
                    }
                }
            } catch (Exception e) {
                System.err.println("[DISCOVERY] Erro no anúncio: " + e.getMessage());
            }
        }).start();
    }

    /** Envia um broadcast na rede e aguarda a resposta de um servidor ativo */
    public static String descobrirIpServidor() {
        System.out.println("[DISCOVERY] Tentando localizar servidor na rede...");
        String ipEncontrado = null;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(3000);

            byte[] sendData = REQUEST_MSG.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT
            );

            socket.send(sendPacket);

            byte[] recvBuf = new byte[256];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            if (response.equals(RESPONSE_MSG)) {
                ipEncontrado = receivePacket.getAddress().getHostAddress();
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[DISCOVERY] Timeout: Nenhuma resposta recebida via Broadcast.");
        } catch (Exception e) {
            System.out.println("[DISCOVERY] Erro na descoberta: " + e.getMessage());
        }

        if (ipEncontrado != null) {
            System.out.println("[DISCOVERY] Servidor localizado em: " + ipEncontrado);
            return ipEncontrado;
        }

        // Caso a descoberta automática falhe, solicita entrada manual do usuário
        System.out.println("\n==================================================");
        System.out.println("   AVISO: SERVIDOR NÃO LOCALIZADO AUTOMATICAMENTE  ");
        System.out.println("==================================================");
        System.out.print("Digite o IP do servidor manualmente (ex: 192.168.x.x) ou aperte Enter para localhost: ");

        Scanner sc = new Scanner(System.in);
        String ipManual = sc.nextLine().trim();

        if (ipManual.isEmpty()) {
            return "localhost";
        }
        return ipManual;
    }
}