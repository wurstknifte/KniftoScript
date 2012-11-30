package org.wk.kniftoscript.comp;

public class OpIdent
{

	public OpIdent(String mn, int opcode, int... paramTypes)
	{
		
		parameterTypes = paramTypes;
	}
	
	public int getOpcode()
	{
		return opcode;
	}
	
	public String getMnenomic()
	{
		return mnenomic;
	}
	
	public int[] getParameterTypes()
	{
		return parameterTypes;
	}
	
	public boolean matches(Token[] tks)
	{
		if(tks.length < parameterTypes.length+1)
			return false;
		
		for(int i = 0;i<tks.length;i++)
		{
			Token t = tks[i];
			if(parameterTypes[i] != t.getType())
				return false;
		}
		return true;
	}
	
	private String mnenomic;
	private int opcode;
	private int[] parameterTypes;
	
	public static final int T_NULL = -1;
	public static final int T_INT = 0;
	public static final int T_STRING = 1;
}
