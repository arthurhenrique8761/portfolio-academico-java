const API_URL = "http://localhost:7070/api/projetos";

const listaProjetos = document.getElementById("listaProjetos");

const mensagem = document.getElementById("mensagem");

// listagem de projetos
async function listarProjetos() {
    try {
        mensagem.textContent = "Carregando projetos...";
        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Erro ao consultar a API.");
        }
        const projetos = await resposta.json();

        exibirProjetos(projetos);

        mensagem.textContent = "Projetos carregados com sucesso.";
    } catch (erro) {
        mensagem.textContent = "Não foi possível carregar os projetos.";
        console.error(erro);
    }
}

// excluir projeto || o parâmetro "projetos" é a variavel de mesmo nome "const projetos" da função de listagem. js é uma zona 
function exibirProjetos(projetos) {
    listaProjetos.replaceChildren();

    if (projetos.length === 0) {
        const mensagemVazia = document.createElement("p");
        mensagemVazia.textContent = "Nenhum projeto cadastrado.";
        listaProjetos.appendChild(mensagemVazia);
        return;
    }

    projetos.forEach(projeto => {
        // cria um div tipo article e infere a classe "projeto"
        const card = document.createElement("article");
        card.classList.add("projeto");

        // cria elementos html e insere as propriedades de cada projeto nos campos de texto de cada tag
        const titulo = document.createElement("h3");
        titulo.textContent = projeto.nome;

        const descricao = document.createElement("p");
        descricao.textContent = projeto.descricao;

        const categoria = document.createElement("p");
        categoria.textContent = "Categoria: " + projeto.categoria;

        const status = document.createElement("p");
        status.textContent = "Status: " + projeto.status;

        const botaoExcluir = document.createElement("button");
        botaoExcluir.textContent = "Excluir";

        const botaoAlterar = document.createElement("button");
        botaoAlterar.textContent = "Alterar";

        function handleExcluirClick() {
            excluirProjeto(projeto.id);
        }
        botaoExcluir.addEventListener("click", handleExcluirClick);

        function handleAlterarClick() {
            alterarProjeto(projeto.id);
        }
        botaoAlterar.addEventListener("click", handleAlterarClick)

        // insere os elementos acima na div article, e insere a div article dentro da div da "listaProjetos"
        card.appendChild(titulo);
        card.appendChild(descricao);
        card.appendChild(categoria);
        card.appendChild(status);
        card.appendChild(botaoExcluir);
        card.appendChild(botaoAlterar);
        listaProjetos.appendChild(card);
    });
}

// cadastrar projeto
const formProjeto = document.getElementById("formProjeto");
formProjeto.addEventListener('submit', async function (evento) {
    evento.preventDefault();

    // definição de um objeto (exatamente isso) tipo projeto, atribuindo os valores dos respectivos campos
    const projeto = {
        nome: document.getElementById("nome").value,
        descricao: document.getElementById("descricao").value,
        categoria: document.getElementById("categoria").value,
        status: document.getElementById("status").value
    };

    try {
        const resposta = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(projeto)
        });
        if (!resposta.ok) {
            throw new Error("Erro ao cadastrar o projeto.");
        }

        alert("Projeto cadastrado com sucesso!");

        formProjeto.reset();
        await listarProjetos();

    } catch (erro) {
        alert("Não foi possível cadastrar o projeto.");
        console.error(erro);
    }
});

async function excluirProjeto(id) {
    const confirmar = confirm("Deseja realmente excluir este projeto?");
    if (!confirmar) {
        return;
    }
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

async function alterarProjeto(id, projeto) {
    const resposta = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(projeto)
    }
    );

    if (!resposta.ok) {
        throw new Error("Erro ao alterar o projeto.");
    }
    return await resposta.json();
}


listarProjetos();