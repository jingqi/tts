package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.IValueEval;
import tts.eval.VoidEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;

/**
 * 操作列表
 */
public final class OpList implements IOp {

	SourceLocation sl;
	ArrayList<IOp> list = new ArrayList<IOp>();

	public static final OpList VOID = new OpList(new SourceLocation(
			"<native_file>", -1));

	public OpList(SourceLocation sl) {
		this.sl = sl;
	}

	public OpList(String file, int line) {
		this.sl = new SourceLocation(file, line);
	}

	public void add(IOp op) {
		list.add(op);
	}

	public IOp get(int i) {
		return list.get(i);
	}

	public int size() {
		return list.size();
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		IValueEval ret = VoidEval.instance;
		for (int i = 0, size = list.size(); i < size; ++i) {
			ret = list.get(i).eval(vm);
		}
		return ret;
	}

	@Override
	public IOp optimize() {
		ArrayList<IOp> nl = new ArrayList<IOp>(list.size());
		for (int i = 0, size = list.size(); i < size; ++i) {
			IOp e = list.get(i).optimize();
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

	@Override
	public SourceLocation getSourceLocation() {
		return sl;
	}
}
