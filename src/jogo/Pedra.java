package jogo;

public class Pedra extends Obstaculo {

    public Pedra(int faixa) {
        super(faixa, 1.0);
    }

    @Override
    public String getTipo() {
        return "pedra";
    }
}
