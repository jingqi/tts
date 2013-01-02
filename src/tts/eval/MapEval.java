package tts.eval;

import java.util.*;
import java.util.Map.Entry;

import tts.vm.ScriptRuntimeException;
import tts.vm.ScriptVM;

public final class MapEval extends ObjectEval {

	Map<IValueEval, IValueEval> entries = new HashMap<IValueEval, IValueEval>();

	@Override
	public EvalType getType() {
		return EvalType.MAP;
	}

	IValueEval get(IValueEval e) {
		return entries.get(e);
	}

	public IValueEval put(IValueEval k, IValueEval v) {
		return entries.put(k, v);
	}

	private class FuncSize extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument",
						NATIVE_FILE, NATIVE_LINE);

			return new IntegerEval(entries.size());
		}
	}

	private class FuncGet extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument",
						NATIVE_FILE, NATIVE_LINE);

			return entries.get(args.get(0));
		}
	}

	@Override
	public IValueEval member(String name) {
		if (name.equals("size"))
			return new FuncSize();
		if (name.equals("get"))
			return new FuncGet();
		throw new ScriptRuntimeException("map no such member: " + name,
				FunctionEval.NATIVE_FILE, FunctionEval.NATIVE_LINE);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MapEval))
			return false;

		MapEval me = (MapEval) o;
		if (me.entries.size() != entries.size())
			return false;
		for (Entry<IValueEval, IValueEval> e : entries.entrySet()) {
			IValueEval v = me.entries.get(e.getKey());
			if (v == null)
				return false;
			if (!v.equals(e.getValue()))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<IValueEval, IValueEval> e : entries.entrySet()) {
			sb.append(e.getKey().toString());
			sb.append(":");
			sb.append(e.getValue().toString());
			sb.append(", ");
		}
		sb.append("}");
		return sb.toString();
	}
}
