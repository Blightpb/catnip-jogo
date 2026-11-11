package jogo;

public class Buraco extends Obstaculo {

    public Buraco(int faixa) {
        super(faixa, 1.0);
    }

    @Override
    public String getTipo() {
        return "buraco";
    }
}
