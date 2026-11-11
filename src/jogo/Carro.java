package jogo;

/** Um carro vem mais rapido que os outros obstaculos: exige reflexo. */
public class Carro extends Obstaculo {

    public Carro(int faixa) {
        super(faixa, 1.5);
    }

    @Override
    public String getTipo() {
        return "carro";
    }
}
