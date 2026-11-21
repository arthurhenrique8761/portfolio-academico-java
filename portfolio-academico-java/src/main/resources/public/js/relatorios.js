const API_URL = "http://localhost:7070/api/projetos";

const mensagemRelatorio = document.getElementById("mensagemRelatorio");
const filtroBusca = document.getElementById("filtroBusca");
const filtroCategoria = document.getElementById("filtroCategoria");
const filtroStatus = document.getElementById("filtroStatus");
const btnLimparFiltros = document.getElementById("btnLimparFiltros");
const btnExportarCsv = document.getElementById("btnExportarCsv");
const btnImprimir = document.getElementById("btnImprimir");
const resumoQuantidade = document.getElementById("resumoQuantidade");
const corpoTabelaRelatorio = document.getElementById("corpoTabelaRelatorio");

let todosProjetos = [];
let projetosFiltrados = [];

async function inicializarRelatorios() {
    try {
        mensagemRelatorio.textContent = "Carregando dados do relatório...";
        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Erro ao consultar a API de projetos.");
        }

        todosProjetos = await resposta.json();
        popularCategoriasExtras();
        aplicarFiltros();

        mensagemRelatorio.textContent = "";
    } catch (erro) {
        mensagemRelatorio.textContent = "Não foi possível carregar os dados do relatório.";
        console.error(erro);
    }
}

function popularCategoriasExtras() {
    const opcoesExistentes = Array.from(filtroCategoria.options).map(opt => opt.value.toLowerCase());

    todosProjetos.forEach(projeto => {
        if (projeto.categoria && projeto.categoria.trim() !== "") {
            const catTrim = projeto.categoria.trim();
            if (!opcoesExistentes.includes(catTrim.toLowerCase())) {
                const option = document.createElement("option");
                option.value = catTrim;
                option.textContent = catTrim;
                filtroCategoria.appendChild(option);
                opcoesExistentes.push(catTrim.toLowerCase());
            }
        }
    });
}

function aplicarFiltros() {
    const termo = filtroBusca.value.trim().toLowerCase();
    const categoriaEscolhida = filtroCategoria.value;
    const statusEscolhido = filtroStatus.value;

    projetosFiltrados = todosProjetos.filter(projeto => {
        const nome = (projeto.nome || "").toLowerCase();
        const descricao = (projeto.descricao || "").toLowerCase();
        const categoria = (projeto.categoria || "").trim();
        const status = (projeto.status || "").trim();

        const correspondeBusca = termo === "" || nome.includes(termo) || descricao.includes(termo);
        const correspondeCategoria = categoriaEscolhida === "Todas" || categoria.toLowerCase() === categoriaEscolhida.toLowerCase();
        const correspondeStatus = statusEscolhido === "Todos" || status.toLowerCase() === statusEscolhido.toLowerCase();

        return correspondeBusca && correspondeCategoria && correspondeStatus;
    });

    renderizarTabela();
}

function renderizarTabela() {
    corpoTabelaRelatorio.replaceChildren();
    resumoQuantidade.textContent = `${projetosFiltrados.length} ${projetosFiltrados.length === 1 ? "registro" : "registros"}`;

    if (projetosFiltrados.length === 0) {
        const linhaVazia = document.createElement("tr");
        const celulaVazia = document.createElement("td");
        celulaVazia.colSpan = 6;
        celulaVazia.classList.add("celula-vazia");
        celulaVazia.textContent = "Nenhum projeto encontrado para os filtros selecionados.";
        linhaVazia.appendChild(celulaVazia);
        corpoTabelaRelatorio.appendChild(linhaVazia);
        return;
    }

    projetosFiltrados.forEach(projeto => {
        const tr = document.createElement("tr");

        const tdId = document.createElement("td");
        tdId.textContent = projeto.id;

        const tdNome = document.createElement("td");
        tdNome.textContent = projeto.nome;

        const tdDescricao = document.createElement("td");
        tdDescricao.textContent = projeto.descricao;

        const tdCategoria = document.createElement("td");
        tdCategoria.textContent = projeto.categoria;

        const tdStatus = document.createElement("td");
        const badgeStatus = document.createElement("span");
        badgeStatus.classList.add("status-badge");
        badgeStatus.textContent = projeto.status;

        const statusLower = (projeto.status || "").toLowerCase();
        if (statusLower === "concluído" || statusLower === "concluido") {
            badgeStatus.classList.add("status-concluido");
        } else if (statusLower === "em desenvolvimento" || statusLower === "em andamento") {
            badgeStatus.classList.add("status-andamento");
        } else {
            badgeStatus.classList.add("status-planejado");
        }

        tdStatus.appendChild(badgeStatus);

        const tdPrazo = document.createElement("td");
        tdPrazo.textContent = projeto.prazo || "-";

        tr.appendChild(tdId);
        tr.appendChild(tdNome);
        tr.appendChild(tdDescricao);
        tr.appendChild(tdCategoria);
        tr.appendChild(tdStatus);
        tr.appendChild(tdPrazo);

        corpoTabelaRelatorio.appendChild(tr);
    });
}

function escaparCsv(valor) {
    const texto = String(valor ?? "");
    const seguro = texto.replace(/"/g, '""');
    return `"${seguro}"`;
}

function exportarCsv() {
    if (projetosFiltrados.length === 0) {
        alert("Não há registros filtrados para exportar.");
        return;
    }

    const cabecalho = ["ID", "Nome", "Descricao", "Categoria", "Status", "Prazo"];
    const linhas = projetosFiltrados.map(p => [
        escaparCsv(p.id),
        escaparCsv(p.nome),
        escaparCsv(p.descricao),
        escaparCsv(p.categoria),
        escaparCsv(p.status),
        escaparCsv(p.prazo || "")
    ].join(";"));

    const conteudoCsv = "\uFEFF" + [cabecalho.join(";"), ...linhas].join("\n");
    const blob = new Blob([conteudoCsv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = "relatorio-projetos.csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

filtroBusca.addEventListener("input", aplicarFiltros);
filtroCategoria.addEventListener("change", aplicarFiltros);
filtroStatus.addEventListener("change", aplicarFiltros);

btnLimparFiltros.addEventListener("click", () => {
    filtroBusca.value = "";
    filtroCategoria.value = "Todas";
    filtroStatus.value = "Todos";
    aplicarFiltros();
});

btnExportarCsv.addEventListener("click", exportarCsv);
btnImprimir.addEventListener("click", () => window.print());

inicializarRelatorios();