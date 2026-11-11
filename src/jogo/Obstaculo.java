package jogo;

/**
 * Classe abstrata para qualquer coisa que faca o jogador perder a rodada.
 * Toda colisao com um obstaculo encerra a partida; as subclasses
 * (Pedra, Buraco, Carro) diferem no tipo e na velocidade.
 */
public abstract class Obstaculo extends ItemDaEstrada {

    protected Obstaculo(int faixa, double velocidadeBase) {
        super(faixa, velocidadeBase);
    }

    @Override
    public void aoColidirComJogador(Partida partida) {
        partida.encerrar();
    }
}
