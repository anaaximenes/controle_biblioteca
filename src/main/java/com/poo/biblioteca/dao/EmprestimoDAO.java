package com.poo.biblioteca.dao;

import com.poo.biblioteca.Copia;
import com.poo.biblioteca.Emprestimo;
import com.poo.biblioteca.Funcionario;
import com.poo.biblioteca.Leitor;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO {
    private static final String URL = "jdbc:sqlite:BDBiblioteca.db";

    public EmprestimoDAO() {
        criarNovaTabela();
    }

    public void criarNovaTabela() {
        String sql = "CREATE TABLE IF NOT EXISTS emprestimo (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " leitor_id integer NOT NULL,\n"
                + " funcionario_id integer NOT NULL,\n"
                + " copia_id integer NOT NULL,\n"
                + " dataEmprestimo text NOT NULL,\n"
                + " dataPrevistaDevolucao text NOT NULL,\n"
                + " dataDevolucaoReal text,\n"
                + " valorMulta real DEFAULT 0,\n"
                + " FOREIGN KEY (leitor_id) REFERENCES leitor(id),\n"
                + " FOREIGN KEY (funcionario_id) REFERENCES funcionario(id),\n"
                + " FOREIGN KEY (copia_id) REFERENCES copia(id)\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Erro ao criar tabela emprestimo: " + e.getMessage());
        }
    }

    public void inserir(Emprestimo emprestimo) {
        String sql = "INSERT INTO emprestimo (leitor_id, funcionario_id, copia_id, dataEmprestimo, dataPrevistaDevolucao, dataDevolucaoReal, valorMulta) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, emprestimo.getLeitor().getId());
            pstmt.setInt(2, emprestimo.getFuncionario().getId());
            pstmt.setInt(3, emprestimo.getCopia().getId());
            pstmt.setString(4, emprestimo.getDataEmprestimo().toString());
            pstmt.setString(5, emprestimo.getDataPrevistaDevolucao().toString());

            if (emprestimo.getDataDevolucaoReal() != null) {
                pstmt.setString(6, emprestimo.getDataDevolucaoReal().toString());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            pstmt.setDouble(7, emprestimo.getValorMulta());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    emprestimo.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir empréstimo: " + e.getMessage());
        }
    }

    public List<Emprestimo> listarTodos() {
        List<Emprestimo> lista = new ArrayList<>();
        String sql = "SELECT * FROM emprestimo";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearEmprestimo(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar empréstimos: " + e.getMessage());
        }
        return lista;
    }

    public Emprestimo buscarPorId(int id) {
        String sql = "SELECT * FROM emprestimo WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmprestimo(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar empréstimo: " + e.getMessage());
        }
        return null;
    }

    public void atualizar(Emprestimo emprestimo) {
        String sql = "UPDATE emprestimo SET leitor_id = ?, funcionario_id = ?, copia_id = ?, "
                + "dataEmprestimo = ?, dataPrevistaDevolucao = ?, dataDevolucaoReal = ?, valorMulta = ? "
                + "WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, emprestimo.getLeitor().getId());
            pstmt.setInt(2, emprestimo.getFuncionario().getId());
            pstmt.setInt(3, emprestimo.getCopia().getId());
            pstmt.setString(4, emprestimo.getDataEmprestimo().toString());
            pstmt.setString(5, emprestimo.getDataPrevistaDevolucao().toString());

            if (emprestimo.getDataDevolucaoReal() != null) {
                pstmt.setString(6, emprestimo.getDataDevolucaoReal().toString());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            pstmt.setDouble(7, emprestimo.getValorMulta());
            pstmt.setInt(8, emprestimo.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar empréstimo: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM emprestimo WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao deletar empréstimo: " + e.getMessage());
        }
    }

    private Emprestimo mapearEmprestimo(ResultSet rs) throws SQLException {
        Emprestimo emp = new Emprestimo();
        emp.setId(rs.getInt("id"));

        Leitor leitor = new Leitor();
        leitor.setId(rs.getInt("leitor_id"));
        emp.setLeitor(leitor);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(rs.getInt("funcionario_id"));
        emp.setFuncionario(funcionario);

        Copia copia = new Copia();
        copia.setId(rs.getInt("copia_id"));
        emp.setCopia(copia);

        emp.setDataEmprestimo(LocalDate.parse(rs.getString("dataEmprestimo")));
        emp.setDataPrevistaDevolucao(LocalDate.parse(rs.getString("dataPrevistaDevolucao")));

        String dataDevReal = rs.getString("dataDevolucaoReal");
        if (dataDevReal != null) {
            emp.setDataDevolucaoReal(LocalDate.parse(dataDevReal));
        }

        emp.setValorMulta(rs.getDouble("valorMulta"));
        return emp;
    }
}
