package com.poo.biblioteca.ui;

import com.poo.biblioteca.Funcionario;
import com.poo.biblioteca.exception.SenhaInvalidaException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Tela de autenticação dos funcionários da biblioteca.
 * Apresenta estética clássica acolhedora em tons de madeira e pergaminho.
 */
public class LoginFrame extends JFrame {

    private final BibliotecaService service;
    private JTextField txtLogin;
    private JPasswordField txtSenha;

    public LoginFrame(BibliotecaService service) {
        this.service = service;
        initComponents();
    }

    private void initComponents() {
        setTitle("Biblioteca - Acesso ao Sistema");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new GridBagLayout());

        // Card central
        JPanel card = UIHelper.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 420));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TABLE_GRID, 1, true),
                new EmptyBorder(25, 25, 25, 25)
        ));

        // Ícone / Cabeçalho
        JLabel lblIcon = new JLabel("📚", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Biblioteca Central", SwingConstants.CENTER);
        lblTitulo.setFont(FONT_TITLE);
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Controle de Acervo & Circulação", SwingConstants.CENTER);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campos
        JLabel lblLogin = UIHelper.createLabel("Login de Acesso:");
        lblLogin.setFont(FONT_BODY_BOLD);
        lblLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtLogin = UIHelper.createTextField("Digite seu login");

        JLabel lblSenha = UIHelper.createLabel("Senha:");
        lblSenha.setFont(FONT_BODY_BOLD);
        lblSenha.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtSenha = UIHelper.createPasswordField("");

        // Ação ao pressionar Enter nos campos
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executarLogin();
                }
            }
        };
        txtLogin.addKeyListener(enterListener);
        txtSenha.addKeyListener(enterListener);

        // Botão Entrar
        JButton btnEntrar = UIHelper.createPrimaryButton("Entrar no Sistema");
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        btnEntrar.addActionListener(e -> executarLogin());

        JLabel lblDica = new JLabel("Credenciais padrão: admin / admin123", SwingConstants.CENTER);
        lblDica.setFont(FONT_SMALL);
        lblDica.setForeground(TEXT_PLACEHOLDER);
        lblDica.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Montagem do card
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(6));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(2));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(20));

        card.add(createFieldWrapper(lblLogin, txtLogin));
        card.add(Box.createVerticalStrut(10));
        card.add(createFieldWrapper(lblSenha, txtSenha));
        card.add(Box.createVerticalStrut(18));
        card.add(btnEntrar);
        card.add(Box.createVerticalStrut(10));
        card.add(lblDica);

        add(card);
    }

    private JPanel createFieldWrapper(JLabel label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT + 24));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        p.add(label, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void executarLogin() {
        String login = UIHelper.getFieldText(txtLogin, "Digite seu login");
        String senha = new String(txtSenha.getPassword());

        if (login.isBlank() || senha.isBlank()) {
            UIHelper.showWarning(this, "Por favor, preencha o login e a senha para continuar.");
            return;
        }

        try {
            Funcionario funcionario = service.buscarFuncionarioPorLogin(login);
            if (funcionario == null) {
                UIHelper.showError(this, "Funcionário não encontrado com o login: " + login);
                return;
            }

            // Invoca autenticação de domínio - lança SenhaInvalidaException se incorreta
            funcionario.autenticar(senha);

            UIHelper.showSuccess(this, "Bem-vindo(a), " + funcionario.getNome() + "!\nLogin realizado com sucesso.");
            dispose();

            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(funcionario, service);
                mainFrame.setVisible(true);
            });

        } catch (SenhaInvalidaException ex) {
            UIHelper.showError(this, "Falha na autenticação: " + ex.getMessage());
            txtSenha.setText("");
            txtSenha.requestFocus();
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro inesperado ao realizar login: " + ex.getMessage());
        }
    }
}
