package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.vm.ScriptVM;

public final class FrameScopOp implements IOp {

	String file;
	int line;
	IOp op;

	public FrameScopOp(IOp op) {
		this.op = op;
		this.file = op.getFile();
		this.line = op.getLine();
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		vm.enterFrame();
		IValueEval ret = VoidEval.instance;
		if (op != null)
			ret = op.eval(vm);
		vm.leaveFrame();
		return ret;
	}

	@Override
	public IOp optimize() {
		if (op != null)
			op = op.optimize();
		if (op == null)
			return null;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n").append(op).append("\n}\n");
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
