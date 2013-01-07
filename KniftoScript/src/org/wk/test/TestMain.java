package org.wk.test;

import java.io.InputStream;

import org.wk.kniftoscript.KniftoScript;
import org.wk.kniftoscript.ScriptException;
import org.wk.kniftoscript.comp.Assembler;
import org.wk.kniftoscript.comp.Lexer;
import org.wk.kniftoscript.comp.Compiler;
import org.wk.kniftoscript.comp.Token;

public class TestMain
{

	public static void main(String[] args)
	{
		KniftoScript ks = null;
		try
		{
			InputStream in = TestMain.class.getResourceAsStream("testscript.kss");
			//System.out.println((char)in.read());
			
			Lexer l = new Lexer(in);
			Compiler c = new Compiler();
			System.out.println("---Compiling...");
			String script = c.compile(l);
			System.out.println("---Compiled script:");
			System.out.print(script);
			
			System.out.println("---Assembling...");
			
			Assembler asm = new Assembler();
			int[] code = asm.assemble(script);
			System.out.println("---Assembled " + code.length + " bytes:");
			for(int i = 0; i <code.length ; i++)
			{
				int b = code[i];
				System.out.println(i + ":\t" + b + "\t" + (char)b);
			}
			System.out.println("---Starting excecution...");
			ks = new KniftoScript();
			ks.loadScript(code);
			
			ks.run();
			
			/*Token t;
			while((t = l.readToken()) != null)
			{
				System.out.println(t.toString());
			}*/
			
			System.out.println("---Script terminated!");
			
			/*for(Field f : KniftoScript.class.getDeclaredFields())
			{
				if(f.getName().startsWith("OP_"))
				{
					String op = f.getName().substring(3);
					System.out.println("addOp(\"" + op + "\");");
				}
			}*/
			
			//script.run();
			System.out.println("Foo = " + ks.getVariable("foo").getValue());
			//System.out.println(script.variables.get("cnt").getValue());
		}catch (ScriptException e)
		{
			System.err.println("PC: " + e.getPC());
			e.printStackTrace();
		}catch(Exception ex)
		{
			ex.printStackTrace();
			System.err.println("PCR: " + ks.pc);
		}
	}	
}
