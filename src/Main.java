package compilador.lexico;

import java.io.IOException;

/**
 * Classe principal para testar o Analisador Léxico de forma isolada.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java compilador.lexico.Main <arquivo_fonte>");
            return;
        }

        // Utiliza try-with-resources para garantir o fechamento seguro do arquivo após a leitura
        try (Lexer lexer = new Lexer(args[0])) {
            Token t;
            
            System.out.println("--- Tokens Lidos ---");
            // O scan retornará null ao atingir o final do arquivo
            while ((t = lexer.scan()) != null) {
                // Formatação simples para exibir a tag e o token
                System.out.println("<" + t.tag + ", " + t.toString() + ">");
            }
            
            // Ao final da varredura, imprime a tabela de símbolos
            lexer.getSymbolTable().printTable();
            
        } catch (IOException e) {
            System.out.println("Erro na leitura do arquivo: " + e.getMessage());
        }
    }
}
