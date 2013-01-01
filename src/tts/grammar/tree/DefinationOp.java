package tts.grammar.tree;

import tts.eval.*;
import tts.vm.*;

public class DefinationOp implements IOp {

	VarType type;
	String name;
	IOp value;

	public DefinationOp(VarType vt, String name, IOp value) {
		this.type = vt;
		this.name = name;
		if (value != null) {
			this.value = value;
		} else {
			switch (vt) {
			case BOOLEAN:
				this.value = new Operand(BooleanEval.FALSE);
				break;

			case INTEGER:
				this.value = new Operand(new IntegerEval(0));
				break;

			case DOUBLE:
				this.value = new Operand(new DoubleEval(0));
				break;

			case STRING:
				this.value = new Operand(new StringEval(""));
				break;

			default:
				this.value = null;
			}
		}
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
