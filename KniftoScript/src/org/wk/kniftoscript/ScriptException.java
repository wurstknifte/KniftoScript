package org.wk.kniftoscript;

public class ScriptException extends Exception
{
	public ScriptException(int pc) 
	{
		programCounter = pc;
	}
	
    public ScriptException(String msg) 
    {
        super(msg);
        programCounter = -1;
    }
	
    public ScriptException(String msg, int pc) 
    {
        super(msg);
        programCounter = pc;
    }
    
    public int getPC()
    {
    	return programCounter;
    }
    
	private static final long serialVersionUID = 1L;

	private int programCounter;
	
}
