package com.poo.biblioteca;

public class Funcionario extends Pessoa {

    private String cargo;
    private String login;
    private String senha;

    public Funcionario() {
        super();
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean autenticar(String senha) {
        //todo
        return false;
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



