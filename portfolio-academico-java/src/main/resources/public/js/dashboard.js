const API_URL = "http://localhost:7070/api/projetos";

const mensagemDashboard = document.getElementById("mensagemDashboard");
const kpiTotal = document.getElementById("kpiTotal");
const kpiConcluidos = document.getElementById("kpiConcluidos");
const kpiAndamento = document.getElementById("kpiAndamento");
const kpiPlanejados = document.getElementById("kpiPlanejados");
const graficoCategorias = document.getElementById("graficoCategorias");
const resumoProgresso = document.getElementById("resumoProgresso");
const listaRecentes = document.getElementById("listaRecentes");

async function carregarDashboard() {
    try {
        mensagemDashboard.textContent = "Atualizando indicadores...";
        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Falha ao buscar dados da API.");
        }

        const projetos = await resposta.json();

        atualizarKPIs(projetos);
        renderizarCategorias(projetos);
        renderizarTaxaConclusao(projetos);
        renderizarRecentes(projetos);

        mensagemDashboard.textContent = "";
    } catch (erro) {
        mensagemDashboard.textContent = "Não foi possível carregar os dados do dashboard.";
        console.error(erro);
    }
}

function atualizarKPIs(projetos) {
    const total = projetos.length;

    const concluidos = projetos.filter(p =>
        p.status.toLowerCase() === "concluído"
    ).length;

    const andamento = projetos.filter(p => {
        const s = p.status.toLowerCase();
        return s === "em desenvolvimento" || s === "em andamento";
    }).length;

    const planejados = projetos.filter(p =>
        p.status.toLowerCase() === "planejado"
    ).length;

    kpiTotal.textContent = total;
    kpiConcluidos.textContent = concluidos;
    kpiAndamento.textContent = andamento;
    kpiPlanejados.textContent = planejados;
}

function renderizarCategorias(projetos) {
    graficoCategorias.replaceChildren();
    const categoriasBase = ["Web", "Software", "Mobile", "Outro"];
    const total = projetos.length;

    categoriasBase.forEach(cat => {
        const qtd = projetos.filter(p =>
            p.categoria && p.categoria.toLowerCase() === cat.toLowerCase()
        ).length;

        const percentual = total > 0 ? Math.round((qtd / total) * 100) : 0;

        const item = document.createElement("div");
        item.classList.add("barra-item");

        const topo = document.createElement("div");
        topo.classList.add("barra-topo");
        topo.innerHTML = `<span><strong>${cat}</strong></span><span>${qtd} (${percentual}%)</span>`;

        const trilha = document.createElement("div");
        trilha.classList.add("barra-trilha");

        const preenchimento = document.createElement("div");
        preenchimento.classList.add("barra-preenchimento");
        preenchimento.style.width = `${percentual}%`;

        trilha.appendChild(preenchimento);
        item.appendChild(topo);
        item.appendChild(trilha);
        graficoCategorias.appendChild(item);
    });
}

function renderizarTaxaConclusao(projetos) {
    resumoProgresso.replaceChildren();
    const total = projetos.length;

    if (total === 0) {
        resumoProgresso.textContent = "Cadastre projetos para visualizar o progresso geral.";
        return;
    }

    const concluidos = projetos.filter(p => p.status.toLowerCase() === "concluído").length;
    const percentualConcluido = Math.round((concluidos / total) * 100);

    const destaque = document.createElement("p");
    destaque.classList.add("percentual-destaque");
    destaque.textContent = `${percentualConcluido}%`;

    const legenda = document.createElement("p");
    legenda.textContent = `${concluidos} de ${total} projeto(s) já foram finalizados.`;

    const trilha = document.createElement("div");
    trilha.classList.add("barra-trilha", "barra-grande");

    const preenchimento = document.createElement("div");
    preenchimento.classList.add("barra-preenchimento", "verde");
    preenchimento.style.width = `${percentualConcluido}%`;

    trilha.appendChild(preenchimento);
    resumoProgresso.appendChild(destaque);
    resumoProgresso.appendChild(legenda);
    resumoProgresso.appendChild(trilha);
}

function renderizarRecentes(projetos) {
    listaRecentes.replaceChildren();

    if (projetos.length === 0) {
        const vazio = document.createElement("p");
        vazio.textContent = "Nenhum projeto cadastrado até o momento.";
        listaRecentes.appendChild(vazio);
        return;
    }

    // Ordena do maior ID para o menor e pega os 5 últimos
    const recentes = [...projetos]
        .sort((a, b) => b.id - a.id)
        .slice(0, 5);

    recentes.forEach(projeto => {
        const card = document.createElement("article");
        card.classList.add("projeto", "projeto-resumo");

        const info = document.createElement("div");
        const titulo = document.createElement("h3");
        titulo.textContent = `#${projeto.id} - ${projeto.nome}`;

        const meta = document.createElement("p");
        meta.textContent = `Categoria: ${projeto.categoria} | Status: ${projeto.status}`;

        info.appendChild(titulo);
        info.appendChild(meta);
        card.appendChild(info);
        listaRecentes.appendChild(card);
    });
}

carregarDashboard();