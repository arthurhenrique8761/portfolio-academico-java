package br.com.arthurhenrique.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import br.com.arthurhenrique.api.Api;
import br.com.arthurhenrique.client.ApiClient;
import br.com.arthurhenrique.log.LogManager;
import br.com.arthurhenrique.model.Projeto;
import br.com.arthurhenrique.service.ProjetoService;

public class TelaPrincipal extends JFrame {

    // =====================================================================
    // CONSTANTES
    // =====================================================================

    // Paleta e tipografia
    private static final Color COR_FUNDO = new Color(244, 246, 248);
    private static final Color COR_TOPO = new Color(38, 50, 56);
    private static final Color COR_CARD = Color.WHITE;
    private static final Color COR_BORDA = new Color(220, 226, 232);
    private static final Color COR_TEXTO = new Color(33, 37, 41);
    private static final Color COR_MUTED = new Color(108, 117, 125);

    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONTE_PADRAO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 13);

    // Domínio (mesmos valores da versão básica e da versão web)
    private static final String[] CATEGORIAS = { "Software", "Mobile", "Web", "Desktop" };
    private static final String[] STATUS = { "Planejado", "Em Desenvolvimento", "Concluído", "Cancelado" };
    private static final String[] OPCOES_CATEGORIA = comOpcaoTodos("Todas", CATEGORIAS);
    private static final String[] OPCOES_STATUS = comOpcaoTodos("Todos", STATUS);
    private static final String[] COLUNAS = { "ID", "Nome", "Categoria", "Status", "Prazo" };

    private static final int COL_ID = 0;
    private static final int COL_NOME = 1;
    private static final int COL_CATEGORIA = 2;
    private static final int COL_STATUS = 3;
    private static final int COL_PRAZO = 4;

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Subtítulo do cabeçalho para cada aba (na ordem das abas)
    private static final String[] SUBTITULOS = {
            "Cadastro, edição e gestão dos projetos do portfólio",
            "Exploração e filtragem de projetos por categoria",
            "Visão geral e indicadores do portfólio",
            "Relatórios detalhados, filtros e exportação de dados"
    };

    // =====================================================================
    // CAMPOS
    // =====================================================================

    // Dados
    private ProjetoService service;
    private ApiClient apiClient;
    private List<Projeto> projetosCache = new ArrayList<>();
    private boolean atualizandoTabela = false;
    private boolean erroCargaExibido = false;

    // Cabeçalho
    private JLabel subtituloCabecalho;

    // Aba Projetos
    private JTextField campoNome;
    private JTextField campoDescricao;
    private JTextField campoPrazo;
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;
    private JButton botaoCadastrar;
    private JButton botaoLimpar;
    private JButton botaoExcluir;
    private JButton botaoAlterar;
    private DefaultTableModel modelo;
    private JTable tabela;

    // Aba Categorias
    private String categoriaAtiva = "Todas";
    private Map<String, JButton> botoesCategoria;
    private DefaultTableModel modeloCategorias;
    private JLabel lblBadgeProjetosCat;

    // Aba Dashboard
    private JLabel lblKpiTotal;
    private JLabel lblKpiConcluidos;
    private JLabel lblKpiDesenvolvimento;
    private JLabel lblKpiPlanejados;
    private Map<String, JLabel> lblDistribuicao;
    private JLabel lblPercentualConclusao;
    private JLabel lblDetalheTaxa;
    private JProgressBar barraConclusao;
    private JPanel listaUltimosProjetos;

    // Aba Relatórios
    private JTextField campoBuscaRelatorio;
    private JComboBox<String> comboFiltroCategoria;
    private JComboBox<String> comboFiltroStatus;
    private JLabel lblQuantidadeRegistros;
    private JTable tabelaRelatorio;
    private TableRowSorter<TableModel> sorterRelatorios;

    // =====================================================================
    // CONSTRUTOR
    // =====================================================================

    public TelaPrincipal() throws Exception {
        setTitle("Sistema de Projetos");
        setSize(1120, 840);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(COR_FUNDO);
        setLayout(new BorderLayout());

        service = new ProjetoService();
        apiClient = new ApiClient();

        criarComponentes();
        criarEventos();

        try {
            service.carregar();
        } catch (Exception e) {
            LogManager.error("DESKTOP_CARGA", e.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao carregar: " + e.getMessage());
        }
        carregarTabela();
    }

    // =====================================================================
    // MONTAGEM DA INTERFACE - estrutura geral
    // =====================================================================

    public void criarComponentes() {
        criarFormulario();
        criarTabelaProjetos();

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarAbas(), BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel criarCabecalho() {
        JPanel topo = new JPanel();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.setBackground(COR_TOPO);
        topo.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel titulo = new JLabel("Sistema de Projetos");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtituloCabecalho = new JLabel(SUBTITULOS[0]);
        subtituloCabecalho.setFont(FONTE_SUBTITULO);
        subtituloCabecalho.setForeground(new Color(207, 216, 220));
        subtituloCabecalho.setAlignmentX(Component.CENTER_ALIGNMENT);

        topo.add(titulo);
        topo.add(Box.createVerticalStrut(4));
        topo.add(subtituloCabecalho);
        return topo;
    }

    private JPanel criarAbas() {
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(FONTE_PADRAO);
        abas.setBackground(COR_FUNDO);
        abas.addTab("Projetos", criarAbaProjetos());
        abas.addTab("Categorias", criarAbaCategorias());
        abas.addTab("Dashboard", criarAbaDashboard());
        abas.addTab("Relatórios", criarAbaRelatorios());

        abas.addChangeListener(e -> subtituloCabecalho.setText(SUBTITULOS[abas.getSelectedIndex()]));

        JPanel painelConteudo = new JPanel(new BorderLayout());
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.add(abas, BorderLayout.CENTER);
        return painelConteudo;
    }

    public void criarEventos() {
        botaoCadastrar.addActionListener(e -> cadastrar());
        botaoLimpar.addActionListener(e -> limparFormulario());
        botaoExcluir.addActionListener(e -> excluirProjeto());
        botaoAlterar.addActionListener(e -> alterarProjeto());
    }

    // =====================================================================
    // ABA PROJETOS - montagem
    // =====================================================================

    private void criarFormulario() {
        campoNome = new JTextField(22);
        campoDescricao = new JTextField(30);
        campoPrazo = new JTextField(10);
        campoPrazo.setToolTipText("AAAA-MM-DD ou DD/MM/AAAA");

        comboCategoria = new JComboBox<>(CATEGORIAS);
        comboStatus = new JComboBox<>(STATUS);

        botaoCadastrar = new JButton("Cadastrar");
        botaoAlterar = new JButton("Alterar");
        botaoExcluir = new JButton("Excluir");
        botaoLimpar = new JButton("Limpar");

        estilizarCampo(campoNome);
        estilizarCampo(campoDescricao);
        estilizarCampo(campoPrazo);
        estilizarCombo(comboCategoria);
        estilizarCombo(comboStatus);
        estilizarBotao(botaoCadastrar, new Color(33, 150, 243), Color.WHITE);
        estilizarBotao(botaoAlterar, new Color(255, 152, 0), Color.WHITE);
        estilizarBotao(botaoExcluir, new Color(220, 53, 69), Color.WHITE);
        estilizarBotao(botaoLimpar, new Color(108, 117, 125), Color.WHITE);
    }

    private void criarTabelaProjetos() {
        modelo = criarModeloTabela();
        tabela = new JTable(modelo);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ao selecionar uma linha, carrega os dados no formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || atualizandoTabela) {
                return;
            }
            int linhaView = tabela.getSelectedRow();
            if (linhaView != -1) {
                preencherFormulario(idDaLinha(linhaView));
            }
        });
    }

    private JPanel criarAbaProjetos() {
        JScrollPane scrollTabela = new JScrollPane(tabela);
        estilizarTabela(tabela, scrollTabela);

        JPanel linha1 = criarLinha(6);
        linha1.add(criarRotulo("Nome:"));
        linha1.add(campoNome);
        linha1.add(criarRotulo("Categoria:"));
        linha1.add(comboCategoria);
        linha1.add(criarRotulo("Status:"));
        linha1.add(comboStatus);

        JPanel linha2 = criarLinha(6);
        linha2.add(criarRotulo("Descrição:"));
        linha2.add(campoDescricao);
        linha2.add(criarRotulo("Prazo:"));
        linha2.add(campoPrazo);

        JPanel linhaBotoes = criarLinha(8);
        linhaBotoes.add(botaoCadastrar);
        linhaBotoes.add(botaoAlterar);
        linhaBotoes.add(botaoExcluir);
        linhaBotoes.add(botaoLimpar);

        JPanel painelFormulario = new JPanel();
        painelFormulario.setLayout(new BoxLayout(painelFormulario, BoxLayout.Y_AXIS));
        painelFormulario.setBackground(COR_CARD);
        painelFormulario.add(criarTituloSecao("Cadastrar / Editar Projeto"));
        painelFormulario.add(Box.createVerticalStrut(6));
        painelFormulario.add(linha1);
        painelFormulario.add(linha2);
        painelFormulario.add(linhaBotoes);

        JPanel cardFormulario = criarCard();
        cardFormulario.setLayout(new BorderLayout());
        cardFormulario.add(painelFormulario, BorderLayout.CENTER);
        cardFormulario.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardFormulario.getPreferredSize().height));

        JPanel cardTabela = criarCard();
        cardTabela.setLayout(new BorderLayout(0, 10));
        cardTabela.add(criarTituloSecao("Projetos Cadastrados"), BorderLayout.NORTH);
        cardTabela.add(scrollTabela, BorderLayout.CENTER);

        JPanel aba = criarPainelAba();
        aba.add(cardFormulario);
        aba.add(Box.createVerticalStrut(16));
        aba.add(cardTabela);
        return aba;
    }

    // =====================================================================
    // ABA CATEGORIAS
    // =====================================================================

    private JPanel criarAbaCategorias() {
        categoriaAtiva = OPCOES_CATEGORIA[0];
        botoesCategoria = new LinkedHashMap<>();
        modeloCategorias = criarModeloTabela();

        // Card com os botões de filtro
        JPanel painelBotoes = criarLinha(8);
        for (String opcao : OPCOES_CATEGORIA) {
            JButton btn = new JButton(opcao);
            estilizarBotao(btn, new Color(241, 243, 245), COR_TEXTO);
            btn.addActionListener(e -> {
                categoriaAtiva = opcao;
                atualizarCategorias();
            });
            botoesCategoria.put(opcao, btn);
            painelBotoes.add(btn);
        }

        JPanel cardFiltro = criarCard();
        cardFiltro.setLayout(new BoxLayout(cardFiltro, BoxLayout.Y_AXIS));
        cardFiltro.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardFiltro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cardFiltro.add(criarTituloSecao("Filtrar por Categoria"));
        cardFiltro.add(painelBotoes);

        // Card com a tabela filtrada e contador
        JTable tabelaCategorias = new JTable(modeloCategorias);
        tabelaCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollCategorias = new JScrollPane(tabelaCategorias);
        estilizarTabela(tabelaCategorias, scrollCategorias);

        lblBadgeProjetosCat = new JLabel("0 projeto(s)");
        lblBadgeProjetosCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadgeProjetosCat.setForeground(COR_TEXTO);
        lblBadgeProjetosCat.setOpaque(true);
        lblBadgeProjetosCat.setBackground(new Color(238, 241, 244));
        lblBadgeProjetosCat.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_CARD);
        cabecalho.add(criarTituloSecao("Todos os Projetos"), BorderLayout.WEST);
        cabecalho.add(lblBadgeProjetosCat, BorderLayout.EAST);

        JPanel cardTabela = criarCard();
        cardTabela.setLayout(new BorderLayout(0, 10));
        cardTabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardTabela.add(cabecalho, BorderLayout.NORTH);
        cardTabela.add(scrollCategorias, BorderLayout.CENTER);

        JPanel aba = criarPainelAba();
        aba.add(cardFiltro);
        aba.add(Box.createVerticalStrut(16));
        aba.add(cardTabela);
        return aba;
    }

    private void atualizarCategorias() {
        int totalGeral = modelo.getRowCount();
        Map<String, Integer> contagem = contarPorCategoria();

        modeloCategorias.setRowCount(0);
        for (int i = 0; i < totalGeral; i++) {
            String categoria = valorCelula(i, COL_CATEGORIA);
            if (categoriaAtiva.equals(OPCOES_CATEGORIA[0]) || categoria.equalsIgnoreCase(categoriaAtiva)) {
                modeloCategorias.addRow(new Object[] {
                        modelo.getValueAt(i, COL_ID),
                        modelo.getValueAt(i, COL_NOME),
                        modelo.getValueAt(i, COL_CATEGORIA),
                        modelo.getValueAt(i, COL_STATUS),
                        modelo.getValueAt(i, COL_PRAZO)
                });
            }
        }
        lblBadgeProjetosCat.setText(modeloCategorias.getRowCount() + " projeto(s)");

        for (String opcao : OPCOES_CATEGORIA) {
            JButton btn = botoesCategoria.get(opcao);
            int qtd = opcao.equals(OPCOES_CATEGORIA[0]) ? totalGeral : contagem.get(opcao);
            btn.setText(opcao + " (" + qtd + ")");

            if (opcao.equalsIgnoreCase(categoriaAtiva)) {
                estilizarBotao(btn, COR_TOPO, Color.WHITE);
            } else {
                estilizarBotao(btn, new Color(241, 243, 245), COR_TEXTO);
            }
        }
    }

    // =====================================================================
    // ABA DASHBOARD
    // =====================================================================

    private JPanel criarAbaDashboard() {
        // KPIs
        lblKpiTotal = new JLabel("0");
        lblKpiConcluidos = new JLabel("0");
        lblKpiDesenvolvimento = new JLabel("0");
        lblKpiPlanejados = new JLabel("0");

        JPanel linhaKpis = new JPanel(new GridLayout(1, 4, 14, 0));
        linhaKpis.setBackground(COR_FUNDO);
        linhaKpis.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaKpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        linhaKpis.add(criarCartaoKpi("Total de Projetos", lblKpiTotal, new Color(33, 150, 243)));
        linhaKpis.add(criarCartaoKpi("Concluídos", lblKpiConcluidos, new Color(40, 167, 69)));
        linhaKpis.add(criarCartaoKpi("Em Desenvolvimento", lblKpiDesenvolvimento, new Color(255, 152, 0)));
        linhaKpis.add(criarCartaoKpi("Planejados", lblKpiPlanejados, new Color(123, 31, 162)));

        // Linha do meio: distribuição + taxa de conclusão
        JPanel linhaMeio = new JPanel(new GridLayout(1, 2, 14, 0));
        linhaMeio.setBackground(COR_FUNDO);
        linhaMeio.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaMeio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        linhaMeio.add(criarCardDistribuicao());
        linhaMeio.add(criarCardTaxaConclusao());

        // Últimos projetos
        listaUltimosProjetos = new JPanel();
        listaUltimosProjetos.setLayout(new BoxLayout(listaUltimosProjetos, BoxLayout.Y_AXIS));
        listaUltimosProjetos.setBackground(COR_CARD);
        listaUltimosProjetos.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cardUltimos = criarCard();
        cardUltimos.setLayout(new BoxLayout(cardUltimos, BoxLayout.Y_AXIS));
        cardUltimos.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardUltimos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        cardUltimos.add(criarTituloSecao("Últimos Projetos Cadastrados"));
        cardUltimos.add(Box.createVerticalStrut(10));
        cardUltimos.add(listaUltimosProjetos);

        JPanel conteudo = criarPainelAba();
        conteudo.add(linhaKpis);
        conteudo.add(Box.createVerticalStrut(14));
        conteudo.add(linhaMeio);
        conteudo.add(Box.createVerticalStrut(14));
        conteudo.add(cardUltimos);

        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COR_FUNDO);

        JPanel aba = new JPanel(new BorderLayout());
        aba.setBackground(COR_FUNDO);
        aba.add(scroll, BorderLayout.CENTER);
        return aba;
    }

    private JPanel criarCardDistribuicao() {
        JPanel card = criarCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(criarTituloSecao("Distribuição por Categoria"));
        card.add(Box.createVerticalStrut(10));

        lblDistribuicao = new LinkedHashMap<>();
        for (String categoria : CATEGORIAS) {
            JLabel lbl = new JLabel(categoria + ": 0");
            lbl.setFont(FONTE_PADRAO);
            lbl.setForeground(COR_TEXTO);
            lblDistribuicao.put(categoria, lbl);
            card.add(lbl);
            card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    private JPanel criarCardTaxaConclusao() {
        lblPercentualConclusao = new JLabel("0%");
        lblPercentualConclusao.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblPercentualConclusao.setForeground(new Color(40, 167, 69));

        lblDetalheTaxa = new JLabel("0 de 0 projetos concluídos");
        lblDetalheTaxa.setFont(FONTE_PADRAO);
        lblDetalheTaxa.setForeground(COR_MUTED);

        barraConclusao = new JProgressBar(0, 100);
        barraConclusao.setValue(0);
        barraConclusao.setStringPainted(true);
        barraConclusao.setForeground(new Color(40, 167, 69));
        barraConclusao.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel card = criarCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(criarTituloSecao("Taxa de Conclusão"));
        card.add(Box.createVerticalStrut(10));
        card.add(lblPercentualConclusao);
        card.add(Box.createVerticalStrut(4));
        card.add(lblDetalheTaxa);
        card.add(Box.createVerticalStrut(12));
        card.add(barraConclusao);
        return card;
    }

    private JPanel criarCartaoKpi(String titulo, JLabel lblValor, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, corDestaque),
                new EmptyBorder(12, 14, 12, 14)));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(COR_TEXTO);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(FONTE_PADRAO);
        lblTitulo.setForeground(COR_MUTED);

        card.add(lblValor, BorderLayout.NORTH);
        card.add(lblTitulo, BorderLayout.SOUTH);
        return card;
    }

    private void atualizarDashboard() {
        int total = modelo.getRowCount();
        int concluido = 0;
        int emDesenvolvimento = 0;
        int planejado = 0;

        for (int i = 0; i < total; i++) {
            String status = valorCelula(i, COL_STATUS);
            if ("Concluído".equalsIgnoreCase(status) || "Concluido".equalsIgnoreCase(status)) {
                concluido++;
            } else if ("Em Desenvolvimento".equalsIgnoreCase(status)) {
                emDesenvolvimento++;
            } else if ("Planejado".equalsIgnoreCase(status)) {
                planejado++;
            }
        }

        lblKpiTotal.setText(String.valueOf(total));
        lblKpiConcluidos.setText(String.valueOf(concluido));
        lblKpiDesenvolvimento.setText(String.valueOf(emDesenvolvimento));
        lblKpiPlanejados.setText(String.valueOf(planejado));

        for (Map.Entry<String, Integer> entrada : contarPorCategoria().entrySet()) {
            lblDistribuicao.get(entrada.getKey()).setText(entrada.getKey() + ": " + entrada.getValue());
        }

        int percentual = total > 0 ? Math.round((concluido * 100f) / total) : 0;
        lblPercentualConclusao.setText(percentual + "%");
        lblDetalheTaxa.setText(concluido + " de " + total + " projetos concluídos");
        barraConclusao.setValue(percentual);

        atualizarListaUltimosProjetos(total);
    }

    private void atualizarListaUltimosProjetos(int total) {
        listaUltimosProjetos.removeAll();

        if (total == 0) {
            JLabel vazio = new JLabel("Nenhum projeto cadastrado ainda.");
            vazio.setFont(FONTE_PADRAO);
            vazio.setForeground(COR_MUTED);
            listaUltimosProjetos.add(vazio);
        } else {
            int inicio = Math.max(0, total - 5);
            for (int i = total - 1; i >= inicio; i--) {
                String prazo = valorCelula(i, COL_PRAZO);
                String infoPrazo = prazo.isEmpty() ? "" : " • Prazo: " + prazo;

                JLabel item = new JLabel("#" + valorCelula(i, COL_ID) + " • " + valorCelula(i, COL_NOME)
                        + " (" + valorCelula(i, COL_CATEGORIA) + " — " + valorCelula(i, COL_STATUS) + ")" + infoPrazo);
                item.setFont(FONTE_PADRAO);
                item.setForeground(COR_TEXTO);
                listaUltimosProjetos.add(item);
                listaUltimosProjetos.add(Box.createVerticalStrut(6));
            }
        }

        listaUltimosProjetos.revalidate();
        listaUltimosProjetos.repaint();
    }

    // =====================================================================
    // ABA RELATÓRIOS
    // =====================================================================

    private JPanel criarAbaRelatorios() {
        // Filtros
        campoBuscaRelatorio = new JTextField(20);
        estilizarCampo(campoBuscaRelatorio);

        comboFiltroCategoria = new JComboBox<>(OPCOES_CATEGORIA);
        comboFiltroStatus = new JComboBox<>(OPCOES_STATUS);
        estilizarCombo(comboFiltroCategoria);
        estilizarCombo(comboFiltroStatus);

        JButton botaoLimparFiltros = new JButton("Limpar filtros");
        JButton botaoExportarCsv = new JButton("Exportar CSV");
        JButton botaoImprimirPdf = new JButton("Imprimir / Salvar PDF");
        estilizarBotao(botaoLimparFiltros, new Color(108, 117, 125), Color.WHITE);
        estilizarBotao(botaoExportarCsv, new Color(40, 167, 69), Color.WHITE);
        estilizarBotao(botaoImprimirPdf, new Color(33, 150, 243), Color.WHITE);

        JPanel linhaFiltros = criarLinha(6);
        linhaFiltros.add(criarRotulo("Buscar:"));
        linhaFiltros.add(campoBuscaRelatorio);
        linhaFiltros.add(criarRotulo("Categoria:"));
        linhaFiltros.add(comboFiltroCategoria);
        linhaFiltros.add(criarRotulo("Status:"));
        linhaFiltros.add(comboFiltroStatus);

        JPanel linhaAcoes = criarLinha(6);
        linhaAcoes.add(botaoLimparFiltros);
        linhaAcoes.add(botaoExportarCsv);
        linhaAcoes.add(botaoImprimirPdf);

        JPanel cardFiltros = criarCard();
        cardFiltros.setLayout(new BoxLayout(cardFiltros, BoxLayout.Y_AXIS));
        cardFiltros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        cardFiltros.add(criarTituloSecao("Filtros do Relatório"));
        cardFiltros.add(Box.createVerticalStrut(6));
        cardFiltros.add(linhaFiltros);
        cardFiltros.add(linhaAcoes);

        // Listagem (usa o mesmo modelo da aba Projetos, com sorter próprio)
        sorterRelatorios = new TableRowSorter<>(modelo);
        tabelaRelatorio = new JTable(modelo);
        tabelaRelatorio.setRowSorter(sorterRelatorios);
        tabelaRelatorio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollRelatorio = new JScrollPane(tabelaRelatorio);
        estilizarTabela(tabelaRelatorio, scrollRelatorio);

        lblQuantidadeRegistros = new JLabel("0 registro(s)");
        lblQuantidadeRegistros.setFont(FONTE_PADRAO);
        lblQuantidadeRegistros.setForeground(COR_MUTED);

        JPanel cabecalhoListagem = new JPanel(new BorderLayout());
        cabecalhoListagem.setBackground(COR_CARD);
        cabecalhoListagem.add(criarTituloSecao("Listagem Analítica"), BorderLayout.WEST);
        cabecalhoListagem.add(lblQuantidadeRegistros, BorderLayout.EAST);

        JPanel cardListagem = criarCard();
        cardListagem.setLayout(new BorderLayout(0, 10));
        cardListagem.add(cabecalhoListagem, BorderLayout.NORTH);
        cardListagem.add(scrollRelatorio, BorderLayout.CENTER);

        // Eventos da aba
        campoBuscaRelatorio.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltrosRelatorio();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltrosRelatorio();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltrosRelatorio();
            }
        });
        comboFiltroCategoria.addActionListener(e -> aplicarFiltrosRelatorio());
        comboFiltroStatus.addActionListener(e -> aplicarFiltrosRelatorio());
        botaoLimparFiltros.addActionListener(e -> limparFiltrosRelatorio());
        botaoExportarCsv.addActionListener(e -> exportarCsv());
        botaoImprimirPdf.addActionListener(e -> imprimirRelatorio());

        JPanel aba = criarPainelAba();
        aba.add(cardFiltros);
        aba.add(Box.createVerticalStrut(16));
        aba.add(cardListagem);
        return aba;
    }

    private void aplicarFiltrosRelatorio() {
        String termoBusca = campoBuscaRelatorio.getText().trim().toLowerCase();
        String catSelecionada = comboFiltroCategoria.getSelectedItem().toString();
        String statusSelecionado = comboFiltroStatus.getSelectedItem().toString();

        sorterRelatorios.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                String nome = entry.getStringValue(COL_NOME).toLowerCase();
                String categoria = entry.getStringValue(COL_CATEGORIA).trim();
                String status = entry.getStringValue(COL_STATUS).trim();
                String prazo = entry.getStringValue(COL_PRAZO).toLowerCase();

                boolean matchTexto = termoBusca.isEmpty() || nome.contains(termoBusca) || prazo.contains(termoBusca);
                boolean matchCategoria = OPCOES_CATEGORIA[0].equalsIgnoreCase(catSelecionada)
                        || categoria.equalsIgnoreCase(catSelecionada);
                boolean matchStatus = OPCOES_STATUS[0].equalsIgnoreCase(statusSelecionado)
                        || status.equalsIgnoreCase(statusSelecionado);

                return matchTexto && matchCategoria && matchStatus;
            }
        });

        lblQuantidadeRegistros.setText(tabelaRelatorio.getRowCount() + " registro(s)");
    }

    private void limparFiltrosRelatorio() {
        campoBuscaRelatorio.setText("");
        comboFiltroCategoria.setSelectedIndex(0);
        comboFiltroStatus.setSelectedIndex(0);
        aplicarFiltrosRelatorio();
    }

    private void exportarCsv() {
        JFileChooser seletor = new JFileChooser();
        seletor.setSelectedFile(new File("relatorio_projetos.csv"));

        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivo = seletor.getSelectedFile();
        if (!arquivo.getName().toLowerCase().endsWith(".csv")) {
            arquivo = new File(arquivo.getAbsolutePath() + ".csv");
        }

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {

            for (int col = 0; col < modelo.getColumnCount(); col++) {
                if (col > 0) {
                    pw.print(";");
                }
                pw.print(escaparCsv(modelo.getColumnName(col)));
            }
            pw.println();

            for (int viewRow = 0; viewRow < tabelaRelatorio.getRowCount(); viewRow++) {
                int modelRow = tabelaRelatorio.convertRowIndexToModel(viewRow);
                for (int col = 0; col < modelo.getColumnCount(); col++) {
                    if (col > 0) {
                        pw.print(";");
                    }
                    pw.print(escaparCsv(valorCelula(modelRow, col)));
                }
                pw.println();
            }

            JOptionPane.showMessageDialog(this,
                    "Relatório exportado com sucesso para:\n" + arquivo.getAbsolutePath(),
                    "Exportação concluída", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            LogManager.error("DESKTOP_EXPORTAR_CSV", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao exportar CSV: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void imprimirRelatorio() {
        try {
            boolean concluido = tabelaRelatorio.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("Relatório de Projetos"),
                    new MessageFormat("Página {0}"));

            if (concluido) {
                JOptionPane.showMessageDialog(this, "Impressão / exportação enviada com sucesso.",
                        "Relatório", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            LogManager.error("DESKTOP_IMPRESSAO", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao imprimir / salvar PDF: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escaparCsv(String texto) {
        if (texto == null) {
            return "";
        }
        String escapado = texto.replace("\"", "\"\"");
        if (escapado.contains(";") || escapado.contains("\"") || escapado.contains("\n") || escapado.contains("\r")) {
            return "\"" + escapado + "\"";
        }
        return escapado;
    }

    // =====================================================================
    // OPERAÇÕES DO PROJETO (cadastrar / alterar / excluir / limpar)
    // =====================================================================

    public void cadastrar() {
        try {
            service.carregar();
            int id = service.gerarId();
            Projeto projeto = montarEValidarProjeto(id, LocalDate.now());

            apiClient.cadastrarProjeto(projeto);
            JOptionPane.showMessageDialog(this, "Projeto Cadastrado: " + projeto.getNome());

            limparFormulario();
            carregarTabela();
        } catch (Exception e) {
            LogManager.error("DESKTOP_CADASTRO", e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void alterarProjeto() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int id = idDaLinha(linhaSelecionada);

            // Preserva a data de criação original do projeto
            Projeto original = buscarNoCache(id);
            LocalDate dataCriacao = original != null ? original.getDataCriacao() : null;
            Projeto projetoAtualizado = montarEValidarProjeto(id, dataCriacao);

            int option = JOptionPane.showConfirmDialog(this,
                    "Um projeto será alterado.\nEstá certo disso?",
                    "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                apiClient.alterarProjeto(id, projetoAtualizado);
                carregarTabela();
                limparFormulario();
                JOptionPane.showMessageDialog(this, "Projeto alterado com sucesso", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LogManager.error("DESKTOP_ALTERACAO", e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void excluirProjeto() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int id = idDaLinha(linhaSelecionada);

            int option = JOptionPane.showConfirmDialog(this,
                    "Um projeto será deletado.\nEstá certo disso?",
                    "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                apiClient.excluirProjeto(id);
                limparFormulario();
                carregarTabela();
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso", "Sucesso",
                        JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            LogManager.error("DESKTOP_EXCLUSAO", e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        campoPrazo.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        tabela.clearSelection();
        campoNome.requestFocus();
    }

    /**
     * Monta o Projeto a partir do formulário usando o construtor completo do
     * model e valida pelo ProjetoService (mesmo fluxo da versão básica).
     */
    private Projeto montarEValidarProjeto(int id, LocalDate dataCriacao) {
        String nome = campoNome.getText().trim();
        String descricao = campoDescricao.getText().trim();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();
        LocalDate prazo = ProjetoService.parseDataFlexivel(campoPrazo.getText().trim());

        Projeto projeto = new Projeto(id, nome, descricao, categoria, status, dataCriacao, prazo);
        service.validarProjeto(projeto, false, null);
        return projeto;
    }

    // =====================================================================
    // DADOS E TABELA (carga via API, atualização e seleção)
    // =====================================================================

    public void carregarTabela() {
        new SwingWorker<List<Projeto>, Void>() {
            @Override
            protected List<Projeto> doInBackground() throws Exception {
                return apiClient.listarProjetos();
            }

            @Override
            protected void done() {
                try {
                    projetosCache = get();
                    erroCargaExibido = false;
                    atualizarTabela(projetosCache);
                } catch (Exception e) {
                    LogManager.error("DESKTOP_CARGA", e.getMessage());
                    // Evita repetir o aviso a cada atualização automática
                    if (!erroCargaExibido) {
                        erroCargaExibido = true;
                        JOptionPane.showMessageDialog(TelaPrincipal.this, "Erro ao carregar: " + e.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    public void atualizarTabela(List<Projeto> projetos) {
        Integer idSelecionado = null;
        int linhaView = tabela.getSelectedRow();
        if (linhaView != -1) {
            idSelecionado = idDaLinha(linhaView);
        }

        // A flag impede que a re-seleção sobrescreva o que o usuário está digitando
        atualizandoTabela = true;
        try {
            modelo.setRowCount(0);
            for (Projeto projeto : projetos) {
                modelo.addRow(new Object[] {
                        projeto.getId(),
                        projeto.getNome(),
                        projeto.getCategoria(),
                        projeto.getStatus(),
                        formatarPrazo(projeto)
                });
            }

            if (idSelecionado != null) {
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    if (idSelecionado.equals(modelo.getValueAt(i, COL_ID))) {
                        int viewRow = tabela.convertRowIndexToView(i);
                        if (viewRow != -1) {
                            tabela.setRowSelectionInterval(viewRow, viewRow);
                        }
                        break;
                    }
                }
            }
        } finally {
            atualizandoTabela = false;
        }

        atualizarAbasDerivadas();
    }

    /** Recalcula as abas que dependem do modelo da tabela de projetos. */
    private void atualizarAbasDerivadas() {
        atualizarCategorias();
        atualizarDashboard();
        aplicarFiltrosRelatorio();
    }

    private void preencherFormulario(int id) {
        Projeto p = buscarNoCache(id);
        if (p == null) {
            return;
        }

        campoNome.setText(p.getNome() != null ? p.getNome() : "");
        campoDescricao.setText(p.getDescricao() != null ? p.getDescricao() : "");
        campoPrazo.setText(formatarPrazo(p));
        if (p.getCategoria() != null) {
            comboCategoria.setSelectedItem(p.getCategoria());
        }
        if (p.getStatus() != null) {
            comboStatus.setSelectedItem(p.getStatus());
        }
    }

    private Projeto buscarNoCache(int id) {
        for (Projeto p : projetosCache) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private int idDaLinha(int linhaView) {
        int linhaModel = tabela.convertRowIndexToModel(linhaView);
        return Integer.parseInt(modelo.getValueAt(linhaModel, COL_ID).toString());
    }

    private String valorCelula(int linhaModel, int coluna) {
        Object valor = modelo.getValueAt(linhaModel, coluna);
        return valor != null ? valor.toString().trim() : "";
    }

    private String formatarPrazo(Projeto projeto) {
        return projeto.getPrazo() != null ? projeto.getPrazo().format(FORMATO_DATA) : "";
    }

    private Map<String, Integer> contarPorCategoria() {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (String categoria : CATEGORIAS) {
            contagem.put(categoria, 0);
        }

        for (int i = 0; i < modelo.getRowCount(); i++) {
            String categoria = valorCelula(i, COL_CATEGORIA);
            for (String conhecida : CATEGORIAS) {
                if (conhecida.equalsIgnoreCase(categoria)) {
                    contagem.merge(conhecida, 1, Integer::sum);
                    break;
                }
            }
        }
        return contagem;
    }

    // =====================================================================
    // FÁBRICA DE COMPONENTES E ESTILO
    // =====================================================================

    private static String[] comOpcaoTodos(String rotulo, String[] opcoes) {
        String[] resultado = new String[opcoes.length + 1];
        resultado[0] = rotulo;
        System.arraycopy(opcoes, 0, resultado, 1, opcoes.length);
        return resultado;
    }

    private DefaultTableModel criarModeloTabela() {
        DefaultTableModel m = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String coluna : COLUNAS) {
            m.addColumn(coluna);
        }
        return m;
    }

    private JPanel criarPainelAba() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(16, 20, 16, 20));
        return painel;
    }

    private JPanel criarCard() {
        JPanel card = new JPanel();
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1, true),
                new EmptyBorder(18, 20, 18, 20)));
        return card;
    }

    private JPanel criarLinha(int espacamentoVertical) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, espacamentoVertical));
        linha.setBackground(COR_CARD);
        linha.setAlignmentX(Component.LEFT_ALIGNMENT);
        return linha;
    }

    private JLabel criarTituloSecao(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_SECAO);
        label.setForeground(COR_TEXTO);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel criarRotulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_PADRAO);
        label.setForeground(COR_TEXTO);
        return label;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(FONTE_PADRAO);
        campo.setForeground(COR_TEXTO);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(FONTE_PADRAO);
        combo.setForeground(COR_TEXTO);
        combo.setBackground(Color.WHITE);
    }

    private void estilizarBotao(JButton botao, Color fundo, Color texto) {
        botao.setFont(FONTE_BOTAO);
        botao.setBackground(fundo);
        botao.setForeground(texto);
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fundo.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)));
    }

    private void estilizarTabela(JTable tabela, JScrollPane scroll) {
        tabela.setFont(FONTE_PADRAO);
        tabela.setForeground(COR_TEXTO);
        tabela.setBackground(Color.WHITE);
        tabela.setRowHeight(28);
        tabela.setGridColor(COR_BORDA);
        tabela.setSelectionBackground(new Color(227, 242, 253));
        tabela.setSelectionForeground(COR_TEXTO);

        if (tabela.getTableHeader() != null) {
            tabela.getTableHeader().setFont(FONTE_BOTAO);
            tabela.getTableHeader().setBackground(new Color(248, 249, 250));
            tabela.getTableHeader().setForeground(COR_TEXTO);
        }

        if (scroll != null) {
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setBorder(BorderFactory.createLineBorder(COR_BORDA, 1, true));
        }
    }

    // =====================================================================
    // PONTO DE ENTRADA
    // =====================================================================

    public static void main(String[] args) throws Exception {
        try {
            Api.iniciar(System.getProperty("user.name"));
        } catch (Exception ex) {
            System.out.println("Aviso ao iniciar API: " + ex.getMessage());
        }

        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);

        // Atualização automática a cada 5 segundos (thread daemon não impede o
        // encerramento)
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "atualizacao-tabela");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(tela::carregarTabela, 5, 5, TimeUnit.SECONDS);
    }
}
