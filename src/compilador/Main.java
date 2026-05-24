package compilador;

import compilador.lexico.Lexer;
import compilador.sintatico.Parser;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java compilador.Main <arquivo_fonte>");
            return;
        }

        try {
            // Instancia o Lexer com o arquivo passado como argumento
            Lexer lex = new Lexer(args[0]);
            
            // O Parser é instanciado recebendo a referência do Lexer
            Parser parse = new Parser(lex);
            
            // Chama o símbolo inicial da sua gramática (ex: programa)
            parse.program();
            
            System.out.println("Análise concluída com sucesso! Código sintaticamente correto.");
            
            // Fecha o Lexer após a análise
            lex.close();
            
        } catch (IOException e) {
            System.err.println("Erro de leitura: " + e.getMessage());
        } catch (Error e) {
            // Captura os erros sintáticos disparados pelo método error()
            System.err.println(e.getMessage());
        }
    }
}