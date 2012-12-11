package org.wk.kniftoscript;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;

public class KniftoScript
{

	public KniftoScript()
	{
		variables = new HashMap<String,Variable>();
		functions = new HashMap<FuncIdent,Integer>();
		aStack = new ArrayDeque<Object>();
		callStack = new ArrayDeque<Integer>();
	}
	
	public void loadScript(int[] data) throws IOException, ScriptException
	{
		int headlen = data[0];
		headlen |= data[1] << 8;
		if(data.length < (headlen + 4))
			throw new IOException("Corrupt script header! Length field invalid");
		
		int checklen = data[headlen-1];
		checklen = data[headlen];
		if(checklen != headlen)
			throw new IOException("Corrupt script header! Length check token does not match");
		
		int[] preallocData = new int[headlen];
		for(int i = 2; i<headlen+1;i++)
			preallocData[i] = data[i];
		preallocate(preallocData);
		
		int[] scriptdata;
		for(int j = headlen+4;j < data.length;i++)
		{
			
		}
	}
	
	public void declareVar(String name, int type) throws ScriptException
	{
		if(Variable.typeIdToName(type) == null)
			throw new ScriptException("Invalid data type id: " + type, pc);
		
		if(variables.get(name) != null)
			throw new ScriptException("Variable " + name + " is already declared", pc);
		
		Variable var = new Variable(name,type);
		variables.put(name, var);	
	}
	
	public void disposeVar(String name) throws ScriptException
	{
		if(variables.get(name) == null)
		{
			throw new ScriptException("Could not dispose: Variable " + name + " is not declared", pc);
		}
		variables.remove(name);
	}
	
	public void setVariable(String name, Object val) throws ScriptException
	{
		if(variables.get(name) == null)
		{
			throw new ScriptException("Could not set: Variable " + name + " is not declared", pc);
		}
		variables.get(name).setVar(val);
	}
	
	public Variable getVariable(String name) throws ScriptException
	{
		if(variables.get(name) == null)
		{
			throw new ScriptException("Variable " + name + " is not declared", pc);
		}
		return variables.get(name);
	}
	
	public void preallocate(int prog[]) throws ScriptException
	{
		
	}
	
	public void run() throws ScriptException
	{
		while(pc < script.length)
		{
			int op = getByte(pc);
			switch(op)
			{
			case OP_NOP:
				pc++;
				break;
			case OP_DEC:
				dec();
				break;
			case OP_DIS:
				dis();
				break;
			case OP_SET:
				set();
				break;
			case OP_SETI:
				seti();
				break;
			case OP_NEW:
				newo();
				break;
			case OP_DEF:
				def();
				break;
				
			case OP_CLS:
				aStack.clear();
				pc++;
				break;
			case OP_PSH:
				push();
				break;
			case OP_PSHI:
				pushi();
				break;
			case OP_POP:
				pop();
				break;
			case OP_ADD:
				add();
				break;
			case OP_SUB:
				sub();
				break;
			case OP_MUL:
				mul();
				break;
			case OP_DIV:
				div();
				break;
				
			case OP_JMP:
				jmp();
				break;
			case OP_JPT:
				if(checkCondition())
					jmp();
				else
					pc += 5;
				break;
			case OP_JPF:
				if(!checkCondition())
					jmp();
				else
					pc += 5;
				break;
			case OP_CAL:
				cal();
				break;
			case OP_PRINTSTACK:
				debugInstruction(OP_PRINTSTACK);
				break;
				
			default:
				throw new ScriptException("Invalid opcode: " + op,pc);
			}
		}
	}
	
	private void dec() throws ScriptException
	{
		int type = readInt(pc+1);
		String name = readString(pc+5);
		declareVar(name,type);
		pc += 6 + name.length();
	}
	
	private void dis() throws ScriptException
	{
		String name = readString(pc+1);
		disposeVar(name);
		pc += 2 + name.length();
	}
	
	private void set() throws ScriptException
	{
		String name1 = readString(pc+1);
		String name2 = readString(pc+2+name1.length());
		if(variables.get(name2) == null)
			throw new ScriptException("Could not set: Variable " + name2 + " not declared!", pc);
		setVariable(name1,variables.get(name2).getValue());
		pc += 3 + name1.length() + name2.length();
	}
	
	private void seti() throws ScriptException
	{
		String name = readString(pc+1);
		String value = readString(pc+2+name.length());
		try{
			float f = Float.parseFloat(value);
			setVariable(name,f);
		}catch(NumberFormatException e)
		{
			setVariable(name,value);
		}
		pc += 3 + name.length() + value.length();
	}
	
	private void newo()
	{
		pc++;
	}
	
	private void def() throws ScriptException
	{
		String name = readString(pc+1);
		
	}
	
	private void push() throws ScriptException
	{
		String varname = readString(pc+1);
		Object varVal = getVariable(varname).getValue();
		aStack.push(varVal);
		pc += 2 + varname.length();
	}
	
	private void pushi() throws ScriptException
	{
		String value = readString(pc+1);
		Object o;
		try{
			o = Variable.stringToNumericValue(value);
		}catch(ScriptException e)
		{
			o = value;
		}
		aStack.push(o);
		pc += 2 + value.length();
	}
	
	private void pop() throws ScriptException
	{
		String varname = readString(pc+1);
		setVariable(varname, aStack.pop());
		pc += 2 + varname.length();
	}
	
	private void add() throws ScriptException
	{
		float f = 0;
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic operation on non-numeric value", pc);
		
		f = numoToFloat(op1) + numoToFloat(op2);
		aStack.push(Float.valueOf(f));
		pc += 1;
	}
	
	private void sub() throws ScriptException
	{
		float f = 0;
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic operation on non-numeric value", pc);
		
		f = numoToFloat(op1) - numoToFloat(op2);
		aStack.push(Float.valueOf(f));
		pc += 1;
	}
	
	private void mul() throws ScriptException
	{
		float f = 0;
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic operation on non-numeric value", pc);
		
		f = numoToFloat(op1) * numoToFloat(op2);
		aStack.push(Float.valueOf(f));
		pc += 1;
	}
	
	private void div() throws ScriptException
	{
		float f = 0;
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic operation on non-numeric value", pc);
		
		f = numoToFloat(op1) / numoToFloat(op2);
		aStack.push(Float.valueOf(f));
		pc += 1;
	}
	
	private void debugInstruction(int instr)
	{
		if(instr == OP_PRINTSTACK)
		{
			System.out.println("--Data stack--");
			Object oguz[] = aStack.toArray();
			for(int i = 0; i< aStack.size();i++)
			{
				Object o = oguz[i];
				System.out.println(i + ">" + o);
			}
			pc += 1;
		}
	}
	
	private boolean isNumericType(Object o)
	{
		return (o instanceof Float) || (o instanceof Integer);
	}
	
	private float numoToFloat(Object o)
	{
		if(o instanceof Integer)
			return ((Integer)o).floatValue();
		else if(o instanceof Float)
			return ((Float)o).floatValue();
		return 0;
	}
	
	private void jmp() throws ScriptException
	{
		int jmpto = readInt(pc+1);
		pc = jmpto;
	}
	
	private void cal() throws ScriptException
	{
		String func = readString(pc+1);
		
		int[] sv = new int[aStack.size()];
		Object[] oguz = aStack.toArray();
		/*for(int i = aStack.size()-1; i>=0;i--)
		{
			sv[i] = Variable.getVariableTypeByValue(oguz[i]);
		}*/
		for(int i = 0; i < aStack.size();i++)
		{
			sv[i] = Variable.getVariableTypeByValue(oguz[i]);
		}
		FuncIdent ident = new FuncIdent(func, sv);
		
		if(functions.get(ident) == null)
			throw new ScriptException("Function '" + ident.toString() + "' is not declared", pc);
		
		int funcAdr = functions.get(ident);
		pc += 2 + func.length();
		callStack.push(pc);
		pc = funcAdr;
	}
	
	private boolean checkCondition()
	{
		Object o = aStack.peek();
		if(o instanceof Integer)
		{
			if(((Integer)o) != 0)
			{
				return true;
			}
		}
		if(o instanceof Float)
		{
			if(((Float)o) != 0)
			{
				return true;
			}
		}
		return false;
	}
	
	private int readInt(int i) throws ScriptException
	{
		int r = 0;
		r |= (getByte(i) & 0xFF);
		r |= (getByte(i+1) & 0xFF) << 8;
		r |= (getByte(i+2) & 0xFF) << 16;
		r |= (getByte(i+3) & 0xFF) << 24;
		return r;
	}
	
	private String readString(int i) throws ScriptException
	{
		String result = "";
		int len = script[i];
		for(int j = 1;j<=len;j++)
		{
			result += (char)getByte(i+j);
		}
		return result;
	}
	
	private int getByte(int i) throws ScriptException
	{
		if(i >= script.length || i < 0)
		{
			throw new ScriptException("Out of data error: PC=" + pc + " DATASIZE=" + script.length + " ISSUED=" + i, pc);
		}
		return script[i] & 0xFF;
	}
	
	public int pc;
	private int[] script;
	private ArrayDeque<Object> aStack;
	private ArrayDeque<Integer> callStack;
	
	public HashMap<String,Variable> variables;
	public HashMap<FuncIdent,Integer> functions;
	
	public static final int OP_NOP = 0;//No operation
	public static final int OP_DEC = 1;//Declare variable
	public static final int OP_DIS = 2;//Dispose variable
	public static final int OP_SET = 3;//Set variable
	public static final int OP_SETI = 4;//Set varaible immediate
	public static final int OP_NEW = 5;//New object
	public static final int OP_DEF = 6;//Define function
	public static final int OP_DIF = 7;//Dispose function
	
	public static final int OP_CLS = 9;//Clear stack
	public static final int OP_PSH = 10;//Push variable to arithmetic stack
	public static final int OP_PSHI = 11;//Push value to arithmetic stack
	public static final int OP_POP = 12;//Pop arithmetic stack to variable
	public static final int OP_ADD = 13;//Add values on arithmetic stack
	public static final int OP_SUB = 14;//Subtract values on arithmetic stack
	public static final int OP_MUL = 15;//Multiply values on arithmetic stack
	public static final int OP_DIV = 16;//Divide values on arithmetic stack
	public static final int OP_NEG = 17;//Negates value on top of stack
	
	public static final int OP_JMP = 21;//Jump unconditional
	public static final int OP_JPT = 22;//Jump if value on stack is != 0
	public static final int OP_JPF = 23;//Jump if value on stack is == 0
	public static final int OP_CAL = 24;//IMPLEMENT! Function call
	
	public static final int OP_HDL = 42;//Hard label
	
	public static final int OP_PRINTSTACK = 254;//Print contents of stack to stdout
	public static final int OP_HLT = 255;//Halt operation
}
