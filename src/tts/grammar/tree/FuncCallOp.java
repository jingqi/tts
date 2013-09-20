package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.*;
import tts.eval.IValueEval.EvalType;
import tts.vm.ScriptVM;
import tts.vm.rtexcept.*;

public final class FuncCallOp extends Op {

	private Op func;
	private ArrayList<Op> args;

	public FuncCallOp(Op func, ArrayList<Op> args) {
		super(func.getSourceLocation());
		this.func = func;
		this.args = args;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval b = func.eval(vm);
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
			as.set(i, args.get(i).eval(vm));

		FunctionEval fe = (FunctionEval) b;
		vm.enterFrame(func.getSourceLocation(), fe.getModuleName());
		final IValueEval ret;
		try {
			ret = fe.call(as, vm, getSourceLocation());
		} catch (ScriptLogicException e) {
			vm.leaveFrame();
			throw e;
		}
		vm.leaveFrame();
		return ret;
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
