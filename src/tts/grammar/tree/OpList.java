package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.util.SourceLocation;
import tts.vm.Frame;

/**
 * 操作列表
 */
public final class OpList extends Op {

	ArrayList<Op> list = new ArrayList<Op>();

	public static final OpList VOID = new OpList(new SourceLocation(
			"<native_file>", -1));

	public OpList(SourceLocation sl) {
		super(sl);
	}

	public OpList(String file, int line) {
		super(new SourceLocation(file, line));
	}

	public void add(Op op) {
		list.add(op);
	}

	public Op get(int i) {
		return list.get(i);
	}

	public int size() {
		return list.size();
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval ret = VoidEval.instance;
		for (int i = 0, size = list.size(); i < size; ++i) {
			ret = list.get(i).eval(f);
		}
		return ret;
	}

	@Override
	public Op optimize() {
		ArrayList<Op> nl = new ArrayList<Op>(list.size());
		for (int i = 0, size = list.size(); i < size; ++i) {
			Op e = list.get(i).optimize();
			if (e != null)
				nl.add(e);
		}

		if (nl.size() == 0)
			return null;
		else if (nl.size() == 1)
			return nl.get(0);
		list = nl;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); ++i)
			sb.append(list.get(i)).append(";\n");
		return sb.toString();
	}
}
