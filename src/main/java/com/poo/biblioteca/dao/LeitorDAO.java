package com.poo.biblioteca.dao;

import com.poo.biblioteca.Leitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class LeitorDAO {
    private static final String URL = "jdbc:sqlite:exemplo.db";

    public LeitorDAO(){
        criarNovaTabela();
    }

    public void criarNovaTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS leitor (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " nome varchar(100) NOT NULL,\n"
                + " cpf varchar(15) NOT NULL, email varchar(70),\n"
                + "telefone varchar(11), matricula varchar(100), ativo int"
                + ");";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'Leitor' criada ou já existente.");
        } catch (Exception e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public int inserir(Leitor l) {
        String sql = "INSERT INTO leitor(nome, cpf, email, telefone, matricula, ativo) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, l.getNome());
            pstmt.setString(2, l.getCpf());
            pstmt.setString(3, l.getEmail());
            pstmt.setString(4, l.getTelefone());
            pstmt.setString(5, l.getMatricula());
            int ativoInt = l.isAtivo() ? 1 : 0;
            pstmt.setInt(6, ativoInt);

            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao inserir leitor: " + e.getMessage());
            return 0;
        }
    }

    public int alterar(Leitor l) {
        String sql = "update leitor set nome=?, cpf=?, email=?, telefone=?, matricula=?, ativo=? where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, l.getNome());
            pstmt.setString(2, l.getCpf());
            pstmt.setString(3, l.getEmail());
            pstmt.setString(4, l.getTelefone());
            pstmt.setString(5, l.getMatricula());
            int ativoInt = l.isAtivo() ? 1 : 0;
            pstmt.setInt(6, ativoInt);
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao alterar leitor: " + e.getMessage());
            return 0;
        }
    }

    public int remover(Leitor l) {
        String sql = "delete from leitor where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, l.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao remover leitor: " + e.getMessage());
            return 0;
        }
    }

    public Leitor carregar(int id) {
        Leitor l = new Leitor();
        String sql = "SELECT * FROM leitor where id=" + id;
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n--- Lista de leitores ---");
            if (rs.next()) {
                l.setId(id);
                l.setNome(rs.getString("nome"));
                l.setCpf(rs.getString("cpf"));
                l.setEmail(rs.getString("email"));
                l.setTelefone(rs.getString("telefone"));
                l.setMatricula(rs.getString("matricula"));
                int ativoInt = rs.getInt("ativo");
                boolean ativo = (ativoInt == 1);
                l.setAtivo(ativo);
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar leitor: " + e.getMessage());
        }
        return l;
    }
}