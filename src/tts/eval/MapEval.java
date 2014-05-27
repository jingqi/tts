package tts.eval;

import java.util.*;
import java.util.Map.Entry;

import tts.eval.function.FunctionEval;
import tts.eval.scope.EvalSlot;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class MapEval extends ObjectEval {

	Map<IValueEval, IValueEval> entries = new HashMap<IValueEval, IValueEval>();

	@Override
	public EvalType getType() {
		return EvalType.MAP;
	}

	public IValueEval get(IValueEval e) {
		return entries.get(e);
	}

	public IValueEval put(IValueEval k, IValueEval v) {
		return entries.put(k, v);
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
	public MapEval clone() {
		MapEval ret = new MapEval();
		for (Entry<IValueEval, IValueEval> e : entries.entrySet()) {
			ret.entries.put(e.getKey(), e.getValue());
		}
		return ret;
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

	private class FuncSize extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new IntegerEval(entries.size());
		}
	}

	private class FuncGet extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);

			return entries.get(args.get(0));
		}
	}

	private class FuncPut extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 2)
				throw new ScriptRuntimeException("need 1 argument", sl);

			return entries.put(args.get(0), args.get(1));
		}
	}

	private class FuncKeys extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
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
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			ArrayEval ret = new ArrayEval();
			ret.addAll(entries.values());
			return ret;
		}
	}

	private class FuncClone extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return MapEval.this.clone();
		}
	}

	@Override
	public IValueEval member(String name) {
		if (name.equals("size"))
			return new FuncSize();
		if (name.equals("get"))
			return new FuncGet();
		if (name.equals("put"))
			return new FuncPut();
		if (name.equals("keys"))
			return new FuncKeys();
		if (name.equals("values"))
			return new FuncValues();
		if (name.equals("clone"))
			return new FuncClone();
		return null;
	}

	@Override
	public EvalSlot lvalueMember(String name) {
		return null;
	}
}
