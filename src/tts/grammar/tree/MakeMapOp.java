package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.IValueEval;
import tts.eval.MapEval;
import tts.trace.SourceLocation;
import tts.vm.Frame;

public class MakeMapOp extends Op {

	public static class Entry {
		Op key, value;

		public Entry(Op k, Op v) {
			key = k;
			value = v;
		}
	}

	ArrayList<Entry> entries;

	public MakeMapOp(ArrayList<Entry> e, String file, int line) {
		super(new SourceLocation(file, line));
		entries = e;
	}

	@Override
	public IValueEval eval(Frame f) {
		MapEval me = new MapEval();
		for (int i = 0, size = entries.size(); i < size; ++i) {
			IValueEval k = entries.get(i).key.eval(f);
			IValueEval v = entries.get(i).value.eval(f);
			me.put(k, v);
		}
		return me;
	}

	@Override
	public Op optimize() {
		for (int i = 0, size = entries.size(); i < size; ++i) {
			Entry e = entries.get(i);
			e.key = e.key.optimize();
			e.value = e.value.optimize();
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0, size = entries.size(); i < size; ++i) {
			Entry e = entries.get(i);
			sb.append(e.key.toString());
			sb.append(":");
			sb.append(e.value.toString());
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}
}
