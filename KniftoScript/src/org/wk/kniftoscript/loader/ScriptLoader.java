package org.wk.kniftoscript.loader;

import java.io.IOException;
import java.io.InputStream;

public class ScriptLoader
{
	
	public ScriptLoader(InputStream in) throws IOException
	{
		bytes = new int[in.available()];
		for(int i = 0;i<in.available();i++)
		{
			bytes[i] = in.read();
		}
	}

	public int[] loadScriptHeader() throws ScriptLoadException 
	{
		int verif = getInt(0);
		if(verif != magicId)
		{
			throw new ScriptLoadException("Binary script data not valid! Found MagicId [" + verif + "], should be [" + magicId + "]");
		}
		headerSize = getInt(4);
		int[] result = new int[headerSize];
		for(int i = 0;i<headerSize;i++)
		{
			result[i] = getByte(8+i);
		}
		return result;
	}
	
	public int[] loadScriptBody()
	{
		int[] result = new int[bytes.length-8-headerSize];
		return result;
	}
	
	private int getByte(int i) throws ScriptLoadException 
	{
		if(i >= bytes.length)
		{
			throw new ScriptLoadException("I'm pretty sure there were be more bytes...");
		}
		return bytes[i] & 0xFF;
	}
	
	private int getInt(int i) throws ScriptLoadException 
	{
		int r = 0;
		for(int j = 0;j<4;j++)
		{
			r |= getByte(i+j);
			r = r << 8;
		}
		return r;
	}
	
	private int[] bytes;
	private final int magicId = 0x48429000;
	private int headerSize;
}
