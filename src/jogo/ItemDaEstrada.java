package jogo;

/**
 * ABSTRACAO: tudo que nasce no topo e desce pela estrada (obstaculos e moedas).
 *
 * Aqui fica o comportamento comum (descer, sair da tela, ser "consumido").
 * O que acontece quando o jogador encosta no item e diferente para cada
 * tipo, por isso aoColidirComJogador() e abstrato.
 */
public abstract class ItemDaEstrada extends ElementoDoJogo {

    private final double velocidadeBase;
    private boolean consumido = false;

    protected ItemDaEstrada(int faixa, double velocidadeBase) {
        super(faixa, -10.0); // nasce um pouco acima da tela
        this.velocidadeBase = velocidadeBase;
    }

    @Override
    public void atualizar(double fatorDeVelocidade) {
        setY(getY() + velocidadeBase * fatorDeVelocidade);
    }

    public boolean saiuDaTela() {
        return getY() > 115.0;
    }

    public boolean isConsumido() {
        return consumido;
    }

    protected void consumir() {
        this.consumido = true;
    }

    /** POLIMORFISMO: cada subclasse reage de um jeito ao tocar o jogador. */
    public abstract void aoColidirComJogador(Partida partida);
}
