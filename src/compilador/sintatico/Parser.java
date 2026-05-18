package compilador.sintatico;

import compilador.lexico.*;
import java.io.*;

public class Parser {
    private Lexer lex;       // O analisador léxico
    private Token look;      // O token atual de lookahead

    // O construtor recebe o lexer já instanciado e lê o primeiro token
    public Parser(Lexer l) throws IOException {
        this.lex = l;
        move(); 
    }

    // Método para ler o próximo token da entrada
    void move() throws IOException {
        look = lex.scan(); // Assumindo que seu Lexer possui o método scan() ou getToken()
    }

    // Método inicial da gramática. (Símbolo inicial)
    public void programa() throws IOException {
        // TODO: Implemente as chamadas para os outros métodos não-terminais
        // Exemplo: match(Tag.CLASS); match(Tag.ID); ...
    }

    // O método match verifica se o token atual é o esperado.
    // Se for, ele consome o token e avança. Se não, dispara um erro.
    void match(int t) throws IOException {
        if (look.tag == t) {
            move();
        } else {
            error("Erro de sintaxe. Esperado: " + t + " encontrado: " + look.tag);
        }
    }

    void error(String s) {
        throw new Error("Perto da linha " + lex.getLine() + ": " + s);
    }
}
