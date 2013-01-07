package org.wk.kniftoscript;

import java.util.Arrays;

public class FuncIdent
{

	public FuncIdent(String name)
	{
		this(name,new int[]{});
	}
	
	public FuncIdent(String name, int[] paramTypes)
	{
		functionName = name;
		parameterTypes = paramTypes;
	}
	
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((functionName == null) ? 0 : functionName.hashCode());
		for(int i = 0; i<parameterTypes.length;i++)
			result = prime * result + parameterTypes[i];
		//result = prime * result + Arrays.hashCode(parameterTypes);
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FuncIdent))
			return false;
		FuncIdent other = (FuncIdent) obj;
		if (functionName == null)
		{
			if (other.functionName != null)
				return false;
		} else if (!functionName.equals(other.functionName))
			return false;
		if (!Arrays.equals(parameterTypes, other.parameterTypes))
			return false;
		return true;
	}

	public String toString()
	{
		String result = functionName + "(";
		for(int i = 0; i<parameterTypes.length;i++)
			result += Variable.typeIdToName(parameterTypes[i]) + ",";
		if(parameterTypes.length != 0)
			result = result.substring(0, result.length()-1);
		result += ")";
		return result;
	}
	
	public String functionName;
	public int[] parameterTypes;
}
