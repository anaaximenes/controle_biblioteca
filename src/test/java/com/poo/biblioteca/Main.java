package com.poo.biblioteca;

import com.poo.biblioteca.exception.DataDevolucaoInvalidaException;
import com.poo.biblioteca.exception.ReservaInvalidaException;
import com.poo.biblioteca.exception.SenhaInvalidaException;
import com.poo.biblioteca.util.SegurancaUtil;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTES DO SISTEMA DE BIBLIOTECA ===\n");

        testarUsuariosEAutenticacao();
        testarCopiaEReserva();
        testarEmprestimoEDevolucao();

        System.out.println("\n=== TODOS OS CENÁRIOS FORAM EXECUTADOS ===");
    }

    private static void testarUsuariosEAutenticacao() {
        System.out.println("--- 1. TESTE DE USUÁRIOS (POLIMORFISMO) E SENHA SHA-256 ---");

        // AJUSTE SE NECESSÁRIO: adapte os construtores conforme os campos da sua classe
        Leitor leitor = new Leitor();
        leitor.setId(1);
        leitor.setNome("Ana Beatriz");
        leitor.setMatricula("202601");

        Funcionario funcionario = new Funcionario();
        funcionario.setId(10);
        funcionario.setNome("Gustavo");
        funcionario.setSenha("senha123"); // Deve gerar hash SHA-256 internamente via setSenha

        // Verificando hash gerado (deve ter 64 caracteres hexadecimais)
        String hashDireto = SegurancaUtil.gerarHash("senha123");
        System.out.println("Hash SHA-256 esperado (64 chars): " + hashDireto + " (tamanho: " + hashDireto.length() + ")");

        // Polimorfismo: Leitor vs Funcionario
        System.out.println("Leitor -> " + leitor.getIdentificadorFormatado()
                + " | Prazo: " + leitor.getPrazoEmprestimoDias() + " dias"
                + " | Limite: " + leitor.getLimiteLivros() + " livros");

        System.out.println("Funcionário -> " + funcionario.getIdentificadorFormatado()
                + " | Prazo: " + funcionario.getPrazoEmprestimoDias() + " dias"
                + " | Limite: " + funcionario.getLimiteLivros() + " livros");

        // Autenticação correta
        try {
            boolean ok = funcionario.autenticar("senha123");
            System.out.println("Login com senha correta: " + ok);
        } catch (SenhaInvalidaException e) {
            System.out.println("Erro inesperado no login válido: " + e.getMessage());
        }

        // Autenticação incorreta (deve lançar SenhaInvalidaException)
        try {
            funcionario.autenticar("senhaErrada");
            System.out.println("FALHA: não deveria autenticar com senha errada.");
        } catch (SenhaInvalidaException e) {
            System.out.println("Exceção capturada corretamente no login inválido: " + e.getMessage());
        }

        System.out.println();
    }

    private static void testarCopiaEReserva() {
        System.out.println("--- 2. TESTE DE CÓPIA E MÁQUINA DE ESTADOS DA RESERVA ---");

        // AJUSTE SE NECESSÁRIO: adapte construtor/setters de Leitor e Obra
        Leitor leitor = new Leitor();
        leitor.setId(2);
        leitor.setNome("Renan");
        leitor.setMatricula("202602");

        Obra obra = new Obra();
        // obra.setTitulo("Java Efetivo"); // ajuste se houver setter/construtor

        Copia copia = new Copia();
        copia.setStatus("DISPONIVEL");
        System.out.println("Cópia disponível inicialmente? " + copia.isDisponivel());

        copia.setStatus("EMPRESTADO");
        System.out.println("Cópia disponível após emprestar? " + copia.isDisponivel());

        // Cenário A: Reserva válida dentro do prazo (dataExpiracao = dataReserva + 3 dias) -> concluir()
        Reserva reservaValida = new Reserva(
                leitor,
                obra,
                LocalDate.now().minusDays(1)
        );
        System.out.println("Status inicial reservaValida: " + reservaValida.getStatus()
                + " | Expira em: " + reservaValida.getDataExpiracao());
        reservaValida.concluir();
        System.out.println("Status após concluir(): " + reservaValida.getStatus());

        // Tentar concluir novamente (deve lançar ReservaInvalidaException)
        try {
            reservaValida.concluir();
            System.out.println("FALHA: não deveria concluir reserva já concluída.");
        } catch (ReservaInvalidaException e) {
            System.out.println("Bloqueio de dupla conclusão ok: " + e.getMessage());
        }

        // Tentar cancelar reserva já concluída (deve lançar ReservaInvalidaException)
        try {
            reservaValida.cancelar();
            System.out.println("FALHA: não deveria cancelar reserva já concluída.");
        } catch (ReservaInvalidaException e) {
            System.out.println("Bloqueio de cancelamento após concluir ok: " + e.getMessage());
        }

        // Cenário B: Reserva expirada (dataReserva há 5 dias -> expirou há 2 dias) -> concluir()
        Reserva reservaExpirada = new Reserva(
                leitor,
                obra,
                LocalDate.now().minusDays(5)
        );
        try {
            reservaExpirada.concluir();
            System.out.println("FALHA: reserva expirada não deveria concluir.");
        } catch (ReservaInvalidaException e) {
            System.out.println("Expiração tratada ok: " + e.getMessage()
                    + " | Novo status: " + reservaExpirada.getStatus());
        }

        // Cenário C: Cancelamento de reserva ativa
        Reserva reservaParaCancelar = new Reserva(
                leitor,
                obra,
                LocalDate.now()
        );
        reservaParaCancelar.cancelar();
        System.out.println("Status após cancelar(): " + reservaParaCancelar.getStatus());

        // Tentar cancelar reserva já cancelada (deve lançar ReservaInvalidaException)
        try {
            reservaParaCancelar.cancelar();
            System.out.println("FALHA: não deveria cancelar reserva já cancelada.");
        } catch (ReservaInvalidaException e) {
            System.out.println("Bloqueio de duplo cancelamento ok: " + e.getMessage());
        }

        System.out.println();
    }

    private static void testarEmprestimoEDevolucao() {
        System.out.println("--- 3. TESTE DE EMPRÉSTIMO, MULTA E DEVOLUÇÃO ---");

        // AJUSTE SE NECESSÁRIO: instancie conforme seus construtores/métodos em Emprestimo
        Leitor leitor = new Leitor();
        leitor.setId(3);
        leitor.setNome("Rodrigo");
        leitor.setMatricula("202603");

        Funcionario funcionario = new Funcionario();
        funcionario.setId(11);
        funcionario.setNome("Carlos");

        Copia copia = new Copia();
        copia.setStatus("EMPRESTADO");

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(201);
        emprestimo.setLeitor(leitor);
        emprestimo.setFuncionario(funcionario);
        emprestimo.setCopia(copia);
        emprestimo.setDataEmprestimo(LocalDate.now().minusDays(10));
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(3)); // 3 dias de atraso hoje

        // Data inválida: anterior à data de empréstimo
        try {
            emprestimo.finalizarDevolucao(LocalDate.now().minusDays(15));
            System.out.println("FALHA: data anterior ao empréstimo deveria falhar.");
        } catch (DataDevolucaoInvalidaException e) {
            System.out.println("Validação de data anterior ok: " + e.getMessage());
        }

        // Devolução válida hoje (com atraso para testar multa e liberação da cópia)
        emprestimo.finalizarDevolucao(LocalDate.now());
        System.out.println("Data empréstimo preservada: " + emprestimo.getDataEmprestimo());
        System.out.println("Data prevista preservada: " + emprestimo.getDataPrevistaDevolucao());
        System.out.println("Data devolução real: " + emprestimo.getDataDevolucaoReal());
        System.out.println("Valor da multa calculada: R$ " + emprestimo.getValorMulta());
        System.out.println("Cópia voltou a ficar disponível? " + copia.isDisponivel());

        // Tentar devolver o mesmo empréstimo duas vezes
        try {
            emprestimo.finalizarDevolucao(LocalDate.now());
            System.out.println("FALHA: não deveria permitir devolver duas vezes.");
        } catch (DataDevolucaoInvalidaException e) {
            System.out.println("Bloqueio de devolução duplicada ok: " + e.getMessage());
        }
    }
}