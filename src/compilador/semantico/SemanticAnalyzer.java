package compilador.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;
import java.util.List;

public class SemanticAnalyzer {
    public enum Type {
        INT,
        FLOAT,
        STRING,
        BOOLEAN,
        VOID,
        ERROR
    }

    private final Map<String, Type> symbols = new HashMap<>();
    private final List<String> errors = new ArrayList<>();

    public void declare(String name, Type type, int line) {
        if (symbols.containsKey(name)) {
            semanticError(line, "Identificador já declarado: " + name);
            return;
        }
        symbols.put(name, type);
    }

    public Type resolve(String name, int line) {
        Type type = symbols.get(name);
        if (type == null) {
            semanticError(line, "Identificador não declarado: " + name);
            return Type.ERROR;
        }
        return type;
    }

    public void ensureAssignable(String targetName, Type valueType, int line) {
        Type targetType = resolve(targetName, line);
        if (targetType != Type.ERROR && valueType != Type.ERROR && targetType != valueType) {
            semanticError(line, "Tipos incompatíveis em atribuição para " + targetName + ": esperado " + targetType + " encontrado " + valueType);
        }
    }

    public void ensureBoolean(Type type, int line, String context) {
        if (type == Type.ERROR) {
            return;
        }
        if (type != Type.BOOLEAN) {
            semanticError(line, context + " espera expressão lógica, mas encontrou " + type);
        }
    }

    public Type applyAddOp(Type left, int operator, Type right, int line) {
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        if (operator == '+') {
            if (left == Type.STRING || right == Type.STRING) {
                return Type.STRING;
            }
            return numericResult(left, right, line, "+");
        }

        if (operator == '-') {
            return numericResult(left, right, line, "-");
        }

        semanticError(line, "Operador aditivo inválido");
        return Type.ERROR;
    }

    public Type applyMulOp(Type left, int operator, Type right, int line) {
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        if (operator == '%') {
            if (left == Type.INT && right == Type.INT) {
                return Type.INT;
            }
            semanticError(line, "Operador % só pode ser aplicado a operandos inteiros");
        }

        if (operator == compilador.lexico.Tag.AND) {
            if (left == Type.BOOLEAN && right == Type.BOOLEAN) {
                return Type.BOOLEAN;
            }
            semanticError(line, "Operador and só pode ser aplicado a operandos booleanos");
        }

        if (left == Type.STRING || right == Type.STRING) {
            semanticError(line, "Operador multiplicativo inválido para string");
        }

        return numericResult(left, right, line, String.valueOf((char) operator));
    }

    public Type applyUnaryMinus(Type operand, int line) {
        if (operand == Type.ERROR) {
            return Type.ERROR;
        }
        if (operand == Type.INT || operand == Type.FLOAT) {
            return operand;
        }
        semanticError(line, "Operador unary '-' espera tipo numérico");
        return Type.ERROR;
    }

    public Type applyNot(Type operand, int line) {
        if (operand == Type.ERROR) {
            return Type.ERROR;
        }
        if (operand == Type.BOOLEAN) {
            return Type.BOOLEAN;
        }
        semanticError(line, "Operador not espera tipo booleano");
        return Type.ERROR;
    }

    public Type applyRelOp(Type left, int operator, Type right, int line) {
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        boolean numericLeft = isNumeric(left);
        boolean numericRight = isNumeric(right);

        if (numericLeft && numericRight) {
            return Type.BOOLEAN;
        }

        if (left == right && (left == Type.STRING || left == Type.BOOLEAN)) {
            if (operator == '=' || operator == compilador.lexico.Tag.NE) {
                return Type.BOOLEAN;
            }
            semanticError(line, "Somente = e <> podem ser aplicados a " + left);
        }

        semanticError(line, "Operação relacional incompatível entre " + left + " e " + right);
        return Type.ERROR;
    }

    public Type applyBooleanOp(Type left, int operator, Type right, int line) {
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        if (left == Type.BOOLEAN && right == Type.BOOLEAN) {
            return Type.BOOLEAN;
        }
        semanticError(line, "Operação lógica inválida: esperados operandos booleanos");
        return Type.ERROR;
    }

    public Type numericResult(Type left, Type right, int line, String operator) {
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        if (!isNumeric(left) || !isNumeric(right)) {
            semanticError(line, "Operador " + operator + " espera operandos numéricos");
            return Type.ERROR;
        }
        if (left == Type.FLOAT || right == Type.FLOAT) {
            return Type.FLOAT;
        }
        return Type.INT;
    }

    public boolean isNumeric(Type type) {
        return type == Type.INT || type == Type.FLOAT;
    }

    public Type promoteDivision(Type left, Type right, int line) {
        if (left == Type.ERROR || right == Type.ERROR) {
            return Type.ERROR;
        }
        if (!isNumeric(left) || !isNumeric(right)) {
            semanticError(line, "Operador / espera operandos numéricos");
            return Type.ERROR;
        }
        return Type.FLOAT;
    }

    public Type getTypeFromKeyword(int tag) {
        if (tag == compilador.lexico.Tag.INT) {
            return Type.INT;
        }
        if (tag == compilador.lexico.Tag.FLOAT) {
            return Type.FLOAT;
        }
        if (tag == compilador.lexico.Tag.STRING) {
            return Type.STRING;
        }
        semanticError(-1, "Tipo inválido");
        return Type.ERROR;
    }

    private void semanticError(int line, String message) {
        if (line >= 0) {
            errors.add("Perto da linha " + line + ": Erro semântico. " + message);
        } else {
            errors.add("Erro semântico. " + message);
        }
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void clearErrors() {
        errors.clear();
    }
}