package tts.eval;

import java.util.*;

import tts.vm.ScriptRuntimeException;

public class ArrayEval extends ObjectEval {

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

	private class FuncGet extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument");
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException();

			long i = ((IntegerEval) args.get(0)).getValue();
			return ArrayEval.this.get((int) i);
		}
	}

	private class FuncAppend extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args) {
			for (int i = 0, size = args.size(); i < size; ++i)
				ArrayEval.this.add(args.get(i));
			return VoidEval.instance;
		}
	}

	@Override
	public IValueEval member(String name) {
		if (name.equals("size"))
			return new IntegerEval(size());
		else if (name.equals("get"))
			return new FuncGet();
		else if (name.equals("append"))
			return new FuncAppend();
		throw new ScriptRuntimeException("array no such member: " + name);
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
