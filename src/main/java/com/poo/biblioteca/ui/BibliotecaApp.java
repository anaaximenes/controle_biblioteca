package com.poo.biblioteca.ui;

import com.poo.biblioteca.Funcionario;

import javax.swing.*;
import java.util.List;

/**
 * Ponto de entrada executável para a Interface Gráfica (UI) da Biblioteca.
 */
public class BibliotecaApp {

    public static void main(String[] args) {
        // Registrar driver SQLite
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Throwable ignored) {}

        // Configuração visual limpa
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            BibliotecaService service = new BibliotecaService();

            // Garante que exista ao menos um funcionário para o primeiro login
            garantirFuncionarioPadrao(service);

            LoginFrame loginFrame = new LoginFrame(service);
            loginFrame.setVisible(true);
        });
    }

    private static void garantirFuncionarioPadrao(BibliotecaService service) {
        try {
            Funcionario admin = service.buscarFuncionarioPorLogin("admin");
            if (admin == null) {
                admin = new Funcionario();
                admin.setNome("Administrador do Sistema");
                admin.setCpf("000.000.000-00");
                admin.setEmail("admin@biblioteca.org");
                admin.setTelefone("(11) 99999-0000");
                admin.setCargo("Bibliotecário-Chefe");
                admin.setLogin("admin");
                admin.setSenha("admin123"); // Criptografa internamente via SegurancaUtil

                service.getFuncionarioDAO().inserir(admin);
                System.out.println("[Sistema] Usuário inicial criado com sucesso: Login 'admin' | Senha 'admin123'");
            }
        } catch (Exception ex) {
            System.err.println("Aviso ao verificar usuário padrão: " + ex.getMessage());
        }
    }
}
