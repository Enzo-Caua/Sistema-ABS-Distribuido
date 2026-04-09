package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Gerencia a lógica da interface gráfica dos atuadores.
 */
public class AtuadoresController {

    @FXML private Label rde; // Velocidade Dianteira Esquerda
    @FXML private Label rdd; // Velocidade Dianteira Direita
    @FXML private Label rte; // Velocidade Traseira Esquerda
    @FXML private Label rtd; // Velocidade Traseira Direita

    @FXML private Label atRDE; // Status frenagem Dianteira Esquerda
    @FXML private Label atRDD; // Status frenagem Dianteira Direita
    @FXML private Label atRTE; // Status frenagem Traseira Esquerda
    @FXML private Label atRTD; // Status frenagem Traseira Direita

    /**
     * Atualiza os textos de velocidade e porcentagem de atuação.
     * Utiliza Platform.runLater para garantir a execução na thread do JavaFX.
     */
    public void atualizarDados(String roda, double velocidade, int atuacao) {
        Platform.runLater(() -> {
            String velStr = String.format("%.1f km/h", velocidade);
            String atStr = atuacao + "%";

            switch (roda) {
                case "DIANTEIRA_ESQ" -> {
                    rde.setText(velStr);
                    atRDE.setText(atStr);
                }
                case "DIANTEIRA_DIR" -> {
                    rdd.setText(velStr);
                    atRDD.setText(atStr);
                }
                case "TRASEIRA_ESQ" -> {
                    rte.setText(velStr);
                    atRTE.setText(atStr);
                }
                case "TRASEIRA_DIR" -> {
                    rtd.setText(velStr);
                    atRTD.setText(atStr);
                }
            }
        });
    }

    /**
     * Reseta os valores de uma roda para o estado inicial quando desconectada.
     */
    public void limparDados(String roda) {
        Platform.runLater(() -> {
            switch (roda) {
                case "DIANTEIRA_ESQ" -> {
                    rde.setText("--");
                    atRDE.setText("--");
                }
                case "DIANTEIRA_DIR" -> {
                    rdd.setText("--");
                    atRDD.setText("--");
                }
                case "TRASEIRA_ESQ" -> {
                    rte.setText("--");
                    atRTE.setText("--");
                }
                case "TRASEIRA_DIR" -> {
                    rtd.setText("--");
                    atRTD.setText("--");
                }
            }
        });
    }
}