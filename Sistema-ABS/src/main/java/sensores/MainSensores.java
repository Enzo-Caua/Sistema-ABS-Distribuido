package sensores;

import servidor.GestorIP;
import javafx.application.Application;
import telas.SensoresApp;

import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.List;

/**
 * Ponto de entrada para o sistema de Sensores.
 * Configura as rodas ativas e lança a monitoração visual.
 */
public class MainSensores {

    public static List<ClienteSensores> clientesGlobais = new CopyOnWriteArrayList<>();

    /** Sincroniza a exibição do menu terminal com o carregamento da interface JavaFX */
    public static final CountDownLatch guiPronta = new CountDownLatch(1);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String ipServidor = GestorIP.descobrirIpServidor();
        int portaServidor = 55000;

        System.out.println("\n=== Configuração Inicial ===");
        System.out.println("1 - Sensor individual | 2 - Dianteiros | 3 - Traseiros | 4 - Todos");
        int opcao = scanner.nextInt();

        configurarSensores(opcao, ipServidor, portaServidor, scanner);

        // Thread para gerenciar o menu do terminal sem bloquear a UI principal
        Thread menuThread = new Thread(() -> {
            try {
                guiPronta.await();
                Thread.sleep(1000);
                exibirMenuControle(scanner);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        menuThread.setDaemon(true);
        menuThread.start();

        System.out.println("Iniciando JavaFX... Aguarde a janela abrir.");
        Application.launch(SensoresApp.class, args);
    }

    /** Permite desligar sensores manualmente via linha de comando */
    private static void exibirMenuControle(Scanner scanner) {
        while (true) {
            System.out.println("\n--- MENU DE CONTROLE (TERMINAL) ---");
            if (clientesGlobais.isEmpty()) {
                System.out.println("Nenhum sensor ativo.");
                break;
            }

            for (int i = 0; i < clientesGlobais.size(); i++) {
                System.out.println((i + 1) + " - Parar " + clientesGlobais.get(i).getSensor().getPosicao());
            }
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");

            int comando = scanner.nextInt();

            if (comando > 0 && comando <= clientesGlobais.size()) {
                ClienteSensores alvo = clientesGlobais.get(comando - 1);
                System.out.println("Encerrando " + alvo.getSensor().getPosicao() + "...");
                alvo.encerrar();
                clientesGlobais.remove(alvo);
            } else if (comando == 0) {
                System.out.println("Menu de terminal encerrado.");
                break;
            }
        }
    }

    private static void configurarSensores(int opcao, String ip, int porta, Scanner sc) {
        if (opcao == 1) {
            System.out.println("1-DE, 2-DD, 3-TE, 4-TD");
            int pos = sc.nextInt();
            adicionarSensor(converterPos(pos), ip, porta);
        } else if (opcao == 2) {
            adicionarSensor("Dianteira Esquerda", ip, porta);
            adicionarSensor("Dianteira Direita", ip, porta);
        } else if (opcao == 3) {
            adicionarSensor("Traseira Esquerda", ip, porta);
            adicionarSensor("Traseira Direita", ip, porta);
        } else {
            adicionarSensor("Dianteira Esquerda", ip, porta);
            adicionarSensor("Dianteira Direita", ip, porta);
            adicionarSensor("Traseira Esquerda", ip, porta);
            adicionarSensor("Traseira Direita", ip, porta);
        }
    }

    private static void adicionarSensor(String pos, String ip, int porta) {
        SensorVelocidade s = new SensorVelocidade(pos);
        ClienteSensores c = new ClienteSensores(s, ip, porta);
        clientesGlobais.add(c);
        new Thread(c).start();
    }

    private static String converterPos(int p) {
        return switch (p) {
            case 2 -> "Dianteira Direita";
            case 3 -> "Traseira Esquerda";
            case 4 -> "Traseira Direita";
            default -> "Dianteira Esquerda";
        };
    }
}