package org.wk.kniftoscript.comp;

public class Token
{

	public Token(int id, String lexem)
	{
		this(id,lexem,-1);
	}
	
	public Token(int id, String lexem, int line)
	{
		this.id = id;
		this.lexem = lexem;
		this.line = line;
	}
	
	public int getID()
	{
		return id;
	}
	
	public String getLexem()
	{
		return lexem;
	}
	
	public String toString()
	{
		return lexem;
	}
	
	public int getLine()
	{
		return line;
	}
	
	public boolean equalsLexem(String s)
	{
		return lexem.equals(s);
	}
	
	public boolean equalsLexemIC(String s)
	{
		return lexem.equalsIgnoreCase(s);
	}
	
	private int id;
	private String lexem;
	
	private int line;
	
	public static final int T_KEYWORD = 0;
	public static final int T_IDENTIFIER = 1;
	public static final int T_INTEGER = 2;
	public static final int T_STRING = 3;
	public static final int T_OPERATOR = 4;
	public static final int T_COMMENT = 5;
}
