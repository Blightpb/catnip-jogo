package jogo;

public class CarroAzul extends Obstaculo {

    public CarroAzul(int faixa) {
        super(faixa, 1.0);
    }

    @Override
    public String getTipo() {
        return "buraco";
    }
}
