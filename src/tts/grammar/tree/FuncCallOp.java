package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.eval.function.FunctionEval;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptNullPointerException;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class FuncCallOp extends Op {

	private Op func;
	private ArrayList<Op> args;

	public FuncCallOp(Op func, ArrayList<Op> args) {
		super(func.getSourceLocation());
		this.func = func;
		this.args = args;
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval b = func.eval(f);
		if (b.getType() == EvalType.NULL)
			throw new ScriptNullPointerException(func.getSourceLocation());
		else if (b.getType() != IValueEval.EvalType.FUNCTION)
			throw new ScriptRuntimeException("Function value needed", func.getSourceLocation());

		// 从右向左计算实参的值
		final int arg_count = args.size();
		ArrayList<IValueEval> as = new ArrayList<IValueEval>(arg_count);
		for (int i = 0; i < arg_count; ++i)
			as.add(null);
		for (int i = arg_count - 1; i >= 0; --i)
			as.set(i, args.get(i).eval(f));

		FunctionEval fe = (FunctionEval) b;
		return fe.call(f, as, getSourceLocation());
	}

	@Override
	public Op optimize() {
		func = func.optimize();
		for (int i = args.size() - 1; i >= 0; --i)
			args.set(i, args.get(i).optimize());
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(func).append("(");
		for (int i = 0; i < args.size(); ++i) {
			if (i != 0)
				sb.append(",");
			sb.append(args.get(i));
		}
		sb.append(")");
		return sb.toString();
	}
}
