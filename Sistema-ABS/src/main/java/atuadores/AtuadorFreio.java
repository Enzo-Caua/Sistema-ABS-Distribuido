package atuadores;

public class AtuadorFreio implements Runnable {

    private final String roda;
    private volatile boolean ativo = true;

    public AtuadorFreio(String roda) {
        this.roda = roda;
    }

    public void parar() {
        this.ativo = false;
    }

    @Override
    public void run() {
        // No caso do atuador, ele pode ficar em um loop se precisar
        // simular temperatura ou desgaste, por ora apenas mantemos o padrão
        while (ativo) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public double aplicarFreio(double velocidade, int nivelAtuacao) {
        double fatorReducao = 0.0;

        // Agora o fator é a tradução direta da porcentagem para decimal
        switch (nivelAtuacao) {
            case 25:  fatorReducao = 0.25; break; // Reduz 25% da velocidade
            case 50:  fatorReducao = 0.50; break; // Reduz 50% da velocidade
            case 75:  fatorReducao = 0.75; break; // Reduz 75% da velocidade
            case 100: fatorReducao = 1.00; break; // Reduz 100% (Velocidade vai para 0)
            default:  fatorReducao = 0.0;  break;
        }

        double novaVelocidade = velocidade * (1 - fatorReducao);

        // Garante que a velocidade não seja negativa por erro de arredondamento
        return Math.max(0.0, novaVelocidade);
    }

    public String getRoda() {
        return roda;
    }
}