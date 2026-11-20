const API_URL = "http://localhost:7070/api/projetos";

const mensagemCategorias = document.getElementById("mensagemCategorias");
const gridCategorias = document.getElementById("gridCategorias");
const tituloListagemCategoria = document.getElementById("tituloListagemCategoria");
const contadorCategoria = document.getElementById("contadorCategoria");
const listaProjetosCategoria = document.getElementById("listaProjetosCategoria");

const CATEGORIAS_PADRAO = ["Web", "Software", "Mobile", "Outro"];

let todosProjetos = [];
let categoriaSelecionada = "Todas";

async function inicializarCategorias() {
    try {
        mensagemCategorias.textContent = "Carregando categorias e projetos...";
        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Erro ao consultar a API de projetos.");
        }

        todosProjetos = await resposta.json();
        renderizarBotoesCategorias();
        renderizarProjetosFiltrados();

        mensagemCategorias.textContent = "";
    } catch (erro) {
        mensagemCategorias.textContent = "Não foi possível carregar as categorias.";
        console.error(erro);
    }
}

function obterListaCategorias() {
    const categoriasEncontradas = [...CATEGORIAS_PADRAO];

    todosProjetos.forEach(projeto => {
        if (projeto.categoria && projeto.categoria.trim() !== "") {
            const jaExiste = categoriasEncontradas.some(
                cat => cat.toLowerCase() === projeto.categoria.trim().toLowerCase()
            );
            if (!jaExiste) {
                categoriasEncontradas.push(projeto.categoria.trim());
            }
        }
    });

    return categoriasEncontradas;
}

function contarProjetosPorCategoria(categoria) {
    if (categoria === "Todas") {
        return todosProjetos.length;
    }

    return todosProjetos.filter(projeto =>
        projeto.categoria &&
        projeto.categoria.trim().toLowerCase() === categoria.toLowerCase()
    ).length;
}

function renderizarBotoesCategorias() {
    gridCategorias.replaceChildren();

    const opcoes = ["Todas", ...obterListaCategorias()];

    opcoes.forEach(categoria => {
        const quantidade = contarProjetosPorCategoria(categoria);

        const botao = document.createElement("button");
        botao.type = "button";
        botao.classList.add("card-categoria");

        if (categoriaSelecionada.toLowerCase() === categoria.toLowerCase()) {
            botao.classList.add("selecionada");
        }

        const nomeSpan = document.createElement("span");
        nomeSpan.classList.add("categoria-nome");
        nomeSpan.textContent = categoria;

        const qtdSpan = document.createElement("span");
        qtdSpan.classList.add("categoria-qtd");
        qtdSpan.textContent = `${quantidade} ${quantidade === 1 ? "projeto" : "projetos"}`;

        botao.appendChild(nomeSpan);
        botao.appendChild(qtdSpan);

        botao.addEventListener("click", () => {
            categoriaSelecionada = categoria;
            renderizarBotoesCategorias();
            renderizarProjetosFiltrados();
        });

        gridCategorias.appendChild(botao);
    });
}

function renderizarProjetosFiltrados() {
    listaProjetosCategoria.replaceChildren();

    const filtrados = categoriaSelecionada === "Todas"
        ? todosProjetos
        : todosProjetos.filter(projeto =>
            projeto.categoria &&
            projeto.categoria.trim().toLowerCase() === categoriaSelecionada.toLowerCase()
        );

    tituloListagemCategoria.textContent = categoriaSelecionada === "Todas"
        ? "Todos os Projetos"
        : `Projetos em: ${categoriaSelecionada}`;

    contadorCategoria.textContent = `${filtrados.length} ${filtrados.length === 1 ? "projeto" : "projetos"}`;

    if (filtrados.length === 0) {
        const vazio = document.createElement("p");
        vazio.textContent = categoriaSelecionada === "Todas"
            ? "Nenhum projeto cadastrado no sistema."
            : `Nenhum projeto encontrado na categoria "${categoriaSelecionada}".`;
        listaProjetosCategoria.appendChild(vazio);
        return;
    }

    filtrados.forEach(projeto => {
        const card = document.createElement("article");
        card.classList.add("projeto");

        const titulo = document.createElement("h3");
        titulo.textContent = `#${projeto.id} - ${projeto.nome}`;

        const descricao = document.createElement("p");
        descricao.textContent = projeto.descricao;

        const meta = document.createElement("p");
        meta.textContent = `Categoria: ${projeto.categoria} | Status: ${projeto.status}`;

        card.appendChild(titulo);
        card.appendChild(descricao);
        card.appendChild(meta);

        listaProjetosCategoria.appendChild(card);
    });
}

inicializarCategorias();