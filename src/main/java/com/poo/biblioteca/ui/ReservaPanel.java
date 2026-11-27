package com.poo.biblioteca.ui;

import com.poo.biblioteca.Copia;
import com.poo.biblioteca.Leitor;
import com.poo.biblioteca.Obra;
import com.poo.biblioteca.Reserva;
import com.poo.biblioteca.StatusCopia;
import com.poo.biblioteca.StatusReserva;
import com.poo.biblioteca.exception.ReservaInvalidaException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Painel de gerenciamento do ciclo de vida das Reservas.
 * Demonstra a máquina de estados (ATIVA -> CONCLUIDA / CANCELADA / EXPIRADA)
 * com tratamento estrito de ReservaInvalidaException em modais.
 */
public class ReservaPanel extends JPanel {

    private final BibliotecaService service;
    private JTable tabela;
    private DefaultTableModel tableModel;

    private JComboBox<LeitorItem> cmbLeitor;
    private JComboBox<ObraItem> cmbObra;
    private JLabel lblReservaSelecionada;
    private Reserva reservaSelecionada = null;

    private static class LeitorItem {
        final int id;
        final String nome;
        final String matricula;

        LeitorItem(int id, String nome, String matricula) {
            this.id = id;
            this.nome = nome;
            this.matricula = matricula;
        }

        @Override
        public String toString() {
            return "[" + id + "] " + nome + " (" + matricula + ")";
        }
    }

    private static class ObraItem {
        final int id;
        final String titulo;
        /*final int copiasDisponiveis;*/

        ObraItem(int id, String titulo/*, int copiasDisponiveis*/) {
            this.id = id;
            this.titulo = titulo;
            /*this.copiasDisponiveis = copiasDisponiveis;*/
        }

        @Override
        public String toString() {
            return "[" + id + "] " + titulo;
        }
    }

    public ReservaPanel(BibliotecaService service) {
        this.service = service;
        initComponents();
        carregarCombos();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitulo = UIHelper.createTitleLabel("Máquina de Estados de Reservas");
        JLabel lblSub = new JLabel("Acompanhamento de filas de espera, prazos de 3 dias e efetivações");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Painel Central (Tabela na parte superior e Ações na inferior)
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 16));
        pnlCentro.setOpaque(false);

        // Tabela
        String[] colunas = {"ID Reserva", "Leitor", "Obra", "Data da Reserva", "Data Expiração", "Status Atual"};
        tableModel = new DefaultTableModel(colunas, 0);
        tabela = UIHelper.createStyledTable(tableModel);

        // Renderizador de Status
        tabela.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(FONT_BODY_BOLD);
                if (value != null) {
                    String st = value.toString();
                    if (st.equals(StatusReserva.ATIVA.name())) {
                        c.setForeground(STATUS_RESERVADA);
                        c.setText("● ATIVA (Aguardando)");
                    } else if (st.equals(StatusReserva.CONCLUIDA.name())) {
                        c.setForeground(STATUS_DISPONIVEL);
                        c.setText("CONCLUÍDA");
                    } else if (st.equals(StatusReserva.CANCELADA.name())) {
                        c.setForeground(BTN_DANGER);
                        c.setText("CANCELADA");
                    } else if (st.equals(StatusReserva.EXPIRADA.name())) {
                        c.setForeground(STATUS_EMPRESTADA);
                        c.setText("EXPIRADA");
                    }
                }
                return c;
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarReservaDaTabela();
            }
        });

        pnlCentro.add(UIHelper.wrapInScrollPane(tabela), BorderLayout.CENTER);

        // Painel Inferior: 2 Cards (Nova Reserva e Ações de Transição)
        JPanel pnlCards = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 220));

        // CARD 1: Nova Reserva
        JPanel cardNova = UIHelper.createCardPanel();
        cardNova.setLayout(new BoxLayout(cardNova, BoxLayout.Y_AXIS));

        JLabel lblNovaTit = new JLabel("Registrar Nova Reserva");
        lblNovaTit.setFont(FONT_SUBTITLE);
        lblNovaTit.setForeground(TEXT_PRIMARY);

        cmbLeitor = new JComboBox<>();
        cmbLeitor.setFont(FONT_INPUT);
        cmbLeitor.setBackground(BG_INPUT);
        cmbLeitor.setForeground(TEXT_PRIMARY);

        cmbObra = new JComboBox<>();
        cmbObra.setFont(FONT_INPUT);
        cmbObra.setBackground(BG_INPUT);
        cmbObra.setForeground(TEXT_PRIMARY);

        JButton btnCriar = UIHelper.createSuccessButton("Confirmar Reserva (+3 dias)");
        btnCriar.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        btnCriar.addActionListener(e -> criarNovaReserva());

        cardNova.add(lblNovaTit);
        cardNova.add(Box.createVerticalStrut(10));
        cardNova.add(UIHelper.createFormRow("Leitor:", cmbLeitor));
        cardNova.add(Box.createVerticalStrut(6));
        cardNova.add(UIHelper.createFormRow("Obra:", cmbObra));
        cardNova.add(Box.createVerticalStrut(12));
        cardNova.add(btnCriar);
        cardNova.add(Box.createVerticalGlue());

        // CARD 2: Ações de Estado
        JPanel cardAcoes = UIHelper.createCardPanel();
        cardAcoes.setLayout(new BoxLayout(cardAcoes, BoxLayout.Y_AXIS));

        JLabel lblAcoesTit = new JLabel("Transições de Estado da Reserva");
        lblAcoesTit.setFont(FONT_SUBTITLE);
        lblAcoesTit.setForeground(TEXT_PRIMARY);

        lblReservaSelecionada = new JLabel("Selecione uma reserva na tabela acima.");
        lblReservaSelecionada.setFont(FONT_BODY);
        lblReservaSelecionada.setForeground(TEXT_SECONDARY);

        JButton btnConcluir = UIHelper.createPrimaryButton("Efetivar / Concluir");
        btnConcluir.addActionListener(e -> concluirReserva());

        JButton btnCancelar = UIHelper.createButton("Cancelar Reserva", BTN_WARNING);
        btnCancelar.addActionListener(e -> cancelarReserva());

        JButton btnExcluir = UIHelper.createDangerButton("Excluir do Banco");
        btnExcluir.addActionListener(e -> excluirReserva());

        JPanel pnlBtnAcoes = new JPanel(new GridLayout(1, 3, 6, 0));
        pnlBtnAcoes.setOpaque(false);
        pnlBtnAcoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        pnlBtnAcoes.add(btnConcluir);
        pnlBtnAcoes.add(btnCancelar);
        pnlBtnAcoes.add(btnExcluir);

        cardAcoes.add(lblAcoesTit);
        cardAcoes.add(Box.createVerticalStrut(10));
        cardAcoes.add(lblReservaSelecionada);
        cardAcoes.add(Box.createVerticalStrut(16));
        cardAcoes.add(pnlBtnAcoes);
        cardAcoes.add(Box.createVerticalGlue());

        pnlCards.add(cardNova);
        pnlCards.add(cardAcoes);

        pnlCentro.add(pnlCards, BorderLayout.SOUTH);
        add(pnlCentro, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        carregarCombos();
        carregarTabela();
    }

    private void carregarCombos() {
        cmbLeitor.removeAllItems();
        cmbObra.removeAllItems();
        try {
            List<Leitor> leitores = service.listarLeitores();
            for (Leitor l : leitores) {
                if (l.isAtivo()) {
                    cmbLeitor.addItem(new LeitorItem(l.getId(), l.getNome(), l.getMatricula()));
                }
            }

            List<Obra> obras = service.listarObras();
            for (Obra o : obras) {
                cmbObra.addItem(new ObraItem(o.getId(), o.getTitulo()));
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar leitores e obras: " + ex.getMessage());
        }
    }

    private void carregarTabela() {
        tableModel.setRowCount(0);
        reservaSelecionada = null;
        lblReservaSelecionada.setText("Selecione uma reserva na tabela acima.");

        try {
            List<Reserva> lista = service.listarReservas();
            for (Reserva r : lista) {
                String nomeLeitor = "Leitor #" + r.getLeitor().getId();
                try {
                    Leitor l = service.getLeitorDAO().carregar(r.getLeitor().getId());
                    if (l != null && l.getNome() != null) {
                        nomeLeitor = l.getNome() + " (" + l.getIdentificadorFormatado() + ")";
                    }
                } catch (Exception ignored) {}

                String tituloObra = "Obra #" + r.getObra().getId();
                try {
                    Obra o = service.getObraDAO().carregar(r.getObra().getId());
                    if (o != null && o.getTitulo() != null) {
                        tituloObra = o.getTitulo();
                    }
                } catch (Exception ignored) {}

                tableModel.addRow(new Object[]{
                        r.getId(),
                        nomeLeitor,
                        tituloObra,
                        UIHelper.formatarDataBR(r.getDataReserva()),
                        UIHelper.formatarDataBR(r.getDataExpiracao()),
                        r.getStatus() != null ? r.getStatus().name() : "-"
                });
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao listar reservas: " + ex.getMessage());
        }
    }

    private void selecionarReservaDaTabela() {
        int row = tabela.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            reservaSelecionada = service.buscarReserva(id);
            if (reservaSelecionada != null) {
                lblReservaSelecionada.setText("<html><b>Reserva #" + reservaSelecionada.getId() + "</b> selecionada.<br>"
                        + "Status: <b>" + reservaSelecionada.getStatus() + "</b> | Expira em: "
                        + UIHelper.formatarDataBR(reservaSelecionada.getDataExpiracao()) + "</html>");
            }
        } else {
            reservaSelecionada = null;
            lblReservaSelecionada.setText("Selecione uma reserva na tabela acima.");
        }
    }

    private void liberarCopiaReservadaDaObra(int obraId) {
        Copia copiaReservada = service.buscarCopiaReservadaPorObra(obraId);
        if (copiaReservada != null && copiaReservada.getId() > 0) {
            copiaReservada.setStatus(StatusCopia.DISPONIVEL);
            service.getCopiaDAO().alterar(copiaReservada);
        }
    }

    private void criarNovaReserva() {
        LeitorItem leitorItem = (LeitorItem) cmbLeitor.getSelectedItem();
        ObraItem obraItem = (ObraItem) cmbObra.getSelectedItem();

        if (leitorItem == null || obraItem == null) {
            UIHelper.showWarning(this, "Selecione o leitor e uma obra com cópia disponível para registrar a reserva.");
            return;
        }

        try {
            // Valida se a obra já possui alguma reserva ATIVA
            boolean possuiReservaAtiva = false;
            for (Reserva r : service.listarReservas()) {
                if (r.getObra() != null
                        && r.getObra().getId() == obraItem.id
                        && r.getStatus() == StatusReserva.ATIVA) {
                    possuiReservaAtiva = true;
                    break;
                }
            }

            if (possuiReservaAtiva) {
                UIHelper.showWarning(this, "Esta obra já possui uma reserva ATIVA no momento.");
                return;
            }

            Leitor leitor = new Leitor();
            leitor.setId(leitorItem.id);
            leitor.setNome(leitorItem.nome);

            Obra obra = new Obra();
            obra.setId(obraItem.id);
            obra.setTitulo(obraItem.titulo);

            // Criação via construtor de negócio que define ATIVA e validade de 3 dias
            Reserva reserva = new Reserva(leitor, obra, LocalDate.now());
            service.getReservaDAO().inserir(reserva);

            UIHelper.showSuccess(this, "Reserva registrada com sucesso!\n\n"
                    + "Código: #" + reserva.getId() + "\n"
                    + "Leitor: " + leitorItem.nome + "\n"
                    + "Obra: " + obraItem.titulo + "\n"
                    + "Data de Expiração: " + UIHelper.formatarDataBR(reserva.getDataExpiracao()) + " (Prazo: 3 dias)");

            carregarCombos();
            carregarTabela();

        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao registrar reserva: " + ex.getMessage());
        }
    }

    private void concluirReserva() {
        if (reservaSelecionada == null) {
            UIHelper.showWarning(this, "Selecione uma reserva ativa na tabela para concluir.");
            return;
        }

        boolean estavaAtiva = reservaSelecionada.getStatus() == StatusReserva.ATIVA;

        try {
            // Executa regra de negócio da máquina de estados
            // Lança ReservaInvalidaException se já cancelada, já concluída ou se expirou o prazo
            reservaSelecionada.concluir();

            // Persiste o novo status no banco
            service.getReservaDAO().atualizar(reservaSelecionada);

            if (estavaAtiva && reservaSelecionada.getObra() != null) {
                liberarCopiaReservadaDaObra(reservaSelecionada.getObra().getId());
            }

            UIHelper.showSuccess(this, "Reserva #" + reservaSelecionada.getId()
                    + " concluída com sucesso!\nO exemplar agora pode ser emprestado ao leitor.");

            carregarCombos();
            carregarTabela();

        } catch (ReservaInvalidaException ex) {
            // Se expirou durante a tentativa de conclusão, persiste o status EXPIRADA assumido
            try {
                service.getReservaDAO().atualizar(reservaSelecionada);
                if (estavaAtiva && reservaSelecionada.getStatus() == StatusReserva.EXPIRADA
                        && reservaSelecionada.getObra() != null) {
                    liberarCopiaReservadaDaObra(reservaSelecionada.getObra().getId());
                }
            } catch (Exception ignored) {}

            UIHelper.showError(this, "Aviso de Regra de Negócio:\n" + ex.getMessage()
                    + "\nStatus assumido: " + reservaSelecionada.getStatus());
            carregarCombos();
            carregarTabela();

        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao concluir reserva: " + ex.getMessage());
        }
    }

    private void cancelarReserva() {
        if (reservaSelecionada == null) {
            UIHelper.showWarning(this, "Selecione uma reserva ativa na tabela para cancelar.");
            return;
        }

        boolean confirmou = UIHelper.showConfirm(this,
                "Deseja realmente cancelar a reserva #" + reservaSelecionada.getId() + "?");
        if (!confirmou) return;

        boolean estavaAtiva = reservaSelecionada.getStatus() == StatusReserva.ATIVA;

        try {
            // Executa regra de negócio da máquina de estados
            reservaSelecionada.cancelar();
            service.getReservaDAO().atualizar(reservaSelecionada);

            if (estavaAtiva && reservaSelecionada.getObra() != null) {
                liberarCopiaReservadaDaObra(reservaSelecionada.getObra().getId());
            }

            UIHelper.showSuccess(this, "Reserva #" + reservaSelecionada.getId() + " cancelada com sucesso.");
            carregarCombos();
            carregarTabela();

        } catch (ReservaInvalidaException ex) {
            UIHelper.showError(this, "Não foi possível cancelar:\n" + ex.getMessage());
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao cancelar reserva: " + ex.getMessage());
        }
    }

    private void excluirReserva() {
        if (reservaSelecionada == null) {
            UIHelper.showWarning(this, "Selecione uma reserva na tabela para excluir.");
            return;
        }

        boolean confirmou = UIHelper.showConfirm(this,
                "Tem certeza que deseja apagar permanentemente o registro da reserva #"
                        + reservaSelecionada.getId() + "?");
        if (!confirmou) return;

        try {
            if (reservaSelecionada.getStatus() == StatusReserva.ATIVA && reservaSelecionada.getObra() != null) {
                liberarCopiaReservadaDaObra(reservaSelecionada.getObra().getId());
            }
            service.getReservaDAO().deletar(reservaSelecionada.getId());
            UIHelper.showSuccess(this, "Registro da reserva apagado com sucesso.");
            carregarCombos();
            carregarTabela();
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao deletar reserva: " + ex.getMessage());
        }
    }
}
