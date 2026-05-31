package compilador.lexico;

/**
 * Contém todas as constantes (tags) para identificar os tipos de tokens.
 */
public class Tag {
    // Tipos de dados e literais
    public final static int NUM = 256;
    public final static int REAL = 257;
    public final static int ID = 258;
    public final static int LITERAL = 259;

    // Palavras reservadas
    public final static int CLASS = 260;
    public final static int INT = 261;
    public final static int STRING = 262;
    public final static int FLOAT = 263;
    public final static int IF = 264;
    public final static int ELSE = 265;
    public final static int DO = 266;
    public final static int WHILE = 267;
    public final static int REPEAT = 268;
    public final static int UNTIL = 269;
    public final static int READ = 270;
    public final static int WRITE = 271;

    // Operadores compostos e lógicos
    public final static int ASSIGN = 272; // :=
    public final static int EQ = 273;     // = (relacional)
    public final static int NE = 274;     // <>
    public final static int LE = 275;     // <=
    public final static int GE = 276;     // >=
    public final static int AND = 277;    // and (ou &&)
    public final static int OR = 278;     // or (ou ||)
    public final static int NOT = 279;    // not

    //Operador de fim de arquivo
    public final static int EOF  = 65535; //EOF
}
