package org.wk.kniftoscript.comp;

import java.io.IOException;
import java.util.ArrayList;

import org.wk.kniftoscript.Variable;

public class Compiler
{

	public Compiler()
	{
	}
	
	public String compile(Lexer l) throws IOException
	{
		l.keywords = new String[]{"PRINTSTACK","SCRIPT","IMPORT","FUNC","RETURN","IF","ELSE","ELSEIF","WHILE","NULL","END","BREAK"};
		l.datatypes = new String[]{"INT","FLOAT","STRING","OBJECT"};
		l.operators = new int[]{'+','-','*','/','.','=','>','<','!','|','&','~','^','%'};
		l.seperators = new int[]{','};
		l.endToken = ';';
		lexer = new TokenBuffer(l);
		asmout = new ASMBuilder();
		headOut = new StringBuilder();
		program(false);
		return joinHeadBody();
	}
	
	private String joinHeadBody()
	{
		return headOut.toString() + "HEADEND\n" + asmout.getAssembly();
	}
	
	private void program(boolean func) throws IOException
	{
		Token t;
		
		ArrayList<Variable> declaredVars;
		declaredVars = new ArrayList<Variable>();
		
		while((t = lexer.readToken()) != null)
		{
			
			if(t.getType() == Token.T_DATATYPE)
				declaredVars.add(variableDec(t));
		
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
				{
					return;
				}
				
				if(t.getValue().equalsIgnoreCase("IF"))
					ifStatement();
				
				if(t.getValue().equalsIgnoreCase("RETURN"))
				{	
					returnStatement();
				}
			}
			
			if(t.getType() == Token.T_IDENTIFIER)
			{
				Token identFT = lexer.peekToken();
				
				if(identFT.getType() != Token.T_OPERATOR && identFT.getType() != Token.T_BRACE)
					throw new CompilerException("Assignment or function call expected. Bad token: " + identFT.toString());
				
				if(identFT.getValue().equals("("))
				{
					functionCall(t);
				}else if(identFT.getValue().equals("="))
				{
					lexer.readToken();
					assignment(t);
				}else
				{
					throw new CompilerException("Assignment or function call expected. Bad operator: " + identFT.toString());
				}
			}
		}
		throw new CompilerException("Token stream ended before end of program block");
	}
	
	//<expression> ::= <arithmetic expression> [<relop> <arithmetic expression>]*
	//<arithmetic expression> ::= <term> [<addop> <term>]*
	//<term> ::= <signed factor> [<mulop> factor]*
	//<signed factor> ::= [<addop>] <factor>
	//<factor> ::= <integer> | <variable> | (<expression>)
	private void expression() throws IOException
	{
		arithmeticExpression();
		Token t = lexer.peekToken();
		while(isRelop(t))
		{
			lexer.readToken();
			arithmeticExpression();
			if(t.getValue().equals("=="))
			{
				out.append("CMP\n");
			}else if(t.getValue().equals("<"))
			{
				out.append("SMT\n");
			}else if(t.getValue().equals(">"))
			{
				out.append("BGT\n");
			}else if(t.getValue().equals("<="))
			{
				out.append("SOET\n");
			}else if(t.getValue().equals(">="))
			{
				out.append("BOET\n");
			}else if(t.getValue().equals("!="))
			{
				out.append("CMP\n");
				out.append("INV\n");
			}
			t = lexer.peekToken();
		}
	}
	
	private void arithmeticExpression() throws IOException
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
			out.append("NEG\n");
	}
	
	private void factor() throws IOException
	{
		Token t = lexer.readToken();
		if(t.getType() == Token.T_LITERAL_INT)
		{
			out.append("PSHI '" + t.getValue() + "'\n");
		}else if(t.getType() == Token.T_IDENTIFIER)
		{
			Token l = lexer.peekToken();
			if(l.getType() == Token.T_BRACE && l.getValue().equals("("))
			{
				functionCall(t);
			}else
			{
				out.append("PSH '" + t.getValue() + "'\n");
			}
		}else if(t.getType() == Token.T_BRACE)
		{
			if(!t.getValue().equals("("))
				throw new CompilerException("Expected opening brace, found " + t.toString());
			
			expression();
			
			Token cb = lexer.readToken();
			if(!(cb.getValue().equals(")") && cb.getType() == Token.T_BRACE))
				throw new CompilerException("Expected closing brace, found " + cb.toString());
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
	
	private boolean isRelop(Token t)
	{
		if(t.getType() == Token.T_OPERATOR)
			return t.getValue().equals("<") || t.getValue().equals(">") || t.getValue().equals("==")
					|| t.getValue().equals("<=") || t.getValue().equals(">=") || t.getValue().equals("!=");
		return false;
	}
	
	private void assignment(Token ident) throws IOException
	{
		expression();
		out.append("POP '" + ident.getValue() + "'\n");
	}
	
	private void ifStatement() throws IOException
	{
		expression();
		out.append("JPF ifNo" + ifBlockCount + "_end\n");
		program(true);
		out.append("ifNo" + ifBlockCount + "_end:\n");
	}
	
	private void returnStatement() throws IOException
	{
		Token t = lexer.peekToken();
		if(t.getType() != Token.T_KEYWORD)
			expression();
		out.append("RET\n");
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
		//out.append("DEF '" + funcName + "'\n");
		out.append("JMP " + funcName + "_end\n");
		out.append(funcName + "_start:\n");
		
		ArrayList<Variable> parameters = new ArrayList<Variable>(); 
		
		t = lexer.peekToken();
		while((t.getType() != Token.T_BRACE || !t.getValue().equals(")")))
		{
			t = lexer.readToken();
			if(t.getType() != Token.T_DATATYPE)
				throw new CompilerException("Expected variable definition, found " + t.toString());
			
			Variable var = variableDec(t);
			parameters.add(var);
			out.append("POP '" + var.getVarName() + "'\n");
			
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
		
		String defInstr = "DEF '" + funcName + "'," + funcName + "_start,"; 
		for(Variable v : parameters)
		{
			defInstr += v.getVarType() + ",";
		}
		defInstr = defInstr.substring(0, defInstr.length() - 1);
		headOut.append(defInstr + "\n");
		
		System.out.println("Function declaration: " + funcName);
		
		program(true);
		
		out.append("RET\n");
		out.append(funcName + "_end:\n");
		System.out.println("End function");
	}
	
	private void functionCall(Token ident) throws IOException
	{
		String fname = ident.getValue();
		
		Token t = lexer.readToken();
		if((t.getType() != Token.T_BRACE) && !t.getValue().equals("("))
			throw new CompilerException("Expected opening brace, found " + t.toString());
		
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
	
	private Variable variableDec(Token type) throws IOException
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
		return new Variable(name.getValue(),typeid);
	}
	
	private int ifBlockCount = 0;
	
	private TokenBuffer lexer;
	private ASMBuilder asmout;
	private StringBuilder headOut;
}
