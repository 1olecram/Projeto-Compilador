package compilador.gerador;

import compilador.semantico.SemanticAnalyzer.Type;
import compilador.lexico.Tag;
import java.util.HashMap;
import java.util.Map;

public class JasminGenerator {
    private String programName;
    private final StringBuilder fieldsBlock = new StringBuilder();
    private final StringBuilder methodBlock = new StringBuilder();
    private final Map<String, Type> varTypes = new HashMap<>();
    private int labelCounter = 0;

    public void begin(String programName) {
        this.programName = programName;
    }

    public void finish() {
        // Finalização opcional do fluxo de acumulação
    }

    public void declare(String name, Type type) {
        varTypes.put(name, type);
        fieldsBlock.append(".field private static ").append(name).append(" ")
                   .append(getDescriptor(type)).append("\n");
    }

    public void append(String code) {
        methodBlock.append(code);
    }

    public void emitLine(String instruction) {
        methodBlock.append("    ").append(instruction).append("\n");
    }

    public void emitLabel(String label) {
        methodBlock.append(label).append(":\n");
    }

    public String newLabel(String prefix) {
        return prefix + "_" + (labelCounter++);
    }

    public String getCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(".class public ").append(programName != null ? programName : "Programa").append("\n");
        sb.append(".super java/lang/Object\n\n");
        sb.append(fieldsBlock.toString()).append("\n");
        sb.append(".method public static main([Ljava/lang/String;)V\n");
        sb.append("    .limit stack 50\n");
        sb.append("    .limit locals 50\n\n");
        sb.append(methodBlock.toString());
        sb.append("    return\n");
        sb.append(".end method\n");
        return sb.toString();
    }

    private String getDescriptor(Type type) {
        if (type == Type.INT) return "I";
        if (type == Type.FLOAT) return "F";
        if (type == Type.STRING) return "Ljava/lang/String;";
        return "I"; // Booleanos e outros erros são mapeados como inteiros na JVM
    }

    public String loadVar(String name) {
        Type t = varTypes.getOrDefault(name, Type.INT);
        return "getstatic " + programName + "/" + name + " " + getDescriptor(t) + "\n";
    }

    public String storeVar(String name) {
        Type t = varTypes.getOrDefault(name, Type.INT);
        return "putstatic " + programName + "/" + name + " " + getDescriptor(t) + "\n";
    }

    public String intConst(int value) {
        if (value >= -1 && value <= 5) return "iconst_" + (value == -1 ? "m1" : value) + "\n";
        if (value >= -128 && value <= 127) return "bipush " + value + "\n";
        if (value >= -32768 && value <= 32767) return "sipush " + value + "\n";
        return "ldc " + value + "\n";
    }

    public String floatConst(float value) {
        return "ldc " + value + "\n";
    }

    public String stringConst(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return "ldc " + value + "\n";
        }
        return "ldc \"" + value + "\"\n";
    }

    public String arithmetic(String leftCode, Type leftType,
                             String rightCode, Type rightType,
                             int op, Type resultType) {
        StringBuilder sb = new StringBuilder();

        if (op == '/') {
            sb.append(leftCode);
            if (leftType == Type.INT) sb.append("i2f\n");
            sb.append(rightCode);
            if (rightType == Type.INT) sb.append("i2f\n");
            sb.append("fdiv\n");
            return sb.toString();
        }

        sb.append(leftCode);
        if (leftType == Type.INT && resultType == Type.FLOAT) sb.append("i2f\n");
        
        sb.append(rightCode);
        if (rightType == Type.INT && resultType == Type.FLOAT) sb.append("i2f\n");

        boolean isFloat = (resultType == Type.FLOAT || leftType == Type.FLOAT || rightType == Type.FLOAT);

        if (op == '+') sb.append(isFloat ? "fadd\n" : "iadd\n");
        else if (op == '-') sb.append(isFloat ? "fsub\n" : "isub\n");
        else if (op == '*') sb.append(isFloat ? "fmul\n" : "imul\n");
        else if (op == '%') sb.append("irem\n");

        return sb.toString();
    }

    public String stringConcat(String leftCode, Type leftType,
                               String rightCode, Type rightType) {
        StringBuilder sb = new StringBuilder();
        sb.append("new java/lang/StringBuilder\n");
        sb.append("dup\n");
        sb.append("invokespecial java/lang/StringBuilder/<init>()V\n");

        sb.append(leftCode);
        if (leftType == Type.INT) sb.append("invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;\n");
        else if (leftType == Type.FLOAT) sb.append("invokevirtual java/lang/StringBuilder/append(F)Ljava/lang/StringBuilder;\n");
        else sb.append("invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;\n");

        sb.append(rightCode);
        if (rightType == Type.INT) sb.append("invokevirtual java/lang/StringBuilder/append(I)Ljava/lang/StringBuilder;\n");
        else if (rightType == Type.FLOAT) sb.append("invokevirtual java/lang/StringBuilder/append(F)Ljava/lang/StringBuilder;\n");
        else sb.append("invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;\n");

        sb.append("invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;\n");
        return sb.toString();
    }

    public String relational(String leftCode, Type leftType,
                             String rightCode, Type rightType,
                             int op) {
        String lTrue = newLabel("L_rel_true");
        String lEnd = newLabel("L_rel_end");
        StringBuilder sb = new StringBuilder();

        sb.append(leftCode);
        if (leftType == Type.INT && rightType == Type.FLOAT) sb.append("i2f\n");

        sb.append(rightCode);
        if (rightType == Type.INT && leftType == Type.FLOAT) sb.append("i2f\n");

        if (leftType == Type.FLOAT || rightType == Type.FLOAT) {
            sb.append("fcmpl\n");
            String branchInstr = "";
            if (op == '=') branchInstr = "ifeq";
            else if (op == '<') branchInstr = "iflt";
            else if (op == '>') branchInstr = "ifgt";
            else if (op == Tag.EQ) branchInstr = "ifeq";
            else if (op == Tag.NE) branchInstr = "ifne";
            else if (op == Tag.GE) branchInstr = "ifge";
            else if (op == Tag.LE) branchInstr = "ifle";
            sb.append(branchInstr).append(" ").append(lTrue).append("\n");
        } else {
            String branchInstr = "";
            if (op == '=') branchInstr = "if_icmpeq";
            else if (op == '<') branchInstr = "if_icmplt";
            else if (op == '>') branchInstr = "if_icmpgt";
            else if (op == Tag.EQ) branchInstr = "if_icmpeq";
            else if (op == Tag.NE) branchInstr = "if_icmpne";
            else if (op == Tag.GE) branchInstr = "if_icmpge";
            else if (op == Tag.LE) branchInstr = "if_icmple";
            sb.append(branchInstr).append(" ").append(lTrue).append("\n");
        }
        sb.append("iconst_0\n");
        sb.append("goto ").append(lEnd).append("\n");
        sb.append(lTrue).append(":\n");
        sb.append("iconst_1\n");
        sb.append(lEnd).append(":\n");
        return sb.toString();
    }

    public String booleanOp(String leftCode, String rightCode, int op) {
        if (op == Tag.AND) {
            return leftCode + rightCode + "iand\n";
        } else {
            return leftCode + rightCode + "ior\n";
        }
    }

    public String unaryNot(String operandCode) {
        return operandCode + "iconst_1\nixor\n";
    }

    public String unaryMinus(String operandCode, Type type) {
        if (type == Type.FLOAT) {
            return operandCode + "fneg\n";
        } else {
            return operandCode + "ineg\n";
        }
    }

    public String read(String name, Type type) {
        String descriptor = getDescriptor(type);
        String method = "";
        if (type == Type.INT) method = "nextInt()I";
        else if (type == Type.FLOAT) method = "nextFloat()F";
        else method = "next()Ljava/lang/String;";

        return "new java/util/Scanner\n" +
               "dup\n" +
               "getstatic java/lang/System/in Ljava/io/InputStream;\n" +
               "invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V\n" +
               "invokevirtual java/util/Scanner/" + method + "\n" +
               "putstatic " + programName + "/" + name + " " + descriptor + "\n";
    }

    public String print(String valueCode, Type type) {
        String descriptor = getDescriptor(type);
        return "getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
               valueCode +
               "invokevirtual java/io/PrintStream/println(" + descriptor + ")V\n";
    }
}