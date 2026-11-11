package jogo;

/**
 * ABSTRACAO + ENCAPSULAMENTO.
 *
 * Representa "qualquer coisa que existe na estrada": tem uma faixa (0, 1 ou 2)
 * e uma posicao vertical y (de 0 = topo ate 100 = base da tela).
 *
 * E abstrata: nao faz sentido criar um "elemento qualquer", so os tipos
 * concretos (Jogador, Pedra, Moeda...). Os atributos sao privados e so podem
 * ser alterados pelos metodos protegidos, entao as subclasses controlam as
 * mudancas de forma segura.
 */
public abstract class ElementoDoJogo implements Colidivel {

    /** Altura (em % da estrada) usada para decidir se dois elementos se tocam. */
    public static final double ALTURA = 9.0;

    private int faixa;
    private double y;

    protected ElementoDoJogo(int faixa, double y) {
        this.faixa = faixa;
        this.y = y;
    }

    public int getFaixa() {
        return faixa;
    }

    public double getY() {
        return y;
    }

    protected void setFaixa(int faixa) {
        this.faixa = faixa;
    }

    protected void setY(double y) {
        this.y = y;
    }

    /** Dois elementos colidem se estao na mesma faixa e muito proximos na vertical. */
    @Override
    public boolean colideCom(ElementoDoJogo outro) {
        return this.faixa == outro.faixa && Math.abs(this.y - outro.y) < ALTURA;
    }

    /** Cada tipo de elemento decide como se atualiza a cada "tick" do jogo. */
    public abstract void atualizar(double fatorDeVelocidade);

    /** Nome usado pelo front-end para escolher o desenho (pedra, moeda...). */
    public abstract String getTipo();
}
