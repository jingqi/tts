package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.grammar.tree.Op;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.Variable;
import tts.vm.rtexcpt.ScriptRuntimeException;

/**
 * 赋值操作
 */
public final class AssignOp extends Op {

	String varname;
	Op value;

	public AssignOp(String name, Op value, SourceLocation sl) {
		super(sl);
		this.varname = name;
		this.value = value;
	}

	public AssignOp(String name, Op value, String file, int line) {
		this(name, value, new SourceLocation(file, line));
	}

	public static void assign(Variable v, IValueEval vv, SourceLocation sl) {
		switch (v.getType()) {
		case VAR:
			v.setValue(vv);
			break;

		case BOOLEAN:
			if (vv.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			v.setValue(vv);
			break;

		case DOUBLE:
			if (vv.getType() == IValueEval.EvalType.DOUBLE)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.EvalType.INTEGER)
				v.setValue(new DoubleEval(((IntegerEval) vv).getValue()));
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			break;

		case INTEGER:
			if (vv.getType() == IValueEval.EvalType.INTEGER)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.EvalType.DOUBLE)
				v.setValue(new IntegerEval((long) ((DoubleEval) vv).getValue()));
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			break;

		case STRING:
			if (vv.getType() == IValueEval.EvalType.STRING || vv.getType() == IValueEval.EvalType.NULL)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			break;

		case ARRAY:
			if (vv.getType() == IValueEval.EvalType.ARRAY || vv.getType() == IValueEval.EvalType.NULL)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			break;

		case MAP:
			if (vv.getType() == IValueEval.EvalType.MAP || vv.getType() == IValueEval.EvalType.NULL)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			break;

		case FUNCTION:
			if (vv.getType() == IValueEval.EvalType.FUNCTION || vv.getType() == IValueEval.EvalType.NULL)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException(
						"type not match in assignment", sl);
			break;

		default:
			throw new ScriptRuntimeException(
					"assignment not supported for type " + v.getType().name, sl);
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = vm.getVariable(varname, this.getSourceLocation());
		IValueEval vv = value.eval(vm);
		assign(v, vv, getSourceLocation());
		return v.getValue();
	}

	@Override
	public Op optimize() {
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
}
