package org.wk.test;

import java.io.InputStream;

import org.wk.kniftoscript.KniftoScript;
import org.wk.kniftoscript.ScriptException;
import org.wk.kniftoscript.comp.Assembler;
import org.wk.kniftoscript.comp.Lexer;
import org.wk.kniftoscript.comp.Compiler;

public class TestMain
{

	public static void main(String[] args)
	{
		/*KniftoScript script = new KniftoScript();
		script.loadScript(new int[]{//SCRIPT UNBRAUCHBAR, INSTRUKTIONEN GEÄNDERT!
				1,1,3,'f','o','o',			//DEC int foo	int foo;
				4,3,'f','o','o',1,'0',		//SET foo 0		foo = 0;
				1,1,3,'c','n','t',			//DEC int cnt	int cnt;
				4,3,'c','n','t',2,'4','2',	//SET cnt 42	cnt = 42;
											//x: 27
				9,							//CLS
				10,3,'f','o','o',			//PSH foo	foo++;
				11,1,'1',					//PSHI 1
				13,							//ADD
				12,3,'f','o','o',			//POP foo
				11,1,'1',					//PSHI 1	cnt--;
				10,3,'c','n','t',			//PSH cnt
				14,							//SUB
				12,3,'c','n','t',			//POP cnt
				10,3,'c','n','t',			//PSH cnt	if(cnt != 0)goto x
				22,27,0,0,0					//JPT 20
		});*/
		try
		{
			InputStream in = TestMain.class.getResourceAsStream("testscript.kss");
			//System.out.println((char)in.read());
			
			Lexer l = new Lexer(in);
			Compiler c = new Compiler();
			String script = c.compile(l);
			System.out.println("---Compiled script---");
			System.out.print(script);
			
			System.out.println("---Assembling---");
			
			Assembler asm = new Assembler();
			int[] code = asm.assemble(script);
			for(int b : code)
			{
				System.out.println(b + "\t" + (char)b);
			}
			
			KniftoScript ks = new KniftoScript();
			ks.loadScript(code);
			
			ks.run();
			
			//Token t;
			//while((t = l.readToken()) != null)
			//{
			//	System.out.println(t.toString());
			//}
			
			System.out.println(">>EOF");
			
			/*for(Field f : KniftoScript.class.getDeclaredFields())
			{
				if(f.getName().startsWith("OP_"))
				{
					String op = f.getName().substring(3);
					System.out.println("addOp(\"" + op + "\");");
				}
			}*/
			
			//script.run();
			System.out.println("Foo = " + ks.variables.get("foo").getValue());
			//System.out.println(script.variables.get("cnt").getValue());
		}catch (ScriptException e)
		{
			System.err.println("PC: " + e.getPC());
			e.printStackTrace();
		}catch(Exception ex)
		{
			//System.err.println("PCR: " + script.pc);
			ex.printStackTrace();
		}
	}	
}
