package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.ArrayEval;
import tts.eval.IValueEval;
import tts.vm.ScriptVM;

public class ArrayOp implements IOp {

	ArrayList<IOp> elements;

	public ArrayOp(ArrayList<IOp> v) {
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

	@Override
	public IOp optimize() {
		for (int i = 0, size = elements.size(); i < size; ++i)
			elements.set(i, elements.get(i).optimize());

		return this;
	}
}
