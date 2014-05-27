package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.*;
import tts.eval.function.UserFunctionEval;
import tts.eval.function.UserFunctionEval.ParamInfo;
import tts.trace.SourceLocation;
import tts.vm.Frame;

public class FuncDefOp extends Op {

	private String func;
	private Op ops;
	private ArrayList<ParamInfo> params;

	public FuncDefOp(String func, Op ops, ArrayList<ParamInfo> params, SourceLocation sl) {
		super(sl);
		this.func = func;
		this.ops = ops;
		this.params = params;
	}

	@Override
	public Op optimize() {
		if (ops != null)
			ops = ops.optimize();
		return this;
	}

	@Override
	public IValueEval eval(Frame f) {
		return new UserFunctionEval(func, f.currentScope(), ops, params);
	}

	@Override
	public String toString() {
		if (ops == null)
			return "";
		return ops.toString();
	}
}
