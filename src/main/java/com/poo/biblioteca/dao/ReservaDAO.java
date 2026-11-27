package com.poo.biblioteca.dao;

import com.poo.biblioteca.Leitor;
import com.poo.biblioteca.Obra;
import com.poo.biblioteca.Reserva;
import com.poo.biblioteca.StatusReserva;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    private static final String URL = "jdbc:sqlite:BDBiblioteca.db";

    public ReservaDAO() {
        criarNovaTabela();
    }

    public void criarNovaTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS reserva (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " leitor_id integer NOT NULL,\n"
                + " obra_id integer NOT NULL,\n"
                + " dataReserva text NOT NULL,\n"
                + " dataExpiracao text NOT NULL,\n"
                + " status varchar(30) NOT NULL,\n"
                + " FOREIGN KEY (leitor_id) REFERENCES leitor(id),\n"
                + " FOREIGN KEY (obra_id) REFERENCES obra(id)\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabela reserva: " + e.getMessage());
        }
    }

    public void inserir(Reserva reserva) {
        String sql = "INSERT INTO reserva (leitor_id, obra_id, dataReserva, dataExpiracao, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, reserva.getLeitor().getId());
            pstmt.setInt(2, reserva.getObra().getId());
            pstmt.setString(3, reserva.getDataReserva().toString());
            pstmt.setString(4, reserva.getDataExpiracao().toString());
            pstmt.setString(5, reserva.getStatus().name());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    reserva.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir reserva: " + e.getMessage());
        }
    }

    public List<Reserva> listarTodos() {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reserva";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearReserva(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar reservas: " + e.getMessage());
        }
        return lista;
    }

    public Reserva buscarPorId(int id) {
        String sql = "SELECT * FROM reserva WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearReserva(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar reserva: " + e.getMessage());
        }
        return null;
    }

    public void atualizar(Reserva reserva) {
        String sql = "UPDATE reserva SET leitor_id = ?, obra_id = ?, dataReserva = ?, dataExpiracao = ?, status = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reserva.getLeitor().getId());
            pstmt.setInt(2, reserva.getObra().getId());
            pstmt.setString(3, reserva.getDataReserva().toString());
            pstmt.setString(4, reserva.getDataExpiracao().toString());
            pstmt.setString(5, reserva.getStatus().name());
            pstmt.setInt(6, reserva.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar reserva: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM reserva WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao deletar reserva: " + e.getMessage());
        }
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setId(rs.getInt("id"));

        Leitor leitor = new Leitor();
        leitor.setId(rs.getInt("leitor_id"));
        reserva.setLeitor(leitor);

        Obra obra = new Obra();
        obra.setId(rs.getInt("obra_id"));
        reserva.setObra(obra);

        reserva.setDataReserva(LocalDate.parse(rs.getString("dataReserva")));
        reserva.setDataExpiracao(LocalDate.parse(rs.getString("dataExpiracao")));
        reserva.setStatus(StatusReserva.valueOf(rs.getString("status")));
        return reserva;
    }
}
