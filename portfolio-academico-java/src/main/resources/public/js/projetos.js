const API_URL = "http://localhost:7070/api/projetos";

const listaProjetos = document.getElementById("listaProjetos");
const mensagem = document.getElementById("mensagem");
const formProjeto = document.getElementById("formProjeto");
const botaoSubmit = formProjeto.querySelector("button[type='submit']");

let idEmEdicao = null;

// Listagem de projetos
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

// Renderização dos cards
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
        titulo.textContent = projeto.nome;

        const descricao = document.createElement("p");
        descricao.textContent = projeto.descricao;

        const categoria = document.createElement("p");
        categoria.textContent = "Categoria: " + projeto.categoria;

        const status = document.createElement("p");
        status.textContent = "Status: " + projeto.status;

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
        card.appendChild(botaoExcluir);
        card.appendChild(botaoAlterar);
        listaProjetos.appendChild(card);
    });
}

// Preenche o formulário para edição
function prepararEdicao(projeto) {
    document.getElementById("nome").value = projeto.nome;
    document.getElementById("descricao").value = projeto.descricao;
    document.getElementById("categoria").value = projeto.categoria;
    document.getElementById("status").value = projeto.status;

    idEmEdicao = projeto.id;
    botaoSubmit.textContent = "Salvar alterações";
    formProjeto.scrollIntoView({ behavior: "smooth" });
}

// Cadastrar (POST) ou Alterar (PUT)
formProjeto.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    const projeto = {
        nome: document.getElementById("nome").value,
        descricao: document.getElementById("descricao").value,
        categoria: document.getElementById("categoria").value,
        status: document.getElementById("status").value
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
                throw new Error("Erro ao cadastrar o projeto.");
            }
            alert("Projeto cadastrado com sucesso!");
        }

        idEmEdicao = null;
        botaoSubmit.textContent = "Cadastrar projeto";
        formProjeto.reset();
        await listarProjetos();

    } catch (erro) {
        alert(idEmEdicao !== null
            ? "Não foi possível alterar o projeto."
            : "Não foi possível cadastrar o projeto.");
        console.error(erro);
    }
});

// Excluir (DELETE)
async function excluirProjeto(id) {
    const confirmar = confirm("Deseja realmente excluir este projeto?");
    if (!confirmar) return;

    try {
        const resposta = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
        if (!resposta.ok) {
            throw new Error("Erro ao excluir o projeto.");
        }
        alert("Projeto excluído com sucesso!");
        await listarProjetos();
    } catch (erro) {
        alert("Não foi possível excluir o projeto.");
        console.error(erro);
    }
}

// Alterar (PUT)
async function alterarProjeto(id, projeto) {
    const resposta = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(projeto)
    });

    if (!resposta.ok) {
        throw new Error("Erro ao alterar o projeto.");
    }
    return await resposta.json();
}

listarProjetos();