package org.wk.kniftoscript.comp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class Lexer
{

	public Lexer(InputStream i) throws IOException
	{
		in = i;
		readNext();
	}
	
	public Token readToken() throws IOException
	{	
		try
		{
			skipWhites();
			if(lchar == TK_COMMENT)
				skipUntil(C_LINEFEED);
			skipWhites();
			
			if(isDigit(lchar))
			{
				String s = readNumber();
				return new Token(Token.T_LITERAL_INT,s);
			}else if(isOperator(lchar))
			{
				char c = (char)lchar;
				readNext();
				return new Token(Token.T_OPERATOR,"" + c);
			}else if(isBrace(lchar))
			{
				char c = (char)lchar;
				readNext();
				return new Token(Token.T_BRACE,"" + c);
			}else if(isQuote(lchar))
			{
				String s = readStringLiteral();
				return new Token(Token.T_LITERAL_STRING,s);
			}else if(isSeperator(lchar))
			{
				char c = (char)lchar;
				readNext();
				return new Token(Token.T_SEPERATOR,"" + c);
			}else
			{
				String s = readIdent();
				if(isKeyword(s))
					return new Token(Token.T_KEYWORD,s);
				else if(isDatatype(s))
					return new Token(Token.T_DATATYPE,s);
				else
					return new Token(Token.T_IDENTIFIER,s);
			}
		
			//return null;
		}catch(EOFException e)
		{
			return null;
		}
	}
	
	private String readStringLiteral() throws IOException
	{
		expectQuote();
		readNext();
		String s = "";
		while(!isQuote(lchar))
		{
			try
			{
				s += (char)lchar;
				readNext();
			}catch(EOFException e)
			{
				throw new CompilerException("EOF reached before closing string literal token");
			}
		}
		expectQuote();
		readNext();
		return s;
	}
	
	private String readNumber() throws IOException
	{
		expectDigit();
		String s = "" + (char)lchar;
		readNext();
		if(lchar == 'x')
			readNext();//TODO Implement better hex digit recognition here
		while(isDigit(lchar))
		{
			s += (char)lchar;
			readNext();
		}
		return s;
	}
	
	private String readIdent() throws IOException
	{
		expectAlpha();
		String s = "" + (char)lchar;
		readNext();
		while(isAlphaNumeric(lchar))
		{
			s += (char)lchar;
			readNext();
		}
		return s;//S sollte niemals "" oder null sein, vorher wird EOFException geworfen
	}
	
	private void expectQuote() throws IOException
	{
		if(!isQuote(lchar))
			throw new CompilerException("Missing quotation mark, found: " + (char)lchar);
	}
	
	private void expectAlpha() throws IOException
	{
		if(!isAlpha(lchar))
			throw new CompilerException("Expected char, found: " + (char)lchar);
	}
	
	private void expectDigit() throws IOException
	{
		if(!isDigit(lchar))
			throw new CompilerException("Expected char, found: " + (char)lchar);
	}
	
	private void skipUntil(int c) throws IOException
	{
		//System.out.println("Skipping until: " + (char)c);
		while(lchar != c)
		{
			//System.out.println("SW: " + (char)lchar);
			readNext();
		}
	}
	
	private void skipWhites() throws IOException
	{
		while(isWhite(lchar))
		{
			//System.out.println("SW: " + (char)lchar);
			readNext();
		}
	}
	
	private boolean isQuote(int i)
	{
		return (i == '\'') || (i == '\"');
	}
	
	private boolean isSeperator(int i)
	{
		for(int j : seperators)
		{
			if(j == i)
				return true;
		}
		return false;
	}
	
	private boolean isWhite(int i)
	{
		return (i == C_SPACE) ||
				(i == C_TAB) ||
				(i == C_LINEFEED) ||
				(i == C_CARRIAGE);
	}
	
	private boolean isAlpha(int i)
	{
		return (i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z') || i == '_';
	}
	
	private boolean isOperator(int i)
	{
		for(int j : operators)
		{
			if(j == i)
				return true;
		}
		return false;
	}
	
	private boolean isBrace(int i)
	{
		return (i == '(') || (i == ')');
	}
	
	private boolean isDigit(int i)
	{
		return (i >= '0' && i <= '9');
	}
	
	private boolean isAlphaNumeric(int i)
	{
		return isAlpha(i) || isDigit(i);
	}
	
	public boolean isKeyword(String s)
	{
		for(String k : keywords)
		{
			if(k.equalsIgnoreCase(s))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isDatatype(String s)
	{
		for(String k : datatypes)
		{
			if(k.equalsIgnoreCase(s))
			{
				return true;
			}
		}
		return false;
	}
	
	public int getCurrentLine()
	{
		return currentLine;
	}
	
	private void readNext() throws IOException
	{
		int i = in.read();
		if(i != -1)
		{
			if(i == C_LINEFEED)
				currentLine++;
			
			lchar = i;
			//System.out.println("R: " + (char)lchar);
		}else
		{
			throw new EOFException("Unexpected EOF");
		}
	}
	
	private int lchar;
	
	private InputStream in;
	
	private int currentLine;
	
	public String[] keywords = new String[]{};// = new String[]{"IMPORT","FUNC","IF","ELSE","ELSEIF","WHILE","NULL","END","BREAK"};
	public String[] datatypes = new String[]{};// = new String[]{"INT","FLOAT","STRING","OBJECT"};
	public int[] operators = new int[]{};// = new int[]{'+','-','*','/','.','=','|','&','~','^','%'};
	public int[] seperators = new int[]{};//= new int[]{',',':'};
	
	public static final int C_SPACE = ' ';
	public static final int C_TAB = '\t';
	public static final int C_LINEFEED = '\n';
	public static final int C_CARRIAGE = 13;
	
	public static final int TK_COMMENT = ';';
	public static final int TK_SEPERATOR = ',';
}
