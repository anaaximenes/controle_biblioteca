package com.poo.biblioteca.dao;

import com.poo.biblioteca.Funcionario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FuncionarioDAO {
    private static final String URL = "jdbc:sqlite:BDBiblioteca.db";

    public FuncionarioDAO(){
        criarNovaTabela();
    }

    public void criarNovaTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS funcionario (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " nome varchar(100) NOT NULL,\n"
                + " cpf varchar(15) NOT NULL, email varchar(70),\n"
                + "telefone varchar(11), cargo varchar(100), login varchar(50),\n"
                + "senha varchar(255)"
                + ");";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'funcionario' criada ou já existente.");
        } catch (Exception e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public int inserir(Funcionario f) {
        String sql = "INSERT INTO funcionario(nome, cpf, email, telefone, cargo, login, senha) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, f.getNome());
            pstmt.setString(2, f.getCpf());
            pstmt.setString(3, f.getEmail());
            pstmt.setString(4, f.getTelefone());
            pstmt.setString(5, f.getCargo());
            pstmt.setString(6, f.getLogin());
            pstmt.setString(7, f.getSenha());

            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao inserir funcionario: " + e.getMessage());
            return 0;
        }
    }

    public int alterar(Funcionario f) {
        String sql = "update funcionario set nome=?, cpf=?, email=?, telefone=?, cargo=?, login=?, senha=? where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, f.getNome());
            pstmt.setString(2, f.getCpf());
            pstmt.setString(3, f.getEmail());
            pstmt.setString(4, f.getTelefone());
            pstmt.setString(5, f.getCargo());
            pstmt.setString(6, f.getLogin());
            pstmt.setString(7, f.getSenha());
            pstmt.setInt(8, f.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao alterar funcionario: " + e.getMessage());
            return 0;
        }
    }

    public int remover(Funcionario f) {
        String sql = "delete from funcionario where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, f.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao remover funcionario: " + e.getMessage());
            return 0;
        }
    }

    public Funcionario carregar(int id) {
        Funcionario f = new Funcionario();
        String sql = "SELECT * FROM funcionario where id=" + id;
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n--- Lista de funcionarios ---");
            if (rs.next()) {
                f.setId(id);
                f.setNome(rs.getString("nome"));
                f.setCpf(rs.getString("cpf"));
                f.setEmail(rs.getString("email"));
                f.setTelefone(rs.getString("telefone"));
                f.setCargo(rs.getString("cargo"));
                f.setLogin(rs.getString("login"));
                f.setSenhaHash(rs.getString("senha"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar funcionario: " + e.getMessage());
        }
        return f;
    }
}