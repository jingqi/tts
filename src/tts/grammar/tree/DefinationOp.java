package tts.grammar.tree;

import tts.eval.*;
import tts.grammar.tree.binaryop.AssignOp;
import tts.vm.*;

public final class DefinationOp implements IOp {

	String file;
	int line;

	VarType type;
	String name;
	IOp value;

	public DefinationOp(VarType vt, String name, IOp value, String file,
			int line) {
		this.type = vt;
		this.name = name;
		this.file = file;
		this.line = line;

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

			case STRING:
				value = new Operand(new StringEval(""), file, line);
				break;
			}
		}
		this.value = value;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		Variable v = new Variable(name, type, null);
		if (value != null)
			AssignOp.assign(v, value.eval(vm), file, line);
		vm.addVariable(name, v);
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
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
