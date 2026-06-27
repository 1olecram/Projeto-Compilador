package compilador.sintatico;

import compilador.lexico.Lexer;
import compilador.lexico.Literal;
import compilador.lexico.Num;
import compilador.lexico.Real;
import compilador.lexico.Tag;
import compilador.lexico.Token;
import compilador.lexico.Word;
import compilador.semantico.SemanticAnalyzer;
import compilador.semantico.SemanticAnalyzer.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;

public class Parser {
    private final Lexer lex;
    private Token look;
    private final SemanticAnalyzer semantic;
    private final List<String> errors = new ArrayList<>();

    private static class ParseFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    // Construtor: recebe o lexer e prepara o primeiro token para o parsing.
    public Parser(Lexer l) throws IOException {
        this.lex = l;
        this.semantic = new SemanticAnalyzer();
        move();
    }

    // Método para ler o próximo token da entrada
    void move() throws IOException {
        look = lex.scan();
        if (look == null) {
            look = new Token(Tag.EOF);
        }
    }

    public void program() throws IOException {
        // programa ::= class identifier "{" [decl-list] body "}"
        try {
            match(Tag.CLASS);
            match(Tag.ID);
            match('{');

            if (isTypeToken(look.tag)) {
                decl_list();
            }

            body();
            match('}');
        } catch (RuntimeException | Error e) {
            recordError(e.getMessage());
        }
    }

    public void decl_list() throws IOException {
        // decl-list ::= decl ";" { decl ";" }
        while (isTypeToken(look.tag)) {
            try {
                decl();
                match(';');
            } catch (RuntimeException | Error e) {
                recordError(e.getMessage());
                recoverToDeclarationBoundary();
            }
        }
    }

    public void decl() throws IOException {
        // decl ::= type ident-list
        Type declaredType = type();
        ident_list(declaredType);
    }

    public void ident_list(Type declaredType) throws IOException {
        declareIdentifier(declaredType);
        while (look.tag == ',') {
            match(',');
            declareIdentifier(declaredType);
        }
    }

    private void declareIdentifier(Type declaredType) throws IOException {
        if (look.tag != Tag.ID) {
            error("Erro de sintaxe. Esperado identificador, mas encontrado: " + tagToString(look.tag));
        }
        String identifier = ((Word) look).getLexeme();
        semantic.declare(identifier, declaredType, lex.getLine());
        match(Tag.ID);
    }

    public Type type() throws IOException {
        // type ::= int | string | float
        switch (look.tag) {
            case Tag.INT:
                match(Tag.INT);
                return Type.INT;
            case Tag.STRING:
                match(Tag.STRING);
                return Type.STRING;
            case Tag.FLOAT:
                match(Tag.FLOAT);
                return Type.FLOAT;
            default:
                error("Erro de sintaxe. Esperado tipo (int, string ou float), mas encontrado: " + tagToString(look.tag));
                return Type.ERROR;
        }
    }

    public void body() throws IOException {
        // body ::= "{" stmt-list "}"
        stmt_list();
    }

    public void stmt_list() throws IOException {
        // stmt-list ::= stmt ";" { stmt ";" }
        while (isStmtStart(look.tag)) {
            try {
                stmt();
                match(';');
            } catch (RuntimeException | Error e) {
                recordError(e.getMessage());
                recoverToStatementBoundary();
            }
        }
    }

    public void stmt() throws IOException {
        // stmt ::= assign-stmt | if-stmt | do-stmt | repeat-stmt | read-stmt | write-stmt
        try {
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
                    }
                    error("Erro de sintaxe. Comando inválido ou malformado. Encontrado: " + tagToString(look.tag));
            }
        } catch (RuntimeException | Error e) {
            throw e;
        }
    }

    public void assign_stmt() throws IOException {
        // assign-stmt ::= identifier ":=" simple_expr
        String identifier = currentIdentifier();
        Type targetType = semantic.resolve(identifier, lex.getLine());
        match(Tag.ID);
        match(Tag.ASSIGN);
        Type valueType = simple_expr();
        if (targetType != Type.ERROR && valueType != Type.ERROR && targetType != valueType) {
            error("Tipos incompatíveis em atribuição para " + identifier + ": esperado " + targetType + " encontrado " + valueType);
        }
    }

    public void if_stmt() throws IOException {
        // if-stmt ::= if "(" condition ")" "{" stmt-list "}" [else "{" stmt-list "}"]
        match(Tag.IF);
        match('(');
        Type conditionType = condition();
        semantic.ensureBoolean(conditionType, lex.getLine(), "Comando if");
        match(')');
        match('{');
        stmt_list();
        match('}');
        if_stmtf();
    }

    public void if_stmtf() throws IOException {
        if (look.tag == Tag.ELSE) {
            match(Tag.ELSE);
            match('{');
            stmt_list();
            match('}');
        }
    }

    public void do_stmt() throws IOException {
        // do-stmt ::= do "{" stmt-list "}" do-suffix
        match(Tag.DO);
        match('{');
        stmt_list();
        match('}');
        do_suffix();
    }

    public void do_suffix() throws IOException {
        // do-suffix ::= while "(" condition ")"
        match(Tag.WHILE);
        match('(');
        Type conditionType = condition();
        semantic.ensureBoolean(conditionType, lex.getLine(), "Comando while");
        match(')');
    }

    public void repeat_stmt() throws IOException {
        // repeat-stmt ::= repeat "{" stmt-list "}" stmt-suffix
        match(Tag.REPEAT);
        match('{');
        stmt_list();
        match('}');
        stmt_suffix();
    }

    public void stmt_suffix() throws IOException {
        // stmt-suffix ::= until "(" condition ")"
        match(Tag.UNTIL);
        match('(');
        Type conditionType = condition();
        semantic.ensureBoolean(conditionType, lex.getLine(), "Comando until");
        match(')');
    }

    public void read_stmt() throws IOException {
        // read-stmt ::= read "(" identifier ")"
        match(Tag.READ);
        match('(');
        String identifier = currentIdentifier();
        semantic.resolve(identifier, lex.getLine());
        match(Tag.ID);
        match(')');
    }

    public void write_stmt() throws IOException {
        // write-stmt ::= write "(" writable ")"
        match(Tag.WRITE);
        match('(');
        writable();
        match(')');
    }

    public Type writable() throws IOException {
        // writable ::= simple-expr
        return simple_expr();
    }

    public Type condition() throws IOException {
        // condition ::= expression
        return expression();
    }

    public Type expression() throws IOException {
        // expression ::= simple-expr { or simple-expr }
        Type left = and_expr();
        while (look.tag == Tag.OR) {
            match(Tag.OR);
            Type right = and_expr();
            left = semantic.applyBooleanOp(left, Tag.OR, right, lex.getLine());
        }
        return left;
    }

    private Type and_expr() throws IOException {
        // expressão auxiliar para dar precedência ao operador and
        Type left = rel_expr();
        while (look.tag == Tag.AND) {
            match(Tag.AND);
            Type right = rel_expr();
            left = semantic.applyBooleanOp(left, Tag.AND, right, lex.getLine());
        }
        return left;
    }

    private Type rel_expr() throws IOException {
        // expression ::= simple-expr [ relop simple-expr ]
        Type left = simple_expr();
        if (isRelop(look.tag)) {
            int op = look.tag;
            relop();
            Type right = simple_expr();
            left = semantic.applyRelOp(left, op, right, lex.getLine());
        }
        return left;
    }

    public Type simple_expr() throws IOException {
        // simple-expr ::= term { addop term }
        Type left = term();
        while (look.tag == '+' || look.tag == '-') {
            int op = look.tag;
            addop();
            Type right = term();
            left = semantic.applyAddOp(left, op, right, lex.getLine());
        }
        return left;
    }

    public Type term() throws IOException {
        // term ::= factor-a { mulop factor-a }
        Type left = factor_a();
        while (look.tag == '*' || look.tag == '/' || look.tag == '%') {
            int op = look.tag;
            mulop();
            Type right = factor_a();
            if (op == '/') {
                left = semantic.promoteDivision(left, right, lex.getLine());
            } else {
                left = semantic.applyMulOp(left, op, right, lex.getLine());
            }
        }
        return left;
    }

    public Type factor_a() throws IOException {
        // factor-a ::= factor | not factor | "-" factor
        if (look.tag == Tag.NOT) {
            match(Tag.NOT);
            return semantic.applyNot(factor_a(), lex.getLine());
        }
        if (look.tag == '-') {
            match('-');
            return semantic.applyUnaryMinus(factor_a(), lex.getLine());
        }
        return factor();
    }

    public Type factor() throws IOException {
        // factor ::= identifier | constant | "(" expression ")"
        switch (look.tag) {
            case Tag.ID: {
                String identifier = currentIdentifier();
                Type type = semantic.resolve(identifier, lex.getLine());
                match(Tag.ID);
                return type;
            }
            case Tag.NUM:
                match(Tag.NUM);
                return Type.INT;
            case Tag.REAL:
                match(Tag.REAL);
                return Type.FLOAT;
            case Tag.LITERAL:
                match(Tag.LITERAL);
                return Type.STRING;
            case '(':
                match('(');
                Type type = expression();
                match(')');
                return type;
            default:
                error("Erro de sintaxe. Esperado identificador, constante ou '(' mas encontrado: " + tagToString(look.tag));
                return Type.ERROR;
        }
    }

    public void relop() throws IOException {
        // relop ::= ">" | ">=" | "<" | "<=" | "<>" | "="
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
        }
        // ::= ">" | ">=" | "<" | "<=" | "<>" | "="
    }

    public void addop() throws IOException {
        // addop ::= "+" | "-" | or
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
                error("Erro de sintaxe. Esperado operador aditivo, mas encontrado: " + tagToString(look.tag));
        }
        // ::= "+" | "_" | or
    }

    public void mulop() throws IOException {
        // mulop ::= "*" | "/" | "%" | and
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
                error("Erro de sintaxe. Esperado operador multiplicativo, mas encontrado: " + tagToString(look.tag));
        }
        // ::= "*" | "/" | "%" | and
    }

    // O método match verifica se o token atual é o esperado.
    // Se for, ele consome o token e avança. Se não, dispara um erro.
    void match(int t) throws IOException {
        // Consome o token atual se ele corresponder ao esperado.
        if (look.tag == t) {
            move();
        } else {
            error("Erro de sintaxe. Esperado: " + tagToString(t) + " encontrado: " + tagToString(look.tag));
        }
    }

    public List<String> getErrors() {
        List<String> allErrors = new ArrayList<>(errors);
        allErrors.addAll(semantic.getErrors());
        return Collections.unmodifiableList(allErrors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty() || semantic.hasErrors();
    }

    private void recoverToStatementBoundary() throws IOException {
        while (look.tag != Tag.EOF && look.tag != ';' && look.tag != '}') {
            move();
        }
        if (look.tag == ';') {
            move();
        }
    }

    private void recoverToDeclarationBoundary() throws IOException {
        while (look.tag != Tag.EOF && look.tag != ';' && look.tag != '{' && look.tag != '}') {
            move();
        }
        if (look.tag == ';') {
            move();
        }
    }

    private boolean isStmtStart(int tag) {
        return tag == Tag.ID || tag == Tag.IF || tag == Tag.DO || tag == Tag.REPEAT || tag == Tag.READ || tag == Tag.WRITE;
    }

    private boolean isRelop(int tag) {
        return tag == '>' || tag == Tag.GE || tag == '<' || tag == Tag.LE || tag == Tag.NE || tag == Tag.EQ;
    }

    private boolean isTypeToken(int tag) {
        return tag == Tag.INT || tag == Tag.STRING || tag == Tag.FLOAT;
    }

    private String currentIdentifier() {
        return ((Word) look).getLexeme();
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
        String message = "Perto da linha " + lex.getLine() + ": " + s;
        errors.add(message);
        throw new ParseFailure();
    }

    private void recordError(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        errors.add(message);
    }
}
