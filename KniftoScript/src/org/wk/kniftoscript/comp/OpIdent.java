package org.wk.kniftoscript.comp;

public class OpIdent
{

	public OpIdent(String mn, int opc, int... paramTypes)
	{
		mnenomic = mn;
		opcode = opc;
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
	
	public int getParameterCount()
	{
		return parameterTypes.length;
	}
	
	public boolean matches(Token[] tks)
	{
		if(tks.length != parameterTypes.length)
		{
			return false;
		}
		
		for(int i = 0;i<tks.length;i++)
		{
			Token t = tks[i];
			if(parameterTypes[i] != t.getType() && t.getType() != Token.T_IDENTIFIER)
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
