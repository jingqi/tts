package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ScriptVM;
import tts.vm.Variable;

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
		v.setValue(vv);
		return vv;
	}
}
