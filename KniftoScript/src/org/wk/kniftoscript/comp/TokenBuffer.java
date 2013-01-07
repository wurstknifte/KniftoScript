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
		Token t;
		while((t = l.readToken()) != null)
		{
			ba.add(t);
			lines.add(l.getCurrentLine());
		}
		buffer = ba.toArray(new Token[ba.size()]);
		Integer[] lbtmp = lines.toArray(new Integer[lines.size()]);
		lineBuffer = new int[lines.size()];
		for(int isd = 0;isd<lbtmp.length;isd++)
		{
			Integer isdi = lbtmp[isd];
			lineBuffer[isd] = isdi.intValue();
		}
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
	
	public int getCurrentLine()
	{
		return lineBuffer[pointer];
	}
	
	private int pointer;
	private Token[] buffer;
	
	private int[] lineBuffer;
}
