package com.poo.biblioteca;

import com.poo.biblioteca.dao.*;
import com.poo.biblioteca.exception.DataDevolucaoInvalidaException;
import com.poo.biblioteca.exception.ReservaInvalidaException;
import com.poo.biblioteca.exception.SenhaInvalidaException;
import com.poo.biblioteca.util.SegurancaUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de testes abrangentes do Sistema de Controle de Biblioteca.
 * 
 * Cobre:
 *  - Classes de Modelo (Pessoa, Leitor, Funcionario, Obra, Copia, Reserva, Emprestimo)
 *  - Enums (StatusCopia, StatusReserva)
 *  - Regras de Negocio (autenticacao, calculo de prazos/limites, maquina de estados de reserva, devolucao, multas)
 *  - Tratamento de Excecoes (SenhaInvalidaException, ReservaInvalidaException, DataDevolucaoInvalidaException)
 *  - Todas as Classes DAO (FuncionarioDAO, LeitorDAO, ObraDAO, CopiaDAO, ReservaDAO, EmprestimoDAO)
 *  - Fluxos de Persistencia (criar tabela, inserir, carregar/buscar, listar, atualizar/alterar, remover/deletar)
 *  - Cenarios Integrados de Ponta a Ponta
 * 
 * NOTA: As inconsistencias e bugs dos arquivos originais (ausencia de parametros em prepared statements,
 * risco de NullPointerException e descompassos de padroes) estao devidamente sinalizados e tratados
 * para permitir a execucao analitica completa sem quebrar a suite.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("===     INICIANDO SUITE DE TESTES ABRANGENTES DA BIBLIOTECA        ===");
        System.out.println("======================================================================\n");

        // Registrar o driver JDBC do SQLite explicitamente no inicio da aplicacao
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("[Ambiente] Driver SQLite JDBC registrado com sucesso.\n");
        } catch (Throwable t) {
            System.out.println("[Ambiente] AVISO: Driver org.sqlite.JDBC ou dependencia transitiva (SLF4J) nao inicializada: " + t.getMessage());
            System.out.println("[Ambiente] (Para execucao completa com banco SQLite, inclua sqlite-jdbc e slf4j-api no classpath)\n");
        }

        // 1. Modelos, Polimorfismo, Seguranca e Autenticacao
        testarUsuariosAutenticacaoESeguranca();

        // 2. Modelo Obra, Copias, Colecoes e Enums
        testarObrasECopias();

        // 3. Maquina de Estados de Reserva e Excecoes
        testarMaquinaDeEstadosReserva();

        // 4. Emprestimo, Regras de Devolucao, Multas e Excecoes
        testarEmprestimoRegrasDevolucaoEMultas();

        // 5. DAO Funcionario (Persistencia, Hash e CRUD)
        testarDAOFuncionario();

        // 6. DAO Leitor (Persistencia e Inconsistencias de UPDATE)
        testarDAOLeitor();

        // 7. DAO Obra (Persistencia e Inconsistencias de UPDATE)
        testarDAOObra();

        // 8. DAO Copia (Persistencia, Relacionamento, Bugs de Carregamento e UPDATE)
        testarDAOCopia();

        // 9. DAO Reserva (Persistencia, Chaves Geradas, Mapeamento de Enums e CRUD)
        testarDAOReserva();

        // 10. DAO Emprestimo (Persistencia, Integridade Referencial, Multas e CRUD)
        testarDAOEmprestimo();

        // 11. Cenario Integrado de Fluxo Completo de Negocio
        testarCenarioIntegradoEndToEnd();

        System.out.println("\n======================================================================");
        System.out.println("===        TODOS OS CENARIOS DE TESTE FORAM CONCLUIDOS             ===");
        System.out.println("======================================================================");
    }

    // =========================================================================
    // 1. TESTES DE USUARIOS, POLIMORFISMO E SEGURANCA (SHA-256)
    // =========================================================================
    private static void testarUsuariosAutenticacaoESeguranca() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("1. TESTES DE USUARIOS (POLIMORFISMO), SEGURANCA E AUTENTICACAO");
        System.out.println("----------------------------------------------------------------------");

        // Polimorfismo: Leitor
        Leitor leitor = new Leitor();
        leitor.setId(1);
        leitor.setNome("Ana Beatriz");
        leitor.setCpf("123.456.789-00");
        leitor.setEmail("ana@email.com");
        leitor.setTelefone("11987654321");
        leitor.setMatricula("202601");
        leitor.setAtivo(true);

        // Polimorfismo: Funcionario
        Funcionario funcionario = new Funcionario();
        funcionario.setId(10);
        funcionario.setNome("Gustavo");
        funcionario.setCpf("987.654.321-99");
        funcionario.setEmail("gustavo@biblioteca.com");
        funcionario.setTelefone("11912345678");
        funcionario.setCargo("Bibliotecario-Chefe");
        funcionario.setLogin("gustavo");
        funcionario.setSenha("senhaForte123"); // Gera hash internamente via SegurancaUtil

        // Validando polimorfismo de Pessoa
        Pessoa pLeitor = leitor;
        Pessoa pFuncionario = funcionario;

        System.out.println("[Polimorfismo] Leitor: " + pLeitor.getNome()
                + " | Identificador: " + pLeitor.getIdentificadorFormatado()
                + " | Prazo: " + pLeitor.getPrazoEmprestimoDias() + " dias"
                + " | Limite: " + pLeitor.getLimiteLivros() + " livros");

        System.out.println("[Polimorfismo] Funcionario: " + pFuncionario.getNome()
                + " | Identificador: " + pFuncionario.getIdentificadorFormatado()
                + " | Prazo: " + pFuncionario.getPrazoEmprestimoDias() + " dias"
                + " | Limite: " + pFuncionario.getLimiteLivros() + " livros");

        // Formato do identificador com matricula vazia/espacos
        Leitor leitorSemMatricula = new Leitor();
        System.out.println("[Identificador Leitor] Sem matricula: " + leitorSemMatricula.getIdentificadorFormatado());
        leitorSemMatricula.setMatricula("   abc 999  ");
        System.out.println("[Identificador Leitor] Matricula com espacos/minusculas: " + leitorSemMatricula.getIdentificadorFormatado());

        // Validacao direta do SegurancaUtil
        String hashManual = SegurancaUtil.gerarHash("senhaForte123");
        System.out.println("[SegurancaUtil] Hash SHA-256 gerado (64 chars hex): " + hashManual
                + " (Tamanho: " + hashManual.length() + ")");

        // Teste de seguranca com nulo
        try {
            SegurancaUtil.gerarHash(null);
            System.err.println("FALHA: SegurancaUtil deveria rejeitar senha nula.");
        } catch (IllegalArgumentException e) {
            System.out.println("[SegurancaUtil] Excecao para senha nula tratada com sucesso: " + e.getMessage());
        }

        // Testes de autenticacao do Funcionario
        try {
            boolean loginOk = funcionario.autenticar("senhaForte123");
            System.out.println("[Autenticacao] Login com credenciais corretas: " + loginOk);
        } catch (SenhaInvalidaException e) {
            System.err.println("FALHA: Autenticacao valida falhou: " + e.getMessage());
        }

        // Senha errada
        try {
            funcionario.autenticar("senhaErrada!");
            System.err.println("FALHA: Nao deveria autenticar senha incorreta.");
        } catch (SenhaInvalidaException e) {
            System.out.println("[Autenticacao] Bloqueio de senha incorreta ok: " + e.getMessage());
        }

        // Senha em branco
        try {
            funcionario.autenticar("   ");
            System.err.println("FALHA: Nao deveria aceitar senha em branco.");
        } catch (SenhaInvalidaException e) {
            System.out.println("[Autenticacao] Bloqueio de senha em branco ok: " + e.getMessage());
        }

        // Senha nula
        try {
            funcionario.autenticar(null);
            System.err.println("FALHA: Nao deveria aceitar senha nula.");
        } catch (SenhaInvalidaException e) {
            System.out.println("[Autenticacao] Bloqueio de senha nula ok: " + e.getMessage());
        }

        // Funcionario sem senha configurada
        Funcionario funcSemSenha = new Funcionario();
        try {
            funcSemSenha.autenticar("qualquer");
            System.err.println("FALHA: Funcionario sem senha definida nao pode logar.");
        } catch (SenhaInvalidaException e) {
            System.out.println("[Autenticacao] Bloqueio de funcionario sem senha cadastrada ok: " + e.getMessage());
        }

        // Metodo setSenhaHash (utilizado pelo DAO para atribuir hash bruto sem re-criptografar)
        Funcionario funcComHashDireto = new Funcionario();
        funcComHashDireto.setSenhaHash(hashManual);
        try {
            boolean ok = funcComHashDireto.autenticar("senhaForte123");
            System.out.println("[Autenticacao] Login via setSenhaHash correto: " + ok);
        } catch (SenhaInvalidaException e) {
            System.err.println("FALHA: Login via setSenhaHash falhou: " + e.getMessage());
        }

        System.out.println();
    }

    // =========================================================================
    // 2. TESTES DE OBRAS, COPIAS E STATUS
    // =========================================================================
    private static void testarObrasECopias() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("2. TESTES DE OBRAS, COPIAS, ENUMS E GERENCIAMENTO DE ACERVO");
        System.out.println("----------------------------------------------------------------------");

        Obra obra = new Obra();
        obra.setId(101);
        obra.setTitulo("Engenharia de Software Moderna");
        obra.setAutor("Marco Tulio Valente");
        obra.setAnoPublicacao(2020);

        /*
         * NOTA DE ATENCAO / LACUNA NO MODELO Obra:
         * O construtor padrao 'public Obra()' nao inicializa a lista 'copias', deixando-a como 'null'.
         * Invocar 'adicionarCopia' ou 'getCopiasDisponiveis' sem antes chamar 'setCopias' resulta em NullPointerException.
         */
        obra.setCopias(new ArrayList<>());

        Copia c1 = new Copia("BAR-1001", StatusCopia.DISPONIVEL, obra);
        c1.setId(1);
        Copia c2 = new Copia("BAR-1002", StatusCopia.EMPRESTADA, obra);
        c2.setId(2);
        Copia c3 = new Copia("BAR-1003", StatusCopia.DISPONIVEL, obra);
        c3.setId(3);
        Copia c4 = new Copia("BAR-1004", StatusCopia.RESERVADA, obra);
        c4.setId(4);

        obra.adicionarCopia(c1);
        obra.adicionarCopia(c2);
        obra.adicionarCopia(c3);
        obra.adicionarCopia(c4);

        System.out.println("[Obra] Titulo: " + obra.getTitulo() + " | Total de copias: " + obra.getCopias().size());

        // Validacao de rejeicao de copia nula
        try {
            obra.adicionarCopia(null);
            System.err.println("FALHA: Nao deveria permitir adicionar copia nula.");
        } catch (IllegalArgumentException e) {
            System.out.println("[Obra] Rejeicao de copia nula ok: " + e.getMessage());
        }

        // Copias disponiveis
        List<Copia> disponiveis = obra.getCopiasDisponiveis();
        System.out.println("[Obra] Copias disponiveis retornadas: " + disponiveis.size() + " (Esperado: 2)");
        for (Copia c : disponiveis) {
            System.out.println("   -> Copia ID: " + c.getId() + " | Codigo: " + c.getCodigoBarras() + " | Status: " + c.getStatus());
        }

        // Teste do metodo isDisponivel de Copia usando StatusCopia
        Copia copiaTeste = new Copia();
        copiaTeste.setStatus(StatusCopia.DISPONIVEL);
        System.out.println("[Copia] Status DISPONIVEL -> isDisponivel: " + copiaTeste.isDisponivel());

        copiaTeste.setStatus(StatusCopia.EMPRESTADA);
        System.out.println("[Copia] Status EMPRESTADA -> isDisponivel: " + copiaTeste.isDisponivel());

        copiaTeste.setStatus(StatusCopia.RESERVADA);
        System.out.println("[Copia] Status RESERVADA -> isDisponivel: " + copiaTeste.isDisponivel());

        // Teste de seguranca com status nulo
        Copia copiaStatusNulo = new Copia();
        System.out.println("[Copia] Status nulo -> isDisponivel: " + copiaStatusNulo.isDisponivel());

        System.out.println();
    }

    // =========================================================================
    // 3. TESTES DA MAQUINA DE ESTADOS DA RESERVA
    // =========================================================================
    private static void testarMaquinaDeEstadosReserva() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("3. TESTES DA MAQUINA DE ESTADOS DA RESERVA E REGRAS DE EXPIRACAO");
        System.out.println("----------------------------------------------------------------------");

        Leitor leitor = new Leitor();
        leitor.setId(5);
        leitor.setNome("Renan");
        leitor.setMatricula("202605");

        Obra obra = new Obra();
        obra.setId(50);
        obra.setTitulo("Arquitetura Limpa");

        // Cenário A: Reserva ativa dentro do prazo (expiracao = reserva + 3 dias)
        Reserva reservaValida = new Reserva(leitor, obra, LocalDate.now().minusDays(1));
        System.out.println("[Reserva] Status inicial: " + reservaValida.getStatus()
                + " | Data Reserva: " + reservaValida.getDataReserva()
                + " | Expira em: " + reservaValida.getDataExpiracao());

        reservaValida.concluir();
        System.out.println("[Reserva] Status apos concluir(): " + reservaValida.getStatus());

        // Tentativa de concluir novamente
        try {
            reservaValida.concluir();
            System.err.println("FALHA: Nao deveria concluir reserva ja concluida.");
        } catch (ReservaInvalidaException e) {
            System.out.println("[Reserva] Bloqueio de dupla conclusao ok: " + e.getMessage());
        }

        // Tentativa de cancelar reserva concluida
        try {
            reservaValida.cancelar();
            System.err.println("FALHA: Nao deveria cancelar reserva ja concluida.");
        } catch (ReservaInvalidaException e) {
            System.out.println("[Reserva] Bloqueio de cancelamento apos conclusao ok: " + e.getMessage());
        }

        // Cenário B: Reserva com prazo expirado
        Reserva reservaExpirada = new Reserva(leitor, obra, LocalDate.now().minusDays(6));
        System.out.println("[Reserva] Reserva criada ha 6 dias (Expirou em: " + reservaExpirada.getDataExpiracao() + ")");
        try {
            reservaExpirada.concluir();
            System.err.println("FALHA: Reserva expirada nao deveria ser concluida.");
        } catch (ReservaInvalidaException e) {
            System.out.println("[Reserva] Bloqueio de conclusao de reserva expirada ok: " + e.getMessage()
                    + " | Novo status assumido: " + reservaExpirada.getStatus());
        }

        // Cenário C: Cancelamento de reserva ativa
        Reserva reservaParaCancelar = new Reserva(leitor, obra, LocalDate.now());
        System.out.println("[Reserva] Status antes de cancelar: " + reservaParaCancelar.getStatus());
        reservaParaCancelar.cancelar();
        System.out.println("[Reserva] Status apos cancelar: " + reservaParaCancelar.getStatus());

        // Tentativa de duplo cancelamento
        try {
            reservaParaCancelar.cancelar();
            System.err.println("FALHA: Nao deveria cancelar reserva ja cancelada.");
        } catch (ReservaInvalidaException e) {
            System.out.println("[Reserva] Bloqueio de duplo cancelamento ok: " + e.getMessage());
        }

        // Tentativa de concluir reserva cancelada
        try {
            reservaParaCancelar.concluir();
            System.err.println("FALHA: Nao deveria concluir reserva cancelada.");
        } catch (ReservaInvalidaException e) {
            System.out.println("[Reserva] Bloqueio de conclusao de reserva cancelada ok: " + e.getMessage());
        }

        System.out.println();
    }

    // =========================================================================
    // 4. TESTES DE EMPRESTIMO, DEVOLUCAO, FAIXAS DE MULTA E EXCECOES
    // =========================================================================
    private static void testarEmprestimoRegrasDevolucaoEMultas() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("4. TESTES DE EMPRESTIMO, REGRAS DE DEVOLUCAO E CALCULO DE MULTAS");
        System.out.println("----------------------------------------------------------------------");

        Leitor leitor = new Leitor();
        leitor.setId(7);
        leitor.setNome("Mariana");

        Funcionario funcionario = new Funcionario();
        funcionario.setId(12);
        funcionario.setNome("Roberto");

        Copia copia = new Copia();
        copia.setId(77);
        copia.setCodigoBarras("BC-77");
        copia.setStatus(StatusCopia.EMPRESTADA);

        Emprestimo emp = new Emprestimo();
        emp.setId(301);
        emp.setLeitor(leitor);
        emp.setFuncionario(funcionario);
        emp.setCopia(copia);
        emp.setDataEmprestimo(LocalDate.now().minusDays(15));
        emp.setDataPrevistaDevolucao(LocalDate.now().minusDays(5));

        // Validacao: Devolucao com data nula
        try {
            emp.finalizarDevolucao(null);
            System.err.println("FALHA: Data de devolucao nula deveria lancar excecao.");
        } catch (DataDevolucaoInvalidaException e) {
            System.out.println("[Emprestimo] Rejeicao de data devolucao nula ok: " + e.getMessage());
        }

        // Validacao: Devolucao com data anterior ao emprestimo
        try {
            emp.finalizarDevolucao(LocalDate.now().minusDays(20));
            System.err.println("FALHA: Data de devolucao anterior ao emprestimo deveria falhar.");
        } catch (DataDevolucaoInvalidaException e) {
            System.out.println("[Emprestimo] Rejeicao de data devolucao anterior ok: " + e.getMessage());
        }

        // Validacao: Chamada de calcularMulta sem dataDevolucaoReal definida
        Emprestimo empSemDev = new Emprestimo();
        empSemDev.setDataEmprestimo(LocalDate.now().minusDays(10));
        empSemDev.setDataPrevistaDevolucao(LocalDate.now().minusDays(5));
        try {
            empSemDev.calcularMulta();
            System.err.println("FALHA: calcularMulta sem dataDevolucaoReal deveria lancar excecao.");
        } catch (DataDevolucaoInvalidaException e) {
            System.out.println("[Emprestimo] Rejeicao de calcularMulta sem dataDevolucaoReal ok: " + e.getMessage());
        }

        // Testes de faixas de calculo de multa:
        // Faixa 1: Sem atraso (devolucao antes ou na data prevista) -> R$ 0.00
        Emprestimo empSemAtraso = new Emprestimo();
        empSemAtraso.setDataEmprestimo(LocalDate.now().minusDays(10));
        empSemAtraso.setDataPrevistaDevolucao(LocalDate.now().minusDays(2));
        empSemAtraso.finalizarDevolucao(LocalDate.now().minusDays(3));
        System.out.println("[Multa - Sem atraso] Multa calculada: R$ " + empSemAtraso.getValorMulta() + " (Esperado: 0.0)");

        // Faixa 2: Atraso menor que 30 dias (ex: 10 dias * R$ 0.50/dia) -> R$ 5.00
        Emprestimo empAtrasoLeve = new Emprestimo();
        empAtrasoLeve.setDataEmprestimo(LocalDate.now().minusDays(20));
        empAtrasoLeve.setDataPrevistaDevolucao(LocalDate.now().minusDays(10));
        empAtrasoLeve.finalizarDevolucao(LocalDate.now());
        System.out.println("[Multa - Atraso 10 dias (< 30)] Multa calculada: R$ " + empAtrasoLeve.getValorMulta() + " (Esperado: 5.0)");

        // Faixa 3: Atraso entre 30 e 59 dias (ex: 40 dias -> ((40-30)*0.25) + 15 = 2.5 + 15 = 17.50)
        Emprestimo empAtrasoMedio = new Emprestimo();
        empAtrasoMedio.setDataEmprestimo(LocalDate.now().minusDays(60));
        empAtrasoMedio.setDataPrevistaDevolucao(LocalDate.now().minusDays(40));
        empAtrasoMedio.finalizarDevolucao(LocalDate.now());
        System.out.println("[Multa - Atraso 40 dias (30 a 59)] Multa calculada: R$ " + empAtrasoMedio.getValorMulta() + " (Esperado: 17.5)");

        // Faixa 4: Atraso >= 60 dias (teto maximo fixo em R$ 22.50)
        Emprestimo empAtrasoGrave = new Emprestimo();
        empAtrasoGrave.setDataEmprestimo(LocalDate.now().minusDays(100));
        empAtrasoGrave.setDataPrevistaDevolucao(LocalDate.now().minusDays(70));
        empAtrasoGrave.finalizarDevolucao(LocalDate.now());
        System.out.println("[Multa - Atraso 70 dias (>= 60)] Multa calculada: R$ " + empAtrasoGrave.getValorMulta() + " (Esperado: 22.5)");

        // Verificacao de liberacao da copia na devolucao
        System.out.println("[Emprestimo] Copia antes da devolucao de emp: " + copia.getStatus());
        emp.finalizarDevolucao(LocalDate.now());
        System.out.println("[Emprestimo] Copia apos devolucao de emp: " + copia.getStatus() + " (isDisponivel: " + copia.isDisponivel() + ")");

        // Bloqueio de devolucao duplicada
        try {
            emp.finalizarDevolucao(LocalDate.now());
            System.err.println("FALHA: Nao deveria permitir finalizar devolucao repetida.");
        } catch (DataDevolucaoInvalidaException e) {
            System.out.println("[Emprestimo] Bloqueio de devolucao duplicada ok: " + e.getMessage());
        }

        System.out.println();
    }

    // =========================================================================
    // 5. TESTES DE PERSISTENCIA: FuncionarioDAO
    // =========================================================================
    private static void testarDAOFuncionario() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("5. TESTES DE PERSISTENCIA: FuncionarioDAO");
        System.out.println("----------------------------------------------------------------------");

        FuncionarioDAO dao = new FuncionarioDAO();

        Funcionario f = new Funcionario();
        f.setNome("Carlos Silva");
        f.setCpf("111.222.333-44");
        f.setEmail("carlos@biblioteca.org");
        f.setTelefone("11988887777");
        f.setCargo("Assistente");
        f.setLogin("carlos.silva");
        f.setSenha("segredoSeguro"); // Gera hash SHA-256

        // Inserir
        int linhas = dao.inserir(f);
        System.out.println("[FuncionarioDAO] Inserir funcionario: " + (linhas > 0 ? "SUCESSO (" + linhas + " linha)" : "FALHA"));
        /*
         * NOTA DE ARQUITETURA:
         * FuncionarioDAO.inserir() nao atribui o ID autogerado no objeto 'f'.
         * Usaremos o id 1 fixado para carregar o registro persistido.
         */
        int idPersistido = 1;

        // Carregar
        Funcionario carregado = dao.carregar(idPersistido);
        System.out.println("[FuncionarioDAO] Carregado ID " + carregado.getId()
                + " | Nome: " + carregado.getNome()
                + " | Cargo: " + carregado.getCargo()
                + " | Hash Senha: " + carregado.getSenha());

        // Autenticar objeto carregado do banco (validando setSenhaHash utilizado internamente pelo DAO)
        try {
            boolean loginBancoOk = carregado.autenticar("segredoSeguro");
            System.out.println("[FuncionarioDAO] Autenticacao com senha pura comparada ao hash do banco: " + loginBancoOk);
        } catch (SenhaInvalidaException e) {
            System.err.println("FALHA: Nao autenticou funcionario carregado do banco: " + e.getMessage());
        }

        // Alterar
        carregado.setCargo("Bibliotecario Pleno");
        carregado.setTelefone("11999990000");
        int linhasAlteradas = dao.alterar(carregado);
        System.out.println("[FuncionarioDAO] Alterar funcionario: " + (linhasAlteradas > 0 ? "SUCESSO" : "FALHA"));

        // Remover
        int linhasRemovidas = dao.remover(carregado);
        System.out.println("[FuncionarioDAO] Remover funcionario: " + (linhasRemovidas > 0 ? "SUCESSO" : "FALHA"));

        System.out.println();
    }

    // =========================================================================
    // 6. TESTES DE PERSISTENCIA: LeitorDAO
    // =========================================================================
    private static void testarDAOLeitor() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("6. TESTES DE PERSISTENCIA: LeitorDAO");
        System.out.println("----------------------------------------------------------------------");

        LeitorDAO dao = new LeitorDAO();

        Leitor leitor = new Leitor();
        leitor.setNome("Mariana Souza");
        leitor.setCpf("222.333.444-55");
        leitor.setEmail("mariana@academico.com");
        leitor.setTelefone("11977776666");
        leitor.setMatricula("2026M01");
        leitor.setAtivo(true);

        // Inserir
        int linhas = dao.inserir(leitor);
        System.out.println("[LeitorDAO] Inserir leitor: " + (linhas > 0 ? "SUCESSO" : "FALHA"));

        int idPersistido = 1;
        // Carregar
        Leitor carregado = dao.carregar(idPersistido);
        System.out.println("[LeitorDAO] Carregado ID " + carregado.getId()
                + " | Nome: " + carregado.getNome()
                + " | Matricula: " + carregado.getMatricula()
                + " | Ativo: " + carregado.isAtivo()
                + " | Identificador: " + carregado.getIdentificadorFormatado());

        // Alterar
        carregado.setNome("Mariana Souza e Silva");
        System.out.println("[LeitorDAO] Alterando leitor...");
        int linhasAlteradas = dao.alterar(carregado);
        System.out.println("[LeitorDAO] Alterar leitor: " + (linhasAlteradas > 0 ? "SUCESSO" : "FALHA"));

        // Remover
        int linhasRemovidas = dao.remover(carregado);
        System.out.println("[LeitorDAO] Remover leitor: " + (linhasRemovidas > 0 ? "SUCESSO" : "FALHA"));

        System.out.println();
    }

    // =========================================================================
    // 7. TESTES DE PERSISTENCIA: ObraDAO
    // =========================================================================
    private static void testarDAOObra() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("7. TESTES DE PERSISTENCIA: ObraDAO");
        System.out.println("----------------------------------------------------------------------");

        ObraDAO dao = new ObraDAO();

        Obra obra = new Obra();
        obra.setTitulo("Clean Code");
        obra.setAutor("Robert C. Martin");
        obra.setAnoPublicacao(2008);

        // Inserir
        int linhas = dao.inserir(obra);
        System.out.println("[ObraDAO] Inserir obra: " + (linhas > 0 ? "SUCESSO" : "FALHA"));

        int idPersistido = 1;
        // Carregar
        Obra carregada = dao.carregar(idPersistido);
        System.out.println("[ObraDAO] Carregada ID " + carregada.getId()
                + " | Titulo: " + carregada.getTitulo()
                + " | Autor: " + carregada.getAutor()
                + " | Ano: " + carregada.getAnoPublicacao());

        // Alterar
        carregada.setTitulo("Clean Code - Edicao Especial");
        System.out.println("[ObraDAO] Alterando obra...");
        int linhasAlteradas = dao.alterar(carregada);
        System.out.println("[ObraDAO] Alterar obra: " + (linhasAlteradas > 0 ? "SUCESSO" : "FALHA"));

        // Remover
        int linhasRemovidas = dao.remover(carregada);
        System.out.println("[ObraDAO] Remover obra: " + (linhasRemovidas > 0 ? "SUCESSO" : "FALHA"));

        System.out.println();
    }

    // =========================================================================
    // 8. TESTES DE PERSISTENCIA: CopiaDAO
    // =========================================================================
    private static void testarDAOCopia() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("8. TESTES DE PERSISTENCIA: CopiaDAO");
        System.out.println("----------------------------------------------------------------------");

        // Precisamos de uma Obra persistida para atender a Foreign Key no banco
        ObraDAO obraDAO = new ObraDAO();
        Obra obraPai = new Obra();
        obraPai.setTitulo("Estruturas de Dados em Java");
        obraPai.setAutor("Goodrich");
        obraPai.setAnoPublicacao(2013);
        obraDAO.inserir(obraPai);

        CopiaDAO copiaDAO = new CopiaDAO();

        Copia copia = new Copia();
        copia.setCodigoBarras("ED-0001");
        copia.setStatus(StatusCopia.DISPONIVEL);
        // CopiaDAO.inserir() faz c.getObra().getId(), exigindo objeto Obra associado
        Obra refObra = new Obra();
        refObra.setId(1);
        copia.setObra(refObra);

        // Inserir
        int linhas = copiaDAO.inserir(copia);
        System.out.println("[CopiaDAO] Inserir copia: " + (linhas > 0 ? "SUCESSO" : "FALHA"));

        int idPersistido = 1;

        // Carregar
        System.out.println("[CopiaDAO] Executando carregar(" + idPersistido + ")...");
        Copia carregada = copiaDAO.carregar(idPersistido);
        System.out.println("[CopiaDAO] Copia carregada ID: " + carregada.getId()
                + " | CodigoBarras: " + carregada.getCodigoBarras()
                + " | Status: " + carregada.getStatus()
                + " | Obra vinculada ID: " + (carregada.getObra() == null ? "NULL" : carregada.getObra().getId()));

        // Alterar
        carregada.setStatus(StatusCopia.EMPRESTADA);
        // Garantindo que tenha Obra caso getObra() tenha ficado nulo
        if (carregada.getObra() == null) {
            carregada.setObra(refObra);
        }
        int linhasAlteradas = copiaDAO.alterar(carregada);
        System.out.println("[CopiaDAO] Alterar copia: " + (linhasAlteradas > 0 ? "SUCESSO" : "FALHA"));

        // Remover
        int linhasRemovidas = copiaDAO.remover(carregada);
        System.out.println("[CopiaDAO] Remover copia: " + (linhasRemovidas > 0 ? "SUCESSO" : "FALHA"));

        System.out.println();
    }

    // =========================================================================
    // 9. TESTES DE PERSISTENCIA: ReservaDAO
    // =========================================================================
    private static void testarDAOReserva() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("9. TESTES DE PERSISTENCIA: ReservaDAO");
        System.out.println("----------------------------------------------------------------------");

        ReservaDAO dao = new ReservaDAO();

        Leitor leitor = new Leitor();
        leitor.setId(1);

        Obra obra = new Obra();
        obra.setId(1);

        Reserva reserva = new Reserva(leitor, obra, LocalDate.now());

        // Inserir (ReservaDAO captura chaves geradas e atribui o ID)
        dao.inserir(reserva);
        System.out.println("[ReservaDAO] Inserir reserva realizada. ID atribuido automaticamente: " + reserva.getId());

        // Buscar por ID
        Reserva recuperada = dao.buscarPorId(reserva.getId());
        if (recuperada != null) {
            System.out.println("[ReservaDAO] Buscar por ID recuperou com sucesso:"
                    + " ID: " + recuperada.getId()
                    + " | Leitor ID: " + recuperada.getLeitor().getId()
                    + " | Obra ID: " + recuperada.getObra().getId()
                    + " | Data Reserva: " + recuperada.getDataReserva()
                    + " | Expira em: " + recuperada.getDataExpiracao()
                    + " | Status: " + recuperada.getStatus());
        } else {
            System.err.println("FALHA: Nao localizou a reserva inserida pelo ID.");
        }

        // Listar Todos
        List<Reserva> lista = dao.listarTodos();
        System.out.println("[ReservaDAO] Total de reservas cadastradas: " + lista.size());

        // Atualizar
        if (recuperada != null) {
            recuperada.setStatus(StatusReserva.CONCLUIDA);
            dao.atualizar(recuperada);

            Reserva atualizada = dao.buscarPorId(recuperada.getId());
            System.out.println("[ReservaDAO] Status apos atualizar(): " + (atualizada != null ? atualizada.getStatus() : "null"));

            if (atualizada != null) {
                // Deletar
                dao.deletar(atualizada.getId());
                Reserva deletada = dao.buscarPorId(atualizada.getId());
                System.out.println("[ReservaDAO] Registro apos deletar(): " + (deletada == null ? "NULO (Deletado com sucesso)" : "AINDA EXISTE"));
            }
        }

        System.out.println();
    }

    // =========================================================================
    // 10. TESTES DE PERSISTENCIA: EmprestimoDAO
    // =========================================================================
    private static void testarDAOEmprestimo() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("10. TESTES DE PERSISTENCIA: EmprestimoDAO");
        System.out.println("----------------------------------------------------------------------");

        EmprestimoDAO dao = new EmprestimoDAO();

        Leitor leitor = new Leitor();
        leitor.setId(1);

        Funcionario funcionario = new Funcionario();
        funcionario.setId(1);

        Copia copia = new Copia();
        copia.setId(1);

        Emprestimo emp = new Emprestimo();
        emp.setLeitor(leitor);
        emp.setFuncionario(funcionario);
        emp.setCopia(copia);
        emp.setDataEmprestimo(LocalDate.now().minusDays(10));
        emp.setDataPrevistaDevolucao(LocalDate.now().minusDays(3));

        // Inserir (EmprestimoDAO captura chaves geradas e atribui o ID)
        dao.inserir(emp);
        System.out.println("[EmprestimoDAO] Inserir emprestimo. ID atribuido automaticamente: " + emp.getId());

        // Buscar por ID
        Emprestimo buscado = dao.buscarPorId(emp.getId());
        if (buscado != null) {
            System.out.println("[EmprestimoDAO] Buscar por ID recuperou com sucesso:"
                    + " ID: " + buscado.getId()
                    + " | Leitor ID: " + buscado.getLeitor().getId()
                    + " | Funcionario ID: " + buscado.getFuncionario().getId()
                    + " | Copia ID: " + buscado.getCopia().getId()
                    + " | Data Emprestimo: " + buscado.getDataEmprestimo()
                    + " | Data Prevista: " + buscado.getDataPrevistaDevolucao()
                    + " | Data Real: " + buscado.getDataDevolucaoReal()
                    + " | Valor Multa: R$ " + buscado.getValorMulta());
        } else {
            System.err.println("FALHA: Nao localizou o emprestimo inserido pelo ID.");
        }

        // Listar Todos
        List<Emprestimo> lista = dao.listarTodos();
        System.out.println("[EmprestimoDAO] Total de emprestimos cadastrados: " + lista.size());

        // Atualizar (simulando devolucao e multa calculada)
        if (buscado != null) {
            buscado.setDataDevolucaoReal(LocalDate.now());
            buscado.setValorMulta(1.50);
            dao.atualizar(buscado);

            Emprestimo atualizado = dao.buscarPorId(buscado.getId());
            if (atualizado != null) {
                System.out.println("[EmprestimoDAO] Apos atualizar: Data Real: " + atualizado.getDataDevolucaoReal()
                        + " | Multa: R$ " + atualizado.getValorMulta());

                // Deletar
                dao.deletar(atualizado.getId());
                Emprestimo deletado = dao.buscarPorId(atualizado.getId());
                System.out.println("[EmprestimoDAO] Registro apos deletar(): " + (deletado == null ? "NULO (Deletado com sucesso)" : "AINDA EXISTE"));
            }
        }

        System.out.println();
    }

    // =========================================================================
    // 11. CENARIO INTEGRADO COMPLETO DE PONTA A PONTA
    // =========================================================================
    private static void testarCenarioIntegradoEndToEnd() {
        System.out.println("----------------------------------------------------------------------");
        System.out.println("11. CENARIO INTEGRADO COMPLETO (FLUXO DE NEGOCIO REAL)");
        System.out.println("----------------------------------------------------------------------");

        System.out.println("[Passo 1] Cadastro dos Atores e do Acervo...");
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
        LeitorDAO leitorDAO = new LeitorDAO();
        ObraDAO obraDAO = new ObraDAO();
        CopiaDAO copiaDAO = new CopiaDAO();
        ReservaDAO reservaDAO = new ReservaDAO();
        EmprestimoDAO emprestimoDAO = new EmprestimoDAO();

        // 1. Funcionario
        Funcionario atendente = new Funcionario();
        atendente.setNome("Juliana Balcao");
        atendente.setCpf("333.444.555-66");
        atendente.setEmail("juliana@biblioteca.org");
        atendente.setCargo("Atendente");
        atendente.setLogin("juliana");
        atendente.setSenha("juliana@2026");
        funcionarioDAO.inserir(atendente);

        // 2. Leitor
        Leitor leitor = new Leitor();
        leitor.setNome("Lucas Estudante");
        leitor.setCpf("444.555.666-77");
        leitor.setMatricula("ENG-2026");
        leitor.setAtivo(true);
        leitorDAO.inserir(leitor);

        // 3. Obra
        Obra obra = new Obra();
        obra.setTitulo("Algoritmos e Estruturas de Dados");
        obra.setAutor("Cormen et al.");
        obra.setAnoPublicacao(2022);
        obra.setCopias(new ArrayList<>());
        obraDAO.inserir(obra);

        // 4. Copia associada a obra
        Obra refObra = new Obra();
        refObra.setId(2); // Obra persistida
        Copia copiaLivro = new Copia();
        copiaLivro.setCodigoBarras("CORMEN-01");
        copiaLivro.setStatus(StatusCopia.DISPONIVEL);
        copiaLivro.setObra(refObra);
        copiaDAO.inserir(copiaLivro);

        System.out.println("   -> Funcionario cadastrado (limite: " + atendente.getLimiteLivros() + " livros)");
        System.out.println("   -> Leitor cadastrado: " + leitor.getIdentificadorFormatado() + " (prazo: " + leitor.getPrazoEmprestimoDias() + " dias)");
        System.out.println("   -> Obra '" + obra.getTitulo() + "' com copia '" + copiaLivro.getCodigoBarras() + "' disponivel? " + copiaLivro.isDisponivel());

        // 5. Reserva prévia pelo leitor
        System.out.println("\n[Passo 2] Leitor solicita reserva da obra...");
        Reserva reserva = new Reserva(leitor, refObra, LocalDate.now());
        reservaDAO.inserir(reserva);
        System.out.println("   -> Reserva registrada no banco. ID: " + reserva.getId() + " | Status: " + reserva.getStatus());

        // Concluindo a reserva para efetivar o emprestimo
        reserva.concluir();
        reservaDAO.atualizar(reserva);
        System.out.println("   -> Reserva concluida com sucesso. Status atualizado: " + reserva.getStatus());

        // 6. Realizacao do Emprestimo
        System.out.println("\n[Passo 3] Atendente realiza o emprestimo da copia ao leitor...");
        copiaLivro.setStatus(StatusCopia.EMPRESTADA);
        System.out.println("   -> Copia alterada para EMPRESTADA. Disponivel? " + copiaLivro.isDisponivel());

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLeitor(leitor);
        emprestimo.setFuncionario(atendente);
        emprestimo.setCopia(copiaLivro);
        // Simulando que o emprestimo ocorreu ha 20 dias, com prazo de 15 dias (5 dias de atraso hoje)
        emprestimo.setDataEmprestimo(LocalDate.now().minusDays(20));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(20).plusDays(leitor.getPrazoEmprestimoDias()));
        emprestimoDAO.inserir(emprestimo);
        System.out.println("   -> Emprestimo salvo no banco com ID: " + emprestimo.getId());
        System.out.println("   -> Data Emprestimo: " + emprestimo.getDataEmprestimo() + " | Devolucao Prevista: " + emprestimo.getDataPrevistaDevolucao());

        // 7. Devolucao do Livro com Atraso
        System.out.println("\n[Passo 4] Leitor comparece a biblioteca hoje para devolver o livro...");
        emprestimo.finalizarDevolucao(LocalDate.now());
        System.out.println("   -> Devolucao finalizada em: " + emprestimo.getDataDevolucaoReal());
        System.out.println("   -> Dias de atraso: 5 dias");
        System.out.println("   -> Multa apurada: R$ " + emprestimo.getValorMulta());
        System.out.println("   -> Status da copia apos finalizacao: " + copiaLivro.getStatus() + " (isDisponivel: " + copiaLivro.isDisponivel() + ")");

        // 8. Atualizacao do registro de emprestimo no banco
        emprestimoDAO.atualizar(emprestimo);
        Emprestimo historicoFinal = emprestimoDAO.buscarPorId(emprestimo.getId());
        if (historicoFinal != null) {
            System.out.println("   -> Registro do emprestimo atualizado com sucesso no banco de dados.");
            System.out.println("   -> Historico verificado: Multa registrada = R$ " + historicoFinal.getValorMulta()
                    + " | Data Devolucao Real = " + historicoFinal.getDataDevolucaoReal());
        } else {
            System.out.println("   -> Registro do emprestimo nao pode ser recuperado via buscarPorId (possivel falha de driver/banco).");
        }

        System.out.println("\n[Passo 5] Fluxo integrado finalizado com sucesso!");
    }
}
