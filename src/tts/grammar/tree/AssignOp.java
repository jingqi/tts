package tts.grammar.tree;

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
			if (vv.getType() != IValueEval.Type.BOOLEAN)
				throw new ScriptRuntimeException();
			v.setValue(vv);
			break;

		case DOUBLE:
			if (vv.getType() == IValueEval.Type.DOUBLE)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.Type.INTEGER)
				v.setValue(new DoubleEval(((IntegerEval) vv).getValue()));
			else
				throw new ScriptRuntimeException();
			break;

		case INTEGER:
			if (vv.getType() == IValueEval.Type.INTEGER)
				v.setValue(vv);
			else if (vv.getType() == IValueEval.Type.DOUBLE)
				v.setValue(new IntegerEval((long) ((DoubleEval) vv).getValue()));
			else
				throw new ScriptRuntimeException();
			break;

		case STRING:
			if (vv.getType() == IValueEval.Type.STRING)
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
