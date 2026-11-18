package com.poo.biblioteca.dao;

import java.util.List;

public interface GenericDAO<T> {
    void inserir(T obj);
    void atualizar(T obj);
    void excluir(int id);
    T buscarPorId(int id);
    List<T> listarTodos();
}