package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Gerencia o painel de status do Servidor Central, exibindo conexões ativas.
 */
public class ServidorController {
    @FXML private Label atRDD, atRDE, atRTD, atRTE; // Atuadores
    @FXML private Label srRDD, srRDE, srRTD, srRTE; // Sensores
    @FXML private Label usuario; // Status do Painel de Controle

    /** Atualiza a cor do indicador do sensor baseado na conexão */
    public void atualizarStatusSensor(String posicao, boolean ativo) {
        mudarEstilo(selecionarLabel(posicao, true), ativo);
    }

    /** Atualiza a cor do indicador do atuador baseado na conexão */
    public void atualizarStatusAtuador(String posicao, boolean ativo) {
        mudarEstilo(selecionarLabel(posicao, false), ativo);
    }

    /** Atualiza a cor do indicador do usuário central */
    public void atualizarStatusUsuario(boolean ativo) {
        mudarEstilo(usuario, ativo);
    }

    /** Altera as classes CSS do Label para mudar sua aparência visual */
    private void mudarEstilo(Label label, boolean ativo) {
        if (label != null) {
            Platform.runLater(() -> {
                label.getStyleClass().removeAll("ativo", "inativo");
                label.getStyleClass().add(ativo ? "ativo" : "inativo");
            });
        }
    }

    /**
     * Mapeia nomes de strings recebidos pela rede para os componentes JavaFX corretos.
     * Trata variações de nomenclatura e remove caracteres especiais.
     */
    private Label selecionarLabel(String nome, boolean isSensor) {
        if (nome == null) return null;
        String n = nome.toUpperCase().replace("_", " ");

        if (isSensor) {
            if (n.contains("DIANTEIRA ESQ") || n.contains("RDE") || n.equals("DIANTEIRA ESQUERDA")) return srRDE;
            if (n.contains("DIANTEIRA DIR") || n.contains("RDD") || n.equals("DIANTEIRA DIREITA")) return srRDD;
            if (n.contains("TRASEIRA ESQ") || n.contains("RTE") || n.equals("TRASEIRA ESQUERDA")) return srRTE;
            if (n.contains("TRASEIRA DIR") || n.contains("RTD") || n.equals("TRASEIRA DIREITA")) return srRTD;
        } else {
            if (n.contains("DIANTEIRA ESQ") || n.contains("RDE") || n.equals("DIANTEIRA ESQUERDA")) return atRDE;
            if (n.contains("DIANTEIRA DIR") || n.contains("RDD") || n.equals("DIANTEIRA DIREITA")) return atRDD;
            if (n.contains("TRASEIRA ESQ") || n.contains("RTE") || n.equals("TRASEIRA ESQUERDA")) return atRTE;
            if (n.contains("TRASEIRA DIR") || n.contains("RTD") || n.equals("TRASEIRA DIREITA")) return atRTD;
        }
        return null;
    }
}