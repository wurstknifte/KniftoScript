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
			}else if(varType == V_LONG)
			{
				value = Long.valueOf(((Integer)o).longValue());
				return;
			}else if(varType == V_FLOAT)
			{
				value = Float.valueOf(((Integer)o).floatValue());
				return;
			}else if(varType == V_OBJECT)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast integer into " + typeIdToName(varType));
		}else if(o instanceof Long)
		{
			if(varType == V_INT)
			{
				value = Integer.valueOf(((Long)o).intValue());
				return;
			}else if(varType == V_LONG)
			{
				value = (Long)o;
				return;
			}else if(varType == V_FLOAT)
			{
				value = Float.valueOf(((Long)o).floatValue());
				return;
			}else if(varType == V_OBJECT)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast long into " + typeIdToName(varType));
		}else if(o instanceof Float)
		{
			if(varType == V_INT)
			{
				value = Integer.valueOf(((Float)o).intValue());
				return;
			}else if(varType == V_LONG)
			{
				value = Long.valueOf(((Float)o).longValue());
				return;
			}else if(varType == V_FLOAT)
			{
				value = (Float)o;
				return;
			}else if(varType == V_OBJECT)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast float into " + typeIdToName(varType));
		}else if(o instanceof String)
		{
			if(varType == V_STRING)
			{
				value = o;
				return;
			}else if(varType == V_OBJECT)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast string into " + typeIdToName(varType));
		}else
		{
			if(varType == V_OBJECT)
			{
				value = o;
				return;
			}
			throw new ScriptException("Cannot cast object into " + typeIdToName(varType));
		}
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
		}else if(i == V_LONG)
		{
			return "long";
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
		}else if(s.equalsIgnoreCase("long"))
		{
			return V_LONG;
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
	
	public static int getVariableTypeByValue(Object o)
	{
		if(o instanceof Integer)
			return V_INT;
		else if(o instanceof Long)
			return V_LONG;
		else if(o instanceof Float)
			return V_FLOAT;
		else if(o instanceof String)
			return V_STRING;
		else
			return V_OBJECT;
	}
	
	public static boolean canCastTo(int type, int castType)
	{
		if(type == castType || castType == V_OBJECT)
			return true;
		
		if(type == V_INT)
		{
			return (castType == V_FLOAT) || (castType == V_LONG);
		}else if(type == V_LONG)
		{
			return (castType == V_INT) || (castType == V_FLOAT);
		}else if(type == V_FLOAT)
		{
			return (castType == V_INT) || (castType == V_LONG);
		}
		
		return false;
	}
	
	public static final int V_RESERVED = 0;
	public static final int V_INT = 1;
	public static final int V_LONG = 2;
	public static final int V_FLOAT = 4;
	public static final int V_DOUBLE = 5;
	public static final int V_STRING = 6;
	public static final int V_OBJECT = 7;
}
