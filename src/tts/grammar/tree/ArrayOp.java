package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.ArrayEval;
import tts.eval.IValueEval;
import tts.trace.SourceLocation;
import tts.vm.Frame;

public final class ArrayOp extends Op {

	ArrayList<Op> elements;

	public ArrayOp(ArrayList<Op> v, SourceLocation sl) {
		super(sl);
		elements = v;
	}

	public ArrayOp(ArrayList<Op> v, String file, int line) {
		this(v, new SourceLocation(file, line));
	}

	@Override
	public IValueEval eval(Frame f) {
		ArrayEval ret = new ArrayEval();
		for (Op op : elements) {
			ret.add(op.eval(f));
		}
		return ret;
	}

	@Override
	public Op optimize() {
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
}
