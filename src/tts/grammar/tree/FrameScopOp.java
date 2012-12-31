package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ScriptVM;

public class FrameScopOp implements IOp {

	IOp op;

	public FrameScopOp(IOp op) {
		this.op = op;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		vm.enterFrame();
		IValueEval ret = op.eval(vm);
		vm.leaveFrame();
		return ret;
	}
}
