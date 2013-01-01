package tts.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import tts.eval.FunctionEval;
import tts.eval.IValueEval;
import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public class FuncCallOp implements IOp {

	IOp func;
	List<IOp> args;

	public FuncCallOp(IOp body, List<IOp> args) {
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
}
