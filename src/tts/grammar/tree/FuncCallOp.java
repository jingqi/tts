package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.FunctionEval;
import tts.eval.IValueEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class FuncCallOp implements IOp {

	IOp func;
	ArrayList<IOp> args;

	public FuncCallOp(IOp body, ArrayList<IOp> args) {
		this.func = body;
		this.args = args;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval b = func.eval(vm);
		if (b.getType() != IValueEval.EvalType.FUNCTION)
			throw new ScriptRuntimeException();

		ArrayList<IValueEval> as = new ArrayList<IValueEval>();
		for (int i = 0, size = args.size(); i < size; ++i)
			as.add(args.get(i).eval(vm));

		return ((FunctionEval) b).call(as);
	}

	@Override
	public IOp optimize() {
		func = func.optimize();
		for (int i = 0, size = args.size(); i < size; ++i)
			args.set(i, args.get(i).optimize());
		return this;
	}
}
