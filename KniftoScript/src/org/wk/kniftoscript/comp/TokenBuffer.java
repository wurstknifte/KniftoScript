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
		Token t;
		while((t = l.readToken()) != null)
		{
			ba.add(t);
		}
		buffer = ba.toArray(new Token[ba.size()]);
	}
	
	public Token readToken()
	{
		if(pointer < buffer.length)
			return buffer[pointer++];
		return null;
	}
	
	public Token peekToken()
	{
		if(pointer < buffer.length)
			return buffer[pointer];
		return null;
	}
	
	public int bufferSize()
	{
		return buffer.length;
	}
	
	public int available()
	{
		return (buffer.length - 1) - pointer;
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
