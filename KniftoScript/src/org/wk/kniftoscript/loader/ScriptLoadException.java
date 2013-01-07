package org.wk.kniftoscript.loader;

import java.io.IOException;

public class ScriptLoadException extends IOException
{

	public ScriptLoadException() 
	{
		
	}
	
    public ScriptLoadException(String msg) 
    {
        super(msg);
    }
	
	private static final long serialVersionUID = 1L;

}
