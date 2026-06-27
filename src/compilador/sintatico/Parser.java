package compilador.sintatico;

import compilador.lexico.Lexer;
import compilador.lexico.Tag;
import compilador.lexico.Token;
import compilador.lexico.Word;
import compilador.semantico.SemanticAnalyzer;
import compilador.semantico.SemanticAnalyzer.Type;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser {
    private final Lexer lex;
    private Token look;
    private final SemanticAnalyzer semantic;
    private final List<String> errors = new ArrayList<>();

    private static class ParseFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public Parser(Lexer l) throws IOException {
        this.lex = l;
        this.semantic = new SemanticAnalyzer();
        move();
    }

    void move() throws IOException {
        look = lex.scan();
        if (look == null) {
            look = new Token(Tag.EOF);
        }
    }

    // program ::= class identifier "{" [decl-list] body "}"
    public void program() throws IOException {
        try {
            match(Tag.CLASS);
            match(Tag.ID);
            match('{');

            if (isTypeToken(look.tag)) {
                decl_list();
            }

            body();
        } catch (RuntimeException | Error e) {
            recordError(e.getMessage());
        }
    }

    // decl-list ::= decl ";" { decl ";" }
    public void decl_list() throws IOException {
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

    // decl ::= type ident-list
    public void decl() throws IOException {
        Type declaredType = type();
        ident_list(declaredType);
    }

    // ident-list ::= identifier { "," identifier }
    public void ident_list(Type declaredType) throws IOException {
        declareIdentifier(declaredType);
        while (look.tag == ',') {
            match(',');
            declareIdentifier(declaredType);
        }
    }

    private void declareIdentifier(Type declaredType) throws IOException {
        if (look.tag != Tag.ID) {
            error("Esperado identificador, mas encontrado: " + tagToString(look.tag));
        }
        String identifier = currentIdentifier();
        semantic.declare(identifier, declaredType, lex.getLine());
        match(Tag.ID);
    }

    // type ::= int | string | float
    public Type type() throws IOException {
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
                error("Esperado tipo (int, string ou float), mas encontrado: " + tagToString(look.tag));
                return Type.ERROR;
        }
    }

    // body ::= "{" stmt-list "}"
    public void body() throws IOException {
        match('{');
        stmt_list();
        match('}');
    }

    // stmt-list ::= stmt ";" { stmt ";" }
    public void stmt_list() throws IOException {
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

    // stmt ::= assign-stmt | if-stmt | do-stmt | repeat-stmt | read-stmt | write-stmt
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
                    error("Fim de arquivo inesperado.");
                }
                error("Comando inválido ou malformado. Encontrado: " + tagToString(look.tag));
        }
    }

    // assign-stmt ::= identifier ":=" simple-expr
    public void assign_stmt() throws IOException {
        String identifier = currentIdentifier();
        Type targetType = semantic.resolve(identifier, lex.getLine());
        match(Tag.ID);
        match(Tag.ASSIGN);
        Type valueType = simple_expr();
        if (targetType != Type.ERROR && valueType != Type.ERROR && targetType != valueType) {
            error("Tipos incompatíveis em atribuição para " + identifier
                    + ": esperado " + targetType + " encontrado " + valueType);
        }
    }

    // if-stmt ::= if "(" condition ")" "{" stmt-list "}" if-stmt'
    public void if_stmt() throws IOException {
        match(Tag.IF);
        match('(');
        Type condType = condition();
        semantic.ensureBoolean(condType, lex.getLine(), "Comando if");
        match(')');
        match('{');
        stmt_list();
        match('}');
        if_stmtf();
    }

    // if-stmt' ::= else "{" stmt-list "}" | λ
    public void if_stmtf() throws IOException {
        if (look.tag == Tag.ELSE) {
            match(Tag.ELSE);
            match('{');
            stmt_list();
            match('}');
        }
        // λ
    }

    // do-stmt ::= do "{" stmt-list "}" do-suffix
    public void do_stmt() throws IOException {
        match(Tag.DO);
        match('{');
        stmt_list();
        match('}');
        do_suffix();
    }

    // do-suffix ::= while "(" condition ")"
    public void do_suffix() throws IOException {
        match(Tag.WHILE);
        match('(');
        Type condType = condition();
        semantic.ensureBoolean(condType, lex.getLine(), "Comando while");
        match(')');
    }

    // repeat-stmt ::= repeat "{" stmt-list "}" stmt-suffix
    public void repeat_stmt() throws IOException {
        match(Tag.REPEAT);
        match('{');
        stmt_list();
        match('}');
        stmt_suffix();
    }

    // stmt-suffix ::= until "(" condition ")"
    public void stmt_suffix() throws IOException {
        match(Tag.UNTIL);
        match('(');
        Type condType = condition();
        semantic.ensureBoolean(condType, lex.getLine(), "Comando until");
        match(')');
    }

    // read-stmt ::= read "(" identifier ")"
    public void read_stmt() throws IOException {
        match(Tag.READ);
        match('(');
        String identifier = currentIdentifier();
        semantic.resolve(identifier, lex.getLine());
        match(Tag.ID);
        match(')');
    }

    // write-stmt ::= write "(" writable ")"
    public void write_stmt() throws IOException {
        match(Tag.WRITE);
        match('(');
        writable();
        match(')');
    }

    // writable ::= simple-expr
    public Type writable() throws IOException {
        return simple_expr();
    }

    // condition ::= expression
    public Type condition() throws IOException {
        return expression();
    }

    // expression ::= simple-expr expression'
    public Type expression() throws IOException {
        Type left = simple_expr();
        return expressionf(left);
    }

    // expression' ::= relop simple-expr | λ
    public Type expressionf(Type left) throws IOException {
        if (isRelop(look.tag)) {
            int op = look.tag;
            relop();
            Type right = simple_expr();
            return semantic.applyRelOp(left, op, right, lex.getLine());
        }
        // λ
        return left;
    }

    // simple-expr ::= term simple-expr'
    public Type simple_expr() throws IOException {
        Type left = term();
        return simple_exprf(left);
    }

    // simple-expr' ::= addop term simple-expr' | λ
    public Type simple_exprf(Type left) throws IOException {
        if (look.tag == '+' || look.tag == '-' || look.tag == Tag.OR) {
            int op = look.tag;
            addop();
            Type right = term();
            Type result;
            if (op == Tag.OR) {
                result = semantic.applyBooleanOp(left, Tag.OR, right, lex.getLine());
            } else {
                result = semantic.applyAddOp(left, op, right, lex.getLine());
            }
            return simple_exprf(result);
        }
        // λ
        return left;
    }

    // term ::= factor-a term'
    public Type term() throws IOException {
        Type left = factor_a();
        return termf(left);
    }

    // term' ::= mulop factor-a term' | λ
    public Type termf(Type left) throws IOException {
        if (look.tag == '*' || look.tag == '/' || look.tag == '%' || look.tag == Tag.AND) {
            int op = look.tag;
            mulop();
            Type right = factor_a();
            Type result;
            if (op == Tag.AND) {
                result = semantic.applyBooleanOp(left, Tag.AND, right, lex.getLine());
            } else if (op == '/') {
                result = semantic.promoteDivision(left, right, lex.getLine());
            } else {
                result = semantic.applyMulOp(left, op, right, lex.getLine());
            }
            return termf(result);
        }
        // λ
        return left;
    }

    // factor-a ::= factor | not factor | "-" factor
    public Type factor_a() throws IOException {
        if (look.tag == Tag.NOT) {
            match(Tag.NOT);
            Type operand = factor();
            return semantic.applyNot(operand, lex.getLine());
        }
        if (look.tag == '-') {
            match('-');
            Type operand = factor();
            return semantic.applyUnaryMinus(operand, lex.getLine());
        }
        return factor();
    }

    // factor ::= identifier | constant | "(" expression ")"
    public Type factor() throws IOException {
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
            case '(': {
                match('(');
                Type type = expression();
                match(')');
                return type;
            }
            default:
                error("Esperado identificador, constante ou '(' mas encontrado: " + tagToString(look.tag));
                return Type.ERROR;
        }
    }

    // relop ::= ">" | ">=" | "<" | "<=" | "<>" | "="
    public void relop() throws IOException {
        switch (look.tag) {
            case '>':    match('>');    break;
            case Tag.GE: match(Tag.GE); break;
            case '<':    match('<');    break;
            case Tag.LE: match(Tag.LE); break;
            case Tag.NE: match(Tag.NE); break;
            case Tag.EQ: match(Tag.EQ); break;
            default:
                error("Esperado operador relacional, mas encontrado: " + tagToString(look.tag));
        }
    }

    // addop ::= "+" | "-" | or
    public void addop() throws IOException {
        switch (look.tag) {
            case '+':    match('+');    break;
            case '-':    match('-');    break;
            case Tag.OR: match(Tag.OR); break;
            default:
                error("Esperado operador aditivo (+, - ou or), mas encontrado: " + tagToString(look.tag));
        }
    }

    // mulop ::= "*" | "/" | "%" | and
    public void mulop() throws IOException {
        switch (look.tag) {
            case '*':     match('*');      break;
            case '/':     match('/');      break;
            case '%':     match('%');      break;
            case Tag.AND: match(Tag.AND);  break;
            default:
                error("Esperado operador multiplicativo (*, /, % ou and), mas encontrado: " + tagToString(look.tag));
        }
    }

    void match(int t) throws IOException {
        if (look.tag == t) {
            move();
        } else {
            error("Esperado: " + tagToString(t) + " encontrado: " + tagToString(look.tag));
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
        return tag == Tag.ID || tag == Tag.IF || tag == Tag.DO
                || tag == Tag.REPEAT || tag == Tag.READ || tag == Tag.WRITE;
    }

    private boolean isRelop(int tag) {
        return tag == '>' || tag == Tag.GE || tag == '<'
                || tag == Tag.LE || tag == Tag.NE || tag == Tag.EQ;
    }

    private boolean isTypeToken(int tag) {
        return tag == Tag.INT || tag == Tag.STRING || tag == Tag.FLOAT;
    }

    private String currentIdentifier() {
        return ((Word) look).getLexeme();
    }

    private String tagToString(int tag) {
        switch (tag) {
            case Tag.NUM:     return "NUM";
            case Tag.REAL:    return "REAL";
            case Tag.ID:      return "ID";
            case Tag.LITERAL: return "LITERAL";
            case Tag.CLASS:   return "CLASS";
            case Tag.INT:     return "INT";
            case Tag.STRING:  return "STRING";
            case Tag.FLOAT:   return "FLOAT";
            case Tag.IF:      return "IF";
            case Tag.ELSE:    return "ELSE";
            case Tag.DO:      return "DO";
            case Tag.WHILE:   return "WHILE";
            case Tag.REPEAT:  return "REPEAT";
            case Tag.UNTIL:   return "UNTIL";
            case Tag.READ:    return "READ";
            case Tag.WRITE:   return "WRITE";
            case Tag.ASSIGN:  return ":=";
            case Tag.EQ:      return "=";
            case Tag.NE:      return "<>";
            case Tag.LE:      return "<=";
            case Tag.GE:      return ">=";
            case Tag.AND:     return "AND";
            case Tag.OR:      return "OR";
            case Tag.NOT:     return "NOT";
            case Tag.EOF:     return "EOF";
            default:
                if (tag < 256) return String.valueOf((char) tag);
                return String.valueOf(tag);
        }
    }

    void error(String s) {
        String message = "Perto da linha " + lex.getLine() + ": " + s;
        errors.add(message);
        throw new ParseFailure();
    }

    private void recordError(String message) {
        if (message == null || message.isBlank()) return;
        errors.add(message);
    }
}