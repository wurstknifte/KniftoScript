package org.wk.kniftoscript;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.wk.kniftoscript.loader.ScriptLoadException;

public class KniftoScript
{

	public KniftoScript()
	{
		//variables = new HashMap<String,Variable>();
		variables = new VariableStack();
		functions = new HashMap<FuncIdent,Integer>();
		aStack = new ArrayDeque<Object>();
		callStack = new ArrayDeque<Integer>();
	}
	
	public void loadScript(int[] data) throws IOException, ScriptException
	{
		script = data;
		headExecution = true;
		try
		{
			run();
		}catch(ScriptException e)
		{
			throw new ScriptLoadException("Script header error, caused by:\n" + e.toString());
		}
		headExecution = false;
		int[] headlessData = new int[data.length];
		for(int i = 0; i < data.length;i++)
		{
			if(i < pc)
				headlessData[i] = OP_NOP;
			else
				headlessData[i] = data[i];
		}
		
		System.out.println(headlessData.length + ";" + data.length);
		pc = 0;
		script = headlessData;
	}
	
	public void declareVar(String name, int type) throws ScriptException
	{
		if(Variable.typeIdToName(type) == null)
			throw new ScriptException("Invalid data type id: " + type, pc);
		
		Variable var = new Variable(name,type);
		variables.declareVariable(name, var);	
	}
	
	public void disposeVar(String name) throws ScriptException
	{
		variables.disposeVariable(name);
	}
	
	public void setVariable(String name, Object val) throws ScriptException
	{
		variables.getVariable(name).setVar(val);
	}
	
	public Variable getVariable(String name) throws ScriptException
	{
		return variables.getVariable(name);
	}
	
	public void declareFunction(FuncIdent ident, int adress) throws ScriptException
	{
		if(functions.get(ident) != null)
			throw new ScriptException("Function " + ident.toString() + " is already declared", pc);
		
		functions.put(ident, adress);
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
			case OP_NEG:
				neg();
				break;
			case OP_CMP:
				cmp();
				break;
			case OP_INV:
				inv();
				break;
			case OP_SMT:
				smt();
				break;
			case OP_BGT:
				bgt();
				break;
			case OP_SOET:
				soet();
				break;
			case OP_BOET:
				boet();
				break;
			case OP_LOR:
				lor();
				break;
			case OP_LAND:
				land();
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
			case OP_RET:
				ret();
				break;
			case OP_HEADEND:
				if(headExecution)
				{
					pc++;
					headExecution = false;
					return;
				}else
				{	
					throw new ScriptException("Header end in script body. Dafuq?",pc);
				}
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
		if(!variables.isDeclared(name2))
			throw new ScriptException("Could not set: Variable " + name2 + " not declared!", pc);
		if(!variables.isDeclared(name1))
			throw new ScriptException("Could not set: Variable " + name1 + " not declared!", pc);
		setVariable(name1,variables.getVariable(name2).getValue());
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
		ArrayList<Integer> prms = new ArrayList<Integer>();
		String name = readString(pc+1);
		int adress = readInt(pc + 2 + name.length());
		int params = getByte(pc + 6 + name.length());
		for(int i = 0; i<params;i++)
		{
			prms.add(getByte(pc + 7 + name.length() + i));
		}
		pc += (7 + name.length() + prms.size());
		int[] types = new int[prms.size()];
		for(int i = 0; i< prms.size();i++)
		{
			types[i] = prms.get(i);
		}
		FuncIdent id = new FuncIdent(name,types);
		declareFunction(id,adress);
		System.out.println("Function declared: " + id.toString());
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
		if(varname.equalsIgnoreCase("null"))
		{
			aStack.pop();
			return;
		}
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
	
	private void neg() throws ScriptException
	{
		Object op = aStack.pop();
		
		if(!isNumericType(op))
			throw new ScriptException("Tried to perform arithmetic operation on non-numeric value", pc);
		
		float f = -numoToFloat(op);
		aStack.push(Float.valueOf(f));
		pc += 1;
	}
	
	private void cmp()
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(op2 instanceof String || op1 instanceof String)
		{
			if(op2.toString().equals(op1.toString()))
				aStack.push(TRUE_VAL);
			else
				aStack.push(FALSE_VAL);
			
			pc += 1;
			return;
		}
		if(isNumericType(op2) && isNumericType(op1))
		{
			float f1 = numoToFloat(op1);
			float f2 = numoToFloat(op2);
			if(f1 == f2)
				aStack.push(TRUE_VAL);
			else
				aStack.push(FALSE_VAL);
			
			pc += 1;
			return;
		}
		aStack.push(FALSE_VAL);
		pc += 1;
	}
	
	private void bgt() throws ScriptException
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic relation on non-numeric value", pc);
		
		float f1 = numoToFloat(op1);
		float f2 = numoToFloat(op2);
		
		if(f1 > f2)
			aStack.push(TRUE_VAL);
		else
			aStack.push(FALSE_VAL);
		
		pc += 1;
	}
	
	private void boet() throws ScriptException
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic relation on non-numeric value", pc);
		
		float f1 = numoToFloat(op1);
		float f2 = numoToFloat(op2);
		
		if(f1 >= f2)
			aStack.push(TRUE_VAL);
		else
			aStack.push(FALSE_VAL);
		
		pc += 1;
	}
	
	private void soet() throws ScriptException
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic relation on non-numeric value", pc);
		
		float f1 = numoToFloat(op1);
		float f2 = numoToFloat(op2);
		
		if(f1 <= f2)
			aStack.push(TRUE_VAL);
		else
			aStack.push(FALSE_VAL);
		
		pc += 1;
	}
	
	private void smt() throws ScriptException
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform arithmetic relation on non-numeric value", pc);
		
		float f1 = numoToFloat(op1);
		float f2 = numoToFloat(op2);
		
		if(f1 < f2)
			aStack.push(TRUE_VAL);
		else
			aStack.push(FALSE_VAL);
		
		pc += 1;
	}
	
	private void lor() throws ScriptException
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform boolean arithmetic operation on non-numeric value", pc);
		
		float f1 = numoToFloat(op1);
		float f2 = numoToFloat(op2);
		
		if(f1 > 0 || f2 > 0)
			aStack.push(TRUE_VAL);
		else
			aStack.push(FALSE_VAL);
		
		pc += 1;
	}
	
	private void land() throws ScriptException
	{
		Object op2 = aStack.pop();
		Object op1 = aStack.pop();
		
		if(!(isNumericType(op1) && isNumericType(op2)))
			throw new ScriptException("Tried to perform boolean arithmetic operation on non-numeric value", pc);
		
		float f1 = numoToFloat(op1);
		float f2 = numoToFloat(op2);
		
		if(f1 > 0 && f2 > 0)
			aStack.push(TRUE_VAL);
		else
			aStack.push(FALSE_VAL);
		
		pc += 1;
	}
	
	
	private void inv()
	{
		if(checkCondition())
		{
			aStack.push(FALSE_VAL);
		}else
		{
			aStack.push(TRUE_VAL);
		}
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
		//Stack muss umgedreht werden, bessere Methode w�re w�nschenswert, aber nicht erforderlich
		int[] sv = new int[aStack.size()];
		Iterator<Object> iter = aStack.descendingIterator();
		int i = 0;
		while(iter.hasNext())
		{
			sv[i] = Variable.getVariableTypeByValue(iter.next());
			i++;
		}
		
		Object[] oguz = aStack.toArray();
		aStack.clear();
		for(int j = 0;j<oguz.length;j++)
		{
			aStack.push(oguz[j]);
		}
		
		FuncIdent ident = new FuncIdent(func, sv); 
		
		if(functions.get(ident) == null)
			throw new ScriptException("Function '" + ident.toString() + "' is not declared", pc);
		
		int funcAdr = functions.get(ident);
		pc += 2 + func.length();
		variables.newStackFrame();
		callStack.push(pc);
		pc = funcAdr;
	}
	
	private void ret() throws ScriptException
	{
		if(callStack.size() == 0)
			throw new ScriptException("Issued return while call stack was empty");
		
		variables.destroyStackFrame();
		pc = callStack.pop().intValue();
	}
	
	private boolean checkCondition()
	{
		Object o = aStack.pop();
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
	
	private boolean headExecution;
	
	//public HashMap<String,Variable> variables;
	public VariableStack variables;
	public HashMap<FuncIdent,Integer> functions;
	
	public static final Integer TRUE_VAL = new Integer(1);
	public static final Integer FALSE_VAL = new Integer(0);
	
	
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
	public static final int OP_CMP = 18;//Compares two values on top of stack
	public static final int OP_INV = 19;//Invertes boolean value on top of stack
	public static final int OP_SMT = 20;//If top < top-1 -> true [Smaller than]
	public static final int OP_BGT = 21;//If top > top-1 -> true [Bigger than]
	public static final int OP_SOET = 22;//If top <= top-1 -> true [Smaller or equal than]
	public static final int OP_BOET = 23;//If top >= top-1 -> true [Bigger or equal than]
	public static final int OP_LOR = 24;//If top || top-1 -> true [Logical or]
	public static final int OP_LAND = 25;//If top && top-1 -> true [Logical and]
	
	public static final int OP_JMP = 30;//Jump unconditional
	public static final int OP_JPT = 31;//Jump if value on stack is != 0
	public static final int OP_JPF = 32;//Jump if value on stack is == 0
	public static final int OP_CAL = 33;//Function call
	public static final int OP_RET = 34;//Function return
	
	public static final int OP_HDL = 42;//Hard label
	
	public static final int OP_HEADEND = 253;//Marks end of script header
	public static final int OP_PRINTSTACK = 254;//Print contents of stack to stdout
	public static final int OP_HLT = 255;//Halt operation
}
