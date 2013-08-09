package org.wk.kniftoscript.comp;

import java.io.IOException;
import java.util.ArrayList;

import org.wk.kniftoscript.ScriptBytecodeRunner;
import org.wk.kniftoscript.Variable;

public class Compiler
{

	public Compiler()
	{
		
	}
	
	public String compile(Lexer l) throws IOException
	{
		ifBlockCount = 0;
		whileBlockCount = 0;
		
		for(String k : keywords)
			l.declareKeyword(k);
		
		for(String d : datatypes)
			l.declareKeyword(d);
		
		for(String o : operators)
			l.declareOperator(o);
		
		l.setIndicator(Lexer.I_COMMENT_SINGLELINE,"#");
		
		lexer = new TokenBuffer(l);
		out = new StringBuilder();
		program(SUB_NONE);
		return out.toString();
	}
	
	private void program(int subType) throws CompilerException
	{
		Token t = lexer.readToken();
		while(t != null)
		{
			if(t.getID() == Token.T_KEYWORD)
			{				
				boolean publicDec = false;
				
				if(t.equalsLexemIC("PUBLIC"))
				{
					t = lexer.readToken();
					publicDec = true;
				}else if(t.equalsLexemIC("PRIVATE"))
				{
					t = lexer.readToken();
					publicDec = false;
				}
				
				if(isDatatype(t))
				{
					variableDeclaration(t);
				}else if(t.equalsLexemIC("FUNC"))
				{
					if(subType != SUB_NONE)
						throw new CompilerException("Error in line " + t.getLine() + ": Functions have to be declared in main block.");
					
					functionDeclaration(publicDec);
				}else if(t.equalsLexemIC("IF"))
				{
					ifStatement();
				}else if(t.equalsLexemIC("ELSE"))
				{
					if(subType != SUB_IF)
						throw new CompilerException("Error in line " + t.getLine() + ": ELSE-blocks can only be part of IF-blocks");
					
					elseBlock = true;
					return;
					
				}else if(t.equalsLexemIC("ELSEIF"))
				{
					//TODO Implement ELSEIF
					throw new CompilerException("Error in line " + t.getLine() + ": ELSEIF is not yet implemented");
				}else if(t.equalsLexemIC("WHILE"))
				{
					whileStatement();
				}else if(t.equalsLexemIC("RETURN"))
				{
					returnStatement();
				}else if(t.equalsLexemIC("END"))
				{
					if(subType == SUB_NONE)
						warningPrint("END in main block prevents following code from being compiled.");
						
					return;
				}else
				{
					throw new CompilerException("Error in line " + t.getLine() + ": Unexpected keyword '" + t.getLexem() + "'");
				}
			}else if(t.getID() == Token.T_IDENTIFIER)
			{
				Token ident = t;
				t = lexer.peekToken();
				assignment(ident);
			}else
			{
				throw new CompilerException("Error in line " + t.getLine() + ": Unexpected token '" + t.toString() + "'");
			}
			
			t = lexer.readToken();
		}
	}
	
	private void assignment(Token i) throws CompilerException
	{
		if(i.getID() != Token.T_IDENTIFIER)
			throw new CompilerException("Error in line " + i.getLine() + ": Left eval has to start with an identifier.");
		
		String lastMember = i.getLexem();
		
		boolean functionFirst = false;
		Token t = lexer.peekToken();
		if(t.getID() == Token.T_OPERATOR && t.equalsLexem("("))
		{
			functionFirst = true;
			functionCall(i,false);
			asm("POPP " + ScriptBytecodeRunner.STACK_RETURN  + " #Pop from return stack");
		}else if(t.getID() == Token.T_OPERATOR && t.equalsLexem(";"))
		{
			asm("PSH '" + i.getLexem() + "'");
		}
		
		t = lexer.peekToken();
		if(t.getID() == Token.T_OPERATOR && t.equalsLexem("."))
		{
			if(!functionFirst)
				asm("PSH '" + i.getLexem() + "'");
			
			lexer.readToken();
			do
			{
				t = lexer.readToken();
				if(t.getID() != Token.T_IDENTIFIER)
					throw new CompilerException("Error in line " + t.getLine() + ": Expected identifier after . operator. Found: " + t.getLexem());
				
				Token member = t;
				t = lexer.peekToken();
				if(t.getID() == Token.T_OPERATOR && (t.equalsLexem(";") || t.equalsLexem(".")))
				{
					asm("GETM '" + member.getLexem() + "'");
				}else if(t.getID() == Token.T_OPERATOR && t.equalsLexem("("))
				{
					functionCall(member,true);
					
					t = lexer.peekToken();
					if(isAssignOp(t))
						throw new CompilerException("Error in line " + t.getLine() + ": Left hand side of assignment must be a variable");
					
					asm("POPP " + ScriptBytecodeRunner.STACK_RETURN + " #Pop from return stack");
				}else if(isAssignOp(t))
				{
					lastMember = member.getLexem();
				}else
				{
					throw new CompilerException("Error in line " + t.getLine() + ": Unrecognized token in left eval: " + t.getLexem());
				}
				
				t = lexer.peekToken();
				if((t.getID() == Token.T_OPERATOR && t.equalsLexem(";")) || isAssignOp(t))
				{
					break;
				}else if(t.getID() == Token.T_OPERATOR && t.equalsLexem("."))
				{
					lexer.readToken();
				}else
				{
					throw new CompilerException("Error in line " + t.getLine() + ": Unrecognized token in left eval: " + t.getLexem());
				}
			}while(t.getID() == Token.T_OPERATOR && t.equalsLexem("."));
		}
			
		if(isAssignOp(t))
		{
			if(t.equalsLexem("="))
			{
				lexer.readToken();
				expression();
				
				t = lexer.readToken();
				if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(";")))
					throw new CompilerException("Error in line " + t.getLine() + ": Expected semicolon after assignment!");
				
				asm("POPM '" + lastMember + "'");
			}else if(t.equalsLexem("+=") || t.equalsLexem("-=") || t.equalsLexem("*=") || t.equalsLexem("/="))
			{
				String sop = t.getLexem();
				
				asm("CPY");
				asm("GETM '" + lastMember + "'");
				
				lexer.readToken();
				expression();
				
				t = lexer.readToken();
				if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(";")))
					throw new CompilerException("Error in line " + t.getLine() + ": Expected semicolon after assignment!");
				
				if(sop.equals("+="))
					asm("ADD");
				else if(sop.equals("-="))
					asm("SUB");
				else if(sop.equals("*="))
					asm("MUL");
				else if(sop.equals("/="))
					asm("DIV");
				
				asm("POPM '" + lastMember + "'");
			}
		}else if(t.getID() == Token.T_OPERATOR && t.equalsLexem(";"))
		{
			asm("POP 'NULL'");
			lexer.readToken();
		}else
		{
			throw new CompilerException("Error in line " + t.getLine() + ": Expected instruction or assignment. Found: " + t.getLexem());
		}
	}
	
	private boolean elseBlock = false;
	
	private void ifStatement() throws CompilerException
	{
		int currentCount = ifBlockCount++;
		
		expression();
		asm("JPF ifNo" + currentCount + "_end");
		asm("CSS");
		program(SUB_IF);
		asm("DSS");
		if(elseBlock)
			asm("JMP ifNo" + currentCount + "_else_end");
		asm("ifNo" + currentCount + "_end:");
		if(elseBlock)
		{
			elseBlock = false;
			program(SUB_ELSE);
			asm("ifNo" + currentCount + "_else_end:");
		}
	}
	
	private void whileStatement() throws CompilerException
	{
		int currentCount = whileBlockCount++;
		
		asm("whileNo" + currentCount + "_start:");
		expression();
		asm("JPF whileNo" + currentCount + "_end");
		asm("CSS");
		program(SUB_WHILE);
		asm("DSS");
		asm("JMP whileNo" + currentCount + "_start");
		asm("whileNo" + currentCount + "_end:");
	}
	
	private void returnStatement() throws CompilerException
	{
		Token t = lexer.peekToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(";")))
		{
			expression();
			asm("PSHP " + ScriptBytecodeRunner.STACK_RETURN + " #Push to return stack");
		}
		
		t = lexer.readToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(";")))
			throw new CompilerException("Error in line " + t.getLine() + ": Expected semicolon after return statement.");
		
		asm("DSF");
		asm("RET");
	}
	
	private void variableDeclaration(Token type) throws CompilerException
	{
		if(!isDatatype(type))
			throw new CompilerException("Error in line " + type.getLine() + ": Expected datatype for variable declaration.");
		String datatype = type.getLexem();
		int typeId = Variable.typeNameToId(datatype);
		if(typeId == -1)
			throw new CompilerException("Error in line " + type.getLine() + ": Unknown datatype in variable declaration.");
		
		Token t = lexer.readToken();
		if(t.getID() != Token.T_IDENTIFIER)
			throw new CompilerException("Error in line " + t.getLine() + ": Expected variable name after datatype in variable declaration.");
		String variableName = t.getLexem();
		
		asm("DEC " + typeId + ",'" + variableName + "'");
		
		t = lexer.readToken();
		if(t.getID() == Token.T_OPERATOR && t.equalsLexem(";"))
		{
			return;
		}else if(t.getID() == Token.T_OPERATOR && t.equalsLexem("="))
		{
			expression();
			
			t = lexer.readToken();
			if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(";")))
				throw new CompilerException("Error in line " + t.getLine() + ": Expected semicolon after assignment expression.");
			
			asm("POP '" + variableName + "'");
		}else
		{
			throw new CompilerException("Error in line " + t.getLine() + ": Expected semicolon or assignment after variable declaration.");
		}
	}
	
	private void functionDeclaration(boolean isPublic) throws CompilerException
	{
		Token t = lexer.readToken();
		if(t.getID() != Token.T_IDENTIFIER)
			throw new CompilerException("Error in line " + t.getLine() + ": Expected function name after FUNC keyword.");
		String funcName = t.getLexem();
		
		t = lexer.readToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem("(")))
			throw new CompilerException("Error in line " + t.getLine() + ": Expected opening parentheses after function name in function declaration.");
		
		ArrayList<Variable> params = new ArrayList<Variable>();
		
		String defInstr = "DEF '" + funcName + "',func_" + funcName + "_start,";
		defInstr += (isPublic ? "1" : "0") + ",";
		
		t = lexer.readToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(")")))
		{
			int paramCount = 0;
			do
			{
				if(paramCount > 0)
					t = lexer.readToken();
				
				if(!isDatatype(t))
					throw new CompilerException("Error in line " + t.getLine() + ": Expected datatype in parameter list. Found: " + t.getLexem());
				int vType = Variable.typeNameToId(t.getLexem());
				if(vType == -1)
					throw new CompilerException("Error in line " + t.getLine() + ": Unknown datatype in parameter list.");
				
				t = lexer.readToken();
				if(t.getID() != Token.T_IDENTIFIER)
					throw new CompilerException("Error in line " + t.getLine() + ": Expected variable name after datatype in parameter list.");
				String vName = t.getLexem();
				
				params.add(new Variable(vName, vType));
				
				paramCount++;
				t = lexer.readToken();
			}while(t.getID() == Token.T_OPERATOR && t.equalsLexem(","));
			
			if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(")")))
				throw new CompilerException("Error in line " + t.getLine() + ": Expected closing parentheses after parameter list in function declaration.");
		}
		
		for(Variable v : params)
		{
			defInstr += v.getVarType() + ",";
		}
		defInstr = defInstr.substring(0, defInstr.length() - 1);
		asm(defInstr);
		asm("JMP func_" + funcName + "_end");
		asm("func_" + funcName + "_start:");
		asm("CSF");
		for(Variable v : params)
		{
			asm("DEC " + v.getVarType() + ",'" + v.getVarName() + "'");
			asm("POPP " + ScriptBytecodeRunner.STACK_PARAM + " #Pop from param stack");
			asm("POP '" + v.getVarName() + "'");
		}
		
		program(SUB_FUNC);
		asm("DSF");
		asm("RET");
		
		asm("func_" + funcName + "_end:");
	}
	
	private void functionCall(Token ident, boolean member) throws CompilerException
	{
		String funcName = ident.getLexem();
		
		Token t = lexer.readToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem("(")))
			throw new CompilerException("Error in line " + t.getLine() + ": Expected opening parentheses after function name in function call.");
		
		t = lexer.peekToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(")")))
		{
			int paramCount = 0;
			
			do
			{
				expression();
				//asm("PSHP " + ScriptBytecodeRunner.STACK_PARAM);
				paramCount++;
				t = lexer.readToken();
			}while(t.getID() == Token.T_OPERATOR && t.equalsLexem(","));
			
			if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(")")))
				throw new CompilerException("Error in line " + t.getLine() + ": Expected closing parentheses after parameter list in function call.");
			
			for(int i = 0; i< paramCount; i++)
			{
				asm("PSHP " + ScriptBytecodeRunner.STACK_PARAM + " #Push to param stack");
			}
			
		}else
		{
			lexer.readToken();
		}
		
		if(member)
			asm("CALM '" + funcName + "'");
		else
			asm("CAL '" + funcName + "'");
	}
	
	private void newStatement() throws CompilerException
	{
		Token t;
		String classname = "";
		do
		{
			t = lexer.readToken();
			if(t.getID() != Token.T_IDENTIFIER && t.getID() != Token.T_KEYWORD)
				throw new CompilerException("Error in line " + t.getLine() + ": Expected class or package name after new-operator. Found: " + t.getLexem());
			
			classname += t.getLexem();
			
			t = lexer.peekToken();
			if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(".")))
			{
				break;
			}else
			{
				classname += ".";
				lexer.readToken();
			}
			
		}while(t.getID() == Token.T_OPERATOR && t.equalsLexem("."));
		
		t = lexer.readToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem("(")))
			throw new CompilerException("Error in line " + t.getLine() + ": Expected opening parentheses after classname in new-statement.");
		
		t = lexer.peekToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(")")))
		{
			do
			{
				expression();
				asm("PSHP " + ScriptBytecodeRunner.STACK_CONSTRUCT  + " #Push to constructor stack");
				t = lexer.readToken();
			}while(t.getID() == Token.T_OPERATOR && t.equalsLexem(","));
			
			if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(")")))
				throw new CompilerException("Error in line " + t.getLine() + ": Expected closing parentheses after parameter list in constructor call.");
			
		}else
		{
			lexer.readToken();
		}
		
		asm("NEW '" + classname + "'");	
	}
	
	//<expression> ::= <arithmetic expression> [<relop> <arithmetic expression>]*
	//<arithmetic expression> ::= <term> [<addop> <term>]*
	//<term> ::= <signed factor> [<mulop> factor]*
	//<signed factor> ::= [<addop>] <factor>
	//<factor> ::= <integer> | <variable> | (<expression>)
	private void expression() throws CompilerException
	{
		arithmeticExpression();
		Token t = lexer.peekToken();
		while(isRelop(t))
		{
			lexer.readToken();
			arithmeticExpression();
			if(t.equalsLexem("=="))
			{
				asm("CMP");
			}else if(t.equalsLexem("<"))
			{
				asm("SMT");
			}else if(t.equalsLexem(">"))
			{
				asm("BGT");
			}else if(t.equalsLexem("<="))
			{
				asm("SOET");
			}else if(t.equalsLexem(">="))
			{
				asm("BOET");
			}else if(t.equalsLexem("!="))
			{
				asm("CMP");
				asm("INV");
			}
			t = lexer.peekToken();
		}
	}
	
	private void arithmeticExpression() throws CompilerException
	{
		term();
		Token t = lexer.peekToken();
		while(isAddop(t))
		{
			lexer.readToken();
			term();
			if(t.equalsLexem("+"))
				asm("ADD");
			else if(t.equalsLexem("-"))
				asm("SUB");
			t = lexer.peekToken();
		}
	}
	
	private void term() throws CompilerException
	{
		signedFactor();
		Token t = lexer.peekToken();
		while(isMulop(t))
		{
			lexer.readToken();
			factor();
			if(t.equalsLexem("*"))
				asm("MUL");
			else if(t.equalsLexem("/"))
				asm("DIV");
			t = lexer.peekToken();
		}
	}
	
	private void signedFactor() throws CompilerException
	{
		boolean negate = false;
		Token t = lexer.peekToken();
		if(isAddop(t) && t.equalsLexem("-"))
		{
			negate = true;
			lexer.readToken();
		}
		
		memberFactor();
		
		if(negate)
			asm("NEG");
	}
	
	private void memberFactor() throws CompilerException
	{
		factor();
		Token t = lexer.peekToken();
		while(t.getID() == Token.T_OPERATOR && t.equalsLexem("."))
		{
			lexer.readToken();
			t = lexer.readToken();
			if(t.getID() != Token.T_IDENTIFIER)
				throw new CompilerException("Error in line " + t.getLine() + ": Expected member identifier. Found: " + t.getLexem());
			Token member = t;
			
			t = lexer.peekToken();
			if(t.getID() == Token.T_OPERATOR && t.equalsLexem("("))
			{
				//Member function call
				functionCall(member,true);
				
				asm("POPP " + ScriptBytecodeRunner.STACK_RETURN  + " #Pop from return stack");
			}else
			{
				//Member field
				asm("GETM '" + member.getLexem() + "'");
			}
			t = lexer.peekToken();
		}
	}
	
	private void factor() throws CompilerException
	{
		Token t = lexer.readToken();
		if(t.getID() == Token.T_INTEGER)
		{
			asm("PSHI " + getTypeOfIntToken(t.getLexem()) +  ",'" + t.getLexem() + "'");
		}else if(t.getID() == Token.T_STRING)
		{
			asm("PSHI " + Variable.V_STRING + ",'" + t.getLexem() + "'");
		}else if(t.getID() == Token.T_IDENTIFIER)
		{
			Token l = lexer.peekToken();
			if(l.getID() == Token.T_OPERATOR && l.equalsLexem("("))
			{
				functionCall(t,false);
				asm("POPP " + ScriptBytecodeRunner.STACK_RETURN  + " #Pop from return stack");
			}else
			{
				asm("PSH '" + t.getLexem() + "'");
			}
		}else if(isParen(t))
		{
			if(!t.equalsLexem("("))
				throw new CompilerException("Error in line " + t.getLine() + ": Expected opening brace in expression");
			
			expression();
			
			Token cb = lexer.readToken();
			if(!(cb.equalsLexem(")") && cb.getID() == Token.T_OPERATOR))
				throw new CompilerException("Error in line " + cb.getLine() +": Expected closing brace in expression");
		}else if(t.getID() == Token.T_KEYWORD && t.equalsLexemIC("NEW"))
		{
			newStatement();
		}else if(t.getID() == Token.T_KEYWORD && t.equalsLexemIC("NULL"))
		{
			asm("PSH 'NULL'");
		}else
		{
			throw new CompilerException("Error in line " + t.getLine() +": Error in expression factor");
		}
	}
	
	private int getTypeOfIntToken(String s)
	{
		try
		{
			Integer.parseInt(s);
			return Variable.V_INT;
		}catch(Exception e){}
		try
		{
			Long.parseLong(s);
			return Variable.V_LONG;
		}catch(Exception e){}
		try
		{
			Float.parseFloat(s);
			return Variable.V_FLOAT;
		}catch(Exception e){}
		return 0;
	}
	
	private boolean isAddop(Token t)
	{
		if(t.getID() == Token.T_OPERATOR)
			return t.equalsLexem("+") || t.equalsLexem("-");
		return false;
	}
	
	private boolean isMulop(Token t)
	{
		if(t.getID() == Token.T_OPERATOR)
			return t.equalsLexem("*") || t.equalsLexem("/");
		return false;
	}
	
	private boolean isRelop(Token t)
	{
		if(t.getID() == Token.T_OPERATOR)
			return t.equalsLexem("<") || t.equalsLexem(">") || t.equalsLexem("==")
					|| t.equalsLexem("<=") || t.equalsLexem(">=") || t.equalsLexem("!=");
		return false;
	}
	
	private boolean isAssignOp(Token t)
	{
		if(t.getID() == Token.T_OPERATOR)
			return t.equalsLexem("=") || t.equalsLexem("+=") || t.equalsLexem("-=")
					|| t.equalsLexem("*=") || t.equalsLexem("/=");
		return false;
	}
	
	private boolean isParen(Token t)
	{
		if(t.getID() != Token.T_OPERATOR)
			return false;
		
		return t.equalsLexem("(") || t.equalsLexem(")");
	}
	
	private boolean isDatatype(Token t)
	{
		if(t.getID() != Token.T_KEYWORD)
			return false;
		
		for(String s : datatypes)
		{
			if(t.equalsLexemIC(s))
				return true;
		}
		return false;
	}
	
	private void asm(String s)
	{
		out.append(s);
		out.append("\n");
	}
	
	private void warningPrint(String s)
	{
		System.out.println("WARNING: " + s);
	}
	
	private TokenBuffer lexer;
	private StringBuilder out;
	
	private int ifBlockCount;
	private int whileBlockCount;
	
	public static final String[] keywords = {"PUBLIC","PRIVATE","FUNC","END","IF","ELSE","ELSEIF","WHILE","RETURN","NEW","NULL"};
	public static final String[] datatypes = {"INT","LONG","FLOAT","STRING","OBJECT"};
	public static final String[] operators = 
		{"+","-","*","/",
		"++","--",
		"=","+=","-=","*=","/=",
		"<",">","<=",">=","==","!=",
		"(",")",";",",","."};
	
	public static final int SUB_NONE = 0;
	public static final int SUB_IF = 1;
	public static final int SUB_ELSE = 2;
	public static final int SUB_FUNC = 3;
	public static final int SUB_WHILE = 4;
}
