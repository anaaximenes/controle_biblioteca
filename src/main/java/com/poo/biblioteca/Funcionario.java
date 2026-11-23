package com.poo.biblioteca;

import com.poo.biblioteca.exception.SenhaInvalidaException;
import com.poo.biblioteca.util.SegurancaUtil;

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
        String hash = SegurancaUtil.gerarHash(senha);
        this.senha = hash;
    }

    public void setSenhaHash(String senhaHash){ //para utilizar na classe DAO para nao criptografar novamente
        this.senha = senhaHash;
    }

    public boolean autenticar(String senha) {
        if (senha == null || senha.isBlank() || this.senha == null) {
            throw new SenhaInvalidaException("Senha inválida!");
        }

        if (!SegurancaUtil.gerarHash(senha).equals(this.senha)){
            throw new SenhaInvalidaException("Senha invalida!");
        }
        return true;
    }

    @Override
    public int getPrazoEmprestimoDias() {
        return 20;
    }

    @Override
    public int getLimiteLivros() {
        return 5;
    }

    @Override
    public String getIdentificadorFormatado() {
        return String.format("FUNC-%04d", this.getId());
    }
}



