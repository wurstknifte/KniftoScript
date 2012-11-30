package org.wk.kniftoscript.comp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.wk.kniftoscript.KniftoScript;

public class Assembler
{
	
	public Assembler()
	{
		initSimpleOpcodes();
	}

	public byte[] assemble(String code) throws IOException
	{
		out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(code.getBytes());
		lexer = new TokenBuffer(new Lexer(in));
		
		while(lexer.available() > 0)
		{
			Token t = lexer.readToken();
			if(t.getType() != Token.T_IDENTIFIER && t.getType() != Token.T_KEYWORD)
				throw new CompilerException("ASM: Expected mnenomic, found " + t.toString());
				
			mnenomic(t.getValue());
		}
		return out.toByteArray();
	}
	
	private void mnenomic(String m)throws IOException
	{
		if(opcodes.get(m) == null)
			throw new CompilerException("ASM: Unknown mnenomic: " + m);
	}
	
	private void initSimpleOpcodes()
	{
		addOp("NOP",KniftoScript.OP_NOP,new int[]{});
		addOp("ADD",KniftoScript.OP_ADD,new int[]{});
		addOp("SUB",KniftoScript.OP_SUB,new int[]{});
		addOp("MUL",KniftoScript.OP_MUL,new int[]{});
		addOp("DIV",KniftoScript.OP_DIV,new int[]{});
		addOp("NEG",KniftoScript.OP_NEG,new int[]{});
		addOp("HLT",KniftoScript.OP_HLT,new int[]{});
	}
	
	private void addOp(String mn, int op, int... params)
	{
		opcodes.put(mn, new OpIdent(mn,op,params));
	}
	
	private TokenBuffer lexer;
	private ByteArrayOutputStream out;
	
	private HashMap<String,OpIdent> opcodes;
}
