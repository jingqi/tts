package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.IOp;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class IndexOp implements IOp {

	IOp body, index;

	public IndexOp(IOp body, IOp index) {
		this.body = body;
		this.index = index;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval b = body.eval(vm);
		if (b.getType() == EvalType.ARRAY) {
			IValueEval i = index.eval(vm);
			if (i.getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException();
			return ((ArrayEval) b).get((int) ((IntegerEval) i).getValue());
		} else if (b.getType() == EvalType.STRING) {
			IValueEval i = index.eval(vm);
			if (i.getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException();
			return ((StringEval) b).charAt((int) ((IntegerEval) i).getValue());
		}

		throw new ScriptRuntimeException();
	}

	@Override
	public IOp optimize() {
		body = body.optimize();
		index = index.optimize();
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(body).append("[").append(index).append("]");
		return sb.toString();
	}
}
