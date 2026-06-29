package compilador.sintatico;

import compilador.lexico.Num;
import compilador.lexico.Real;
import compilador.lexico.Literal;
import compilador.lexico.Lexer;
import compilador.lexico.Tag;
import compilador.lexico.Token;
import compilador.lexico.Word;
import compilador.semantico.SemanticAnalyzer;
import compilador.semantico.SemanticAnalyzer.Type;
import compilador.gerador.JasminGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser {
    private final Lexer lex;
    private Token look;
    private final SemanticAnalyzer semantic;
    private final List<String> errors = new ArrayList<>();
    
    private final JasminGenerator generator;
    private String programName = "Programa";

    private static class ExprResult {
        final Type type;
        final String code;
        ExprResult(Type type, String code) {
            this.type = type;
            this.code = code;
        }
    }

    private static class ParseFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public Parser(Lexer l) throws IOException {
        this.lex = l;
        this.semantic = new SemanticAnalyzer();
        this.generator = new JasminGenerator();
        move();
    }

    public JasminGenerator getGenerator() {
        return generator;
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
            if (look.tag == Tag.ID) {
                programName = currentIdentifier();
            }
            match(Tag.ID);
            generator.begin(programName);
            match('{');

            if (look.tag == Tag.INT || look.tag == Tag.STRING || look.tag == Tag.FLOAT) {
                decl_list();
            }

            body();
            match('}');
            generator.finish();
            if(look.tag != Tag.EOF) {
                error("Erro de sintaxe. Fim de arquivo esperado, mas encontrado: " + tagToString(look.tag));
            }
        } catch (RuntimeException | Error e) {
            recordError(e.getMessage());
        }
    }

    // decl-list ::= decl ";" { decl ";" }
    public void decl_list() throws IOException {
        while (look.tag == Tag.INT || look.tag == Tag.STRING || look.tag == Tag.FLOAT) {
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
            error("Erro de sintaxe. Esperado identificador, mas encontrado: " + tagToString(look.tag));
        }
        String identifier = currentIdentifier();
        semantic.declare(identifier, declaredType, lex.getLine());
        generator.declare(identifier, declaredType);
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
                error("Erro de sintaxe. Esperado tipo (int, string ou float), mas encontrado: " + tagToString(look.tag));
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
        while (look.tag == Tag.ID ||
                look.tag == Tag.IF ||
                look.tag == Tag.DO ||
                look.tag == Tag.REPEAT ||
                look.tag == Tag.READ ||
                look.tag == Tag.WRITE) {
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
                    error("Erro de sintaxe. Fim de arquivo inesperado.");
                }
                error("Erro de sintaxe. Comando inválido ou malformado. Encontrado: " + tagToString(look.tag));
        }
    }

    // assign-stmt ::= identifier ":=" simple-expr
    public void assign_stmt() throws IOException {
        String identifier = currentIdentifier();
        Type targetType = semantic.resolve(identifier, lex.getLine());
        match(Tag.ID);
        match(Tag.ASSIGN);
        ExprResult expr = simple_expr();
        if (targetType != Type.ERROR && expr.type != Type.ERROR && targetType != expr.type) {
            error("Tipos incompatíveis em atribuição para " + identifier
                    + ": esperado " + targetType + " encontrado " + expr.type);
        }
        generator.append(expr.code);
        generator.append(generator.storeVar(identifier));
    }

    // if-stmt ::= if "(" condition ")" "{" stmt-list "}" if-stmt'
    public void if_stmt() throws IOException {
        match(Tag.IF);
        match('(');
        ExprResult cond = condition();
        semantic.ensureBoolean(cond.type, lex.getLine(), "Comando if");
        match(')');
        
        String labelElse = generator.newLabel("L_if_else");
        String labelEnd = generator.newLabel("L_if_end");
        
        generator.append(cond.code);
        generator.append("ifeq " + labelElse + "\n");
        
        match('{');
        stmt_list();
        match('}');
        
        generator.append("goto " + labelEnd + "\n");
        generator.append(labelElse + ":\n");
        
        if_stmtf(labelEnd);
    }

    // if-stmt' ::= else "{" stmt-list "}" | λ
    public void if_stmtf(String labelEnd) throws IOException {
        if (look.tag == Tag.ELSE) {
            match(Tag.ELSE);
            match('{');
            stmt_list();
            match('}');
        }
        generator.append(labelEnd + ":\n");
    }

    // do-stmt ::= do "{" stmt-list "}" do-suffix
    public void do_stmt() throws IOException {
        match(Tag.DO);
        String labelStart = generator.newLabel("L_do_start");
        generator.append(labelStart + ":\n");
        match('{');
        stmt_list();
        match('}');
        do_suffix(labelStart);
    }

    // do-suffix ::= while "(" condition ")"
    public void do_suffix(String labelStart) throws IOException {
        match(Tag.WHILE);
        match('(');
        ExprResult cond = condition();
        semantic.ensureBoolean(cond.type, lex.getLine(), "Comando while");
        match(')');
        generator.append(cond.code);
        generator.append("ifne " + labelStart + "\n");
    }

    // repeat-stmt ::= repeat "{" stmt-list "}" stmt-suffix
    public void repeat_stmt() throws IOException {
        match(Tag.REPEAT);
        String labelStart = generator.newLabel("L_repeat_start");
        generator.append(labelStart + ":\n");
        match('{');
        stmt_list();
        match('}');
        stmt_suffix(labelStart);
    }

    // stmt-suffix ::= until "(" condition ")"
    public void stmt_suffix(String labelStart) throws IOException {
        match(Tag.UNTIL);
        match('(');
        ExprResult cond = condition();
        semantic.ensureBoolean(cond.type, lex.getLine(), "Comando until");
        match(')');
        generator.append(cond.code);
        generator.append("ifeq " + labelStart + "\n");
    }

    // read-stmt ::= read "(" identifier ")"
    public void read_stmt() throws IOException {
        match(Tag.READ);
        match('(');
        String identifier = currentIdentifier();
        Type type = semantic.resolve(identifier, lex.getLine());
        match(Tag.ID);
        match(')');
        generator.append(generator.read(identifier, type));
    }

    // write-stmt ::= write(" writable ")
    public void write_stmt() throws IOException {
        match(Tag.WRITE);
        match('(');
        ExprResult expr = writable();
        match(')');
        generator.append(generator.print(expr.code, expr.type));
    }

    // writable ::= simple-expr
    public ExprResult writable() throws IOException {
        return simple_expr();
    }

    // condition ::= expression
    public ExprResult condition() throws IOException {
        return expression();
    }

    // expression ::= simple-expr expression'
    public ExprResult expression() throws IOException {
        ExprResult left = simple_expr();
        return expressionf(left);
    }

    // expression' ::= relop simple-expr | λ
    public ExprResult expressionf(ExprResult left) throws IOException {
        if (look.tag == '>' || look.tag == Tag.GE || look.tag == '<' ||
                look.tag == Tag.LE || look.tag == Tag.NE || look.tag == Tag.EQ) {
            int op = look.tag;
            relop();
            ExprResult right = simple_expr();
            Type resType = semantic.applyRelOp(left.type, op, right.type, lex.getLine());
            String code = generator.relational(left.code, left.type, right.code, right.type, op);
            return new ExprResult(resType, code);
        }
        return left;
    }

    // simple-expr ::= term simple-expr'
    public ExprResult simple_expr() throws IOException {
        ExprResult left = term();
        return simple_exprf(left);
    }

    // simple-expr' ::= addop term simple-expr' | λ
    public ExprResult simple_exprf(ExprResult left) throws IOException {
        if (look.tag == '+' || look.tag == '-' || look.tag == Tag.OR) {
            int op = look.tag;
            addop();
            ExprResult right = term();
            Type resultType;
            String code;
            if (op == Tag.OR) {
                resultType = semantic.applyBooleanOp(left.type, Tag.OR, right.type, lex.getLine());
                code = generator.booleanOp(left.code, right.code, Tag.OR);
            } else {
                resultType = semantic.applyAddOp(left.type, op, right.type, lex.getLine());
                if (op == '+' && (left.type == Type.STRING || right.type == Type.STRING)) {
                    code = generator.stringConcat(left.code, left.type, right.code, right.type);
                } else {
                    code = generator.arithmetic(left.code, left.type, right.code, right.type, op, resultType);
                }
            }
            return simple_exprf(new ExprResult(resultType, code));
        }
        return left;
    }

    // term ::= factor-a term'
    public ExprResult term() throws IOException {
        ExprResult left = factor_a();
        return termf(left);
    }

    // term' ::= mulop factor-a term' | λ
    public ExprResult termf(ExprResult left) throws IOException {
        if (look.tag == '*' || look.tag == '/' || look.tag == '%' || look.tag == Tag.AND) {
            int op = look.tag;
            mulop();
            ExprResult right = factor_a();
            Type resultType;
            String code;
            if (op == Tag.AND) {
                resultType = semantic.applyBooleanOp(left.type, Tag.AND, right.type, lex.getLine());
                code = generator.booleanOp(left.code, right.code, Tag.AND);
            } else if (op == '/') {
                resultType = semantic.promoteDivision(left.type, right.type, lex.getLine());
                code = generator.arithmetic(left.code, left.type, right.code, right.type, op, resultType);
            } else {
                resultType = semantic.applyMulOp(left.type, op, right.type, lex.getLine());
                code = generator.arithmetic(left.code, left.type, right.code, right.type, op, resultType);
            }
            return termf(new ExprResult(resultType, code));
        }
        return left;
    }

    // factor-a ::= factor | not factor | "-" factor
    public ExprResult factor_a() throws IOException {
        if (look.tag == Tag.NOT) {
            match(Tag.NOT);
            ExprResult operand = factor();
            Type resType = semantic.applyNot(operand.type, lex.getLine());
            String code = generator.unaryNot(operand.code);
            return new ExprResult(resType, code);
        }
        if (look.tag == '-') {
            match('-');
            ExprResult operand = factor();
            Type resType = semantic.applyUnaryMinus(operand.type, lex.getLine());
            String code = generator.unaryMinus(operand.code, operand.type);
            return new ExprResult(resType, code);
        }
        return factor();
    }

    // factor ::= identifier | constant | "(" expression ")"
// factor ::= identifier | constant | "(" expression ")"
// factor ::= identifier | constant | "(" expression ")"
    public ExprResult factor() throws IOException {
        switch (look.tag) {
            case Tag.ID: {
                String identifier = currentIdentifier();
                Type type = semantic.resolve(identifier, lex.getLine());
                String code = generator.loadVar(identifier);
                match(Tag.ID);
                return new ExprResult(type, code);
            }
            case Tag.NUM: {
                // Cast correto para a classe Num
                int val = ((Num) look).value; 
                String code = generator.intConst(val);
                match(Tag.NUM);
                return new ExprResult(Type.INT, code);
            }
            case Tag.REAL: {
                // Cast correto para a classe Real
                float val = ((Real) look).value; 
                String code = generator.floatConst(val);
                match(Tag.REAL);
                return new ExprResult(Type.FLOAT, code);
            }
            case Tag.LITERAL: {
                // Cast correto para a classe Literal
                String val = ((Literal) look).value; 
                String code = generator.stringConst(val);
                match(Tag.LITERAL);
                return new ExprResult(Type.STRING, code);
            }
            case '(': {
                match('(');
                ExprResult res = expression();
                match(')');
                return res;
            }
            default:
                error("Erro de sintaxe. Esperado identificador, constante ou '(' mas encontrado: " + tagToString(look.tag));
                return new ExprResult(Type.ERROR, "");
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
                error("Erro de sintaxe. Esperado operador relacional, mas encontrado: " + tagToString(look.tag));
        }
    }

    // addop ::= "+" | "-" | or
    public void addop() throws IOException {
        switch (look.tag) {
            case '+':    match('+');    break;
            case '-':    match('-');    break;
            case Tag.OR: match(Tag.OR); break;
            default:
                error("Erro de sintaxe. Esperado operador aditivo (+, - ou or), mas encontrado: " + tagToString(look.tag));
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
                error("Erro de sintaxe. Esperado operador multiplicativo (*, /, % ou and), mas encontrado: " + tagToString(look.tag));
        }
    }

    void match(int t) throws IOException {
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
        String message = "Erro de sintaxe. Perto da linha " + lex.getLine() + ": " + s;
        errors.add(message);
        throw new ParseFailure();
    }

    private void recordError(String message) {
        if (message == null || message.isBlank()) return;
        errors.add(message);
    }
}