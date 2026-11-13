package jogo;

/** Um carro vem mais rapido que os outros obstaculos: exige reflexo. */
public class CarroRoxo extends Obstaculo {

    public CarroRoxo(int faixa) {
        super(faixa, 1.5);
    }

    @Override
    public String getTipo() {
        return "carro";
    }
}
