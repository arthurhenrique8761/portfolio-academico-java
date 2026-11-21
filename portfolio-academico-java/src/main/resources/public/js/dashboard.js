const API_URL = "http://localhost:7070/api/projetos";

const mensagemDashboard = document.getElementById("mensagemDashboard");
const kpiTotal = document.getElementById("kpiTotal");
const kpiConcluidos = document.getElementById("kpiConcluidos");
const kpiAndamento = document.getElementById("kpiAndamento");
const kpiCancelados = document.getElementById("kpiCancelados");
const legendaPrazos = document.getElementById("legendaPrazos");
const avisoUsuarios = document.getElementById("avisoUsuarios");
const listaRecentes = document.getElementById("listaRecentes");

const charts = {};

function destruirGraficoSeExistir(chave) {
    if (charts[chave]) {
        charts[chave].destroy();
    }
}

async function carregarDashboard() {
    try {
        mensagemDashboard.textContent = "Atualizando indicadores e gráficos...";
        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Falha ao buscar dados da API.");
        }

        const projetos = await resposta.json();

        atualizarKPIs(projetos);
        renderizarGraficoStatusPizza(projetos);
        renderizarGraficoCategoriasColunas(projetos);
        renderizarGraficoEvolucaoLinha(projetos);
        renderizarGraficoPrazosDonut(projetos);
        renderizarGraficoUsuariosHorizontais(projetos);
        renderizarRecentes(projetos);

        mensagemDashboard.textContent = "";
    } catch (erro) {
        mensagemDashboard.textContent = "Não foi possível carregar os dados do dashboard.";
        console.error(erro);
    }
}

// Dashboard 1 - Visão Geral (Cards)
function atualizarKPIs(projetos) {
    const total = projetos.length;

    const concluidos = projetos.filter(p =>
        (p.status || "").toLowerCase() === "concluído"
    ).length;

    const ativos = projetos.filter(p => {
        const s = (p.status || "").toLowerCase();
        return s === "em desenvolvimento" || s === "em andamento" || s === "planejado";
    }).length;

    const cancelados = projetos.filter(p =>
        (p.status || "").toLowerCase() === "cancelado"
    ).length;

    kpiTotal.textContent = total;
    kpiConcluidos.textContent = concluidos;
    kpiAndamento.textContent = ativos;
    kpiCancelados.textContent = cancelados;
}

// Dashboard 2 - Status dos Projetos (Pizza)
function renderizarGraficoStatusPizza(projetos) {
    destruirGraficoSeExistir("status");

    const statusOrdem = ["Concluído", "Em Desenvolvimento", "Planejado", "Cancelado"];
    const valores = statusOrdem.map(st =>
        projetos.filter(p => (p.status || "").toLowerCase() === st.toLowerCase()).length
    );

    const ctx = document.getElementById("graficoStatusPizza");
    charts.status = new Chart(ctx, {
        type: "pie",
        data: {
            labels: statusOrdem,
            datasets: [{
                data: valores,
                backgroundColor: ["#2e7d32", "#ed6c02", "#1976d2", "#c62828"],
                borderWidth: 2,
                borderColor: "#ffffff"
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom" }
            }
        }
    });
}

// Dashboard 3 - Projetos por Categoria (Colunas / Barras Verticais)
function renderizarGraficoCategoriasColunas(projetos) {
    destruirGraficoSeExistir("categorias");

    const categoriasOrdem = ["Web", "Software", "Mobile", "Desktop"];
    const valores = categoriasOrdem.map(cat =>
        projetos.filter(p => (p.categoria || "").toLowerCase() === cat.toLowerCase()).length
    );

    const ctx = document.getElementById("graficoCategoriasColunas");
    charts.categorias = new Chart(ctx, {
        type: "bar",
        data: {
            labels: categoriasOrdem,
            datasets: [{
                label: "Quantidade de Projetos",
                data: valores,
                backgroundColor: ["#1976d2", "#263238", "#00897b", "#5e35b1"],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                }
            }
        }
    });
}

// Dashboard 4 - Evolução Mensal (Linha)
function renderizarGraficoEvolucaoLinha(projetos) {
    destruirGraficoSeExistir("evolucao");

    const meses = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"];
    const contagens = Array(12).fill(0);

    projetos.forEach(p => {
        if (p.dataCriacao) {
            const partes = String(p.dataCriacao).split("-");
            if (partes.length >= 2) {
                const mesIdx = parseInt(partes[1], 10) - 1;
                if (mesIdx >= 0 && mesIdx < 12) {
                    contagens[mesIdx]++;
                }
            }
        }
    });

    const ctx = document.getElementById("graficoEvolucaoLinha");
    charts.evolucao = new Chart(ctx, {
        type: "line",
        data: {
            labels: meses,
            datasets: [{
                label: "Projetos Criados",
                data: contagens,
                borderColor: "#1976d2",
                backgroundColor: "rgba(25, 118, 210, 0.15)",
                fill: true,
                tension: 0.3,
                pointRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom" }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                }
            }
        }
    });
}

// Dashboard 5 - Cumprimento de Prazos (Rosca / Donut)
function renderizarGraficoPrazosDonut(projetos) {
    destruirGraficoSeExistir("prazos");

    const comPrazo = projetos.filter(p => Boolean(p.prazo));
    const hoje = new Date().toISOString().slice(0, 10);

    let noPrazo = 0;
    let atrasados = 0;

    comPrazo.forEach(p => {
        const concluido = (p.status || "").toLowerCase() === "concluído";
        const cancelado = (p.status || "").toLowerCase() === "cancelado";
        if (!concluido && !cancelado && p.prazo < hoje) {
            atrasados++;
        } else {
            noPrazo++;
        }
    });

    if (comPrazo.length === 0) {
        legendaPrazos.textContent = "Nenhum projeto possui prazo definido ainda.";
    } else {
        const percNoPrazo = Math.round((noPrazo / comPrazo.length) * 100);
        const percAtrasados = 100 - percNoPrazo;
        legendaPrazos.textContent = `No prazo: ${noPrazo} (${percNoPrazo}%) | Atrasados: ${atrasados} (${percAtrasados}%)`;
    }

    const ctx = document.getElementById("graficoPrazosDonut");
    charts.prazos = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["No prazo", "Atrasados"],
            datasets: [{
                data: comPrazo.length > 0 ? [noPrazo, atrasados] : [0, 0],
                backgroundColor: ["#2e7d32", "#c62828"],
                borderWidth: 2,
                borderColor: "#ffffff"
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "62%",
            plugins: {
                legend: { position: "bottom" }
            }
        }
    });
}

// Dashboard 6 - Usuários Mais Ativos (Barras Horizontais)
function renderizarGraficoUsuariosHorizontais(projetos) {
    destruirGraficoSeExistir("usuarios");

    const contagemPorUsuario = {};
    projetos.forEach(p => {
        const u = (p.usuario || p.responsavel || "").trim();
        if (u) {
            contagemPorUsuario[u] = (contagemPorUsuario[u] || 0) + 1;
        }
    });

    const entradas = Object.entries(contagemPorUsuario).sort((a, b) => b[1] - a[1]);

    let labels;
    let dados;

    if (entradas.length > 0) {
        avisoUsuarios.textContent = "";
        labels = entradas.map(e => e[0]);
        dados = entradas.map(e => e[1]);
    } else {
        avisoUsuarios.textContent = "Pré-visualização (aguardando integração de login de usuários):";
        labels = ["José", "Maria", "João"];
        dados = [30, 20, 15];
    }

    const ctx = document.getElementById("graficoUsuariosHorizontais");
    charts.usuarios = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "Projetos",
                data: dados,
                backgroundColor: "#37474f",
                borderRadius: 6
            }]
        },
        options: {
            indexAxis: "y",
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: { precision: 0 }
                }
            }
        }
    });
}

function renderizarRecentes(projetos) {
    listaRecentes.replaceChildren();

    if (projetos.length === 0) {
        const vazio = document.createElement("p");
        vazio.textContent = "Nenhum projeto cadastrado até o momento.";
        listaRecentes.appendChild(vazio);
        return;
    }

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
        meta.textContent = `Categoria: ${projeto.categoria} | Status: ${projeto.status} | Prazo: ${projeto.prazo || "Sem prazo"}`;

        info.appendChild(titulo);
        info.appendChild(meta);
        card.appendChild(info);
        listaRecentes.appendChild(card);
    });
}

carregarDashboard();