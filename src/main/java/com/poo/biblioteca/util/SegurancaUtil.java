package com.poo.biblioteca.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SegurancaUtil {

    // Construtor privado: impede que alguém dê "new SegurancaUtil()" por engano,
    // já que todos os métodos dessa classe são estáticos (utilitários).
    private SegurancaUtil() {
    }

    /**
     * Recebe uma senha em texto puro (ex: "123456") e retorna o hash SHA-256
     * em formato hexadecimal com 64 caracteres.
     */
    public static String gerarHash(String senha) {
        if (senha == null) {
            throw new IllegalArgumentException("A senha não pode ser nula!");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Converte a String para bytes em UTF-8 (garante suporte a acentos/ç)
            // e executa o cálculo do hash, gerando um array de 32 bytes.
            byte[] hashBytes = md.digest(senha.getBytes(StandardCharsets.UTF_8));

            // Converte os 32 bytes crus em texto hexadecimal legível (64 caracteres)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                // "%02x" formata cada byte em 2 dígitos hexadecimais minúsculos
                // (ex: o número 10 vira "0a", mantendo o tamanho fixo).
                hexString.append(String.format("%02x", b));
            }

            // Retorna a String final pronta para ser salva no banco ou comparada no login
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Exceção checada exigida pelo Java caso o nome do algoritmo estivesse errado.
            // Como "SHA-256" é padrão oficial do Java, convertemos para RuntimeException.
            throw new RuntimeException("Erro interno: algoritmo SHA-256 não encontrado.", e);
        }
    }
}