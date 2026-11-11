package jogo;

/**
 * HERANCA: o Jogador e um ElementoDoJogo (ja ganha faixa, y e colideCom).
 * Ele fica parado na parte de baixo da tela e so muda de faixa.
 */
public class Jogador extends ElementoDoJogo {

    public static final double POSICAO_Y = 85.0;

    public Jogador() {
        super(Partida.FAIXAS / 2, POSICAO_Y); // comeca na faixa do meio
    }

    public void moverParaEsquerda() {
        if (getFaixa() > 0) {
            setFaixa(getFaixa() - 1);
        }
    }

    public void moverParaDireita() {
        if (getFaixa() < Partida.FAIXAS - 1) {
            setFaixa(getFaixa() + 1);
        }
    }

    /** O jogador nao desce a tela: quem se move e a estrada. */
    @Override
    public void atualizar(double fatorDeVelocidade) {
        // intencionalmente vazio
    }

    @Override
    public String getTipo() {
        return "jogador";
    }
}
