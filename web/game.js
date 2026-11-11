
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

const el = { 
    estrada: document.getElementById("estrada"),
    itens: document.getElementById("itens"),
    jogador: document.getElementById("jogador"),
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
        el.painel.classList.add("oculto");
    } else {
        el.painel.classList.remove("oculto");

        if (estado.estado === "FIM" && estadoAnterior !== "FIM") {
            mostrarFimDeJogo(estado);
        }
    }

    estadoAnterior = estado.estado;
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
        estadoAnterior !== "JOGANDO"
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
