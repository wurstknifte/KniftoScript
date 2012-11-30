package org.wk.test;

import java.io.InputStream;
import org.wk.kniftoscript.comp.Lexer;
import org.wk.kniftoscript.comp.Token;
import org.wk.kniftoscript.comp.Compiler;

public class TestMain
{

	public static void main(String[] args)
	{
		/*KniftoScript script = new KniftoScript();
		script.loadScript(new int[]{
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
			//Token t;
			//while((t = l.readToken()) != null)
			//{
			//	System.out.println(t.toString());
			//}
			
			System.out.println(">>EOF");
			//script.run();
			//System.out.println(script.variables.get("foo").getValue());
			//System.out.println(script.variables.get("cnt").getValue());
		} //catch (ScriptException e)
		//{
			//System.err.println("PCR: " + script.pc);
			//System.err.println("PC: " + e.getPC());
			//e.printStackTrace();
		/*}*/catch(Exception ex)
		{
			//System.err.println("PCR: " + script.pc);
			ex.printStackTrace();
		}
	}

	/*public static final int OP_NOP = 0;//No operation
	public static final int OP_DEC = 1;//Declare variable
	public static final int OP_DIS = 2;//Dispose variable
	public static final int OP_SET = 3;//Set variable
	public static final int OP_SETI = 4;//Set varaible immediate
	public static final int OP_NEW = 5;//New object
	
	public static final int OP_CLS = 9;//Clear stack
	public static final int OP_PSH = 10;//Push variable to arithmetic stack
	public static final int OP_PSHI = 11;//Push value to arithmetic stack
	public static final int OP_POP = 12;//Pop arithmetic stack to variable
	public static final int OP_ADD = 13;//Add values on arithmetic stack
	public static final int OP_SUB = 14;//Subtract values on arithmetic stack
	public static final int OP_MUL = 15;//Multiply values on arithmetic stack
	public static final int OP_DIV = 16;//Divide values on arithmetic stack
	
	public static final int OP_JMP = 21;//Jump unconditional
	public static final int OP_JPT = 22;//Jump if value on stack is != 0
	public static final int OP_JPF = 23;//Jump if value on stack is == 0
	
	public static final int OP_HLT = 255;//Halt operation*/
	
}
