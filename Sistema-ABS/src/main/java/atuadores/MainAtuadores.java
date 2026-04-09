package atuadores;

import javafx.application.Application;
import servidor.GestorIP;
import telas.AtuadoresApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Ponto de entrada para o sistema de atuadores.
 * Gerencia a configuração inicial via terminal e a inicialização da GUI.
 */
public class MainAtuadores {

    /** Lista thread-safe de clientes ativos para manipulação segura via terminal e rede */
    public static List<ClienteAtuadores> clientesGlobais = new CopyOnWriteArrayList<>();

    /** Sincronizador que impede a conexão de rede antes da Interface Gráfica estar pronta */
    public static final CountDownLatch guiPronta = new CountDownLatch(1);

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Tenta localizar o servidor automaticamente via UDP Broadcast
        String ipServidor = GestorIP.descobrirIpServidor();
        int portaServidor = 60000;

        System.out.println("=== Configuração Inicial dos Atuadores ===");
        System.out.println("1 - Atuador individual\n2 - Dianteiros\n3 - Traseiros\n4 - Todos");
        System.out.print("Escolha: ");
        int selecao = sc.nextInt();

        List<String> rodasParaConectar = configurarListaRodas(selecao, sc);

        System.out.println("\nIniciando interface gráfica... Aguarde.");

        // Lança o JavaFX em uma thread separada para não bloquear o terminal
        new Thread(() -> {
            Application.launch(AtuadoresApp.class, args);
        }).start();

        try {
            // Aguarda o sinal (countDown) vindo do método start() da aplicação JavaFX
            guiPronta.await();
            Thread.sleep(1000);

            System.out.println("\n[SISTEMA] Interface carregada com sucesso.");

            // Após a UI estar pronta, inicia as conexões de rede
            System.out.println("[REDE] Conectando atuadores ao servidor " + ipServidor + "...");
            for (String nomeRoda : rodasParaConectar) {
                AtuadorFreio atuador = new AtuadorFreio(nomeRoda);
                ClienteAtuadores cliente = new ClienteAtuadores(atuador, ipServidor, portaServidor, AtuadoresApp.controller);
                clientesGlobais.add(cliente);
                new Thread(cliente).start();
            }

            exibirMenuControle(sc);

        } catch (InterruptedException e) {
            System.err.println("Erro na inicialização: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Menu interativo no terminal para desligar atuadores específicos durante a execução.
     */
    private static void exibirMenuControle(Scanner scanner) {
        while (true) {
            System.out.println("\n--- MENU DE CONTROLE (TERMINAL) ---");
            if (clientesGlobais.isEmpty()) {
                System.out.println("Nenhum atuador ativo no momento.");
                break;
            }

            for (int i = 0; i < clientesGlobais.size(); i++) {
                System.out.println((i + 1) + " - Parar Atuador: " + clientesGlobais.get(i).getAtuador().getRoda());
            }
            System.out.println("0 - Sair do programa");
            System.out.print("Escolha: ");

            int comando = scanner.nextInt();

            if (comando > 0 && comando <= clientesGlobais.size()) {
                ClienteAtuadores alvo = clientesGlobais.get(comando - 1);
                System.out.println("Encerrando conexão de " + alvo.getAtuador().getRoda() + "...");
                alvo.encerrar();
                clientesGlobais.remove(alvo);
            } else if (comando == 0) {
                System.out.println("Encerrando aplicação completa...");
                clientesGlobais.forEach(ClienteAtuadores::encerrar);
                System.exit(0);
                break;
            }
        }
    }

    /**
     * Mapeia a escolha numérica do usuário para os nomes literais das rodas.
     */
    private static List<String> configurarListaRodas(int selecao, Scanner sc) {
        List<String> lista = new ArrayList<>();
        switch (selecao) {
            case 1 -> {
                System.out.println("Selecione a roda: 1-DIANTEIRA_ESQ, 2-DIANTEIRA_DIR, 3-TRASEIRA_ESQ, 4-TRASEIRA_DIR");
                int e = sc.nextInt();
                lista.add(switch (e) {
                    case 2 -> "DIANTEIRA_DIR";
                    case 3 -> "TRASEIRA_ESQ";
                    case 4 -> "TRASEIRA_DIR";
                    default -> "DIANTEIRA_ESQ";
                });
            }
            case 2 -> {
                lista.add("DIANTEIRA_ESQ");
                lista.add("DIANTEIRA_DIR");
            }
            case 3 -> {
                lista.add("TRASEIRA_ESQ");
                lista.add("TRASEIRA_DIR");
            }
            default -> {
                lista.add("DIANTEIRA_ESQ");
                lista.add("DIANTEIRA_DIR");
                lista.add("TRASEIRA_ESQ");
                lista.add("TRASEIRA_DIR");
            }
        }
        return lista;
    }
}