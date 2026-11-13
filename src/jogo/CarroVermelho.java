package jogo;

public class CarroVermelho extends Obstaculo {

    public CarroVermelho(int faixa) {
        super(faixa, 1.0);
    }

    @Override
    public String getTipo() {
        return "pedra";
    }
}
