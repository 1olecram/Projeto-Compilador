# Analisador Léxico e Sintático - Compiladores

Este repositório contém a implementação do "Projeto de Compiladores": um Analisador Léxico (Scanner) e um Analisador Sintático (Parser) preditivo descendente recursivo, desenvolvidos em Java, em conjunto com uma Tabela de Símbolos (`SymbolTable`).

## Funcionalidades

- **Scanner (Análise Léxica)**: Varredura de arquivos fonte, eliminando espaços e comentários (tanto de uma linha `//` quanto em bloco `/* */`).
- **Análise Numérica e Literal**: Identificação e extração de identificadores, literais string (`"texto"`), números inteiros (`Num`) e números de ponto flutuante (`Real`).
- **Tokens**: Reconhecimento de todos os operadores relacionais, aritméticos e lógicos da gramática específica do trabalho.
- **Parser (Análise Sintática)**: Implementação de um analisador preditivo recursivo descendente que valida a gramática proposta para a linguagem (declarações de variáveis, comandos condicionais e de repetição, atribuições, leitura/escrita e expressões aritméticas/lógicas).
- **Tabela de Símbolos**: Armazenamento automático de palavras reservadas e inicialização de identificadores mapeados durante a compilação.

## Estrutura do Projeto

- `src/compilador/`: Contém o ponto de entrada principal (`Main.java`).
- `src/compilador/lexico/`: Contém as classes relativas à análise léxica (tokens, tabela de símbolos e analisador léxico).
- `src/compilador/sintatico/`: Contém a classe do analisador sintático (`Parser.java`).
- `testes/`: Diretório contendo casos de teste (ex: `teste_01.txt`, `teste_06.txt`).

## Como Compilar e Executar

1. **Compilação**:
   Certifique-se de ter o `javac` disponível na linha de comando e execute a partir da raiz do projeto:

```bash
javac src/compilador/.java src/compilador/lexico/.java src/compilador/semantico/.java src/compilador/sintatico/.java
```

2. **Execução**:
   Após compilar as classes, ainda na raiz do projeto, execute:

```bash
java -cp src compilador.Main testes/teste_06.txt
```

*(O compilador executará as análises léxica e sintática sobre o arquivo. Caso o código esteja sintaticamente correto, uma mensagem de sucesso será exibida; se houver algum erro de sintaxe, a execução exibirá o erro e a linha aproximada do ocorrido).*
O argumento final é o arquivo de entrada a ser analisado. Para testar outros casos, basta trocar o nome do arquivo.

## Boas Práticas

- Foi utilizada a interface `Closeable` e blocos `try-with-resources` para evitar Resource Leaks.
- A tabela de símbolos substitui o antigo formato `Hashtable` por instâncias genéricas da Collections API baseadas em `HashMap`, melhorando performance local.
