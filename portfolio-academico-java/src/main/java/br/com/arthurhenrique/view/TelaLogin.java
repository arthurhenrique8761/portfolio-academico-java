package br.com.arthurhenrique.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import br.com.arthurhenrique.log.LogManager;
import br.com.arthurhenrique.model.Usuario;
import br.com.arthurhenrique.service.AuthService;

public class TelaLogin extends JDialog {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_CADASTRO = "CADASTRO";

    private static final Color COR_FUNDO = new Color(236, 239, 241);
    private static final Color COR_CARD = Color.WHITE;
    private static final Color COR_PRIMARIA = new Color(38, 50, 56);
    private static final Color COR_PRIMARIA_HOVER = new Color(55, 71, 79);
    private static final Color COR_BORDA = new Color(207, 216, 220);
    private static final Color COR_TEXTO = new Color(38, 50, 56);
    private static final Color COR_SUBTITULO = new Color(84, 110, 122);
    private static final Color COR_ERRO = new Color(198, 40, 40);
    private static final Color COR_SUCESSO = new Color(46, 125, 50);

    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_MSG = new Font("Segoe UI", Font.PLAIN, 12);

    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel painelCards;

    // Campos Login
    private JTextField campoUsuarioLogin;
    private JPasswordField campoSenhaLogin;
    private JLabel lblMensagemLogin;
    private JButton btnEntrar;

    // Campos Cadastro
    private JTextField campoNomeCadastro;
    private JTextField campoUsuarioCadastro;
    private JPasswordField campoSenhaCadastro;
    private JLabel lblMensagemCadastro;
    private JButton btnCadastrar;

    // Dados da sessão autenticada
    private String usuarioLogado;
    private String nomeUsuarioLogado;
    private String perfilUsuarioLogado;

    public TelaLogin(JFrame parent) {
        super(parent, "Sistema de Projetos - Acesso", true);
        this.authService = new AuthService();

        try {
            this.authService.inicializar();
        } catch (Exception e) {
            LogManager.error("SWING_AUTH_INIT", e.getMessage());
        }

        setSize(430, 470);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(COR_FUNDO);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        painelCards = new JPanel(cardLayout);
        painelCards.setBackground(COR_FUNDO);
        painelCards.setBorder(new EmptyBorder(24, 24, 24, 24));

        painelCards.add(criarCardLogin(), CARD_LOGIN);
        painelCards.add(criarCardCadastro(), CARD_CADASTRO);

        add(painelCards, BorderLayout.CENTER);
        mostrarLogin();
    }

    private JPanel criarCardLogin() {
        JPanel card = criarContainerCard();

        JLabel titulo = new JLabel("Sistema de Projetos");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Entre com suas credenciais para acessar");
        subtitulo.setFont(FONTE_SUBTITULO);
        subtitulo.setForeground(COR_SUBTITULO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        campoUsuarioLogin = new JTextField();
        campoSenhaLogin = new JPasswordField();
        estilizarCampo(campoUsuarioLogin);
        estilizarCampo(campoSenhaLogin);

        JPanel form = new JPanel(new GridLayout(4, 1, 0, 6));
        form.setBackground(COR_CARD);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));
        form.add(criarLabel("Usuário"));
        form.add(campoUsuarioLogin);
        form.add(criarLabel("Senha"));
        form.add(campoSenhaLogin);

        lblMensagemLogin = new JLabel(" ");
        lblMensagemLogin.setFont(FONTE_MSG);
        lblMensagemLogin.setForeground(COR_ERRO);
        lblMensagemLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnEntrar = new JButton("Entrar");
        estilizarBotaoPrincipal(btnEntrar);
        btnEntrar.addActionListener(e -> realizarLogin());

        JLabel linkCadastro = criarLinkAlternancia(
                "<html>Não tem conta? <b>Cadastre-se</b></html>",
                this::mostrarCadastro
        );

        card.add(titulo);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitulo);
        card.add(Box.createVerticalStrut(18));
        card.add(form);
        card.add(Box.createVerticalStrut(8));
        card.add(lblMensagemLogin);
        card.add(Box.createVerticalStrut(10));
        card.add(btnEntrar);
        card.add(Box.createVerticalStrut(14));
        card.add(linkCadastro);

        return card;
    }

    private JPanel criarCardCadastro() {
        JPanel card = criarContainerCard();

        JLabel titulo = new JLabel("Criar Conta");
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(COR_TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Cadastre um novo usuário para acessar o sistema");
        subtitulo.setFont(FONTE_SUBTITULO);
        subtitulo.setForeground(COR_SUBTITULO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        campoNomeCadastro = new JTextField();
        campoUsuarioCadastro = new JTextField();
        campoSenhaCadastro = new JPasswordField();
        estilizarCampo(campoNomeCadastro);
        estilizarCampo(campoUsuarioCadastro);
        estilizarCampo(campoSenhaCadastro);

        JPanel form = new JPanel(new GridLayout(6, 1, 0, 5));
        form.setBackground(COR_CARD);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 225));
        form.add(criarLabel("Nome completo"));
        form.add(campoNomeCadastro);
        form.add(criarLabel("Usuário"));
        form.add(campoUsuarioCadastro);
        form.add(criarLabel("Senha (mínimo 4 caracteres)"));
        form.add(campoSenhaCadastro);

        lblMensagemCadastro = new JLabel(" ");
        lblMensagemCadastro.setFont(FONTE_MSG);
        lblMensagemCadastro.setForeground(COR_ERRO);
        lblMensagemCadastro.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCadastrar = new JButton("Cadastrar");
        estilizarBotaoPrincipal(btnCadastrar);
        btnCadastrar.addActionListener(e -> realizarCadastro());

        JLabel linkLogin = criarLinkAlternancia(
                "<html>Já tem conta? <b>Fazer login</b></html>",
                this::mostrarLogin
        );

        card.add(titulo);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitulo);
        card.add(Box.createVerticalStrut(14));
        card.add(form);
        card.add(Box.createVerticalStrut(6));
        card.add(lblMensagemCadastro);
        card.add(Box.createVerticalStrut(8));
        card.add(btnCadastrar);
        card.add(Box.createVerticalStrut(12));
        card.add(linkLogin);

        return card;
    }

    private void mostrarLogin() {
        lblMensagemLogin.setText(" ");
        cardLayout.show(painelCards, CARD_LOGIN);
        getRootPane().setDefaultButton(btnEntrar);
        campoUsuarioLogin.requestFocusInWindow();
    }

    private void mostrarCadastro() {
        lblMensagemCadastro.setText(" ");
        cardLayout.show(painelCards, CARD_CADASTRO);
        getRootPane().setDefaultButton(btnCadastrar);
        campoNomeCadastro.requestFocusInWindow();
    }

    private void realizarLogin() {
        lblMensagemLogin.setText(" ");
        String login = campoUsuarioLogin.getText().trim();
        char[] senhaChars = campoSenhaLogin.getPassword();
        String senha = new String(senhaChars);
        Arrays.fill(senhaChars, '\0');

        if (login.isEmpty() || senha.isEmpty()) {
            lblMensagemLogin.setForeground(COR_ERRO);
            lblMensagemLogin.setText("Preencha o usuário e a senha.");
            return;
        }

        try {
            Optional<?> resultado = authService.autenticar(login, senha);
            if (resultado.isPresent()) {
                Object valor = resultado.get();
                if (valor instanceof java.util.Map<?, ?> mapa) {
                    Object u = mapa.get("usuario");
                    Object n = mapa.get("nome");
                    Object p = mapa.get("perfil");

                    this.usuarioLogado = (u != null && !u.toString().isBlank())
                            ? u.toString().trim()
                            : login;
                    this.nomeUsuarioLogado = (n != null && !n.toString().isBlank())
                            ? n.toString().trim()
                            : this.usuarioLogado;
                    this.perfilUsuarioLogado = (p != null && !p.toString().isBlank())
                            ? p.toString().trim()
                            : "USER";
                } else if (valor instanceof Usuario usuario) {
                    this.usuarioLogado = (usuario.getUsuario() != null && !usuario.getUsuario().isBlank())
                            ? usuario.getUsuario().trim()
                            : login;
                    this.nomeUsuarioLogado = (usuario.getNome() != null && !usuario.getNome().isBlank())
                            ? usuario.getNome().trim()
                            : this.usuarioLogado;
                    this.perfilUsuarioLogado = (usuario.getPerfil() != null && !usuario.getPerfil().isBlank())
                            ? usuario.getPerfil().trim()
                            : "USER";
                } else {
                    this.usuarioLogado = login;
                    this.nomeUsuarioLogado = login;
                    this.perfilUsuarioLogado = "USER";
                }
                dispose();
            } else {
                lblMensagemLogin.setForeground(COR_ERRO);
                lblMensagemLogin.setText("Usuário ou senha inválidos.");
                campoSenhaLogin.setText("");
                campoSenhaLogin.requestFocusInWindow();
            }
        } catch (Exception ex) {
            LogManager.error("SWING_LOGIN", ex.getMessage());
            lblMensagemLogin.setForeground(COR_ERRO);
            lblMensagemLogin.setText("Erro ao autenticar: " + ex.getMessage());
        }
    }

    private void realizarCadastro() {
        lblMensagemCadastro.setText(" ");
        String nome = campoNomeCadastro.getText().trim();
        String usuario = campoUsuarioCadastro.getText().trim();
        char[] senhaChars = campoSenhaCadastro.getPassword();
        String senha = new String(senhaChars);
        Arrays.fill(senhaChars, '\0');

        try {
            authService.cadastrar(nome, usuario, senha);
            campoNomeCadastro.setText("");
            campoUsuarioCadastro.setText("");
            campoSenhaCadastro.setText("");

            mostrarLogin();
            campoUsuarioLogin.setText(usuario);
            lblMensagemLogin.setForeground(COR_SUCESSO);
            lblMensagemLogin.setText("Conta criada com sucesso! Digite sua senha para entrar.");
            campoSenhaLogin.requestFocusInWindow();
        } catch (IllegalArgumentException ex) {
            lblMensagemCadastro.setForeground(COR_ERRO);
            lblMensagemCadastro.setText(ex.getMessage());
        } catch (Exception ex) {
            LogManager.error("SWING_CADASTRO_USUARIO", ex.getMessage());
            lblMensagemCadastro.setForeground(COR_ERRO);
            lblMensagemCadastro.setText("Erro ao cadastrar usuário: " + ex.getMessage());
        }
    }

    private JPanel criarContainerCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1, true),
                new EmptyBorder(24, 24, 20, 24)
        ));
        return card;
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(FONTE_LABEL);
        label.setForeground(COR_TEXTO);
        return label;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(FONTE_CAMPO);
        campo.setForeground(COR_TEXTO);
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void estilizarBotaoPrincipal(JButton botao) {
        botao.setFont(FONTE_BOTAO);
        botao.setBackground(COR_PRIMARIA);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setAlignmentX(Component.LEFT_ALIGNMENT);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        botao.setBorder(new EmptyBorder(10, 14, 10, 14));
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(COR_PRIMARIA_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(COR_PRIMARIA);
            }
        });
    }

    private JLabel criarLinkAlternancia(String html, Runnable acao) {
        JLabel link = new JLabel(html, SwingConstants.CENTER);
        link.setFont(FONTE_SUBTITULO);
        link.setForeground(COR_SUBTITULO);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.setAlignmentX(Component.LEFT_ALIGNMENT);
        link.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                acao.run();
            }
        });
        return link;
    }

    public boolean isAutenticado() {
        return usuarioLogado != null && !usuarioLogado.isBlank();
    }

    public String getUsuarioLogado() {
        return usuarioLogado;
    }

    public String getNomeUsuarioLogado() {
        return nomeUsuarioLogado;
    }

    public String getPerfilUsuarioLogado() {
        return perfilUsuarioLogado;
    }
}
