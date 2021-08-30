package Isi;

import exception.IsiLexicalException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IsiScanner2 {

    private char[] content;
    private int estado;
    private int line;
    private int column;
    private char auxcurrentChar;
    private int pos;

    public IsiScanner2(String filename) {
        try {
            String txtConteudo;
            txtConteudo = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);

            System.out.println("DEBUG --------");
            System.out.println(txtConteudo);
            System.out.println("--------------");

            content = txtConteudo.toCharArray();

            txtConteudo.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Token nextToken() {
        char currenteChar;
        String term = "";
        if (isEOF()) {
            return null;
        }
        estado = 0;
        while (true) {
            currenteChar = nextChar();
            Token token;
            
            switch (estado) {
                case 0:
                    if (isChar(currenteChar)) {
                        term += currenteChar;
                        estado = 1;

                    } else if (isDigit(currenteChar)) {
                        estado = 3;
                        term += currenteChar;

                    } else if (isSpace(currenteChar)) {
                        estado = 0;
                    }else if(isOperator(currenteChar)){
                        estado = 5;
                    }else{
                        throw new IsiLexicalException("Simbolo desconhecido");
                    }
                    break;
                case 1:
                    if (isChar(currenteChar) || isDigit(currenteChar)) {
                        estado = 1;
                        term += currenteChar;
                    } else if(isSpace(currenteChar) || isOperator(currenteChar)){
                        estado = 2;
                    }else{
                        throw new IsiLexicalException("Identificador mal formado");
                    }
                    break;
                case 2:
                    back();
                    token = new Token();
                    token.setType(Token.TK_IDENTIFIER);
                    token.setText(term);
                    return token;
                case 3:
                    if(isDigit(currenteChar)){
                        estado = 3;
                        term += currenteChar;
                    }else if(!isChar(currenteChar)){
                        estado = 4;
                    }else{
                        throw new IsiLexicalException("numero mal formado");
                    }break;
                case 4:
                    token = new Token();
                    token.setType(Token.TK_IDENTIFIER);
                    token.setText(term);
                    back();
                    return token;
                case 5:
                    term += currenteChar;
                    token = new Token();
                    token.setType(Token.TK_OPERATOR);
                    token.setText(term);
                  
                    return token;
            }
        }
    }
        //  /*  public static String imprimi(String texto) throws IOException{
        //
        //        String txtConteudo;
        //        txtConteudo = new String(Files.readAllBytes(Paths.get(texto)), StandardCharsets.UTF_8);
        //        return "O texto do arquivo é: " +texto;
        //
        //    }*/
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isOperator(char c) {
        return c == '>' || c == '<' || c == '=' || c == '!' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '!' || c == '|' || c == '&' || c == ':';
    }

    private boolean isSpace(char c) {
        if (c == '\n' || c == '\r') {
            line++;
            column = 0;
            //System.out.println("QUEBRA DE LINHA");
        }
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private char nextChar() {
        if (isEOF()) {
            return '\0';
        }
        return content[pos++];
    }

    private boolean isEOF() {
        return pos >= content.length;
    }

    private void back() {
        pos--;
        column--;
    }

    private boolean isEOF(char c) {
        return c == '\0';
    }
}
