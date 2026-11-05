package br.com.arthurhenrique.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import br.com.arthurhenrique.model.Projeto;
import br.com.arthurhenrique.service.ProjetoService;

// TO DO
//

public class TelaPrincipal extends JFrame {

    private ProjetoService service;

    // campos de texto
    private JTextField campoNome;
    private JTextField campoDescricao;

    // tabela de opções
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    // tabela
    private DefaultTableModel modelo;
    private JTable tabela;

    // botões
    private JButton botaoCadastrar;
    private JButton botaoLimpar;
    private JButton botaoExcluir;
    private JButton botaoAlterar;

    public TelaPrincipal() throws Exception {
        // configurações da janela
        setTitle("Sistema de Projetos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // chamada dos métodos criam os elementos da janela (modularização de tarefas)

        service = new ProjetoService();

        /*
         * os metodos estão sem nome pois como a classe atual herda de JFrame, os
         * métodos do JFrame passam a ser
         * dessa classe, sem a necessidade de instânciar um objeto JFrame
         */
        criarComponentes();
        criarEventos();

        service.carregar();
        carregarTabela();
    }

    public void criarComponentes() {

        // definição e instanciação dos elementos da janela
        campoNome = new JTextField(30);

        campoDescricao = new JTextField(30);

        comboCategoria = new JComboBox<>();
        comboCategoria.addItem("Web");
        comboCategoria.addItem("Software");
        comboCategoria.addItem("Mobile");
        comboCategoria.addItem("Outro");

        comboStatus = new JComboBox<>();
        comboStatus.addItem("Planejado");
        comboStatus.addItem("Em desenvolvimento");
        comboStatus.addItem("Concluído");

        botaoCadastrar = new JButton("Cadastrar");
        botaoLimpar = new JButton("Limpar");
        botaoExcluir = new JButton("Exclur");
        botaoAlterar = new JButton("Alterar");

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");
        tabela = new JTable(modelo);

        // instanciação da janela e inserção dos componentes (em ordem)
        JPanel painelFormulario = new JPanel();

        painelFormulario.add(new JLabel("Nome: "));
        painelFormulario.add(campoNome);

        painelFormulario.add(new JLabel("Descrição: "));
        painelFormulario.add(campoDescricao);

        painelFormulario.add(new JLabel("Categoria: "));
        painelFormulario.add(comboCategoria);

        painelFormulario.add(new JLabel("Status: "));
        painelFormulario.add(comboStatus);

        painelFormulario.add(botaoCadastrar);
        painelFormulario.add(botaoLimpar);
        painelFormulario.add(botaoExcluir);
        painelFormulario.add(botaoAlterar);

        setLayout(new BoxLayout(
                getContentPane(),
                BoxLayout.Y_AXIS));

        add(painelFormulario);
        add(new JScrollPane(tabela));
    }

    public void criarEventos() {
        botaoCadastrar.addActionListener(e -> {
            try {
                cadastrar();
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Erro: " + e1.getMessage());
            }
        });

        botaoLimpar.addActionListener(e -> limparFormulário());

        botaoExcluir.addActionListener(e -> excluirProjeto());

        botaoAlterar.addActionListener(e -> alterarProjeto());
    }

    public void limparFormulário() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocus();
    }

    public void cadastrar() throws Exception {
        int id = service.gerarId();
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do projeto");
            return;
        }

        Projeto projeto = new Projeto(id, nome, descricao, categoria, status);

        service.adicionar(projeto);

        service.salvar();

        JOptionPane.showMessageDialog(this, "Projeto Cadastrado: " + nome);

        carregarTabela();
        limparFormulário();
    }

    public void excluirProjeto() {
        int linhaSelecionada = tabela.getSelectedRow();

        if (linhaSelecionada != -1) {
            int option = JOptionPane.showConfirmDialog(this, "Um projeto será deletado.\nEstá certo disso?",
                    "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option == 0) {
                boolean excluir = service.removerPorId(linhaSelecionada + 1);
                if (excluir) {
                    try {
                        service.salvar();
                        carregarTabela();
                        JOptionPane.showMessageDialog(null, "Projeto excluido com sucesso", "Sucesso",
                                JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(null, "Erro ao salvar no disco", "Erro",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Não foi possivel excluir o projeto", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    public void alterarProjeto() {
        /*
         * selecionar um preencher os dados no formulario e perguntar se deve alterar.
         * o programa deve reconhecer se há algum projeto selecionado e, ao clicar em
         * alterar, perguntar se tem certeza
         */

        int linhaSelecionada = tabela.getSelectedRow();

        if (linhaSelecionada != -1) {
            try {
                int id = linhaSelecionada + 1;
                String nome = campoNome.getText();
                String descricao = campoDescricao.getText();
                String categoria = comboCategoria.getSelectedItem().toString();
                String status = comboStatus.getSelectedItem().toString();

                if (nome.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Informe o nome do projeto");
                    return;
                }

                int option = JOptionPane.showConfirmDialog(this, "Um projeto será alterado.\nEstá certo disso?",
                        "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (option == 0) {
                    Projeto projetoAtualizado = new Projeto(id, nome, descricao, categoria, status);

                    service.alterar(projetoAtualizado);
                    service.salvar();
                    carregarTabela();

                    carregarTabela();

                    JOptionPane.showMessageDialog(this, "Projeto alterado com sucesso", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Não foi possivel alterar o projeto");
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    public void carregarTabela() {
        modelo.setRowCount(0);

        for (Projeto projeto : service.listar()) {
            modelo.addRow(new Object[] {
                    projeto.getId(),
                    projeto.getNome(),
                    projeto.getCategoria(),
                    projeto.getStatus()
            });
        }

    }

    public static void main(String[] args) throws Exception {
        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);
    }
}
