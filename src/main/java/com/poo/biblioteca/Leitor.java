package com.poo.biblioteca;

public class Leitor extends Pessoa{

    private String matricula;
    private boolean ativo;

    public Leitor() {
        super();
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public int getPrazoEmprestimoDias() {
        //todo
        return 0;
    }

    @Override
    public int getLimiteLivros() {
        //todo
        return 0;
    }

    @Override
    public String getIdentificadorFormatado() {
        //todo
        return null;
    }
}
