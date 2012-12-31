package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.*;

public class DefinationOp implements IOp {

	VarType type;
	String name;
	IOp value;

	public DefinationOp(VarType vt, String name, IOp value) {
		this.type = vt;
		this.name = name;
		this.value = value;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval e = null;
		if (value != null)
			e = value.eval(vm);
		vm.addVariable(name, new Variable(name, type, e));
		return VoidEval.instance;
	}
}
