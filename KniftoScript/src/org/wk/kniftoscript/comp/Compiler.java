package org.wk.kniftoscript.comp;

import java.io.IOException;

import org.wk.kniftoscript.Variable;

public class Compiler
{

	public Compiler()
	{
	}
	
	public String compile(Lexer l) throws IOException
	{
		l.keywords = new String[]{"PRINTSTACK","SCRIPT","IMPORT","FUNC","IF","ELSE","ELSEIF","WHILE","NULL","END","BREAK"};
		l.datatypes = new String[]{"INT","FLOAT","STRING","OBJECT"};
		l.operators = new int[]{'+','-','*','/','.','=','|','&','~','^','%'};
		l.seperators = new int[]{','};
		lexer = new TokenBuffer(l);
		out = new StringBuilder();
		program(false);
		return out.toString();
	}
	
	private void program(boolean func) throws IOException
	{
		Token t;
		
		while((t = lexer.readToken()) != null)
		{
			
			if(t.getType() == Token.T_DATATYPE)
				variableDec(t);
		
			if(t.getType() == Token.T_KEYWORD)
			{
				if(t.getValue().equalsIgnoreCase("FUNC"))
				{
					if(!func)
						functionDec();
					else
						throw new CompilerException("Function declared in function");
				}
				
				if(t.getValue().equalsIgnoreCase("PRINTSTACK"))
				{
					out.append("PRINTSTACK\n");
				}
				
				if(t.getValue().equalsIgnoreCase("END"))
					return;
				
				if(t.getValue().equalsIgnoreCase("IF"))
					ifStatement();
			}
			
			if(t.getType() == Token.T_IDENTIFIER)
			{
				Token identFT = lexer.readToken();
				
				if(identFT.getType() != Token.T_OPERATOR && identFT.getType() != Token.T_BRACE)
					throw new CompilerException("Assignment or function call expected. Bad token: " + identFT.toString());
				
				if(identFT.getValue().equals("("))
				{
					functionCall(t);
				}else if(identFT.getValue().equals("="))
				{
					assignment(t);
				}else
				{
					throw new CompilerException("Assignment or function call expected. Bad operator: " + identFT.toString());
				}
			}
		}
		throw new CompilerException("Token stream ended before end of program block");
	}
	
	//<expression> ::= <term> [<addop> <term>]*
	//<term> ::= <signed factor> [<mulop> factor]*
	//<signed factor> ::= [<addop>] <factor>
	//<factor> ::= <integer> | <variable> | (<expression>)
	private void expression() throws IOException
	{
		term();
		Token t = lexer.peekToken();
		while(isAddop(t))
		{
			lexer.readToken();
			term();
			if(t.getValue().equals("+"))
				out.append("ADD\n");
			else if(t.getValue().equals("-"))
				out.append("SUB\n");
			t = lexer.peekToken();
		}
	}
	
	private void term() throws IOException
	{
		signedFactor();
		Token t = lexer.peekToken();
		while(isMulop(t))
		{
			lexer.readToken();
			factor();
			if(t.getValue().equals("*"))
				out.append("MUL\n");
			else if(t.getValue().equals("/"))
				out.append("DIV\n");
			t = lexer.peekToken();
		}
	}
	
	private void signedFactor() throws IOException
	{
		boolean negate = false;
		Token t = lexer.peekToken();
		if(isAddop(t) && t.getValue().equals("-"))
		{
			negate = true;
			lexer.readToken();
		}
		
		factor();
		
		if(negate)
			out.append("NEG\n");//Negating code here
	}
	
	private void factor() throws IOException
	{
		Token t = lexer.readToken();
		if(t.getType() == Token.T_LITERAL_INT)
		{
			out.append("PSHI '" + t.getValue() + "'\n");
		}else if(t.getType() == Token.T_IDENTIFIER)
		{
			out.append("PSH '" + t.getValue() + "'\n");
		}else if(t.getType() == Token.T_BRACE)
		{
			if(!t.getValue().equals("("))
				throw new CompilerException("Expected opening brace, found " + t.getValue());
			
			expression();
			
			Token cb = lexer.readToken();
			if(!(cb.getValue().equals(")") && cb.getType() == Token.T_BRACE))
				throw new CompilerException("Expected closing brace, found " + cb.getValue());
		}
	}
	
	private boolean isAddop(Token t)
	{
		if(t.getType() == Token.T_OPERATOR)
			return t.getValue().equals("+") || t.getValue().equals("-");
		return false;
	}
	
	private boolean isMulop(Token t)
	{
		if(t.getType() == Token.T_OPERATOR)
			return t.getValue().equals("*") || t.getValue().equals("/");
		return false;
	}
	
	private void assignment(Token ident) throws IOException
	{
		expression();
		System.out.println("SET " + ident.getValue() + " TO X");
		out.append("POP '" + ident.getValue() + "'\n");
	}
	
	private void ifStatement() throws IOException
	{
		//Token t = lexer.readToken();
		expression();
		program(true);
	}
	
	private void functionDec() throws IOException
	{
		Token t = lexer.readToken();
		
		if(t.getType() != Token.T_IDENTIFIER)
			throw new CompilerException("Identifier expected, found " + t.toString());
		
		String funcName = t.getValue();
		
		t = lexer.readToken();
		if(t.getType() != Token.T_BRACE || !t.getValue().equals("("))
			throw new CompilerException("Expected parameter list, found " + t.toString());
		
		//Eventuell vertauschen
		out.append("DEF '" + funcName + "'\n");
		out.append("JMP " + funcName + "_end\n");
		
		t = lexer.peekToken();
		while((t.getType() != Token.T_BRACE || !t.getValue().equals(")")))
		{
			t = lexer.readToken();
			if(t.getType() != Token.T_DATATYPE)
				throw new CompilerException("Expected variable definition, found " + t.toString());
			
			String vname = variableDec(t);
			out.append("POP '" + vname + "'\n");
			
			t = lexer.peekToken();
			if(t == null)
			{
				throw new CompilerException("Token stream ended before closing parameter list!");//FIXME Compiler hängt statt diese Nachricht auszugeben
			}else if(t.getType() == Token.T_SEPERATOR && t.getValue().equals(","))
			{
				lexer.readToken();
				continue;
			}else if(t.getType() == Token.T_BRACE && t.getValue().equals(")"))
			{//FIXME Unprofessionell. Um Nörgler vorzubeugen, unbedingt fixen
				lexer.readToken();
				break;
			}else
			{
				throw new CompilerException("Illegal statement in parameter list: " + t.toString());
			}
		}
		
		System.out.println("Function declaration: " + funcName);
		
		program(true);
		
		out.append(funcName + "_end:\n");
		System.out.println("End function");
	}
	
	private void functionCall(Token ident) throws IOException
	{
		String fname = ident.getValue();
		
		Token t = lexer.peekToken();
		while((t.getType() != Token.T_BRACE || !t.getValue().equals(")")))
		{
			expression();
			
			t = lexer.peekToken();
			if(t == null)
			{
				throw new CompilerException("Token stream ended before closing parameter list!");//FIXME Compiler hängt statt diese Nachricht auszugeben
			}else if(t.getType() == Token.T_SEPERATOR && t.getValue().equals(","))
			{
				lexer.readToken();
				continue;
			}else if(t.getType() == Token.T_BRACE && t.getValue().equals(")"))
			{//FIXME Unprofessionell. Um Nörgler vorzubeugen, unbedingt fixen
				lexer.readToken();
				break;
			}else
			{
				throw new CompilerException("Illegal statement in parameter list: " + t.toString());
			}
		}
		out.append("CAL '" + fname + "'\n");
	}
	
	private String variableDec(Token type) throws IOException
	{
		int typeid = Variable.typeNameToId(type.getValue());
		if(typeid == -1)
			throw new CompilerException("Datatype '" + type.getValue() + "' is not defined!");
		
		Token name = lexer.readToken();
		if(name.getType() != Token.T_IDENTIFIER)
			throw new CompilerException("Expected identifier!");
		
		out.append("DEC " + typeid + ",'" + name.getValue() + "'\n");
		
		Token nxt = lexer.peekToken();
		if(nxt.getType() == Token.T_OPERATOR && nxt.getValue().equals("="))//Intitialisierung
		{
			lexer.readToken();
			assignment(name);
		}
		
		System.out.println("Declared variable '" + name.getValue() + "' as " + type.getValue());
		return name.getValue();
	}
	
	private TokenBuffer lexer;
	private StringBuilder out;
}
