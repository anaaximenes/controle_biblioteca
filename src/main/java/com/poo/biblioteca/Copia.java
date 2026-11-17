package com.poo.biblioteca;

public class Copia {
    private int id;
    private String codigoBarras;
    private String  status;
    private Obra obra;

    public Copia() {
    }

    public Copia(int id, String codigoBarras, String status, Obra obra) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Obra getObra() {
        return obra;
    }

    public void setObra(Obra obra) {
        this.obra = obra;
    }

    public boolean isDisponivel() {
        //todo
        return false;
    }
}
