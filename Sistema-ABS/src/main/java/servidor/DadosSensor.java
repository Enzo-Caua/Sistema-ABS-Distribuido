package servidor;

public class DadosSensor {
    private final String nomeRoda;
    private final double velocidade;

    public DadosSensor(String nomeRoda, double velocidade) {
        this.nomeRoda = nomeRoda;
        this.velocidade = velocidade;
    }
    // Getters...
    public String getNomeRoda() { return nomeRoda; }
    public double getVelocidade() { return velocidade; }
}