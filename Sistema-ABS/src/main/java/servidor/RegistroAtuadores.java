package servidor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegistroAtuadores {
    private final Map<String, GestorAtuadores> atuadores = new ConcurrentHashMap<>();

    public void registrar(String nome, GestorAtuadores handler) {
        atuadores.put(nome, handler);
        System.out.println("[REGISTRO] Atuador conectado: " + nome);
    }

    public void remover(String nome) {
        atuadores.remove(nome);
    }

    public void enviarComando(String nomeRoda, String comando) {
        GestorAtuadores handler = atuadores.get(nomeRoda);
        if (handler != null) {
            handler.enviarComando(comando);
        }
    }
}