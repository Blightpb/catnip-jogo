# CatNip 🐱 — Corrida das Moedas

Jogo feito em **Java** (lógica) com **HTML, CSS e JavaScript** (tela).
O gato corre por uma estrada de 3 faixas, desvia de obstáculos (carros) e
coleta moedas. Ao bater, a rodada termina, as moedas ficam **salvas** e o
desafio da próxima rodada é coletar **mais moedas que na anterior**.

**Controles:** setas ← → (ou A / D) no computador. Espaço ou Enter começa uma
rodada. No celular aparecem botões de toque.

---

## Jogar online (Render)

O jogo está publicado e funciona em qualquer dispositivo, sem instalar nada:

 **https://catnip-jogo-1.onrender.com/** 

- Abra o link no navegador do computador ou do celular.
- No plano gratuito do Render o serviço "dorme" após ~15 minutos sem uso. No
  primeiro acesso depois disso, **aguarde cerca de 1 minuto** até a página
  carregar.
- Cada pessoa joga a própria partida (o servidor identifica o visitante por um
  cookie), então várias pessoas podem jogar ao mesmo tempo.
- O placar fica guardado no disco do servidor, que é temporário: ele **zera
  quando o serviço reinicia**.

---

## Executar no seu computador

### Requisito

JDK 11 ou superior (testado com Java 21). Para conferir se está instalado:

```
java -version
javac -version
```

### Passo a passo

1. Baixe ou clone o projeto e abra a pasta dele no terminal.
2. Inicie o jogo de um destes jeitos:

   - **Windows:** dê dois cliques em `executar.bat`
   - **Linux/macOS:** `./executar.sh` (se der erro de permissão, rode antes
     `chmod +x executar.sh`)
   - **Manualmente (qualquer sistema):**
     ```
     javac -d out src/jogo/*.java
     java -cp out jogo.ServidorJogo
     ```

3. Aguarde a mensagem `Jogo rodando na porta 8080` e abra no navegador:
   **http://localhost:8080**

Para usar outra porta: `java -cp out jogo.ServidorJogo 9000` e acesse
`http://localhost:9000`. Para encerrar o jogo, volte ao terminal e aperte
`Ctrl + C`.

> Execute sempre a partir da **pasta do projeto**, para o servidor encontrar a
> pasta `web/`. O placar é gravado na pasta `dados/`.

Para jogar pelo celular na mesma rede Wi-Fi, descubra o IP do computador
(`ipconfig` no Windows) e acesse `http://IP-DO-COMPUTADOR:8080` no celular.

---

## Como funciona

O navegador só desenha e envia as teclas. Toda a regra do jogo roda no Java:

| Rota                                 | O que faz                        |
|--------------------------------------|----------------------------------|
| `GET /api/estado`                    | devolve o estado do jogo em JSON |
| `POST /api/iniciar`                  | começa uma nova rodada           |
| `POST /api/mover?dir=esq` (ou `dir`) | move o jogador                   |
| `GET /saude`                         | usado pelo Render para checar o serviço |

O servidor avança todas as partidas a cada 40 ms e o navegador consulta o
estado na mesma frequência. O placar (total de moedas, recorde, rodada
anterior) é gravado em `dados/`, um arquivo por visitante.

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

Partida      → controla a rodada (usa os elementos e o Placar)
Placar       → guarda moedas e recorde (implements Persistivel)
ServidorJogo → servidor HTTP + relógio do jogo + sessão de cada visitante (main)
```
