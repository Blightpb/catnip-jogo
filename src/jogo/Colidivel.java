package jogo;

/**
 * INTERFACE: define um contrato. Qualquer coisa que possa colidir com outro
 * elemento do jogo precisa saber responder se colidiu ou nao.
 */
public interface Colidivel {

    boolean colideCom(ElementoDoJogo outro);
}
