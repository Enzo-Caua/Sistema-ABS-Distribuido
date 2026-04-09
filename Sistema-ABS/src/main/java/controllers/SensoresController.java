package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import sensores.ClienteSensores;
import sensores.MainSensores;
import sensores.SensorVelocidade;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia a lógica visual do painel de sensores de velocidade.
 */
public class SensoresController {

    @FXML private Label rde, rdd, rte, rtd;

    private final Map<String, Label> mapaLabels = new HashMap<>();

    public void initialize() {
        // Mapeia identificações de texto para os componentes da interface
        mapaLabels.put("Dianteira Esquerda", rde);
        mapaLabels.put("Dianteira Direita", rdd);
        mapaLabels.put("Traseira Esquerda", rte);
        mapaLabels.put("Traseira Direita", rtd);

        mapaLabels.values().forEach(l -> {
            if(l != null) l.setText("---");
        });

        iniciarMonitoramentoInterface();
    }

    /**
     * Inicia uma thread daemon para observar as mudanças nos sensores e atualizar a UI.
     */
    private void iniciarMonitoramentoInterface() {
        Thread monitor = new Thread(() -> {
            try {
                while (true) {
                    Platform.runLater(() -> {
                        // Reseta labels para verificar quais sensores ainda estão presentes
                        mapaLabels.values().forEach(l -> l.setText("--"));

                        // Itera sobre a lista global de sensores conectados
                        for (ClienteSensores cliente : MainSensores.clientesGlobais) {
                            SensorVelocidade s = cliente.getSensor();
                            Label labelAlvo = mapaLabels.get(s.getPosicao());

                            if (labelAlvo != null) {
                                if (s.isAtivo()) {
                                    labelAlvo.setText(String.format("%.1f km/h", s.getVelocidade()));
                                } else {
                                    labelAlvo.setText("Parado");
                                }
                            }
                        }
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}