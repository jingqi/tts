package tts.grammar.tree.binaryop;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.grammar.tree.IOp;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.ScriptNullPointerException;
import tts.vm.rtexcpt.ScriptRuntimeException;

public final class IndexOp implements IOp {

	IOp body, index;

	public IndexOp(IOp body, IOp index) {
		this.body = body;
		this.index = index;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval b = body.eval(vm);
		if (b.getType() == EvalType.NULL) {
			throw new ScriptNullPointerException(index.getSourceLocation());
		} else if (b.getType() == EvalType.ARRAY) {
			IValueEval i = index.eval(vm);
			if (i.getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException("integer needed for index", index);
			return ((ArrayEval) b).get((int) ((IntegerEval) i).getValue());
		} else if (b.getType() == EvalType.STRING) {
			IValueEval i = index.eval(vm);
			if (i.getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException("integer needed for index", index);
			return ((StringEval) b).charAt((int) ((IntegerEval) i).getValue());
		} else if (b.getType() == EvalType.MAP) {
			IValueEval i = index.eval(vm);
			IValueEval ret = ((MapEval) b).get(i);
			if (ret == null)
				return NullEval.instance;
			return ret;
		}

		throw new ScriptRuntimeException("value can not be indexed", body);
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

	@Override
	public SourceLocation getSourceLocation() {
		return body.getSourceLocation();
	}
}
