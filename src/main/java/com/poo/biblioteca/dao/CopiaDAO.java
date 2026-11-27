package com.poo.biblioteca.dao;

import com.poo.biblioteca.Copia;
import com.poo.biblioteca.Obra;
import com.poo.biblioteca.StatusCopia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CopiaDAO {
    private static final String URL = "jdbc:sqlite:BDBiblioteca.db";

    public CopiaDAO(){
        criarNovaTabela();
    }

    public void criarNovaTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS copia (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " codigoBarras varchar(30) NOT NULL UNIQUE,\n"
                + " status varchar(30) NOT NULL, obraId integer NOT NULL,"
                + "FOREIGN KEY (obraId) REFERENCES obra(id)"
                + ");";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'Copia' criada ou já existente.");
        } catch (Exception e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public int inserir(Copia c) {
        String sql = "INSERT INTO copia(codigoBarras, status, obraId) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getCodigoBarras());
            pstmt.setString(2, c.getStatus().name());
            pstmt.setInt(3, c.getObra().getId());

            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao inserir copia: " + e.getMessage());
            return 0;
        }
    }

    public int alterar(Copia c) {
        String sql = "update copia set codigoBarras=?, status=?, obraId=? where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getCodigoBarras());
            pstmt.setString(2, c.getStatus().name());
            pstmt.setInt(3, c.getObra().getId());
            pstmt.setInt(4, c.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao alterar copia: " + e.getMessage());
            return 0;
        }
    }

    public int remover(Copia o) {
        String sql = "delete from copia where id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, o.getId());
            return pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao remover obra: " + e.getMessage());
            return 0;
        }
    }

    public Copia carregar(int id) {
        Copia c = new Copia();
        String sql = "SELECT * FROM copia where id=" + id;
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            System.out.println("\n--- Lista de copias ---");
            if (rs.next()) {
                c.setId(id);
                c.setCodigoBarras(rs.getString("codigoBarras"));
                c.setStatus(StatusCopia.valueOf(rs.getString("status")));
                c.setObra(new Obra());
                c.getObra().setId(rs.getInt("obraId"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar copia: " + e.getMessage());
        }
        return c;
    }
}