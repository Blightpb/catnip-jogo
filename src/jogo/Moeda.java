package jogo;

/**
 * A Moeda herda de ItemDaEstrada (desce a tela) e implementa a interface
 * Coletavel (tem um valor). Ao tocar o jogador, soma pontos e some da tela.
 */
public class Moeda extends ItemDaEstrada implements Coletavel {

    private static final int VALOR = 1;

    public Moeda(int faixa) {
        super(faixa, 1.0);
    }

    @Override
    public int getValor() {
        return VALOR;
    }

    @Override
    public void aoColidirComJogador(Partida partida) {
        partida.somarMoedas(getValor());
        consumir();
    }

    @Override
    public String getTipo() {
        return "moeda";
    }
}
