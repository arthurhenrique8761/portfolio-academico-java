const API_URL = "http://localhost:7070/api/projetos";

const listaProjetos = document.getElementById("listaProjetos");
const mensagem = document.getElementById("mensagem");
const mensagemFormulario = document.getElementById("mensagemFormulario");
const formProjeto = document.getElementById("formProjeto");
const botaoSubmit = formProjeto.querySelector("button[type='submit']");

const inputBuscaId = document.getElementById("buscaId");
const btnBuscarId = document.getElementById("btnBuscarId");
const btnLimparBusca = document.getElementById("btnLimparBusca");

let idEmEdicao = null;
let dataCriacaoEmEdicao = null;

function formatarDataBR(dataIso, fallback = "Não definido") {
    if (!dataIso) {
        return fallback;
    }

    const parteData = String(dataIso).split("T")[0];
    const partes = parteData.split("-");
    if (partes.length !== 3) {
        return dataIso;
    }

    const [ano, mes, dia] = partes;
    return `${dia}/${mes}/${ano}`;
}

function mostrarAvisoFormulario(texto, tipo = "erro") {
    if (!mensagemFormulario) return;
    mensagemFormulario.textContent = texto;
    mensagemFormulario.classList.remove("aviso-erro", "aviso-sucesso");
    mensagemFormulario.classList.add(tipo === "sucesso" ? "aviso-sucesso" : "aviso-erro");
    mensagemFormulario.style.display = "block";
}

function limparAvisoFormulario() {
    if (!mensagemFormulario) return;
    mensagemFormulario.textContent = "";
    mensagemFormulario.classList.remove("aviso-erro", "aviso-sucesso");
    mensagemFormulario.style.display = "none";
}

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
        prazo.textContent = "Prazo: " + formatarDataBR(projeto.prazo, "Não definido");

        const acoesCard = document.createElement("div");
        acoesCard.classList.add("acoes-projeto");

        const botaoExcluir = document.createElement("button");
        botaoExcluir.type = "button";
        botaoExcluir.textContent = "Excluir";

        const botaoAlterar = document.createElement("button");
        botaoAlterar.type = "button";
        botaoAlterar.textContent = "Alterar";
        botaoAlterar.addEventListener("click", () => prepararEdicao(projeto));

        const caixaConfirmacao = document.createElement("div");
        caixaConfirmacao.classList.add("confirmacao-exclusao");
        caixaConfirmacao.style.display = "none";

        const textoConfirmacao = document.createElement("span");
        textoConfirmacao.textContent = `Excluir o projeto #${projeto.id}? `;

        const botaoConfirmar = document.createElement("button");
        botaoConfirmar.type = "button";
        botaoConfirmar.classList.add("btn-confirmar-exclusao");
        botaoConfirmar.textContent = "Confirmar";
        botaoConfirmar.addEventListener("click", () => excluirProjeto(projeto.id));

        const botaoCancelar = document.createElement("button");
        botaoCancelar.type = "button";
        botaoCancelar.classList.add("btn-cancelar-exclusao");
        botaoCancelar.textContent = "Cancelar";
        botaoCancelar.addEventListener("click", () => {
            caixaConfirmacao.style.display = "none";
            botaoExcluir.disabled = false;
        });

        botaoExcluir.addEventListener("click", () => {
            caixaConfirmacao.style.display = "flex";
            botaoExcluir.disabled = true;
        });

        caixaConfirmacao.appendChild(textoConfirmacao);
        caixaConfirmacao.appendChild(botaoConfirmar);
        caixaConfirmacao.appendChild(botaoCancelar);

        acoesCard.appendChild(botaoExcluir);
        acoesCard.appendChild(botaoAlterar);

        card.appendChild(titulo);
        card.appendChild(descricao);
        card.appendChild(categoria);
        card.appendChild(status);
        card.appendChild(prazo);
        card.appendChild(acoesCard);
        card.appendChild(caixaConfirmacao);
        listaProjetos.appendChild(card);
    });
}

function prepararEdicao(projeto) {
    limparAvisoFormulario();
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
    limparAvisoFormulario();

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
            idEmEdicao = null;
            dataCriacaoEmEdicao = null;
            botaoSubmit.textContent = "Cadastrar projeto";
            formProjeto.reset();
            await listarProjetos();
            mostrarAvisoFormulario("Projeto alterado com sucesso!", "sucesso");
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

            idEmEdicao = null;
            dataCriacaoEmEdicao = null;
            botaoSubmit.textContent = "Cadastrar projeto";
            formProjeto.reset();
            await listarProjetos();
            mostrarAvisoFormulario("Projeto cadastrado com sucesso!", "sucesso");
        }
    } catch (erro) {
        mostrarAvisoFormulario(
            erro.message || (idEmEdicao !== null
                ? "Não foi possível alterar o projeto."
                : "Não foi possível cadastrar o projeto."),
            "erro"
        );
        console.error(erro);
    }
});

async function excluirProjeto(id) {
    limparAvisoFormulario();

    try {
        const resposta = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
        if (!resposta.ok) {
            const msgErro = await extrairErroResposta(resposta, "Não foi possível excluir o projeto.");
            throw new Error(msgErro);
        }
        await listarProjetos();
        mostrarAvisoFormulario("Projeto excluído com sucesso!", "sucesso");
    } catch (erro) {
        mostrarAvisoFormulario(erro.message || "Não foi possível excluir o projeto.", "erro");
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