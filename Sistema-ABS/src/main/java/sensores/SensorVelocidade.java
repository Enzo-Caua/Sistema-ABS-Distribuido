package sensores;

import java.util.Random;

/**
 * Simula a física de leitura de velocidade de uma roda.
 * Acelera gradualmente e depois mantém uma variação randômica.
 */
public class SensorVelocidade implements Runnable {

    private final String posicao;
    private volatile double velocidade;
    private volatile boolean ativo = true;

    public SensorVelocidade(String posicao) {
        this.posicao = posicao;
        this.velocidade = 0;
    }

    public void parar() {
        this.ativo = false;
    }

    @Override
    public void run() {
        double aceleracao = 10;
        double velocidadeMax = 80;
        double deltaTime = 1;
        Random random = new Random();

        // Fase 1: Simula o veículo ganhando velocidade até 80km/h
        while (ativo && velocidade < velocidadeMax) {
            velocidade += aceleracao * deltaTime;
            if (velocidade > velocidadeMax) velocidade = velocidadeMax;
            dormir(2000);
        }

        // Fase 2: Simula o veículo em movimento constante com pequenas variações
        while (ativo) {
            double variacao = (random.nextDouble() * 10) - 5;
            double novaVelocidade = velocidadeMax + variacao;
            if (novaVelocidade < 75) novaVelocidade = 75;
            if (novaVelocidade > 85) novaVelocidade = 85;
            velocidade = novaVelocidade;
            dormir(2000);
        }

        System.out.println("Logica do Sensor [" + posicao + "] encerrada.");
    }

    private void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String getPosicao() { return posicao; }
    public double getVelocidade() { return velocidade; }
    public boolean isAtivo() { return ativo; }
}