package tts.grammar.tree;

import java.util.List;

import tts.eval.ArrayEval;
import tts.eval.IValueEval;
import tts.vm.ScriptVM;

public class ArrayOp implements IOp {

	List<IOp> elements;

	public ArrayOp(List<IOp> v) {
		elements = v;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		ArrayEval ret = new ArrayEval();
		for (IOp op : elements) {
			ret.add(op.eval(vm));
		}
		return ret;
	}
}
