package testes;

import java.util.Random;

public class GerarVelocidades {

    public static void main(String[] args) {

        double velocidade = 0;
        double aceleracao = 10; // km/h por segundo
        double velocidadeMax = 80;
        double deltaTime = 1; // 1 segundo

        Random random = new Random();

        // Fase 1: Aceleração até 80 km/h
        while (velocidade < velocidadeMax) {
            velocidade += aceleracao * deltaTime;

            if (velocidade > velocidadeMax) {
                velocidade = velocidadeMax;
            }

            System.out.printf("Acelerando: %.2f km/h\n", velocidade);

            dormir(1000);
        }

        // Fase 2: Variação aleatória entre 75 e 85 km/h
        while (true) {

            // gera variação entre -5 e +5
            double variacao = (random.nextDouble() * 10) - 5;

            double novaVelocidade = velocidadeMax + variacao;

            // garante limites (75 a 85)
            if (novaVelocidade < 75) novaVelocidade = 75;
            if (novaVelocidade > 85) novaVelocidade = 85;

            velocidade = novaVelocidade;

            System.out.printf("Velocidade estabilizada: %.2f km/h\n", velocidade);

            dormir(1000);
        }
    }

    // Função pra simular passagem do tempo
    public static void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}