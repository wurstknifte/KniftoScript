package org.wk.kniftoscript.comp;

public class Token
{

	public Token(int type, String value)
	{
		typeId = type;
		tokenValue = value;
	}
	
	public int getType()
	{
		return typeId;
	}
	
	public String getValue()
	{
		return tokenValue;
	}
	
	public String toString()
	{
		String result = "(" + typeId + "=";
		switch(typeId)
		{
		case -1:
			result += "INVALID";
			break;
		case 0:
			result += "NULL";
			break;
		case 1:
			result += "KEYWORD";
			break;
		case 2:
			result += "OPERATOR";
			break;
		case 3:
			result += "IDENTIFIER";
			break;
		case 4:
			result += "LITERAL";
			break;
		case 7:
			result += "STRING LITERAL";
			break;
		case 8:
			result += "END TOKEN";
			break;
		case 5:
		case 6:
			result += "I have no idea why I defined this one.";
			break;
		case 9:
			result += "DATATYPE";	
			break;
		case 10:
			result += "BRACE";
			break;
		case 11:
			result += "SEPERATOR";
			break;
		default:
			result += "I don't know what you have done, but this token seems awkward...";	
		}
		result += "):" + tokenValue;
		return result;
	}
	
	private int typeId;
	private String tokenValue;
	
	public static final int T_INVALID = -1;
	public static final int T_NULL = 0;
	public static final int T_KEYWORD = 1;
	public static final int T_OPERATOR = 2;
	public static final int T_IDENTIFIER = 3;
	public static final int T_LITERAL_INT = 4;
	public static final int T_LITERAL_INTHEX = 5;
	public static final int T_LITERAL_FLOAT = 6;
	public static final int T_LITERAL_STRING = 7;
	public static final int T_COMMAND_END = 8;
	public static final int T_DATATYPE = 9;
	public static final int T_BRACE = 10;
	public static final int T_SEPERATOR = 11;
}
