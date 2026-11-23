const API_URL = "http://localhost:7070/api/projetos";

const listaProjetos = document.getElementById("listaProjetos");
const mensagem = document.getElementById("mensagem");
const formProjeto = document.getElementById("formProjeto");
const botaoSubmit = formProjeto.querySelector("button[type='submit']");

const inputBuscaId = document.getElementById("buscaId");
const btnBuscarId = document.getElementById("btnBuscarId");
const btnLimparBusca = document.getElementById("btnLimparBusca");

let idEmEdicao = null;
let dataCriacaoEmEdicao = null;

async function buscarProjetoPorId(id) {
    const idNumero = Number(id);

    if (!Number.isInteger(idNumero) || idNumero <= 0) {
        mensagem.textContent = "Informe um ID válido: use apenas números inteiros maiores que zero (ex.: 1, 2, 3).";
        return;
    }

    try {
        mensagem.textContent = `Buscando projeto #${idNumero}...`;
        const resposta = await fetch(`${API_URL}/${idNumero}`);

        if (!resposta.ok) {
            const msgErro = await extrairErroResposta(resposta, "Projeto não encontrado.");
            throw new Error(msgErro);
        }

        const projeto = await resposta.json();
        exibirProjetos(projeto ? [projeto] : []);
        mensagem.textContent = `Projeto #${idNumero} encontrado.`;
    } catch (erro) {
        listaProjetos.replaceChildren();
        mensagem.textContent = erro.message || "Erro ao buscar projeto por ID.";
        console.error(erro);
    }
}

btnBuscarId.addEventListener("click", () => {
    buscarProjetoPorId(inputBuscaId.value.trim());
});

inputBuscaId.addEventListener("keydown", (evento) => {
    if (evento.key === "Enter") {
        evento.preventDefault();
        buscarProjetoPorId(inputBuscaId.value.trim());
    }
});

btnLimparBusca.addEventListener("click", () => {
    inputBuscaId.value = "";
    listarProjetos();
});

async function listarProjetos() {
    try {
        mensagem.textContent = "Carregando projetos...";
        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Erro ao consultar a API.");
        }

        const projetos = await resposta.json();
        exibirProjetos(projetos);

        mensagem.textContent = projetos.length > 0
            ? "Projetos carregados com sucesso."
            : "";
    } catch (erro) {
        mensagem.textContent = "Não foi possível carregar os projetos.";
        console.error(erro);
    }
}

function exibirProjetos(projetos) {
    listaProjetos.replaceChildren();

    if (projetos.length === 0) {
        const mensagemVazia = document.createElement("p");
        mensagemVazia.textContent = "Nenhum projeto cadastrado.";
        listaProjetos.appendChild(mensagemVazia);
        return;
    }

    projetos.forEach(projeto => {
        const card = document.createElement("article");
        card.classList.add("projeto");

        const titulo = document.createElement("h3");
        titulo.textContent = `#${projeto.id} - ${projeto.nome}`;

        const descricao = document.createElement("p");
        descricao.textContent = projeto.descricao;

        const categoria = document.createElement("p");
        categoria.textContent = "Categoria: " + projeto.categoria;

        const status = document.createElement("p");
        status.textContent = "Status: " + projeto.status;

        const prazo = document.createElement("p");
        prazo.textContent = "Prazo: " + (projeto.prazo || "Não definido");

        const botaoExcluir = document.createElement("button");
        botaoExcluir.type = "button";
        botaoExcluir.textContent = "Excluir";
        botaoExcluir.addEventListener("click", () => excluirProjeto(projeto.id));

        const botaoAlterar = document.createElement("button");
        botaoAlterar.type = "button";
        botaoAlterar.textContent = "Alterar";
        botaoAlterar.addEventListener("click", () => prepararEdicao(projeto));

        card.appendChild(titulo);
        card.appendChild(descricao);
        card.appendChild(categoria);
        card.appendChild(status);
        card.appendChild(prazo);
        card.appendChild(botaoExcluir);
        card.appendChild(botaoAlterar);
        listaProjetos.appendChild(card);
    });
}

function prepararEdicao(projeto) {
    document.getElementById("nome").value = projeto.nome || "";
    document.getElementById("descricao").value = projeto.descricao || "";
    document.getElementById("categoria").value = projeto.categoria || "Software";
    document.getElementById("status").value = projeto.status || "Planejado";
    document.getElementById("prazo").value = projeto.prazo || "";

    idEmEdicao = projeto.id;
    dataCriacaoEmEdicao = projeto.dataCriacao || null;
    botaoSubmit.textContent = "Salvar alterações";
    formProjeto.scrollIntoView({ behavior: "smooth" });
}

async function extrairErroResposta(resposta, fallback) {
    try {
        const dados = await resposta.json();
        if (dados && dados.erro) {
            return dados.erro;
        }
    } catch (_) {
    }
    return fallback;
}

formProjeto.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    const prazoValor = document.getElementById("prazo").value;
    const hojeIso = new Date().toISOString().slice(0, 10);

    const projeto = {
        nome: document.getElementById("nome").value,
        descricao: document.getElementById("descricao").value,
        categoria: document.getElementById("categoria").value,
        status: document.getElementById("status").value,
        dataCriacao: idEmEdicao !== null && dataCriacaoEmEdicao ? dataCriacaoEmEdicao : hojeIso,
        prazo: prazoValor ? prazoValor : null
    };

    try {
        if (idEmEdicao !== null) {
            await alterarProjeto(idEmEdicao, projeto);
            alert("Projeto alterado com sucesso!");
        } else {
            const resposta = await fetch(API_URL, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(projeto)
            });

            if (!resposta.ok) {
                const msgErro = await extrairErroResposta(resposta, "Não foi possível cadastrar o projeto.");
                throw new Error(msgErro);
            }
            alert("Projeto cadastrado com sucesso!");
        }

        idEmEdicao = null;
        dataCriacaoEmEdicao = null;
        botaoSubmit.textContent = "Cadastrar projeto";
        formProjeto.reset();
        await listarProjetos();

    } catch (erro) {
        alert(erro.message || (idEmEdicao !== null
            ? "Não foi possível alterar o projeto."
            : "Não foi possível cadastrar o projeto."));
        console.error(erro);
    }
});

async function excluirProjeto(id) {
    const confirmar = confirm("Deseja realmente excluir este projeto?");
    if (!confirmar) return;

    try {
        const resposta = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
        if (!resposta.ok) {
            const msgErro = await extrairErroResposta(resposta, "Não foi possível excluir o projeto.");
            throw new Error(msgErro);
        }
        alert("Projeto excluído com sucesso!");
        await listarProjetos();
    } catch (erro) {
        alert(erro.message || "Não foi possível excluir o projeto.");
        console.error(erro);
    }
}

async function alterarProjeto(id, projeto) {
    const resposta = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(projeto)
    });

    if (!resposta.ok) {
        const msgErro = await extrairErroResposta(resposta, "Não foi possível alterar o projeto.");
        throw new Error(msgErro);
    }
    return await resposta.json();
}

listarProjetos();