package org.wk.kniftoscript;

public class KniftoScript
{

	public KniftoScript()
	{
		variables = new VariableStack();
		//context = new HashMap<String, Object>();
	}
	
	public void loadScript(int[] code)
	{
		scriptRunner = new ScriptBytecodeRunner(code, this);
	}
	
	public void run() throws ScriptException
	{
		if(scriptRunner != null)
			scriptRunner.run();
	}
	
	public void declareVar(String name, int type) throws ScriptException
	{
		if(Variable.typeIdToName(type) == null)
			throw new ScriptException("Invalid data type id: " + type + ", Variable name: " + name);
		
		Variable var = new Variable(name,type);
		variables.declareVariable(name, var);	
	}
	
	public boolean isVarDeclared(String name)
	{
		return variables.isDeclared(name);
	}
	
	public void disposeVar(String name) throws ScriptException
	{
		variables.disposeVariable(name);
	}
	
	public void setVariable(String name, Object val) throws ScriptException
	{
		if(variables.isDeclared(name))
		{
			variables.getVariable(name).setVar(val);
		}else
		{
			throw new ScriptException("Variable '" + name + "' is not declared!");
		}
	}
	
	public void putVariable(String name, Object value) throws ScriptException
	{
		putVariable(name,Variable.V_OBJECT,value);
	}
	
	public void putVariable(String name, int type, Object value) throws ScriptException
	{
		if(!variables.isDeclared(name))
			declareVar(name, type);
		
		setVariable(name, value);
	}
	
	public Variable getVariable(String name) throws ScriptException
	{
		return variables.getVariable(name);
	}
	
	public Object callFunction(String functionName, Object... parameter) throws ScriptException
	{
		if(scriptRunner == null)
			throw new ScriptException("Function '" + functionName + "' is not declared!");
		
		return scriptRunner.callFunctionInScript(functionName, parameter);
	}
	
	public VariableStack variables;
	
	private ScriptBytecodeRunner scriptRunner;
	
	public static final Integer TRUE_VAL = new Integer(1);
	public static final Integer FALSE_VAL = new Integer(0);
}
