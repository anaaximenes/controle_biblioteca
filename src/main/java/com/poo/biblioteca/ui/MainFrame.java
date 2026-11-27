package com.poo.biblioteca.ui;

import com.poo.biblioteca.Funcionario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Janela Principal da Aplicação com Navegação por Barra Lateral.
 * Estilo visual acolhedor em tons de madeira e pergaminho.
 */
public class MainFrame extends JFrame {

    private final Funcionario funcionarioLogado;
    private final BibliotecaService service;

    private JPanel pnlContent;
    private CardLayout cardLayout;
    private final Map<String, JPanel> navButtons = new LinkedHashMap<>();
    private final Map<String, Runnable> refreshActions = new LinkedHashMap<>();
    private String selectedKey = "EMPRESTIMOS";

    public MainFrame(Funcionario funcionarioLogado, BibliotecaService service) {
        this.funcionarioLogado = funcionarioLogado;
        this.service = service;
        initComponents();
    }

    private void initComponents() {
        setTitle("Biblioteca - Sistema Integrado de Controle de Acervo e Circulação");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1240, 780);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);

        // Barra Lateral (Sidebar)
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // Área Central (CardLayout)
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(BG_MAIN);

        // Criação e registro das telas
        EmprestimoPanel emprestimoPanel = new EmprestimoPanel(service, funcionarioLogado);
        ReservaPanel reservaPanel = new ReservaPanel(service);
        LeitorPanel leitorPanel = new LeitorPanel(service);
        ObraPanel obraPanel = new ObraPanel(service);
        CopiaPanel copiaPanel = new CopiaPanel(service);
        FuncionarioPanel funcionarioPanel = new FuncionarioPanel(service);

        pnlContent.add(emprestimoPanel, "EMPRESTIMOS");
        pnlContent.add(reservaPanel, "RESERVAS");
        pnlContent.add(leitorPanel, "LEITORES");
        pnlContent.add(obraPanel, "OBRAS");
        pnlContent.add(copiaPanel, "COPIAS");
        pnlContent.add(funcionarioPanel, "FUNCIONARIOS");

        refreshActions.put("EMPRESTIMOS", emprestimoPanel::recarregarDados);
        refreshActions.put("RESERVAS", reservaPanel::recarregarDados);
        refreshActions.put("LEITORES", leitorPanel::recarregarDados);
        refreshActions.put("OBRAS", obraPanel::recarregarDados);
        refreshActions.put("COPIAS", copiaPanel::recarregarDados);
        refreshActions.put("FUNCIONARIOS", funcionarioPanel::recarregarDados);

        root.add(pnlContent, BorderLayout.CENTER);
        setContentPane(root);

        // Seleção inicial
        selectNav("EMPRESTIMOS");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, getHeight()));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(0x27, 0x18, 0x15)));

        // Topo da Sidebar: Logo e Usuário
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(22, 18, 22, 18));

        JLabel lblLogo = new JLabel("BIBLIOTECA");
        lblLogo.setFont(FONT_SIDEBAR_TITLE);
        lblLogo.setForeground(Color.WHITE);

        String nome = (funcionarioLogado != null) ? funcionarioLogado.getNome() : "Operador";
        String cargo = (funcionarioLogado != null && funcionarioLogado.getCargo() != null)
                ? funcionarioLogado.getCargo() : "Administração";

        JLabel lblNomeUser = new JLabel(nome);
        lblNomeUser.setFont(FONT_BODY_BOLD);
        lblNomeUser.setForeground(new Color(0xD7, 0xCC, 0xC8));

        JLabel lblCargo = new JLabel(cargo);
        lblCargo.setFont(FONT_SMALL);
        lblCargo.setForeground(new Color(0xA1, 0x88, 0x7F));

        topPanel.add(lblLogo);
        topPanel.add(Box.createVerticalStrut(14));
        topPanel.add(lblNomeUser);
        topPanel.add(Box.createVerticalStrut(2));
        topPanel.add(lblCargo);

        sidebar.add(topPanel, BorderLayout.NORTH);

        // Meio: Menu de Itens
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 8, 10, 8));

        addNavItem(menuPanel, "EMPRESTIMOS", "Empréstimos e Multas");
        addNavItem(menuPanel, "RESERVAS", "Reservas");
        addNavItem(menuPanel, "LEITORES", "Leitores");
        addNavItem(menuPanel, "OBRAS", "Obras do Acervo");
        addNavItem(menuPanel, "COPIAS", "Cópias");
        addNavItem(menuPanel, "FUNCIONARIOS", "Funcionários");

        menuPanel.add(Box.createVerticalGlue());
        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Rodapé: Botão Sair
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(14, 14, 18, 14));

        JButton btnSair = new JButton("Encerrar Sessão");
        btnSair.setFont(FONT_BUTTON);
        btnSair.setForeground(new Color(0xEF, 0x9A, 0x9A));
        btnSair.setBackground(new Color(0x4E, 0x34, 0x2E));
        btnSair.setFocusPainted(false);
        btnSair.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x8D, 0x6E, 0x63), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        btnSair.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSair.addActionListener(e -> logout());

        bottomPanel.add(btnSair, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel container, String key, String title) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(true);
        item.setBackground(SIDEBAR_BG);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setPreferredSize(new Dimension(SIDEBAR_WIDTH - 16, 44));
        item.setBorder(new EmptyBorder(8, 14, 8, 14));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_SIDEBAR);
        lbl.setForeground(SIDEBAR_TEXT);
        item.add(lbl, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(selectedKey)) {
                    item.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!key.equals(selectedKey)) {
                    item.setBackground(SIDEBAR_BG);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectNav(key);
            }
        });

        navButtons.put(key, item);
        container.add(item);
        container.add(Box.createVerticalStrut(4));
    }

    private void selectNav(String key) {
        selectedKey = key;
        Runnable refresh = refreshActions.get(key);
        if (refresh != null) {
            refresh.run();
        }
        cardLayout.show(pnlContent, key);

        for (Map.Entry<String, JPanel> entry : navButtons.entrySet()) {
            boolean isSelected = entry.getKey().equals(key);
            JPanel p = entry.getValue();
            p.setBackground(isSelected ? SIDEBAR_SELECTED : SIDEBAR_BG);
            JLabel l = (JLabel) p.getComponent(0);
            l.setFont(isSelected ? FONT_BODY_BOLD : FONT_SIDEBAR);
            l.setForeground(isSelected ? Color.WHITE : SIDEBAR_TEXT);
        }
    }

    private void logout() {
        boolean confirmou = UIHelper.showConfirm(this,
                "Deseja realmente encerrar a sessão e voltar à tela de login?");
        if (!confirmou) return;

        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(service);
            loginFrame.setVisible(true);
        });
    }
}
