package Isi;

import exception.IsiLexicalException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class IsiScanner {
	
	private char[] content;
	private char auxcurrentChar;
	private int    estado;
	private int    pos;
	private int    line;
	private int    column;
	public ArrayList<String> especiais = new ArrayList();
	
	public IsiScanner(String filename) {
		try {
			line = 1;
			column = 0;
			String txtConteudo;
			txtConteudo = new String(Files.readAllBytes(Paths.get(filename)),StandardCharsets.UTF_8);
//			System.out.println("DEBUG --------");
//			System.out.println(txtConteudo);
//			System.out.println("--------------");
			content = txtConteudo.toCharArray();
			pos=0;
			this.addEspeciais("if","else","class","super","var","let","instanceof","typeof","this","import","new","extends","yield","void", "interface","enum","with","delete","debugger","require","const","enum","in","for","of","while","do","switch","case","default","break","continue","try","catch","throw","finally","function","return","true","false","null");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Token nextToken() {
		char currentChar;
		Token token;
		String term="";
		if (isEOF()) {
			return null;
		}
		estado = 0;
		while (true) {
			currentChar = nextChar();
			column++;
			System.out.print(currentChar);
			if (!(pos >= content.length)){auxcurrentChar = content[pos];
			//System.out.println("currentChar: "+currentChar+" aux "+auxcurrentChar+"line"+line+"column"+column);
			}
			switch(estado) {
			case 0:
				if (isChar(currentChar)) {
					term += currentChar;
					estado = 1;
				}
				else if (isDigit(currentChar)) {
					estado = 2;
					term += currentChar;
				}
				else if (isSpace(currentChar)) {
					estado = 0;
				}
				else if (isOperator(currentChar)) {
				    if(currentChar=='/' && auxcurrentChar=='/')//se for commentline, n setei token ainda, erro qnd é ultima linha
				    {   currentChar = nextChar();
				        term += currentChar;
				        while(!(currentChar=='\n' || currentChar == '\r')){
				            currentChar = nextChar();
				            term += currentChar;
				            //isEOF(currentChar)==false
				            //currentChar == '\0'
				        }
				        estado=0;
    					term = term.substring(0, term.length()-1);
					    System.out.print(term);
					    System.out.println(" é um TK_COMMENTLINE");
				        break;
				    }
				    if(currentChar=='/' && auxcurrentChar=='*')//se for commentblock, n setei token ainda, erro qnd er ultima linha
				    {   currentChar = nextChar();//add o *
				        auxcurrentChar = content[pos];
				        term += currentChar;
				        currentChar = nextChar();//caso venha uma / logo em seguida pra n entrar no while e já sair
				        auxcurrentChar = content[pos];
				        term += currentChar;
				        int aux =0;
				        while(!(currentChar=='*' && auxcurrentChar == '/')){
				            currentChar = nextChar();
				            auxcurrentChar = content[pos];
				            term += currentChar;
				            aux++;
				            //System.out.println("aux "+aux);
				        }
				        currentChar = nextChar();
				        term += currentChar;
				        estado=0;
    					//term = term.substring(0, term.length()-1);
					    System.out.print(term);
					    System.out.println(" é um TK_COMMENTBLOCK");
					    term = "";
				        break;
				    }
					term += currentChar;
					token = new Token();
					token.setType(Token.TK_OPERATOR);
					System.out.println(" é um TK_OPERATOR");
					token.setText(term);
					token.setLine(line);
					token.setColumn(column - term.length());
					return token;
				}
				else {
					throw new IsiLexicalException("Unrecognized SYMBOL");
				}
				break;
			case 1:
				if (isChar(currentChar) || isDigit(currentChar)) {
					estado = 1;
					term += currentChar;
				}
				else if (isSpace(currentChar) || isOperator(currentChar) || isEOF(currentChar)){
					if (!isEOF(currentChar))
						back();
					token = new Token();
					token.setType(Token.TK_IDENTIFIER);
					System.out.println(" é um TK_IDENTIFIER");
					token.setText(term);
					token.setLine(line);
					token.setColumn(column - term.length());
					return token;
				}
				else {
					throw new IsiLexicalException("Malformed Identifier");
				}
				break;
			case 2:
				if (isDigit(currentChar) || currentChar == '.') {
					estado = 2;
					term += currentChar;
				}
				else if (!isChar(currentChar) || isEOF(currentChar)) {
					if (!isEOF(currentChar))
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
				break;
			}
		}
		
		
		
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private boolean isChar(char c) {
		return (c >= 'a' && c <= 'z') || (c>='A' && c <= 'Z');
	}
	
	private boolean isOperator(char c) {
		return c == '>' || c == '<' || c == '=' || c == '!' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '!' || c == '|' || c == '&' || c == ':';
	}
	
	private boolean isSpace(char c) {
		if (c == '\n' || c== '\r') {
			line++;
			column=0;
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
	
	void addEspeciais(String ... esp){
	    for (String k: esp){
	    especiais.add(k);}
	}
	
	
	
}