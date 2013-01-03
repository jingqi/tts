package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.ArrayEval;
import tts.eval.IValueEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;

public final class ArrayOp implements IOp {

	SourceLocation sl;
	ArrayList<IOp> elements;

	public ArrayOp(ArrayList<IOp> v, SourceLocation sl) {
		this.sl = sl;
		elements = v;
	}

	public ArrayOp(ArrayList<IOp> v, String file, int line) {
		this(v, new SourceLocation(file, line));
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0, size = elements.size(); i < size; ++i)
			sb.append(elements.get(i)).append(", ");
		sb.append("]");
		return sb.toString();
	}

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}

}
