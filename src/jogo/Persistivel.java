package jogo;

/**
 * INTERFACE: contrato para quem precisa guardar dados e recupera-los depois
 * (o Placar usa isso para salvar as moedas em arquivo).
 */
public interface Persistivel {

    void salvar();

    void carregar();
}
