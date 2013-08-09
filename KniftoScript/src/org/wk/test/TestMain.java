package org.wk.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.wk.kniftoscript.KniftoScript;
import org.wk.kniftoscript.ScriptBytecodeRunner;
import org.wk.kniftoscript.ScriptException;
import org.wk.kniftoscript.Variable;
import org.wk.kniftoscript.comp.Assembler;
import org.wk.kniftoscript.comp.Lexer;
import org.wk.kniftoscript.comp.Compiler;
import org.wk.kniftoscript.comp.Token;

public class TestMain
{

	public static void main(String[] args)
	{
		/*String src = 
				"public int data = 1+1;\n" +
				"private float data2 = 1+(data*2);";
		
		ByteArrayInputStream bin = new ByteArrayInputStream(src.getBytes());*/
		TestMain appInstance = new TestMain();
		
		InputStream bin = TestMain.class.getResourceAsStream("testscript.kss");
		try{
			Lexer lx = new Lexer(bin);
			
			/*String[] keywords = {"PUBLIC","PRIVATE","FUNC","END","IF","ELSE","ELSEIF","WHILE","RETURN"};
			String[] datatypes = {"INT","FLOAT","STRING","OBJECT"};
			String[] operators = 
				{"+","-","*","/",
				"++","--",
				"=","+=","-=","*=","/=",
				"<",">","<=",">=","==","!=",
				"(",")",";",","};
			
			for(String k : keywords)
				lx.declareKeyword(k);
			
			for(String d : datatypes)
				lx.declareKeyword(d);
			
			for(String o : operators)
				lx.declareOperator(o);
			
			lx.setIndicator(Lexer.I_COMMENT_SINGLELINE,"#");
			
			Token t = lx.readToken();
			while(t != null)
			{
				System.out.println(t.getID() + ": " + t.getLexem());
				t = lx.readToken();
			}*/
			
			System.out.println("---Compiling...");
			Compiler comp = new Compiler();
			String asmSrc = comp.compile(lx);
			System.out.println("---Compiled script:");
			System.out.println(asmSrc);
			System.out.println("---Assembling...");
			Assembler asm = new Assembler();
			int[] code = asm.assemble(asmSrc);
			System.out.println("---Assembled " + code.length + " bytes:");
			for(int i = 0; i <code.length ; i++)
			{
				int b = code[i];
				System.out.println(i + ":\t" + b + "\t" + (char)b);
			}
			System.out.println("---Starting excecution...");
			KniftoScript ks = new KniftoScript();
			ks.loadScript(code);
			
			ks.putVariable("out", System.out);
			
			ks.run();
			ks.setVariable("thisIsAKlohbahhl", "this is a string");
			long time = System.currentTimeMillis();
			Object res = ks.callFunction("onInit",new Integer(5));
			long scriptTime = System.currentTimeMillis() - time;
			
			System.out.println("---Script terminated! Runtime: " + ( scriptTime == 0 ? "< 1ms" : (scriptTime + "ms")));
			
			System.out.println("Script result: " + res);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public static String bytesToHex(byte[] bytes) 
	{
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) 
	    {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public TestMain()
	{
		
	}
	
	public static int fact(int i)
	{
		if(i == 0)
			return 1;
		
		return fact(i-1)*i;
	}
	
	public int rand(int max)
	{
		return (int)(Math.random() * max);
	}
	
}


