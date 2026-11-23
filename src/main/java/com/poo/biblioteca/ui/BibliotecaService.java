package com.poo.biblioteca.ui;

import com.poo.biblioteca.*;
import com.poo.biblioteca.dao.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de serviço que encapsula os DAOs existentes e adiciona
 * métodos de listagem (listarTodos) para as entidades que não possuem
 * esse recurso nos DAOs originais (Leitor, Funcionario, Obra, Copia).
 *
 * Nenhum DAO existente é alterado — esta classe apenas os reutiliza e
 * complementa com consultas de listagem via JDBC direto.
 */
public class BibliotecaService {

    private static final String URL = "jdbc:sqlite:exemplo.db";

    private final LeitorDAO leitorDAO;
    private final FuncionarioDAO funcionarioDAO;
    private final ObraDAO obraDAO;
    private final CopiaDAO copiaDAO;
    private final ReservaDAO reservaDAO;
    private final EmprestimoDAO emprestimoDAO;

    public BibliotecaService() {
        this.leitorDAO = new LeitorDAO();
        this.funcionarioDAO = new FuncionarioDAO();
        this.obraDAO = new ObraDAO();
        this.copiaDAO = new CopiaDAO();
        this.reservaDAO = new ReservaDAO();
        this.emprestimoDAO = new EmprestimoDAO();
    }

    // ── Acesso aos DAOs ──
    public LeitorDAO getLeitorDAO()           { return leitorDAO; }
    public FuncionarioDAO getFuncionarioDAO()  { return funcionarioDAO; }
    public ObraDAO getObraDAO()               { return obraDAO; }
    public CopiaDAO getCopiaDAO()             { return copiaDAO; }
    public ReservaDAO getReservaDAO()         { return reservaDAO; }
    public EmprestimoDAO getEmprestimoDAO()   { return emprestimoDAO; }

    // ── Listagem de Leitores ──
    public List<Leitor> listarLeitores() {
        List<Leitor> lista = new ArrayList<>();
        String sql = "SELECT * FROM leitor ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Leitor l = new Leitor();
                l.setId(rs.getInt("id"));
                l.setNome(rs.getString("nome"));
                l.setCpf(rs.getString("cpf"));
                l.setEmail(rs.getString("email"));
                l.setTelefone(rs.getString("telefone"));
                l.setMatricula(rs.getString("matricula"));
                l.setAtivo(rs.getInt("ativo") == 1);
                lista.add(l);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar leitores: " + e.getMessage());
        }
        return lista;
    }

    // ── Listagem de Funcionários ──
    public List<Funcionario> listarFuncionarios() {
        List<Funcionario> lista = new ArrayList<>();
        String sql = "SELECT * FROM funcionario ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Funcionario f = new Funcionario();
                f.setId(rs.getInt("id"));
                f.setNome(rs.getString("nome"));
                f.setCpf(rs.getString("cpf"));
                f.setEmail(rs.getString("email"));
                f.setTelefone(rs.getString("telefone"));
                f.setCargo(rs.getString("cargo"));
                f.setLogin(rs.getString("login"));
                f.setSenhaHash(rs.getString("senha"));
                lista.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar funcionarios: " + e.getMessage());
        }
        return lista;
    }

    // ── Buscar Funcionário por Login ──
    public Funcionario buscarFuncionarioPorLogin(String login) {
        String sql = "SELECT * FROM funcionario WHERE login = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Funcionario f = new Funcionario();
                f.setId(rs.getInt("id"));
                f.setNome(rs.getString("nome"));
                f.setCpf(rs.getString("cpf"));
                f.setEmail(rs.getString("email"));
                f.setTelefone(rs.getString("telefone"));
                f.setCargo(rs.getString("cargo"));
                f.setLogin(rs.getString("login"));
                f.setSenhaHash(rs.getString("senha"));
                return f;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar funcionário por login: " + e.getMessage());
        }
        return null;
    }

    // ── Listagem de Obras ──
    public List<Obra> listarObras() {
        List<Obra> lista = new ArrayList<>();
        String sql = "SELECT * FROM obra ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Obra o = new Obra();
                o.setId(rs.getInt("id"));
                o.setTitulo(rs.getString("titulo"));
                o.setAutor(rs.getString("autor"));
                o.setAnoPublicacao(rs.getInt("anoPublicacao"));
                lista.add(o);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar obras: " + e.getMessage());
        }
        return lista;
    }

    // ── Listagem de Cópias ──
    public List<Copia> listarCopias() {
        List<Copia> lista = new ArrayList<>();
        String sql = "SELECT c.*, o.titulo AS obraTitulo FROM copia c LEFT JOIN obra o ON c.obraId = o.id ORDER BY c.id";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Copia c = new Copia();
                c.setId(rs.getInt("id"));
                c.setCodigoBarras(rs.getString("codigoBarras"));
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    c.setStatus(StatusCopia.valueOf(statusStr));
                }
                Obra obra = new Obra();
                obra.setId(rs.getInt("obraId"));
                obra.setTitulo(rs.getString("obraTitulo"));
                c.setObra(obra);
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar copias: " + e.getMessage());
        }
        return lista;
    }

    // ── Listagem de Cópias Disponíveis ──
    public List<Copia> listarCopiasDisponiveis() {
        List<Copia> lista = new ArrayList<>();
        String sql = "SELECT c.*, o.titulo AS obraTitulo FROM copia c LEFT JOIN obra o ON c.obraId = o.id WHERE c.status = 'DISPONIVEL' ORDER BY c.id";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Copia c = new Copia();
                c.setId(rs.getInt("id"));
                c.setCodigoBarras(rs.getString("codigoBarras"));
                c.setStatus(StatusCopia.DISPONIVEL);
                Obra obra = new Obra();
                obra.setId(rs.getInt("obraId"));
                obra.setTitulo(rs.getString("obraTitulo"));
                c.setObra(obra);
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cópias disponíveis: " + e.getMessage());
        }
        return lista;
    }

    // ── Wrappers de delegação para os DAOs ──

    // Reservas
    public List<Reserva> listarReservas()         { return reservaDAO.listarTodos(); }
    public Reserva buscarReserva(int id)           { return reservaDAO.buscarPorId(id); }

    // Empréstimos
    public List<Emprestimo> listarEmprestimos()    { return emprestimoDAO.listarTodos(); }
    public Emprestimo buscarEmprestimo(int id)      { return emprestimoDAO.buscarPorId(id); }
}
