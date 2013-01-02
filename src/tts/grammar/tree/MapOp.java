package tts.grammar.tree;

import java.util.ArrayList;

import tts.eval.IValueEval;
import tts.eval.MapEval;
import tts.vm.ScriptVM;

public class MapOp implements IOp {

	public static class Entry {
		IOp key, value;

		public Entry(IOp k, IOp v) {
			key = k;
			value = v;
		}
	}

	String file;
	int line;
	ArrayList<Entry> entries;

	public MapOp(ArrayList<Entry> e, String file, int line) {
		entries = e;
		this.file = file;
		this.line = line;
	}

	@Override
	public IValueEval eval(ScriptVM vm) {
		MapEval me = new MapEval();
		for (int i = 0, size = entries.size(); i < size; ++i) {
			IValueEval k = entries.get(i).key.eval(vm);
			IValueEval v = entries.get(i).value.eval(vm);
			me.put(k, v);
		}
		return me;
	}

	@Override
	public IOp optimize() {
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

	@Override
	public String getFile() {
		return file;
	}

	@Override
	public int getLine() {
		return line;
	}
}
