package org.wk.kniftoscript;

public class FuncIdent
{

	public FuncIdent(String name)
	{
		this(name,null);
	}
	
	public FuncIdent(String name, int[] paramTypes)
	{
		functionName = name;
		parameterTypes = paramTypes;
	}
	
	public boolean equals(Object o)
	{
		if(o == this)
			return true;
		
		if(o instanceof FuncIdent)
		{
			FuncIdent fi = (FuncIdent)o;
			return (fi.functionName.equalsIgnoreCase(functionName)) && (fi.parameterTypes == parameterTypes);
		}
		return false;
	}
	
	public String functionName;
	public int[] parameterTypes;
}
