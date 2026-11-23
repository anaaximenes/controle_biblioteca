package com.poo.biblioteca;

import com.poo.biblioteca.exception.DataDevolucaoInvalidaException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Emprestimo {
    private int id;
    private Leitor leitor;
    private Funcionario funcionario;
    private Copia copia;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucaoReal;
    private double valorMulta;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Copia getCopia() {
        return copia;
    }

    public void setCopia(Copia copia) {
        this.copia = copia;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) {
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }

    public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) {
        this.dataDevolucaoReal = dataDevolucaoReal;
    }

    public double getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(double valorMulta) {
        this.valorMulta = valorMulta;
    }

    public double calcularMulta(){
        if (dataDevolucaoReal == null){
            throw new DataDevolucaoInvalidaException("A data de devolução real não pode ser nula!");
        }
        if (dataDevolucaoReal.isAfter(dataPrevistaDevolucao)){
            long diasAtraso = ChronoUnit.DAYS.between(dataPrevistaDevolucao, dataDevolucaoReal);
            if (diasAtraso < 30){
                valorMulta = diasAtraso * 0.5;
            }
            else if (diasAtraso < 60){
                valorMulta = ((diasAtraso - 30) * 0.25) + 15;
            }
            else {
                valorMulta = 22.5;
            }
        }
        else{
            valorMulta = 0;
        }
        return valorMulta;
    }

    public void finalizarDevolucao(LocalDate dataDevolucao){
        if (dataDevolucaoReal != null){
            throw new DataDevolucaoInvalidaException("Este empréstimo já foi finalizado anteriormente.");
        }

        if (dataDevolucao == null){
            throw new DataDevolucaoInvalidaException("A data de devolução real não pode ser nula!");
        }

        if (dataDevolucao.isBefore(dataEmprestimo)){
            throw new DataDevolucaoInvalidaException("A data de devolucao real não pode ser anterior a data do empréstimo!");
        }

        this.dataDevolucaoReal = dataDevolucao;
        this.calcularMulta();

        if (this.copia != null){
            this.copia.setStatus(StatusCopia.DISPONIVEL);
        }
    }
}
