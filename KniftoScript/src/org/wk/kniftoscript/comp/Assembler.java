package org.wk.kniftoscript.comp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.wk.kniftoscript.KniftoScript;
import org.wk.kniftoscript.Variable;

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
		l.keywords = getMnenomics();
		l.datatypes = new String[]{"null"};
		l.seperators = new int[]{',',':'};
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
			if(t.getType() == Token.T_KEYWORD)
			{
				mnenomic(t);
			}else if(t.getType() == Token.T_IDENTIFIER)
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
				program[ij + 1] = (byte)((adr & 0x000000FF) << 8);
				program[ij + 2] = (byte)((adr & 0x000000FF) << 16);
				program[ij + 3] = (byte)((adr & 0x000000FF) << 24);
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
		String lbl = t.getValue();
		Token dp = lexer.readToken();
		if(dp.getType() != Token.T_SEPERATOR && !dp.getValue().equals(":"))
			throw new CompilerException("ASM: Bad label definition, expected :, found " + dp.toString());
		
		labels.put(lbl,out.size());
		System.out.println("Defined label: " + lbl + " = " + labels.get(lbl));
	}
	
	private void mnenomic(Token t) throws IOException
	{
		String m = t.getValue();
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
		if(t.getType() != Token.T_SEPERATOR)
			throw new CompilerException("ASM: Expected seperator, found " + t.getValue());
	}
	
	private void specialMnenomic(String m) throws IOException
	{
		if(m.equalsIgnoreCase("DEF"))//Declare function
		{
			funcDef();
			return;
		}
		
		throw new CompilerException("ASM: Unknown special mnenomic: " + m);
	}
	
	private void funcDef() throws CompilerException
	{
		Token t = lexer.readToken();
		if(t.getType() != Token.T_LITERAL_STRING)
			throw new CompilerException("ASM: Expected string literal, found " + t.toString());
		
		String name = t.getValue();
		ArrayList<Integer> params = new ArrayList<Integer>();
		
		t = lexer.peekToken();
		while(t.getType() == Token.T_SEPERATOR && t.getValue() == ",")
		{
			lexer.readToken();
			t = lexer.readToken();
			if(t.getType() != Token.T_LITERAL_INT)
				throw new CompilerException("ASM: Expected int literal for datatype, found " + t.toString());
			
			int typeid = Variable.typeNameToId(t.getValue());
			params.add(typeid);
			t = lexer.peekToken();
		}
		out.write(KniftoScript.OP_DEF);
		printString(name);
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
				if(t.getType() == Token.T_LITERAL_STRING)
				{
					printString(t.getValue());
				}else if(t.getType() == Token.T_LITERAL_INT)
				{
					int val = Integer.parseInt(t.getValue());
					printInt(val);
				}else if(t.getType() == Token.T_IDENTIFIER)
				{
					neededLabels.put(out.size(), t.getValue());
					System.out.println("Requested label '" + t.getValue() + "'");
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
		int s = Token.T_LITERAL_STRING;
		int i = Token.T_LITERAL_INT;
		
		addOp("NOP");
		addOp("DEC",i,s);
		addOp("DIS",s);
		addOp("SET",s,s);
		addOp("SETI",s,s);
		addOp("NEW");//TODO Implement new object instruction
		addSpecOp("DEF");
		addOp("DIF",s);
		addOp("CLS");
		addOp("PSH",s);
		addOp("PSHI",s);
		addOp("POP",s);
		addOp("ADD");
		addOp("SUB");
		addOp("MUL");
		addOp("DIV");
		addOp("NEG");
		addOp("JMP",i);
		addOp("JPT",i);
		addOp("JPF",i);
		addOp("CAL",s);
		addOp("HDL",s);
		addOp("HLT");
		addOp("PRINTSTACK");
	}
	
	private void addOp(String mn, int... params)
	{
		//Get the opcode by reflection magic :D
		try
		{
			Field f = KniftoScript.class.getDeclaredField("OP_" + mn);
			int op = f.getInt(new KniftoScript());
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
