package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.grammar.tree.IOp;
import tts.vm.*;

/**
 * 赋值操作
 */
public final class AssignOp implements IOp {

	String file;
	int line;

	String varname;
	IOp value;

	public AssignOp(String name, IOp value, String file, int line) {
		this.varname = name;
		this.value = value;
		this.file = file;
		this.line = line;
	}

	public static void assign(Variable v, IValueEval vv, String file, int line) {
		switch (v.getType()) {
		case BOOLEAN:
			if (vv.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			v.setValue(vv);
			break;

		case DOUBLE:
			if (vv.getType() == IValueEval.EvalType.DOUBLE)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.EvalType.INTEGER)
				v.setValue(new DoubleEval(((IntegerEval) vv).getValue()));
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			break;

		case INTEGER:
			if (vv.getType() == IValueEval.EvalType.INTEGER)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.EvalType.DOUBLE)
				v.setValue(new IntegerEval((long) ((DoubleEval) vv).getValue()));
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			break;

		case STRING:
			if (vv.getType() == IValueEval.EvalType.STRING)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			break;

		case ARRAY:
			if (vv.getType() == IValueEval.EvalType.ARRAY)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			break;

		case MAP:
			if (vv.getType() == IValueEval.EvalType.MAP)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			break;

		case FUNCTION:
			if (vv.getType() == IValueEval.EvalType.FUNCTION)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", file, line);
			break;

		default:
			throw new ScriptRuntimeException(
					"assignment not supported for type " + v.getType().name,
					file, line);
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = vm.getVariable(varname);
		IValueEval vv = value.eval(vm);
		assign(v, vv, file, line);
		return v.getValue();
	}

	@Override
	public IOp optimize() {
		if (value != null)
			value = value.optimize();
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(varname).append(" = ").append(value);
		return sb.toString();
	}

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
