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
        look = lex.scan();
        if (look == null) {
            look = new Token(Tag.EOF);
        }
    }

    public void program() throws IOException { // Método inicial da gramática. (Símbolo inicial)
        if (look.tag == Tag.EOF) {
            error("Erro de sintaxe. Esperado: " + tagToString(Tag.CLASS) + " encontrado: " + tagToString(look.tag));
        } else {
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
                error("Erro de sintaxe. Esperado tipo (int, string ou float), mas encontrado: " + tagToString(look.tag));
                break;
        }
    }

    public void body() throws IOException {
        // match('{');
        stmt_list();
        // match('}');
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
                if (look.tag == Tag.EOF) {
                    error("Erro de sintaxe. Fim de arquivo inesperado.");
                } else {
                    error("Erro de sintaxe. Comando inválido ou malformado. Encontrado: " + tagToString(look.tag));
                }
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
        if (look.tag == Tag.ELSE) {
            match(Tag.ELSE);
            match('{');
            stmt_list();
            match('}');
        }
        // ::= else “{“ stmt_list “}” | λ
    }

    public void do_stmt() throws IOException {
        match(Tag.DO);
        match('{');
        stmt_list();
        match('}');
        do_suffix();
        // ::= do “{“ stmt_list “}” do_suffix
    }

    public void do_suffix() throws IOException {
        match(Tag.WHILE);
        match('(');
        condition();
        match(')');
        // ::= while “(“ condition “)”
    }

    public void repeat_stmt() throws IOException {
        match(Tag.REPEAT);
        match('{');
        stmt_list();
        match('}');
        stmt_suffix();
        // ::= repeat “{“ stmt_list “}” stmt_suffix
    }

    public void stmt_suffix() throws IOException {
        match(Tag.UNTIL);
        match('(');
        condition();
        match(')');
        // ::= until “(“ condition “)”
    }

    public void read_stmt() throws IOException {
        match(Tag.READ);
        match('(');
        match(Tag.ID);
        match(')');
        // ::= read "(" identifier ")"
    }

    public void write_stmt() throws IOException {
        match(Tag.WRITE);
        match('(');
        writable();
        match(')');
        // ::= write "(" writable ")"
    }

    public void writable() throws IOException {
        simple_expr();
        // ::= simple_expr
    }

    public void condition() throws IOException {
        expression();
        // ::= expression
    }

    public void expression() throws IOException {
        simple_expr();
        expressionf();
        // ::= simple_expr expressionf
    }

    public void expressionf() throws IOException {
        if (look.tag == '>' || look.tag == Tag.GE || look.tag == '<' ||
                look.tag == Tag.LE || look.tag == Tag.NE || look.tag == Tag.EQ) {
            relop();
            simple_expr();
        }
        // ::= relop simple_expr | λ
    }

    public void simple_expr() throws IOException {
        term();
        simple_exprf();
        // ::= term simple_exprf
    }

    public void simple_exprf() throws IOException {
        if (look.tag == '+' || look.tag == '-' || look.tag == Tag.OR) {
            addop();
            term();
            simple_exprf();
        }
        // ::= addop term simple_exprf | λ
    }

    public void term() throws IOException {
        factor_a();
        termf();
        // ::= factor_a termf
    }

    public void termf() throws IOException {
        if (look.tag == '*' || look.tag == '/' || look.tag == '%' || look.tag == Tag.AND) {
            mulop();
            factor_a();
            termf();
        }
        // ::= mulop factor_a termf | λ
    }

    public void factor_a() throws IOException {
        if (look.tag == Tag.NOT) {
            match(Tag.NOT);
            factor();
        } else if (look.tag == '-') {
            match('-');
            factor();
        } else {
            factor();
        }
        // ::= factor | not factor | "-" factor
    }

    public void factor() throws IOException {
        switch (look.tag) {
            case Tag.ID:
                match(Tag.ID);
                break;
            case Tag.NUM:
                match(Tag.NUM);
                break;
            case Tag.REAL:
                match(Tag.REAL);
                break;
            case Tag.LITERAL:
                match(Tag.LITERAL);
                break;
            case '(':
                match('(');
                expression();
                match(')');
                break;
            default:
                error("Erro de sintaxe. Esperado identificador, constante ou '(' mas encontrado: " + tagToString(look.tag));
                break;
        }
        // ::= identifier | constant | "(" expression ")"
    }

    public void relop() throws IOException {
        switch (look.tag) {
            case '>':
                match('>');
                break;
            case Tag.GE:
                match(Tag.GE);
                break;
            case '<':
                match('<');
                break;
            case Tag.LE:
                match(Tag.LE);
                break;
            case Tag.NE:
                match(Tag.NE);
                break;
            case Tag.EQ:
                match(Tag.EQ);
                break;
            default:
                error("Erro de sintaxe. Esperado operador relacional, mas encontrado: " + tagToString(look.tag));
                break;
        }
        // ::= ">" | ">=" | "<" | "<=" | "<>" | "="
    }

    public void addop() throws IOException {
        switch (look.tag) {
            case '+':
                match('+');
                break;
            case '-':
                match('-');
                break;
            case Tag.OR:
                match(Tag.OR);
                break;
            default:
                error("Erro de sintaxe. Esperado operador relacional, mas encontrado: " + tagToString(look.tag));
                break;
        }
        // ::= "+" | "_" | or
    }

    public void mulop() throws IOException {
        switch (look.tag) {
            case '*':
                match('*');
                break;
            case '/':
                match('/');
                break;
            case '%':
                match('%');
                break;
            case Tag.AND:
                match(Tag.AND);
                break;
            default:
                error("Erro de sintaxe. Esperado operador relacional, mas encontrado: " + tagToString(look.tag));
                break;
        }
        // ::= "*" | "/" | "%" | and
    }

    // O método match verifica se o token atual é o esperado.
    // Se for, ele consome o token e avança. Se não, dispara um erro.
    void match(int t) throws IOException {
        if (look.tag == t) {
            move();
        } else {
            error("Erro de sintaxe. Esperado: " + tagToString(t) + " encontrado: " + tagToString(look.tag));
        }
    }

    private String tagToString(int tag) {
        switch (tag) {
            case Tag.NUM: return "NUM";
            case Tag.REAL: return "REAL";
            case Tag.ID: return "ID";
            case Tag.LITERAL: return "LITERAL";
            case Tag.CLASS: return "CLASS";
            case Tag.INT: return "INT";
            case Tag.STRING: return "STRING";
            case Tag.FLOAT: return "FLOAT";
            case Tag.IF: return "IF";
            case Tag.ELSE: return "ELSE";
            case Tag.DO: return "DO";
            case Tag.WHILE: return "WHILE";
            case Tag.REPEAT: return "REPEAT";
            case Tag.UNTIL: return "UNTIL";
            case Tag.READ: return "READ";
            case Tag.WRITE: return "WRITE";
            case Tag.ASSIGN: return ":=";
            case Tag.EQ: return "=";
            case Tag.NE: return "<>";
            case Tag.LE: return "<=";
            case Tag.GE: return ">=";
            case Tag.AND: return "AND";
            case Tag.OR: return "OR";
            case Tag.NOT: return "NOT";
            case Tag.EOF: return "EOF";
            default:
                if (tag < 256) {
                    return String.valueOf((char) tag);
                }
                return String.valueOf(tag);
        }
    }

    void error(String s) {
        throw new Error("Perto da linha " + lex.getLine() + ": " + s);
    }
}
