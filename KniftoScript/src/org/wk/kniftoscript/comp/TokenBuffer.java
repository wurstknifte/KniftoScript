package org.wk.kniftoscript.comp;

import java.io.IOException;
import java.util.ArrayList;

public class TokenBuffer
{

	public TokenBuffer(Lexer l) throws IOException
	{
		fillBuffer(l);
		pointer = 0;
	}
	
	private void fillBuffer(Lexer l) throws IOException
	{
		ArrayList<Token> ba = new ArrayList<Token>();
		ArrayList<Integer> lines = new ArrayList<Integer>();
		
		Token t = l.readToken();
		int lastline = 0;
		while(t != null)
		{
			lines.add(lastline);
			ba.add(t);
			lastline = l.getCurrentLine();
			t = l.readToken();
		}
		
		buffer = ba.toArray(new Token[ba.size()]);
	}
	
	public Token readToken()
	{
		if(available() > 0)
			return buffer[pointer++];
		return null;
	}
	
	public Token peekToken()
	{
		if(available() > 0)
			return buffer[pointer];
		return null;
	}
	
	public int bufferSize()
	{
		return buffer.length;
	}
	
	public int available()
	{
		return buffer.length - pointer;
	}
	
	public int getPointer()
	{
		return pointer;
	}
	
	public void reset()
	{
		pointer = 0;
	}
	
	public void absolute(int i)
	{
		pointer = i;
	}
	
	public void relative(int i)
	{
		pointer += i;
	}
	
	private int pointer;
	private Token[] buffer;
}