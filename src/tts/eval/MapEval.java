package tts.eval;

import java.util.*;
import java.util.Map.Entry;

import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcpt.ScriptRuntimeException;

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
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new IntegerEval(entries.size());
		}
	}

	private class FuncGet extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);

			return entries.get(args.get(0));
		}
	}

	private class FuncKeys extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			ArrayEval ret = new ArrayEval();
			for (IValueEval v : entries.keySet())
				ret.add(v);
			return ret;
		}
	}

	private class FuncValues extends FunctionEval {
		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			ArrayEval ret = new ArrayEval();
			ret.addAll(entries.values());
			return ret;
		}
	}

	@Override
	public IValueEval member(String name, SourceLocation sl) {
		if (name.equals("size"))
			return new FuncSize();
		if (name.equals("get"))
			return new FuncGet();
		if (name.equals("keys"))
			return new FuncKeys();
		if (name.equals("values"))
			return new FuncValues();
		throw new ScriptRuntimeException("map no such member: " + name, sl);
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
	public int hashCode() {
		int h = 17;
		for (Entry<IValueEval, IValueEval> e : entries.entrySet()) {
			h = h * 31 + e.getKey().hashCode() * 17 + e.getValue().hashCode();
		}
		return h;
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
