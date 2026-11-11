package jogo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Guarda o que fica salvo entre as rodadas: total de moedas acumuladas,
 * recorde, moedas da rodada anterior e numero de partidas.
 *
 * ENCAPSULAMENTO: os numeros sao privados; so mudam pelo metodo
 * registrarRodada(), que garante as regras do placar.
 * Implementa Persistivel para gravar tudo num arquivo .properties.
 */
public class Placar implements Persistivel {

    private final Path arquivo;
    private int totalMoedas;
    private int recorde;
    private int rodadaAnterior;
    private int partidas;

    public Placar(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    /**
     * Registra o fim de uma rodada.
     *
     * @return true se o jogador coletou mais moedas que na rodada anterior
     */
    public synchronized boolean registrarRodada(int moedasDaRodada) {
        boolean superou = partidas > 0 && moedasDaRodada > rodadaAnterior;
        totalMoedas += moedasDaRodada; // as moedas ficam salvas mesmo perdendo
        if (moedasDaRodada > recorde) {
            recorde = moedasDaRodada;
        }
        rodadaAnterior = moedasDaRodada;
        partidas++;
        salvar();
        return superou;
    }

    public synchronized int getTotalMoedas() {
        return totalMoedas;
    }

    public synchronized int getRecorde() {
        return recorde;
    }

    public synchronized int getRodadaAnterior() {
        return rodadaAnterior;
    }

    public synchronized int getPartidas() {
        return partidas;
    }

    @Override
    public synchronized void salvar() {
        Properties dados = new Properties();
        dados.setProperty("totalMoedas", String.valueOf(totalMoedas));
        dados.setProperty("recorde", String.valueOf(recorde));
        dados.setProperty("rodadaAnterior", String.valueOf(rodadaAnterior));
        dados.setProperty("partidas", String.valueOf(partidas));
        try (OutputStream saida = Files.newOutputStream(arquivo)) {
            dados.store(saida, "Placar do jogo");
        } catch (IOException e) {
            System.err.println("Nao foi possivel salvar o placar: " + e.getMessage());
        }
    }

    @Override
    public synchronized void carregar() {
        if (!Files.exists(arquivo)) {
            return;
        }
        Properties dados = new Properties();
        try (InputStream entrada = Files.newInputStream(arquivo)) {
            dados.load(entrada);
            totalMoedas = lerInteiro(dados, "totalMoedas");
            recorde = lerInteiro(dados, "recorde");
            rodadaAnterior = lerInteiro(dados, "rodadaAnterior");
            partidas = lerInteiro(dados, "partidas");
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler o placar: " + e.getMessage());
        }
    }

    private int lerInteiro(Properties dados, String chave) {
        try {
            return Integer.parseInt(dados.getProperty(chave, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
