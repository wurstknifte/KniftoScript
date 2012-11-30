package org.wk.kniftoscript;

public class Variable
{

	public Variable(String name, int type)
	{
		varName = name;
		varType = type;
	}
	
	public void setVar(Object o) throws ScriptException
	{
		if(o instanceof Integer)
		{
			if(varType == V_INT)
			{
				value = (Integer)o;
				return;
			}else if(varType == V_FLOAT)
			{
				value = Float.valueOf(((Integer)o).floatValue());
				return;
			}
			throw new ScriptException("Cannot cast integer into " + varType);
		}else if(o instanceof Float)
		{
			if(varType == V_INT)
			{
				value = Integer.valueOf(((Float)o).intValue());
				return;
			}else if(varType == V_FLOAT)
			{
				value = (Float)o;
				return;
			}
			throw new ScriptException("Cannot cast float into " + typeIdToName(varType));
		}else if(o instanceof String)
		{
			if(varType == V_STRING)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast string into " + typeIdToName(varType));
		}else if(o instanceof VarObject)
		{
			if(varType == V_OBJECT)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast object into " + typeIdToName(varType));
		}
		throw new ScriptException("Invalid object type for variable type " + typeIdToName(varType));
	}
	
	public Object getValue()
	{
		return value;
	}
	
	public String getVarName()
	{
		return varName;
	}
	
	public int getVarType()
	{
		return varType;
	}
	
	private String varName;
	private Object value;
	private int varType;
	
	public static String typeIdToName(int i)
	{
		if(i == V_RESERVED)
		{
			return "RESERVED TYPE";
		}else if(i == V_INT)
		{
			return "int";
		}else if(i == V_FLOAT)
		{
			return "float";
		}else if(i == V_STRING)
		{
			return "string";
		}else if(i == V_OBJECT)
		{
			return "object";
		}
		return null;
	}
	
	public static int typeNameToId(String s)
	{
		if(s.equalsIgnoreCase("int"))
		{
			return V_INT;
		}else if(s.equalsIgnoreCase("float"))
		{
			return V_FLOAT;
		}else if(s.equalsIgnoreCase("string"))
		{
			return V_STRING;
		}else if(s.equalsIgnoreCase("object"))
		{
			return V_OBJECT;
		}
		return -1;
	}
	
	public static Object stringToNumericValue(String s) throws ScriptException
	{
		Object o;
		try
		{
			o = Integer.parseInt(s);
			return o;
		}catch(NumberFormatException e){}
		try
		{
			o = Float.parseFloat(s);
			return o;
		}catch(NumberFormatException e){}
		throw new ScriptException("Bad numeric value: " + s, -1);
	}
	
	public static final int V_RESERVED = 0;
	public static final int V_INT = 1;
	public static final int V_FLOAT = 2;
	public static final int V_STRING = 3;
	public static final int V_OBJECT = 4;
}
