package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import usuario.ClienteUsuario;

/**
 * Controla o Painel do Usuário, permitindo frenagem manual e monitoramento das rodas.
 */
public class UsuarioController {

    @FXML private ComboBox<String> FrRDD, FrRDE, FrRTD, FrRTE; // Controles de Freio
    @FXML private Label atRDD, atRDE, atRTD, atRTE; // Indicadores de Atuador
    @FXML private Label rdd, rde, rtd, rte;        // Velocímetros
    @FXML private Label srRDD, srRDE, srRTD, srRTE; // Indicadores de Sensor

    private ClienteUsuario clienteRede;

    public void initialize() {
        var opcoes = FXCollections.observableArrayList("0%", "25%", "50%", "75%", "100%");

        setupCombo(FrRDD, "DIANTEIRA_DIR", opcoes);
        setupCombo(FrRDE, "DIANTEIRA_ESQ", opcoes);
        setupCombo(FrRTD, "TRASEIRA_DIR", opcoes);
        setupCombo(FrRTE, "TRASEIRA_ESQ", opcoes);
    }

    public void setClienteRede(ClienteUsuario cliente) {
        this.clienteRede = cliente;
    }

    /** Configura o comportamento de cada ComboBox de freio */
    private void setupCombo(ComboBox<String> combo, String roda, javafx.collections.ObservableList<String> itens) {
        combo.setItems(itens);
        combo.setValue("0%");
        combo.setOnAction(e -> {
            if (clienteRede != null) {
                clienteRede.enviarComandoFrenagem(roda, combo.getValue());
            }
        });
    }

    /** Atualiza o texto do velocímetro de uma roda específica */
    public void atualizarVelocidade(String posicao, double velocidade) {
        Platform.runLater(() -> {
            Label lb = selecionarLabel(posicao, "VELOCIDADE");
            if (lb != null) {
                lb.setText(String.format("%.1f km/h", velocidade));
            }
        });
    }

    /** Altera visualmente o status do sensor (Verde para conectado, Cinza para desconectado) */
    public void atualizarStatusSensor(String posicao, boolean ativo) {
        mudarEstilo(selecionarLabel(posicao, "SENSOR"), ativo);
    }

    /** Altera visualmente o status do atuador */
    public void atualizarStatusAtuador(String posicao, boolean ativo) {
        mudarEstilo(selecionarLabel(posicao, "ATUADOR"), ativo);
    }

    private void mudarEstilo(Label label, boolean ativo) {
        if (label != null) {
            Platform.runLater(() -> {
                label.getStyleClass().removeAll("ativo", "inativo");
                label.getStyleClass().add(ativo ? "ativo" : "inativo");
            });
        }
    }

    /** Traduz strings de identificação para os componentes FXML da interface de usuário */
    private Label selecionarLabel(String nome, String tipo) {
        if (nome == null) return null;
        String n = nome.toUpperCase();

        boolean esqDian = n.contains("DIANTEIRA_ESQ") || n.contains("RDE");
        boolean dirDian = n.contains("DIANTEIRA_DIR") || n.contains("RDD");
        boolean esqTras = n.contains("TRASEIRA_ESQ")  || n.contains("RTE");
        boolean dirTras = n.contains("TRASEIRA_DIR")  || n.contains("RTD");

        return switch (tipo) {
            case "VELOCIDADE" -> {
                if (esqDian) yield rde;
                if (dirDian) yield rdd;
                if (esqTras) yield rte;
                if (dirTras) yield rtd;
                yield null;
            }
            case "SENSOR" -> {
                if (esqDian) yield srRDE;
                if (dirDian) yield srRDD;
                if (esqTras) yield srRTE;
                if (dirTras) yield srRTD;
                yield null;
            }
            case "ATUADOR" -> {
                if (esqDian) yield atRDE;
                if (dirDian) yield atRDD;
                if (esqTras) yield atRTE;
                if (dirTras) yield atRTD;
                yield null;
            }
            default -> null;
        };
    }
}