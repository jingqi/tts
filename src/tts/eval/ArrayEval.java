package tts.eval;

import java.util.*;

import tts.eval.function.FunctionEval;
import tts.scope.Variable;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class ArrayEval extends ObjectEval {

	ArrayList<IValueEval> values = new ArrayList<IValueEval>();

	public int size() {
		return values.size();
	}

	public IValueEval get(int i) {
		return values.get(i);
	}

	public void add(IValueEval v) {
		values.add(v);
	}

	public void addAll(Collection<IValueEval> vs) {
		values.addAll(vs);
	}

	public void addAll(ArrayEval ae) {
		values.addAll(ae.values);
	}

	@Override
	public EvalType getType() {
		return EvalType.ARRAY;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ArrayEval))
			return false;
		ArrayEval ae = (ArrayEval) o;
		if (ae.size() != size())
			return false;
		for (int i = 0, size = size(); i < size; ++i)
			if (!get(i).equals(ae.get(i)))
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int h = 17;
		for (int i = 0, size = size(); i < size; ++i)
			h = h * 17 + get(i).hashCode();
		return h;
	}

	@Override
	public ArrayEval clone() {
		ArrayEval ret = new ArrayEval();
		for (int i = 0, size = values.size(); i < size; ++i)
			ret.values.add(values.get(i));
		return ret;
	}

	@Override
	public String toString() {
		return values.toString();
	}

	private class FuncSize extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("Need 0 argument", sl);

			return new IntegerEval(size());
		}
	}

	private class FuncGet extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("Need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException("Need integer", sl);

			long i = ((IntegerEval) args.get(0)).getValue();
			return get((int) i);
		}
	}

	private class FuncAppend extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			for (int i = 0, size = args.size(); i < size; ++i)
				values.add(args.get(i));
			return ArrayEval.this;
		}
	}

	private class FuncContains extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("Need 1 argument", sl);

			IValueEval ve = args.get(0);
			return BooleanEval.valueOf(values.contains(ve));
		}
	}

	private class FuncRemove extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("Need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException("Integer needed", sl);

			int i = (int) ((IntegerEval) args.get(0)).getValue();
			values.remove(i);
			return VoidEval.instance;
		}
	}

	private class FuncSet extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 2)
				throw new ScriptRuntimeException("Need 2 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException("Integer needed", sl);

			long i = ((IntegerEval) args.get(0)).getValue();
			return values.set((int) i, args.get(1));
		}
	}

	private class FuncClear extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("Need 0 argument", sl);
			values.clear();
			return VoidEval.instance;
		}
	}

	private class FuncClone extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("Need 0 argument", sl);

			return ArrayEval.this.clone();
		}
	}

	@Override
	public IValueEval member(String name) {
		if (name.equals("size"))
			return new FuncSize();
		else if (name.equals("get"))
			return new FuncGet();
		else if (name.equals("append"))
			return new FuncAppend();
		else if (name.equals("contains"))
			return new FuncContains();
		else if (name.equals("remove"))
			return new FuncRemove();
		else if (name.equals("set"))
			return new FuncSet();
		else if (name.equals("clear"))
			return new FuncClear();
		else if (name.equals("clone"))
			return new FuncClone();
		return null;
	}

	@Override
	public Variable lvalueMember(String name) {
		return null;
	}
}
