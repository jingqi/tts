package tts.grammar.tree;

import tts.eval.*;
import tts.eval.scope.EvalSlot;
import tts.eval.scope.VarType;
import tts.grammar.tree.binaryop.AssignOp;
import tts.util.SourceLocation;
import tts.vm.Frame;

public final class DefinationOp extends Op {

	VarType type;
	String name;
	Op value;

	public DefinationOp(VarType vt, String name, Op value, String file,
			int line) {
		super(new SourceLocation(file, line));
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
	public IValueEval eval(Frame f) {
		EvalSlot v = new EvalSlot(type, null);
		if (value != null)
			AssignOp.assign(v, value.eval(f), getSourceLocation());
		else
			AssignOp.assign(v, NullEval.instance, getSourceLocation());
		f.addVariable(name, v, getSourceLocation());
		return VoidEval.instance;
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
		sb.append(type.name).append(" ").append(name);
		if (value != null)
			sb.append("=").append(value);
		return sb.toString();
	}
}
