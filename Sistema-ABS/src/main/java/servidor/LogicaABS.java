package servidor;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementa o algoritmo de controle do Sistema Anti-Bloqueio (ABS).
 */
public class LogicaABS {

    /**
     * Processa os dados brutos recebidos, calcula o deslizamento e decide o nível de frenagem.
     * @param rawData String contendo as velocidades das rodas.
     * @param registro Mapa de atuadores conectados.
     */
    public void processar(String rawData, RegistroAtuadores registro) {
        List<DadosSensor> leituras = parseDados(rawData);

        // Define a velocidade de referência (normalmente a maior velocidade entre as rodas)
        double vRef = leituras.stream().mapToDouble(DadosSensor::getVelocidade).max().orElse(0);

        for (DadosSensor s : leituras) {
            String nomeRoda = s.getNomeRoda();
            int nivelManual = ServidorCentral.getFrenagemManual(nomeRoda);
            int nivelFinal;

            // Hierarquia de decisão: Se o usuário definiu um valor manual (> 0), ele tem prioridade absoluta.
            if (nivelManual > 0) {
                nivelFinal = nivelManual;
            } else {
                // Se estiver em 0% (automático), o ABS calcula a frenagem baseada na diferença para a referência
                double diff = vRef - s.getVelocidade();
                nivelFinal = calcularNivelAtuacao(diff);
            }

            // Envia a instrução final para o cliente Atuador correspondente
            registro.enviarComando(nomeRoda, s.getVelocidade() + ";" + nivelFinal);
        }
    }

    /**
     * Define a intensidade da frenagem baseada na disparidade de velocidade (Slip).
     */
    private int calcularNivelAtuacao(double diff) {
        if (diff > 12) return 100;
        if (diff > 8)  return 75;
        if (diff > 5)  return 50;
        if (diff > 2)  return 25;
        return 0;
    }

    /** Converte a mensagem concatenada em uma lista de objetos estruturados */
    private List<DadosSensor> parseDados(String data) {
        List<DadosSensor> lista = new ArrayList<>();
        for (String s : data.split(";")) {
            if (s.isEmpty()) continue;
            String[] par = s.split(":");
            lista.add(new DadosSensor(normalizarNome(par[0]), Double.parseDouble(par[1])));
        }
        return lista;
    }

    /** Garante que diferentes formatos de nomes de rodas sejam convertidos para um padrão único */
    private String normalizarNome(String nome) {
        String n = nome.toUpperCase().trim();
        if (n.contains("DIANTEIRA ESQUERDA") || n.contains("DIANTEIRA_ESQ") || n.contains("RDE")) return "DIANTEIRA_ESQ";
        if (n.contains("DIANTEIRA DIREITA")  || n.contains("DIANTEIRA_DIR") || n.contains("RDD")) return "DIANTEIRA_DIR";
        if (n.contains("TRASEIRA ESQUERDA")   || n.contains("TRASEIRA_ESQ")  || n.contains("RTE")) return "TRASEIRA_ESQ";
        if (n.contains("TRASEIRA DIREITA")    || n.contains("TRASEIRA_DIR")  || n.contains("RTD")) return "TRASEIRA_DIR";
        return n.replace(" ", "_");
    }
}