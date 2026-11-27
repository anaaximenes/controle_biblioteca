package com.poo.biblioteca.dao;

import com.poo.biblioteca.Obra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ObraDAO {
    private static final String URL = "jdbc:sqlite:exemplo.db";

    public ObraDAO(){
        criarNovaTabela();
    }

    public void criarNovaTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS obra (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " titulo varchar(100) NOT NULL,\n"
                + " autor varchar(100) NOT NULL, anoPublicacao integer,\n"
                + " isbn varchar(20)"
                + ");";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            boolean temColunaIsbn = false;
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(obra)")) {
                while (rs.next()) {
                    if ("isbn".equalsIgnoreCase(rs.getString("name"))) {
                        temColunaIsbn = true;
                        break;
                    }
                }
            }
            if (!temColunaIsbn) {
                stmt.execute("ALTER TABLE obra ADD COLUMN isbn varchar(20)");
            }
            System.out.println("Tabela 'Obra' criada ou já existente.");
        } catch (Exception e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public int inserir(Obra o) {
        String sql = "INSERT INTO obra(titulo, autor, anoPublicacao, isbn) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, o.getTitulo());
            pstmt.setString(2, o.getAutor());
            pstmt.setInt(3, o.getAnoPublicacao());
            pstmt.setString(4, o.getIsbn());

            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao inserir obra: " + e.getMessage());
            return 0;
        }
    }

    public int alterar(Obra o) {
        String sql = "update obra set titulo=?, autor=?, anoPublicacao=?, isbn=? where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, o.getTitulo());
            pstmt.setString(2, o.getAutor());
            pstmt.setInt(3, o.getAnoPublicacao());
            pstmt.setString(4, o.getIsbn());
            pstmt.setInt(5, o.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao alterar obra: " + e.getMessage());
            return 0;
        }
    }

    public int remover(Obra o) {
        String sql = "delete from obra where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, o.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao remover obra: " + e.getMessage());
            return 0;
        }
    }

    public Obra carregar(int id) {
        Obra o = new Obra();
        String sql = "SELECT * FROM obra where id=" + id;
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n--- Lista de obras ---");
            if (rs.next()) {
                o.setId(id);
                o.setTitulo(rs.getString("titulo"));
                o.setAutor(rs.getString("autor"));
                o.setAnoPublicacao(rs.getInt("anoPublicacao"));
                o.setIsbn(rs.getString("isbn"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar obra: " + e.getMessage());
        }
        return o;
    }
}