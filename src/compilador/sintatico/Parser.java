package compilador.sintatico;

import compilador.lexico.*;
import java.io.*;

public class Parser {
    private Lexer lex; // O analisador léxico
    private Token look; // O token atual de lookahead

    // O construtor recebe o lexer já instanciado e lê o primeiro token
    public Parser(Lexer l) throws IOException {
        this.lex = l;
        move();
    }

    // Método para ler o próximo token da entrada
    void move() throws IOException {
        look = lex.scan(); // Assumindo que seu Lexer possui o método scan() ou getToken()
    }

    public void program() throws IOException { // Método inicial da gramática. (Símbolo inicial)
        match(Tag.CLASS);
        match(Tag.ID);
        match('{');

        // lista de declarações opcional
        if (look.tag == Tag.INT || look.tag == Tag.STRING || look.tag == Tag.FLOAT) {
            decl_list();
        }

        body();
        match('}');
        // ::= class identifier "{" [decl-list] body "}"
    }

    public void decl_list() throws IOException {
        decl();
        match(';');
        while (look.tag == Tag.INT || look.tag == Tag.STRING || look.tag == Tag.FLOAT) {
            decl();
            match(';');
        }
        // ::= decl ";" { decl ";"}
    }

    public void decl() throws IOException {
        type();
        ident_list();
        // ::= type ident-lis
    }

    public void ident_list() throws IOException {
        match(Tag.ID);
        while (look.tag == ',') {
            match(',');
            match(Tag.ID);
        }
        // ::= identifier {"," identifier}
    }

    public void type() throws IOException {
        switch (look.tag) {
            case Tag.INT:
                match(Tag.INT);
                break;

            case Tag.STRING:
                match(Tag.STRING);
                break;

            case Tag.FLOAT:
                match(Tag.FLOAT);
                break;
            default:
                error("Erro de sintaxe. Esperado tipo (int, string ou float), mas encontrado: " + look.tag);
                break;
        }
    }

    public void body() throws IOException {
        match('{');
        stmt_list();
        match('}');
        // ::= "{" stmt_list "}"
    }

    public void stmt_list() throws IOException {
        stmt();
        match(';');
        while (look.tag == Tag.ID ||
                look.tag == Tag.IF ||
                look.tag == Tag.DO ||
                look.tag == Tag.REPEAT ||
                look.tag == Tag.READ ||
                look.tag == Tag.WRITE) {
            stmt();
            match(';');
        }
        // ::= stmt ";" { stmt ";" }
    }

    public void stmt() throws IOException {
        switch (look.tag) {
            case Tag.ID:
                assign_stmt(); 
                break;

            case Tag.IF:
                if_stmt(); 
                break;

            case Tag.DO:
                do_stmt(); 
                break;

            case Tag.REPEAT:
                repeat_stmt(); 
                break;

            case Tag.READ:
                read_stmt(); 
                break;

            case Tag.WRITE:
                write_stmt(); 
                break;

            default:
                error("Erro de sintaxe. Comando inválido ou malformado. Encontrado: " + look.tag);
                break;
        }
        // ::= assign_stmt | if_stmt | do_stmt | repeat_stmt | read_stmt | write_stmt
    }

    public void assign_stmt() throws IOException {
        match(Tag.ID);
        match(Tag.ASSIGN);
        simple_expr();
        // ::= identifier ":=" simple_expr
    }

    public void if_stmt() throws IOException {
        match(Tag.IF);
        match('(');
        condition();
        match(')');
        match('{');
        stmt_list();
        match('}');
        if_stmtf();
        // ::= if "(" condition ")" "{" stmt_list "}" if_stmtf
    }

    public void if_stmtf() throws IOException {
        // ::= else “{“ stmt_list “}” | λ
    }

    public void do_stmt() throws IOException {
        // ::= do “{“ stmt_list “}” do_suffix
    }

    public void do_suffix() throws IOException {
        // ::= while “(“ condition “)”
    }

    public void repeat_stmt() throws IOException {
        // ::= repeat “{“ stmt_list “}” stmt_suffix
    }

    public void stmt_suffix() throws IOException {
        // ::= until “(“ condition “)”
    }

    public void read_stmt() throws IOException {
        // ::= read "(" identifier ")"
    }

    public void write_stmt() throws IOException {
        // ::= write "(" writable ")"
    }

    public void writable() throws IOException {
        // ::= simple_expr
    }

    public void condition() throws IOException {
        // ::= expression
    }

    public void expression() throws IOException {
        // ::= simple_expr expressionf
    }

    public void expressionf() throws IOException {
        // ::= relop simple_expr | λ
    }

    public void simple_expr() throws IOException {
        // ::= term simple_exprf
    }

    public void simple_exprf() throws IOException {
        // ::= addop term simple_exprf | λ
    }

    public void term() throws IOException {
        // ::= factor_a termf
    }

    public void termf() throws IOException {
        // ::= mulop factor_a termf | λ
    }

    public void factor_a() throws IOException {
        // ::= factor | not factor | "_" factor
    }

    public void factor() throws IOException {
        // ::= identifier | constant | "(" expression ")"
    }

    public void relop() throws IOException {
        // ::= ">" | ">=" | "<" | "<=" | "<>" | "="
    }

    public void addop() throws IOException {
        // ::= "+" | "_" | or
    }

    public void mulop() throws IOException {
        // ::= "*" | "/" | "%" | and
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
