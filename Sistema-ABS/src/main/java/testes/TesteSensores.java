package testes;

import sensores.SensorVelocidade;

public class TesteSensores {

    public static void main(String[] args) {

        // 🚗 Criando sensores (um por roda)
        SensorVelocidade dianteiraEsquerda = new SensorVelocidade("Dianteira Esquerda");
        SensorVelocidade dianteiraDireita = new SensorVelocidade("Dianteira Direita");
        SensorVelocidade traseiraEsquerda = new SensorVelocidade("Traseira Esquerda");
        SensorVelocidade traseiraDireita = new SensorVelocidade("Traseira Direita");

        // 🔄 Criando threads
        Thread t1 = new Thread(dianteiraEsquerda);
        Thread t2 = new Thread(dianteiraDireita);
        Thread t3 = new Thread(traseiraEsquerda);
        Thread t4 = new Thread(traseiraDireita);

        // ▶️ Iniciando sensores
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        // 🧠 Monitorando velocidades (como se fosse a ECU do ABS)
        while (true) {
            System.out.println("\n===== LEITURA DOS SENSORES =====");

            double vDE = dianteiraEsquerda.getVelocidade();
            double vDD = dianteiraDireita.getVelocidade();
            double vTE = traseiraEsquerda.getVelocidade();
            double vTD = traseiraDireita.getVelocidade();

            System.out.printf("DE: %.2f km/h\n", vDE);
            System.out.printf("DD: %.2f km/h\n", vDD);
            System.out.printf("TE: %.2f km/h\n", vTE);
            System.out.printf("TD: %.2f km/h\n", vTD);

            // 📊 Média
            double media = (vDE + vDD + vTE + vTD) / 4.0;
            System.out.printf("MÉDIA: %.2f km/h\n", media);
            dormir(2000);
        }
    }

    // Função auxiliar
    public static void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}