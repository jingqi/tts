package tts.grammar.tree;

import tts.eval.*;
import tts.grammar.tree.binaryop.AssignOp;
import tts.util.SourceLocation;
import tts.vm.*;

public final class DefinationOp implements IOp {

	SourceLocation sl;

	VarType type;
	String name;
	IOp value;

	public DefinationOp(VarType vt, String name, IOp value, String file,
			int line) {
		this.sl = new SourceLocation(file, line);
		this.type = vt;
		this.name = name;

		if (value == null) {
			switch (vt) {
			case BOOLEAN:
				value = new Operand(BooleanEval.FALSE, file, line);
				break;

			case INTEGER:
				value = new Operand(new IntegerEval(0), file, line);
				break;

			case DOUBLE:
				value = new Operand(new DoubleEval(0), file, line);
				break;
			}
		}
		this.value = value;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = new Variable(name, type, null);
		if (value != null)
			AssignOp.assign(v, value.eval(vm), sl);
		else
			AssignOp.assign(v, NullEval.instance, sl);
		vm.addVariable(name, v, sl);
		return VoidEval.instance;
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
		sb.append(type.name).append(" ").append(name);
		if (value != null)
			sb.append("=").append(value);
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
