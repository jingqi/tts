package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.Op;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptNullPointerException;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class IndexOp extends Op {

	Op body, index;

	public IndexOp(Op body, Op index) {
		super(body.getSourceLocation());
		this.body = body;
		this.index = index;
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval b = body.eval(f);
		if (b.getType() == EvalType.NULL) {
			throw new ScriptNullPointerException(index.getSourceLocation());
		} else if (b.getType() == EvalType.ARRAY) {
			IValueEval i = index.eval(f);
			if (i.getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException("Integer needed for index", index.getSourceLocation());
			return ((ArrayEval) b).get((int) ((IntegerEval) i).getValue());
		} else if (b.getType() == EvalType.STRING) {
			IValueEval i = index.eval(f);
			if (i.getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException("Integer needed for index", index.getSourceLocation());
			return ((StringEval) b).charAt((int) ((IntegerEval) i).getValue());
		} else if (b.getType() == EvalType.MAP) {
			IValueEval i = index.eval(f);
			IValueEval ret = ((MapEval) b).get(i);
			if (ret == null)
				return NullEval.instance;
			return ret;
		}

		throw new ScriptRuntimeException("Value can not be indexed", body.getSourceLocation());
	}

	@Override
	public Op optimize() {
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
