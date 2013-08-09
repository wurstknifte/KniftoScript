package org.wk.kniftoscript.comp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer
{
	
	private final boolean CASE_SENSITIVE = false;
	
	public Lexer(InputStream in) throws IOException
	{
		keywords = new ArrayList<String>();
		indicators = new HashMap<Integer, String>();
		operators = new ArrayList<String>();
		
		setIndicator(I_DECIMAL_POINT,".");
		setIndicator(I_STRING_START,"\"");
		setIndicator(I_STRING_END,"\"");
		setIndicator(I_COMMENT_SINGLELINE,"#");

		this.in = in;
		readNext();
	}
	
	public void declareKeyword(String s)
	{
		String keyword = s;
		
		if(!CASE_SENSITIVE)
			keyword = keyword.toLowerCase();
		
		if(!isKeyword(keyword))
			keywords.add(keyword);
	}
	
	public void declareOperator(String op)
	{
		operators.add(op);
	}
	
	public void setIndicator(int id, String s)
	{
		indicators.put(Integer.valueOf(id), s);
	}
	
	public boolean isIndicator(int id,int c)
	{
		return (indicators.get(Integer.valueOf(id)).indexOf(c) > -1);
	}
	
	public int getCurrentLine()
	{
		return currentLine;
	}
	
	public Token readToken() throws IOException
	{
		skipWhites();
		
		if(lchar == -1)
			return null;
		
		if(isIndicator(I_COMMENT_SINGLELINE, lchar))
		{
			while(!(lchar == C_CR || lchar == C_LF || lchar == -1))//Skip to next line
			{
				readNext();
			}
		}
		
		skipWhites();
		
		String lexem = "";
		int typeId = -1;
		int line = getCurrentLine();
		
		if(isAlpha(lchar))
		{
			do
			{
				lexem += (char)lchar;
				readNext();
			}while(isAlpha(lchar) || isNumeric(lchar));// || lchar == '.');//. for object-orientated things; Removed due to .-Operator
			
			if(isKeyword(lexem))
				typeId = Token.T_KEYWORD;
			else
				typeId = Token.T_IDENTIFIER;
		}else if(isNumeric(lchar))
		{
			do
			{
				lexem += (char)lchar;
				readNext();
			}while(isNumeric(lchar) || isIndicator(I_DECIMAL_POINT,lchar));
			typeId = Token.T_INTEGER;
		}else if(isIndicator(I_STRING_START,lchar))//String literal
		{
			lexem = readStringLiteral();
			typeId = Token.T_STRING;
		}else if(isPartOfOperator("" + (char)lchar))
		{
			typeId = Token.T_OPERATOR;
			
			String op = "" + (char)lchar;
			while(isPartOfOperator(op))
			{
				readNext();
				op += (char)lchar;
			}
			
			lexem = op.substring(0,op.length()-1);
			
			if(!isOperator(lexem))
				throw new CompilerException("Unexpected end in operator path: " + op);
			
		}else if(isIndicator(I_COMMENT_SINGLELINE, lchar))
		{
			while(!(lchar == C_CR || lchar == C_LF || lchar == -1))//Skip to next line
			{
				readNext();
			}
			
		}else
		{
			throw new IOException("Unrecognized character in line " + getCurrentLine() + ": " + lchar + "(" + (char)lchar + ")");
		}
		
		return new Token(typeId,lexem,line);
	}
	
	private String readStringLiteral() throws IOException
	{
		readNext();
		String s = "";
		while(!(isIndicator(I_STRING_END,lchar)))
		{
			s += (char)lchar;
			readNext();
			if(lchar == -1)
				throw new IOException("EOF reached before closing string literal token");
		}
		readNext();
		return s;
	}
	
	private void skipWhites() throws IOException
	{
		while(lchar == C_SPACE || lchar == C_CR || lchar == C_LF || lchar == C_TAB)
			readNext();
	}
	
	private boolean isAlpha(int c)
	{
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
	}
	
	private boolean isNumeric(int c)
	{
		return (c >= '0' && c <= '9');
	}
	
	private boolean isKeyword(String s)
	{
		String keyword = s;
		if(!CASE_SENSITIVE)
			keyword = keyword.toLowerCase();
		
		return keywords.contains(keyword);
	}
	
	private boolean isOperator(String op)
	{
		for(String o : operators)
		{
			if(o.equals(op))
				return true;
		}
		return false;
	}
	
	private boolean isPartOfOperator(String op)
	{
		for(String o : operators)
		{
			if(o.startsWith(op))
				return true;
		}
		return false;
	}
	
	private void readNext() throws IOException
	{
		if(lchar == C_LF)
			currentLine++;
		lchar = in.read();
	}
	
	private ArrayList<String> keywords;
	private ArrayList<String> operators;
	private HashMap<Integer, String> indicators;
	
	private int lchar;
	private int currentLine = 1;
	
	private InputStream in;
	
	public static final int I_DECIMAL_POINT = 4;
	public static final int I_STRING_START = 5;
	public static final int I_STRING_END = 6;
	public static final int I_COMMENT_SINGLELINE = 10;
	
	public static final char C_CR = 0x0D;
	public static final char C_LF = 0x0A;
	public static final char C_SPACE = 0x20;
	public static final char C_TAB = 0x09;
}
