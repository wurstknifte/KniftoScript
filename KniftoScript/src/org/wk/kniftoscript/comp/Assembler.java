package org.wk.kniftoscript.comp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.wk.kniftoscript.KniftoScript;
import org.wk.kniftoscript.ScriptBytecodeRunner;

public class Assembler
{
	
	public Assembler()
	{
		opcodes = new HashMap<String, OpIdent>();
		initOpcodes();
	}

	public int[] assemble(String code) throws IOException
	{
		neededLabels = new HashMap<Integer, String>();
		labels = new HashMap<String, Integer>();
		
		out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(code.getBytes());
		Lexer l = new Lexer(in);
		for(String s : getMnenomics())
			l.declareKeyword(s);
		l.declareKeyword("null");
		l.declareOperator(",");
		l.declareOperator(":");
		l.setIndicator(Lexer.I_STRING_START, "'");
		l.setIndicator(Lexer.I_STRING_END, "'");
		l.setIndicator(Lexer.I_COMMENT_SINGLELINE, "#");
		lexer = new TokenBuffer(l);
		
		/*//Collect labels
		while(lexer.available() > 0)
		{
			Token t = lexer.readToken();
			if(t.getType() == Token.T_IDENTIFIER)
			{
				Token dp = lexer.peekToken();
				if(dp.getType() == Token.T_SEPERATOR && dp.getValue().equals(":"))
					label(t);
			}
		}*/
		
		//lexer.reset();
		
		while(lexer.available() > 0)
		{
			Token t = lexer.readToken();
			if(t.getID() == Token.T_KEYWORD)
			{
				mnenomic(t);
			}else if(t.getID() == Token.T_IDENTIFIER)
			{
				label(t);
			}else{
				throw new CompilerException("ASM: Expected mnenomic or label, found " + t.toString());
			}
		}
		
		byte[] program = out.toByteArray();
		int[] intarray = new int[program.length];
		
		for(int ij = 0; ij<program.length;ij++)
		{
			
			if(neededLabels.get(ij) != null)
			{
				String label = neededLabels.get(ij);
				if(labels.get(label) == null)
					throw new CompilerException("ASM: Undefined label: " + label);
				
				int adr = labels.get(label);
				
				program[ij] = (byte)((adr & 0x000000FF));
				program[ij + 1] = (byte)((adr & 0x0000FF00) >> 8);
				program[ij + 2] = (byte)((adr & 0x00FF0000) >> 16);
				program[ij + 3] = (byte)((adr & 0xFF000000) >> 24);
			}
			
			int i = program[ij];
			if(i < 0)
				i = i + 256;
			intarray[ij] = i;
		}
		
		return intarray;
	}
	
	private void label(Token t) throws IOException
	{
		String lbl = t.getLexem();
		Token dp = lexer.readToken();
		if(dp.getID() != Token.T_OPERATOR && !dp.equalsLexem(":"))
			throw new CompilerException("ASM: Bad label definition or unknown mnenomic(" + lbl + "); expected :, found " + dp.toString());
		
		labels.put(lbl,out.size());
		System.out.println("Defined label: " + lbl + " = " + labels.get(lbl));
	}
	
	private void mnenomic(Token t) throws IOException
	{
		String m = t.getLexem();
		if(opcodes.get(m) == null)
			throw new CompilerException("ASM: Unknown mnenomic: " + m);
		
		if(opcodes.get(m).getOpcode() == -1)
		{
			specialMnenomic(m);
			return;
		}
		
		OpIdent ident = opcodes.get(m);
		Token[] args = new Token[ident.getParameterCount()];
		for(int i = 0; i<ident.getParameterCount();i++)
		{
			args[i] = lexer.readToken();
			if(i < ident.getParameterCount()-1)
				expectSeperator(lexer.readToken());
		}
		if(ident.matches(args))
		{
			printInstruction(ident,args);
		}else
		{
			throw new CompilerException("ASM: Bad arguments for mnenomic '" + m + "'");
		}
	}
	
	private void expectSeperator(Token t) throws IOException
	{
		if(t.getID() != Token.T_OPERATOR)
			throw new CompilerException("ASM: Expected seperator, found " + t.getLexem());
	}
	
	private void specialMnenomic(String m) throws IOException
	{
		if(m.equalsIgnoreCase("DEF"))//Declare function
		{
			funcDef();
			return;
		}else if(m.equalsIgnoreCase("POPP"))//POP-PUSH
		{
			poppPshp(true);
			return;
		}else if(m.equalsIgnoreCase("PSHP"))//PUSH-POP
		{
			poppPshp(false);
			return;
		}
		
		throw new CompilerException("ASM: Unknown special mnenomic: " + m);
	}
	
	//These are in here to save 3 bytes
	private void poppPshp(boolean isPopp) throws CompilerException
	{
		Token t = lexer.readToken();
		if(t.getID() != Token.T_INTEGER)
			throw new CompilerException("ASM: Expected integer literal, found " + t.toString());
		
		out.write(isPopp ? ScriptBytecodeRunner.OP_POPP : ScriptBytecodeRunner.OP_PSHP);
		out.write(Integer.parseInt(t.getLexem()) & 0xFF);
	}
	
	private void funcDef() throws CompilerException
	{
		//DEF Instruction = <Opcode><Name><Adress><Access type><Parameter count>[<Parameter Type>]*
		Token t = lexer.readToken();
		if(t.getID() != Token.T_STRING)
			throw new CompilerException("ASM: Expected string literal, found " + t.toString());
		String name = t.getLexem();
		
		Token label = lexer.readToken();
		if(!(label.getID() == Token.T_OPERATOR && label.equalsLexem(",")))
			throw new CompilerException("ASM: Expected seperator, found " + t.toString());
		label = lexer.readToken();
		
		out.write(ScriptBytecodeRunner.OP_DEF);
		printString(name);
		if(label.getID() == Token.T_IDENTIFIER)
		{
			neededLabels.put(out.size(), label.getLexem());
			printInt(0);
		}else if(label.getID() == Token.T_INTEGER)
		{
			printInt(Integer.parseInt(label.getLexem()));
		}else
		{
			throw new CompilerException("ASM: Expected adress or label, found " + t.toString());
		}
		
		t = lexer.readToken();
		if(!(t.getID() == Token.T_OPERATOR && t.equalsLexem(",")))
			throw new CompilerException("ASM: Expected seperator, found " + t.toString());
		
		t = lexer.readToken();
		if(t.getID() != Token.T_INTEGER)
			throw new CompilerException("ASM: Expected integer for access type, found " + t.toString());
		out.write(Integer.parseInt(t.getLexem()) & 0xFF);
		
		ArrayList<Integer> params = new ArrayList<Integer>();
		
		t = lexer.peekToken();
		while(t.getID() == Token.T_OPERATOR && t.equalsLexem(","))
		{
			lexer.readToken();
			t = lexer.readToken();
			if(t.getID() != Token.T_INTEGER)
				throw new CompilerException("ASM: Expected int literal for datatype, found " + t.toString());
			
			int typeid = Integer.parseInt(t.getLexem());//Variable.typeNameToId(t.getValue());
			params.add(typeid);
			t = lexer.peekToken();
		}
		
		out.write(params.size() & 0xFF);
		for(int dt : params)
			out.write(dt & 0xff);
	}
	
	private void printInstruction(OpIdent op, Token[] tks) throws CompilerException
	{
		if(op.matches(tks))
		{
			out.write(op.getOpcode());
			for(int i = 0; i<op.getParameterCount();i++)
			{
				Token t = tks[i];
				if(t.getID() == Token.T_STRING)
				{
					printString(t.getLexem());
				}else if(t.getID() == Token.T_INTEGER)
				{
					int val = Integer.parseInt(t.getLexem());
					printInt(val);
				}else if(t.getID() == Token.T_IDENTIFIER)
				{
					neededLabels.put(out.size(), t.getLexem());
					System.out.println("Requested label '" + t.getLexem() + "'");
					printInt(0);
				}
			}
		}
	}
	
	private void printInt(int val)
	{
		out.write((val & 0x000000ff));
		out.write((val & 0x0000ff00) >>> 8);
		out.write((val & 0x00ff0000) >>> 16);
		out.write((val & 0xff000000) >>> 24);
	}
	
	private void printString(String s)
	{
		out.write(s.length()&0xFF);
		for(int i = 0; i<s.length();i++)
		{
			out.write(s.charAt(i)&0xFF);
		}
	}
	
	private String[] getMnenomics()
	{
		ArrayList<String> m = new ArrayList<String>(opcodes.size());
		for(OpIdent i : opcodes.values())
		{
			m.add(i.getMnenomic());
		}
		return m.toArray(new String[m.size()]);
	}
	
	private void initOpcodes()
	{
		int s = Token.T_STRING;
		int i = Token.T_INTEGER;
		
		addOp("NOP");
		addOp("DEC",i,s);
		addOp("DIS",s);
		addOp("SET",s,s);
		addOp("SETI",s,s);
		addOp("NEW",s);
		addSpecOp("DEF");
		addOp("DIF",s);
		addOp("CLS");
		addOp("PSH",s);
		addOp("PSHI",i,s);
		addOp("POP",s);
		addOp("ADD");
		addOp("SUB");
		addOp("MUL");
		addOp("DIV");
		addOp("NEG");
		addOp("CMP");
		addOp("BGT");
		addOp("SMT");
		addOp("BOET");
		addOp("SOET");
		addOp("LOR");
		addOp("LAND");
		addSpecOp("POPP");
		addSpecOp("PSHP");
		addOp("INV");
		addOp("GETM",s);
		addOp("POPM",s);
		addOp("CPY");
		addOp("JMP",i);
		addOp("JPT",i);
		addOp("JPF",i);
		addOp("CAL",s);
		addOp("RET");
		addOp("CSF");
		addOp("DSF");
		addOp("CSS");
		addOp("DSS");
		addOp("CALM",s);
		addOp("HDL",s);
		addOp("HLT");
		addOp("PRINTSTACK");
		addOp("HEADEND");
	}
	
	private void addOp(String mn, int... params)
	{
		//Get the opcode by reflection magic :D
		try
		{
			Field f = ScriptBytecodeRunner.class.getDeclaredField("OP_" + mn);
			int op = f.getInt(new ScriptBytecodeRunner(new int[]{},new KniftoScript()));
			opcodes.put(mn, new OpIdent(mn,op,params));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addSpecOp(String mn)
	{
		opcodes.put(mn, new OpIdent(mn,-1));
	}
	
	private HashMap<Integer,String> neededLabels;//Adresse, Name des einzutragenden Labels
	private HashMap<String,Integer> labels;//Name des definierten Labels, Adresse
	
	private TokenBuffer lexer;
	private ByteArrayOutputStream out;
	
	private HashMap<String,OpIdent> opcodes;
}
