// Front-end: so desenha o que o Java manda e envia as teclas para ele.
// Toda a logica do jogo (colisao, pontos, recorde) fica no backend Java.

const FAIXAS = 3;
const INTERVALO_MS = 40;

// Caminhos das imagens dos elementos do jogo
const IMAGENS = {
    pedra: "/gif/carro-vermelho.gif",
    buraco: "/gif/carro-azul.gif",
    carro: "/gif/carro-roxo.gif",
    moeda: "/gif/moeda.gif",
};

// Imagens do gato e duração da animação de game over.
// DURACAO_GAME_OVER_MS deve ser igual à duração de "gato-game-over" no style.css.
const IMAGEM_GATO = "/gif/gato.gif";
const IMAGEM_GATO_MORTO = "/gif/gato-morto.gif";
const DURACAO_GAME_OVER_MS = 1700;

// Carrega a imagem do gato derrotado antes da batida, para não piscar na troca
new Image().src = IMAGEM_GATO_MORTO;

const el = {
    estrada: document.getElementById("estrada"),
    itens: document.getElementById("itens"),
    jogador: document.getElementById("jogador"),
    jogadorImg: document.querySelector("#jogador img"),
    painel: document.getElementById("painel"),
    titulo: document.getElementById("titulo"),
    mensagem: document.getElementById("mensagem"),
    botao: document.getElementById("botao"),
    moedas: document.getElementById("moedas"),
    meta: document.getElementById("meta"),
    recorde: document.getElementById("recorde"),
    total: document.getElementById("total"),
};

let estadoAnterior = "AGUARDANDO";
let animandoFim = false; // true enquanto o gato faz a animação de game over
let fimMostrado = false; // true depois que a tela "Jogar de novo" apareceu
let timerFim = null;

// Comunicação com o backend Java
async function chamar(caminho, metodo = "GET") {
    try {
        const resposta = await fetch(caminho, { method: metodo });
        return await resposta.json();
    } catch (erro) {
        return null; // Servidor fora do ar: tenta novamente no próximo ciclo
    }
}

// Desenha os elementos recebidos do backend
function desenhar(estado) {
    el.moedas.textContent = estado.moedas;
    el.meta.textContent = estado.meta;
    el.recorde.textContent = estado.recorde;
    el.total.textContent = estado.totalSalvo;

    // Posicionamento do jogador
    el.jogador.style.left = (estado.faixa * 100) / FAIXAS + "%";

    // Desenha os obstáculos e as moedas utilizando imagens
    el.itens.innerHTML = estado.itens
        .map((item) => {
            const imagem = IMAGENS[item.tipo];

            return `
                <div class="item ${item.tipo}"
                     style="left:${(item.faixa * 100) / FAIXAS}%;top:${item.y}%">

                    <img
                        src="${imagem || ""}"
                        alt="${item.tipo}"
                        draggable="false"
                    >

                </div>
            `;
        })
        .join("");

    // Animação da estrada
    el.estrada.classList.toggle(
        "correndo",
        estado.estado === "JOGANDO"
    );

    // Painel do jogo
    if (estado.estado === "JOGANDO") {
        restaurarGato(); // nova rodada: o gato volta a correr
        el.painel.classList.add("oculto");
    } else if (estado.estado === "FIM" && estadoAnterior === "JOGANDO") {
        // Acabou de bater: toca a animação e só depois mostra "Jogar de novo"
        iniciarAnimacaoFim(estado);
        el.painel.classList.add("oculto");
    } else if (animandoFim) {
        el.painel.classList.add("oculto"); // espera a animação terminar
    } else {
        el.painel.classList.remove("oculto");

        // Página aberta com a rodada já terminada: mostra o resultado direto
        if (estado.estado === "FIM" && !fimMostrado) {
            mostrarFimDeJogo(estado);
            fimMostrado = true;
        }
    }

    estadoAnterior = estado.estado;
}

// Animação de game over: o gato congela, pula e cai para fora da estrada.
// Quando ela termina, aparece a tela para jogar novamente.
function iniciarAnimacaoFim(estado) {
    animandoFim = true;
    fimMostrado = false;

    el.jogadorImg.src = IMAGEM_GATO_MORTO;
    el.jogador.classList.add("morto");

    clearTimeout(timerFim);
    timerFim = setTimeout(() => {
        animandoFim = false;
        fimMostrado = true;
        mostrarFimDeJogo(estado);
        el.painel.classList.remove("oculto");
    }, DURACAO_GAME_OVER_MS);
}

// Volta o gato ao normal (e cancela a animação, se ainda estiver rodando)
function restaurarGato() {
    clearTimeout(timerFim);
    animandoFim = false;
    fimMostrado = false;

    if (el.jogador.classList.contains("morto")) {
        el.jogador.classList.remove("morto");
        el.jogadorImg.src = IMAGEM_GATO;
    }
}

// Exibe o resultado da partida
function mostrarFimDeJogo(estado) {
    el.titulo.textContent = "Você bateu!";

    let texto = `Nesta rodada você coletou <strong>${estado.moedas}</strong> moeda(s).<br>`;

    texto += `Suas moedas foram salvas (total: ${estado.totalSalvo}).<br><br>`;

    if (estado.novoRecorde) {
        texto += `<br>Novo recorde!`;
    }

    el.mensagem.innerHTML = texto;
    el.botao.textContent = "Jogar de novo";
}

// Inicia uma nova partida
async function iniciar() {
    const estado = await chamar("/api/iniciar", "POST");

    if (estado) desenhar(estado);
}

// Movimentação do jogador
async function mover(direcao) {
    const estado = await chamar("/api/mover?dir=" + direcao, "POST");

    if (estado) desenhar(estado);
}

// Atualiza o estado do jogo
async function atualizar() {
    const estado = await chamar("/api/estado");

    if (estado) desenhar(estado);
}

// ---------- Controles ----------

document.addEventListener("keydown", (e) => {
    if (e.repeat) return;

    const tecla = e.key.toLowerCase();

    if (tecla === "arrowleft" || tecla === "a") {
        mover("esq");

    } else if (tecla === "arrowright" || tecla === "d") {
        mover("dir");

    } else if (
        (tecla === " " || tecla === "enter") &&
        estadoAnterior !== "JOGANDO" &&
        !animandoFim // só pode jogar de novo depois da animação
    ) {
        e.preventDefault();
        iniciar();
    }
});

// Botões de controle
el.botao.addEventListener("click", iniciar);

document.getElementById("esq").addEventListener("click", () => mover("esq"));

document.getElementById("dir").addEventListener("click", () => mover("dir"));

// Atualização automática
setInterval(atualizar, INTERVALO_MS);

atualizar();