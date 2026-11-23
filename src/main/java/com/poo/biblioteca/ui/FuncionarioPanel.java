package com.poo.biblioteca.ui;

import com.poo.biblioteca.Funcionario;
import com.poo.biblioteca.exception.SenhaInvalidaException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Painel de gerenciamento de Funcionários da Biblioteca.
 * Controla cargos, logins e credenciais criptografadas via SHA-256.
 */
public class FuncionarioPanel extends JPanel {

    private final BibliotecaService service;
    private JTable tabela;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtNome;
    private JTextField txtCpf;
    private JTextField txtCargo;
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JTextField txtEmail;
    private JTextField txtTelefone;
    private JLabel lblHashAtual;

    private String hashSenhaExistente = null;

    public FuncionarioPanel(BibliotecaService service) {
        this.service = service;
        initComponents();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitulo = UIHelper.createTitleLabel("👨‍💼 Gestão de Funcionários");
        JLabel lblSub = new JLabel("Controle de acessos, cargos e operadores do sistema");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Divisão Tabela e Formulário
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.60);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Tabela
        String[] colunas = {"ID", "Identificador", "Nome", "CPF", "Cargo", "Login", "E-mail", "Telefone"};
        tableModel = new DefaultTableModel(colunas, 0);
        tabela = UIHelper.createStyledTable(tableModel);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioDaTabela();
            }
        });

        JPanel pnlTabela = new JPanel(new BorderLayout(0, 8));
        pnlTabela.setOpaque(false);
        pnlTabela.add(UIHelper.wrapInScrollPane(tabela), BorderLayout.CENTER);

        JButton btnRecarregar = UIHelper.createButton("🔄 Atualizar Lista", BTN_PRIMARY);
        btnRecarregar.addActionListener(e -> carregarTabela());
        JPanel pnlAcoesTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlAcoesTab.setOpaque(false);
        pnlAcoesTab.add(btnRecarregar);
        pnlTabela.add(pnlAcoesTab, BorderLayout.SOUTH);

        splitPane.setLeftComponent(pnlTabela);

        // Formulário (Card)
        JPanel pnlCard = UIHelper.createCardPanel();
        pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));

        JLabel lblFormTitulo = new JLabel("Dados do Funcionário");
        lblFormTitulo.setFont(FONT_SUBTITLE);
        lblFormTitulo.setForeground(TEXT_PRIMARY);

        JLabel lblObrigatorio = new JLabel("<html><font color='#C62828'>*</font> Campos obrigatórios</html>");
        lblObrigatorio.setFont(FONT_SMALL);
        lblObrigatorio.setForeground(TEXT_SECONDARY);

        txtId = UIHelper.createTextField("");
        txtId.setEditable(false);
        txtId.setBackground(BG_MAIN);

        txtNome = UIHelper.createTextField("Nome do colaborador");
        txtCpf = UIHelper.createTextField("000.000.000-00");
        UIHelper.aplicarMascaraCpf(txtCpf);
        txtCargo = UIHelper.createTextField("Ex: Bibliotecário-Chefe");
        txtLogin = UIHelper.createTextField("usuario.login");
        txtSenha = UIHelper.createPasswordField("");
        txtEmail = UIHelper.createTextField("colaborador@biblioteca.org");
        txtTelefone = UIHelper.createTextField("(00) 00000-0000");
        UIHelper.aplicarMascaraTelefone(txtTelefone);

        lblHashAtual = new JLabel("Nota: Deixe a senha em branco na edição para manter a atual.");
        lblHashAtual.setFont(FONT_SMALL);
        lblHashAtual.setForeground(ACCENT);

        pnlCard.add(lblFormTitulo);
        pnlCard.add(Box.createVerticalStrut(4));
        pnlCard.add(lblObrigatorio);
        pnlCard.add(Box.createVerticalStrut(12));
        pnlCard.add(UIHelper.createFormRow("Código (ID):", txtId));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Nome Completo:", txtNome, true));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("CPF:", txtCpf, true));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Cargo:", txtCargo));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Login de Acesso:", txtLogin, true));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Senha:", txtSenha, true));
        pnlCard.add(Box.createVerticalStrut(4));
        pnlCard.add(lblHashAtual);
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("E-mail:", txtEmail));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Telefone:", txtTelefone));
        pnlCard.add(Box.createVerticalStrut(16));

        // Botões
        JPanel pnlBotoes = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlBotoes.setOpaque(false);
        pnlBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));

        JButton btnNovo = UIHelper.createButton("Novo", BTN_PRIMARY);
        btnNovo.addActionListener(e -> limparFormulario());

        JButton btnSalvar = UIHelper.createSuccessButton("Salvar");
        btnSalvar.addActionListener(e -> salvarFuncionario());

        JButton btnExcluir = UIHelper.createDangerButton("Excluir");
        btnExcluir.addActionListener(e -> excluirFuncionario());

        pnlBotoes.add(btnNovo);
        pnlBotoes.add(btnSalvar);
        pnlBotoes.add(btnExcluir);

        pnlCard.add(pnlBotoes);
        pnlCard.add(Box.createVerticalGlue());

        splitPane.setRightComponent(pnlCard);
        add(splitPane, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        carregarTabela();
    }

    private void carregarTabela() {
        tableModel.setRowCount(0);
        try {
            List<Funcionario> lista = service.listarFuncionarios();
            for (Funcionario f : lista) {
                tableModel.addRow(new Object[]{
                        f.getId(),
                        f.getIdentificadorFormatado(),
                        f.getNome(),
                        UIHelper.formatarCpf(f.getCpf()),
                        f.getCargo(),
                        f.getLogin(),
                        f.getEmail(),
                        UIHelper.formatarTelefone(f.getTelefone())
                });
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar lista de funcionários: " + ex.getMessage());
        }
    }

    private void preencherFormularioDaTabela() {
        int row = tabela.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Funcionario f = service.getFuncionarioDAO().carregar(id);
            if (f != null && f.getId() > 0) {
                txtId.setText(String.valueOf(f.getId()));
                txtNome.setText(f.getNome() != null ? f.getNome() : "");
                txtCpf.setText(UIHelper.formatarCpf(f.getCpf()));
                txtCargo.setText(f.getCargo() != null ? f.getCargo() : "");
                txtLogin.setText(f.getLogin() != null ? f.getLogin() : "");
                txtEmail.setText(f.getEmail() != null ? f.getEmail() : "");
                txtTelefone.setText(UIHelper.formatarTelefone(f.getTelefone()));
                txtSenha.setText("");
                hashSenhaExistente = f.getSenha();
                lblHashAtual.setText("Senha criptografada no banco (SHA-256). Preencha apenas para alterar.");
            }
        }
    }

    private void limparFormulario() {
        tabela.clearSelection();
        txtId.setText("");
        txtNome.setText("");
        txtCpf.setText("");
        txtCargo.setText("");
        txtLogin.setText("");
        txtSenha.setText("");
        txtEmail.setText("");
        txtTelefone.setText("");
        hashSenhaExistente = null;
        lblHashAtual.setText("Nota: A senha é protegida por hash SHA-256.");
    }

    private void salvarFuncionario() {
        String nome = txtNome.getText().trim();
        String cpf = txtCpf.getText().trim();
        String cargo = txtCargo.getText().trim();
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());
        String email = txtEmail.getText().trim();
        String telefone = txtTelefone.getText().trim();

        String cpfDigitos = UIHelper.apenasDigitos(cpf);
        String telDigitos = UIHelper.apenasDigitos(telefone);

        if (nome.isEmpty() || cpfDigitos.isEmpty() || login.isEmpty()) {
            UIHelper.showWarning(this, "Os campos obrigatórios (*) 'Nome Completo', 'CPF' e 'Login de Acesso' devem ser preenchidos.");
            return;
        }

        if (cpfDigitos.length() != 11) {
            UIHelper.showWarning(this, "Informe um CPF válido contendo 11 dígitos.");
            return;
        }

        if (!telDigitos.isEmpty() && telDigitos.length() < 10) {
            UIHelper.showWarning(this, "Informe um telefone válido com DDD (10 ou 11 dígitos) ou deixe o campo vazio.");
            return;
        }

        cpf = UIHelper.formatarCpf(cpfDigitos);
        telefone = telDigitos.isEmpty() ? "" : UIHelper.formatarTelefone(telDigitos);

        boolean isEdicao = !txtId.getText().isBlank();

        if (!isEdicao && senha.isBlank()) {
            UIHelper.showWarning(this, "Para novos funcionários, a senha de acesso é obrigatória.");
            return;
        }

        try {
            Funcionario f = new Funcionario();
            f.setNome(nome);
            f.setCpf(cpf);
            f.setCargo(cargo);
            f.setLogin(login);
            f.setEmail(email);
            f.setTelefone(telefone);

            if (!senha.isBlank()) {
                f.setSenha(senha); // Gera hash SHA-256 internamente
            } else if (isEdicao && hashSenhaExistente != null) {
                f.setSenhaHash(hashSenhaExistente); // Mantém o hash original
            }

            if (isEdicao) {
                f.setId(Integer.parseInt(txtId.getText()));
                int linhas = service.getFuncionarioDAO().alterar(f);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Funcionário \"" + f.getNome() + "\" atualizado com sucesso!");
                } else {
                    UIHelper.showWarning(this, "Aviso: a atualização não afetou nenhum registro.");
                }
            } else {
                int linhas = service.getFuncionarioDAO().inserir(f);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Funcionário cadastrado com sucesso!\nIdentificador: "
                            + f.getIdentificadorFormatado());
                } else {
                    UIHelper.showError(this, "Não foi possível inserir o funcionário.");
                }
            }

            carregarTabela();
            limparFormulario();

        } catch (SenhaInvalidaException ex) {
            UIHelper.showError(this, "Erro de validação de senha: " + ex.getMessage());
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao salvar funcionário: " + ex.getMessage());
        }
    }

    private void excluirFuncionario() {
        if (txtId.getText().isBlank()) {
            UIHelper.showWarning(this, "Selecione um funcionário na tabela para excluir.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        String nome = txtNome.getText();

        boolean confirmou = UIHelper.showConfirm(this, "Tem certeza que deseja remover o funcionário:\n"
                + nome + " (ID: " + id + ")?");
        if (!confirmou) return;

        try {
            Funcionario f = new Funcionario();
            f.setId(id);
            int linhas = service.getFuncionarioDAO().remover(f);
            if (linhas > 0) {
                UIHelper.showSuccess(this, "Funcionário removido com sucesso!");
                carregarTabela();
                limparFormulario();
            } else {
                UIHelper.showError(this, "Não foi possível remover o funcionário do banco.");
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao excluir funcionário: " + ex.getMessage());
        }
    }
}
