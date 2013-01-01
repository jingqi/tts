package tts.grammar.tree;

import tts.eval.*;
import tts.grammar.tree.binaryop.AssignOp;
import tts.vm.*;

public class DefinationOp implements IOp {

	VarType type;
	String name;
	IOp value;

	public DefinationOp(VarType vt, String name, IOp value) {
		this.type = vt;
		this.name = name;

		if (value == null) {
			switch (vt) {
			case BOOLEAN:
				value = new Operand(BooleanEval.FALSE);
				break;

			case INTEGER:
				value = new Operand(new IntegerEval(0));
				break;

			case DOUBLE:
				value = new Operand(new DoubleEval(0));
				break;

			case STRING:
				value = new Operand(new StringEval(""));
				break;
			}
		}
		this.value = value;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = new Variable(name, type, null);
		if (value != null)
			AssignOp.assign(v, value.eval(vm));
		vm.addVariable(name, v);
		return VoidEval.instance;
	}

	@Override
	public IOp optimize() {
		if (value != null)
			value = value.optimize();
		return this;
	}
}
