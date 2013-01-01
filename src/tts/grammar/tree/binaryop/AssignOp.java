package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.grammar.tree.IOp;
import tts.vm.*;

/**
 * 赋值操作
 */
public class AssignOp implements IOp {

	String varname;
	IOp value;

	public AssignOp(String name, IOp value) {
		this.varname = name;
		this.value = value;
	}

	public static void assign(Variable v, IValueEval vv) {
		switch (v.getType()) {
		case BOOLEAN:
			if (vv.getType() != IValueEval.EvalType.BOOLEAN)
				throw new ScriptRuntimeException();
			v.setValue(vv);
			break;

		case DOUBLE:
			if (vv.getType() == IValueEval.EvalType.DOUBLE)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.EvalType.INTEGER)
				v.setValue(new DoubleEval(((IntegerEval) vv).getValue()));
			else
				throw new ScriptRuntimeException();
			break;

		case INTEGER:
			if (vv.getType() == IValueEval.EvalType.INTEGER)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.EvalType.DOUBLE)
				v.setValue(new IntegerEval((long) ((DoubleEval) vv).getValue()));
			else
				throw new ScriptRuntimeException();
			break;

		case STRING:
			if (vv.getType() == IValueEval.EvalType.STRING)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException();
			break;

		case ARRAY:
			if (vv.getType() == IValueEval.EvalType.ARRAY)
				v.setValue(vv);
			else
				throw new ScriptRuntimeException();
			break;

		default:
			throw new ScriptRuntimeException();
		}
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = vm.getVariable(varname);
		IValueEval vv = value.eval(vm);
		assign(v, vv);
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
}
