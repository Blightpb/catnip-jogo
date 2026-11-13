package jogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Controla uma rodada: cria os itens, move tudo, detecta colisoes e decide
 * quando o jogo termina. E a classe que "conversa" com o Placar.
 *
 * Os metodos sao synchronized porque o relogio do jogo (outra thread) e as
 * requisicoes do navegador acessam a mesma partida ao mesmo tempo.
 */
public class Partida {

    public static final int FAIXAS = 3;

    public enum Estado { AGUARDANDO, JOGANDO, FIM }

    private final Placar placar;
    private final Random sorteio = new Random();
    private final List<ItemDaEstrada> itens = new ArrayList<>();

    private Jogador jogador = new Jogador();
    private Estado estado = Estado.AGUARDANDO;
    private int moedasDaRodada;
    private int metaAnterior;
    private long ticks;
    private boolean linhaDeDescanso;
    private boolean superouAnterior;
    private boolean novoRecorde;

    public Partida(Placar placar) {
        this.placar = placar;
    }

    public synchronized void iniciar() {
        itens.clear();
        jogador = new Jogador();
        moedasDaRodada = 0;
        ticks = 0;
        linhaDeDescanso = false;
        superouAnterior = false;
        novoRecorde = false;
        metaAnterior = placar.getRodadaAnterior();
        estado = Estado.JOGANDO;
    }

    public synchronized void moverJogador(String direcao) {
        if (estado != Estado.JOGANDO) {
            return;
        }
        if ("esq".equals(direcao)) {
            jogador.moverParaEsquerda();
        } else if ("dir".equals(direcao)) {
            jogador.moverParaDireita();
        }
    }

    /** Chamado pelo relogio do servidor a cada ~40 ms. */
    public synchronized void atualizar() {
        if (estado != Estado.JOGANDO) {
            return;
        }
        ticks++;

        // O jogo fica mais rapido com o tempo (ate 2,2x).
        double fator = Math.min(2.2, 1.0 + ticks / 1500.0);
        int intervalo = Math.max(14, 28 - (int) (ticks / 200));
        if (ticks % intervalo == 0) {
            gerarLinha();
        }

        Iterator<ItemDaEstrada> it = itens.iterator();
        while (it.hasNext()) {
            ItemDaEstrada item = it.next();
            item.atualizar(fator);
            if (jogador.colideCom(item)) {
                item.aoColidirComJogador(this); // POLIMORFISMO em acao
            }
            if (item.isConsumido() || item.saiuDaTela()) {
                it.remove();
            }
            if (estado == Estado.FIM) {
                break;
            }
        }
    }

    /** Chamado pelos itens quando o jogador encosta neles. */
    public synchronized void somarMoedas(int valor) {
        moedasDaRodada += valor;
    }

    /** Chamado pelos obstaculos: termina a rodada e salva as moedas. */
    public synchronized void encerrar() {
        if (estado != Estado.JOGANDO) {
            return;
        }
        estado = Estado.FIM;
        novoRecorde = moedasDaRodada > placar.getRecorde();
        superouAnterior = placar.registrarRodada(moedasDaRodada);
    }

    private void gerarLinha() {
        List<Integer> faixas = new ArrayList<>();
        for (int i = 0; i < FAIXAS; i++) {
            faixas.add(i);
        }
        Collections.shuffle(faixas, sorteio);

        // Depois de uma linha dificil, vem uma linha so com moeda (da tempo de respirar).
        if (linhaDeDescanso) {
            linhaDeDescanso = false;
            itens.add(new Moeda(faixas.get(0)));
            return;
        }

        boolean dois = ticks > 600 && sorteio.nextInt(100) < 30;
        int quantidade = dois ? 2 : 1;
        for (int i = 0; i < quantidade; i++) {
            itens.add(criarObstaculo(faixas.remove(0)));
        }
        linhaDeDescanso = dois;

        // Sempre sobra pelo menos uma faixa livre; nela pode aparecer uma moeda.
        if (sorteio.nextInt(100) < 70) {
            itens.add(new Moeda(faixas.get(0)));
        }
    }

    private Obstaculo criarObstaculo(int faixa) {
        switch (sorteio.nextInt(3)) {
            case 0:
                return new CarroVermelho(faixa);
            case 1:
                return new CarroAzul(faixa);
            default:
                return new CarroRoxo(faixa);
        }
    }

    /** Monta o estado atual em JSON para o navegador desenhar. */
    public synchronized String paraJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"estado\":\"").append(estado).append("\",");
        sb.append("\"faixa\":").append(jogador.getFaixa()).append(',');
        sb.append("\"jogadorY\":").append(formatar(jogador.getY())).append(',');
        sb.append("\"moedas\":").append(moedasDaRodada).append(',');
        sb.append("\"meta\":").append(metaAnterior).append(',');
        sb.append("\"superou\":").append(superouAnterior).append(',');
        sb.append("\"novoRecorde\":").append(novoRecorde).append(',');
        sb.append("\"totalSalvo\":").append(placar.getTotalMoedas()).append(',');
        sb.append("\"recorde\":").append(placar.getRecorde()).append(',');
        sb.append("\"rodadaAnterior\":").append(placar.getRodadaAnterior()).append(',');
        sb.append("\"partidas\":").append(placar.getPartidas()).append(',');
        sb.append("\"itens\":[");
        for (int i = 0; i < itens.size(); i++) {
            ItemDaEstrada item = itens.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"tipo\":\"").append(item.getTipo()).append("\",");
            sb.append("\"faixa\":").append(item.getFaixa()).append(',');
            sb.append("\"y\":").append(formatar(item.getY())).append('}');
        }
        sb.append("]}");
        return sb.toString();
    }

    private String formatar(double valor) {
        return String.format(Locale.US, "%.1f", valor);
    }
}
