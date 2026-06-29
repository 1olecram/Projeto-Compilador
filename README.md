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
- `src/compilador/lexico/`: Classes relativas à análise léxica (tokens, tabela de símbolos e scanner).
- `src/compilador/sintatico/`: Classe do analisador sintático (`Parser.java`).
- `src/compilador/semantico/`: Classes responsáveis pela validação de tipos e regras semânticas (`SemanticAnalyzer.java`).
- `src/compilador/gerador/`: Classes responsáveis por traduzir a árvore do programa para código Jasmin (`JasminGenerator.java`).
- `testes/`: Diretório contendo casos de teste (ex: `teste_01.txt`, `teste_06.txt`).
- `jasmin-2.4/`: Ferramenta externa utilizada para montar o arquivo `.j` em `.class`.

## Como Compilar e Executar

1. **Compilação**:
   Certifique-se de ter o `javac` disponível na linha de comando e execute a partir da raiz do projeto:

```bash
javac src/compilador/*.java src/compilador/gerador/*.java src/compilador/lexico/*.java src/compilador/semantico/*.java src/compilador/sintatico/*.java
```

2. **Execução**:
   Após compilar as classes, ainda na raiz do projeto, execute:

```bash
java -cp src compilador.Main testes/teste_06.txt
```

```bash
java -jar jasmin-2.4\jasmin-2.4\jasmin.jar testes\teste_06.j
```
*(Após executar esse comando, o terminal exibirá uma mensagem semelhante a: ```Generated : Teste06.class```. Utilize o nome do arquivo gerado (neste exemplo, ```Teste06```), ou seja, o nome antes da extensão ```.class```, no próximo comando)*
```bash
java Teste06
```
*(O compilador executará as análises léxica, sintática e semântica sobre o arquivo de entrada. Caso o código esteja correto, uma mensagem de sucesso será exibida e o código intermediário/objeto será gerado. Se houver algum erro léxico, sintático ou semântico, a execução exibirá uma mensagem indicando o tipo do erro e a linha aproximada onde ele ocorreu.)*

O argumento final corresponde ao arquivo de entrada que será analisado. Para testar outros casos, basta substituir o nome do arquivo.
## Boas Práticas

- Foi utilizada a interface `Closeable` e blocos `try-with-resources` para evitar Resource Leaks.
- A tabela de símbolos substitui o antigo formato `Hashtable` por instâncias genéricas da Collections API baseadas em `HashMap`, melhorando performance local.
