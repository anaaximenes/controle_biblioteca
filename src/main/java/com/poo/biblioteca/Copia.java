package com.poo.biblioteca;

public class Copia {
    private int id;
    private String codigoBarras;
    private StatusCopia status;
    private Obra obra;

    public Copia() {
    }

    public Copia(String codigoBarras, StatusCopia status, Obra obra) {
        this.codigoBarras = codigoBarras;
        this.status = status;
        this.obra = obra;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public StatusCopia getStatus() {
        return status;
    }

    public void setStatus(StatusCopia status) {
        this.status = status;
    }

    public Obra getObra() {
        return obra;
    }

    public void setObra(Obra obra) {
        this.obra = obra;
    }

    public boolean isDisponivel() {
        return this.status == StatusCopia.DISPONIVEL;
    }
}
