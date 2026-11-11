# Corrida das Moedas

Jogo feito em **Java** (lógica) com **HTML, CSS e JavaScript** (tela).
O personagem corre por uma estrada de 3 faixas, desvia de obstáculos
(pedras, buracos e carros) e coleta moedas. Ao bater, a rodada termina, as
moedas ficam **salvas** e o desafio da próxima rodada é coletar **mais moedas
que na anterior**.

## Como executar

Requisito: JDK 11 ou superior (testado com Java 21).

- **Windows:** dê dois cliques em `executar.bat`
- **Linux/macOS:** `./executar.sh`
- **Manualmente:**
  ```
  javac -d out src/jogo/*.java
  java -cp out jogo.ServidorJogo
  ```

Depois abra **http://localhost:8080** no navegador.
(Para usar outra porta: `java -cp out jogo.ServidorJogo 9000`.)

**Controles:** setas ← → (ou A / D). Espaço ou Enter começa uma rodada.
No celular aparecem botões de toque.

> Execute sempre a partir da pasta do projeto, para o servidor achar a pasta
> `web/` e gravar o `placar.properties`.

## Publicar na web (acessar de qualquer dispositivo)

O projeto já vem com `Dockerfile` e `render.yaml`. Cada visitante recebe um
cookie (`sid`) e joga a **própria partida**, então várias pessoas podem jogar
ao mesmo tempo. A porta vem da variável `PORT` (padrão 8080).

Exemplo com o **Render** (plano gratuito):

1. Suba esta pasta para um repositório no GitHub.
2. Em render.com, entre com o GitHub e clique em **New → Blueprint**
   (ou **New → Web Service** e escolha *Docker*).
3. Selecione o repositório e confirme. Em ~3 minutos o Render gera um link
   `https://catnip-jogo.onrender.com` (ou parecido).

Observações:
- No plano gratuito o serviço "dorme" após ~15 min sem uso e leva ~1 minuto
  para acordar no primeiro acesso.
- O disco é temporário: o placar (`dados/`) zera quando o serviço reinicia.
  Para guardar de forma permanente, use um disco persistente (plano pago)
  e aponte `DATA_DIR` para ele.

## Como funciona

O navegador só desenha e envia as teclas. Toda a regra do jogo roda no Java:

| Rota                    | O que faz                          |
|-------------------------|------------------------------------|
| `GET /api/estado`       | devolve o estado do jogo em JSON   |
| `POST /api/iniciar`     | começa uma nova rodada             |
| `POST /api/mover?dir=esq` (ou `dir`) | move o jogador       |

O servidor avança o jogo a cada 40 ms e o navegador consulta o estado na
mesma frequência. O placar (total de moedas, recorde, rodada anterior)
é gravado em `placar.properties`.

## Pilares da POO no código (`src/jogo`)

| Pilar | Onde aparece |
|-------|--------------|
| **Abstração** | `ElementoDoJogo`, `ItemDaEstrada` e `Obstaculo` são classes abstratas: modelam o conceito geral, sem existir "de verdade" no jogo. |
| **Herança** | `Jogador` e `ItemDaEstrada` herdam de `ElementoDoJogo`; `Obstaculo` e `Moeda` herdam de `ItemDaEstrada`; `Pedra`, `Buraco` e `Carro` herdam de `Obstaculo`. |
| **Polimorfismo** | Em `Partida.atualizar()`, o mesmo comando `item.aoColidirComJogador(this)` faz coisas diferentes: a `Moeda` soma pontos, o `Obstaculo` encerra a rodada. Também `atualizar()` e `getTipo()`. |
| **Encapsulamento** | Atributos `private` (`faixa`, `y`, `moedasDaRodada`, `totalMoedas`...) alterados só por métodos controlados, como `Placar.registrarRodada()` e `Jogador.moverParaEsquerda()`. |
| **Interfaces** | `Colidivel` (implementada por `ElementoDoJogo`), `Coletavel` (implementada por `Moeda`) e `Persistivel` (implementada por `Placar`). |

### Hierarquia de classes

```
ElementoDoJogo (abstrata, implements Colidivel)
├── Jogador
└── ItemDaEstrada (abstrata)
    ├── Moeda (implements Coletavel)
    └── Obstaculo (abstrata)
        ├── Pedra
        ├── Buraco
        └── Carro

Partida  → controla a rodada (usa os elementos e o Placar)
Placar   → guarda moedas e recorde (implements Persistivel)
ServidorJogo → servidor HTTP + relógio do jogo (main)
```

## Ideias para evoluir

- Novo obstáculo: crie `class Cone extends Obstaculo` e adicione-o em
  `Partida.criarObstaculo()`.
- Novo item coletável (escudo, ímã de moedas) implementando `Coletavel`.
- Moeda de ouro valendo 5, herdando de `Moeda`.
- Trocar os emojis em `web/game.js` por imagens.