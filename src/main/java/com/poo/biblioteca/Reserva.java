package com.poo.biblioteca;

import com.poo.biblioteca.exception.ReservaInvalidaException;

import java.time.LocalDate;

import static com.poo.biblioteca.StatusReserva.*;

public class Reserva {
    private int id;
    private Leitor leitor;
    private Obra obra;
    private LocalDate dataReserva;
    private LocalDate dataExpiracao;
    private StatusReserva status;

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

    public Obra getObra() {
        return obra;
    }

    public void setObra(Obra obra) {
        this.obra = obra;
    }

    public LocalDate getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDate dataReserva) {
        this.dataReserva = dataReserva;
    }

    public LocalDate getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDate dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public Reserva(Leitor leitor, Obra obra, LocalDate dataReserva) {
        this.leitor = leitor;
        this.obra = obra;
        this.dataReserva = dataReserva;
        this.dataExpiracao = dataReserva.plusDays(3);
        this.status = ATIVA;
    }

    public Reserva() {
    }

    public void cancelar(){
        if (this.status != ATIVA){
            throw new ReservaInvalidaException("Reserva já cancelada ou concluida!");
        }
        this.status = CANCELADA;
    }

    public void concluir(){
        if (this.status != ATIVA){
            throw new ReservaInvalidaException("Reserva já cancelada ou concluida!");
        }
        if (LocalDate.now().isAfter(this.dataExpiracao)){
            this.status = EXPIRADA;
            throw new ReservaInvalidaException("Reserva expirada!");
        }
        else {
            this.status = CONCLUIDA;
        }
    }
}
