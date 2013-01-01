package tts.grammar.tree;

import javax.script.ScriptException;

import tts.eval.*;
import tts.vm.*;

/**
 * 赋值操作
 */
public class AssignOp implements IOp {

	String varname;
	IOp eval;

	public AssignOp(String name, IOp value) {
		this.varname = name;
		this.eval = value;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = vm.getVariable(varname);
		IValueEval vv = eval.eval(vm);
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
		return v.getValue();
	}
}
