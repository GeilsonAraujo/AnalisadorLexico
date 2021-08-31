package Isi;

import exception.IsiLexicalException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class IsiScanner2 {

    private char[] content;
    private int estado;
    private int line;
    private int column;
    private char auxcurrentChar;
    private int pos;
    public ArrayList<String> especiais = new ArrayList();

    public IsiScanner2(String filename) {
        try {
            line = 1;
            column = 0;
            pos = 0;

            String txtConteudo;
            txtConteudo = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
            content = txtConteudo.toCharArray();
            this.addEspeciais("if", "else", "class", "super", "var", "let", "instanceof", "typeof", "this", "import", "new", "extends", "yield", "void", "interface", "enum", "with", "delete", "debugger", "require", "const", "enum", "in", "for", "of", "while", "do", "switch", "case", "default", "break", "continue", "try", "catch", "throw", "finally", "function", "return", "true", "false", "null");

            //txtConteudo.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Token nextToken() {
        char currenteChar;
        String term = "";
        Token token;

        if (isEOF()) {
            return null;
        }
        estado = 0;
        while (true) {
            currenteChar = nextChar();
            column++;
            System.out.print(currenteChar);

            if (!(pos >= content.length)) {
                auxcurrentChar = content[pos];
            }

            switch (estado) {
                case 0:
                    if (isChar(currenteChar)) {
                        term += currenteChar;
                        estado = 1;

                    } else if (isDigit(currenteChar)) {
                        estado = 2;
                        term += currenteChar;

                    } else if (isSpace(currenteChar)) {
                        estado = 0;
                    } else if (isOperator(currenteChar)) {
                        if (currenteChar == '/' && auxcurrentChar == '/')//se for commentline, n setei token ainda, erro qnd é ultima linha
                        {
                            currenteChar = nextChar();
                            term += currenteChar;
                            while (!(currenteChar == '\n' || currenteChar == '\r')) {
                                currenteChar = nextChar();
                                term += currenteChar;
                                //isEOF(currentChar)==false
                                //currentChar == '\0'
                            }
                            estado = 0;

                            term = term.substring(0, term.length() - 1);
                            System.out.print(term);
                            System.out.println(" é um TK_COMMENTLINE");

                            token = new Token();
                            token.setLine(line);
                            token.setText(term);
                            token.setType(Token.TK_COMMENTLINE);
                            return token;
                        }
                        
                        if(currenteChar=='/' && auxcurrentChar=='*')//se for commentblock, n setei token ainda, erro qnd er ultima linha
				    {   currenteChar = nextChar();//add o *
				        auxcurrentChar = content[pos];
				        term += currenteChar;
				        currenteChar = nextChar();//caso venha uma / logo em seguida pra n entrar no while e já sair
				        auxcurrentChar = content[pos];
				        term += currenteChar;
				        int aux =0;
				        while(!(currenteChar=='*' && auxcurrentChar == '/')){
				            currenteChar = nextChar();
				            auxcurrentChar = content[pos];
				            term += currenteChar;
				            aux++;
				            //System.out.println("aux "+aux);
				        }
				        currenteChar = nextChar();
				        term += currenteChar;
				        estado=0;
    					//term = term.substring(0, term.length()-1);
					    System.out.print(term);
					    System.out.println(" é um TK_COMMENTBLOCK");
					    
                                            
                                            token = new Token();
                                            token.setText(term);
                                            token.setType(Token.TK_COMMENTBLOCK);
                                            token.setText(term);
                                            term = "";
                                            
                                            return token;
                                            
				    }
					term += currenteChar;
					token = new Token();
					token.setType(Token.TK_OPERATOR);
					System.out.println(" é um TK_OPERATOR");
					token.setText(term);
					token.setLine(line);
					token.setColumn(column - term.length());
					return token;
                    } else {
                        throw new IsiLexicalException("Simbolo desconhecido");
                    }
                    break;

                case 1:
                    if (isChar(currenteChar) || isDigit(currenteChar)) {
                        estado = 1;
                        term += currenteChar;
                    } else if (isSpace(currenteChar) || isOperator(currenteChar) || isEOF(currenteChar)){
					if (!isEOF(currenteChar))
						back();
					token = new Token();
					token.setType(Token.TK_IDENTIFIER);
                                        
					System.out.println(" é um TK_IDENTIFIER");
					token.setText(term);
					token.setLine(line);
					token.setColumn(column - term.length());
					return token;
                    } else {
                        throw new IsiLexicalException("Identificador mal formado");
                    }
                    break;
                    
                case 2: if (isDigit(currenteChar) || currenteChar == '.') {
					estado = 2;
					term += currenteChar;
				}
				else if (!isChar(currenteChar) || isEOF(currenteChar)) {
					if (!isEOF(currenteChar))
						back();
					token = new Token();
					token.setType(Token.TK_NUMBER);
					System.out.println(" é um TK_NUMBER");
					token.setText(term);
					token.setLine(line);
					token.setColumn(column - term.length());
					return token;
				}
				else {
					throw new IsiLexicalException("Unrecognized Number");
				}
//                case 2:
//                    back();
//                    token = new Token();
//                    token.setType(Token.TK_IDENTIFIER);
//                    token.setText(term);
//                    return token;
                    case 3:
                        if (isDigit(currenteChar)) {
                            estado = 3;
                            term += currenteChar;
                        } else if (!isChar(currenteChar)) {
                            estado = 4;
                        } else {
                            throw new IsiLexicalException("numero mal formado");
                        }
                        break;
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

    void addEspeciais(String... esp) {
        for (String k : esp) {
            especiais.add(k);
        }
    }
}
