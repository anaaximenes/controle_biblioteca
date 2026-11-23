package com.poo.biblioteca;

import java.util.ArrayList;
import java.util.List;

public class Obra {
    private int id;
    private String titulo;
    private String autor;
    private int anoPublicacao;
    private List<Copia> copias;

    public Obra(){
    }

    public Obra(String titulo, String autor, int anoPublicacao, List<Copia> copias) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.copias = copias;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(int anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public List<Copia> getCopias() {
        return copias;
    }

    public void setCopias(List<Copia> copias) {
        this.copias = copias;
    }

    public void adicionarCopia (Copia c){
        if (c == null) {
            throw new IllegalArgumentException("Copia nao pode ser nula!");
        }
        this.copias.add(c);
    }

    public List<Copia> getCopiasDisponiveis(){
        List<Copia> disponiveis = new ArrayList<>();
        for (Copia c : this.copias) {
            if (c != null && c.isDisponivel()) {
                disponiveis.add(c);
            }
        }
        return disponiveis;
    }
}

