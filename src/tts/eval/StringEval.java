package tts.eval;

import java.util.List;

import tts.util.CharList;
import tts.util.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class StringEval extends ObjectEval {

	private CharList value = new CharList();

	public StringEval() {
	}

	public StringEval(String s) {
		value.addAll(s.toCharArray());
	}

	public String getValue() {
		StringBuilder sb = new StringBuilder(value.size());
		for (int i = 0, size = value.size(); i < size; ++i)
			sb.append(value.get(i));
		return sb.toString();
	}

	public IValueEval charAt(int i) {
		StringEval ret = new StringEval();
		ret.value.add(value.get(i));
		return ret;
	}

	@Override
	public StringEval clone() {
		StringEval ret = new StringEval();
		for (int i = 0, size = value.size(); i < size; ++i)
			ret.value.add(value.get(i));
		return ret;
	}

	@Override
	public String toString() {
		return "\"" + getValue() + "\"";
	}

	@Override
	public EvalType getType() {
		return EvalType.STRING;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof StringEval))
			return false;
		return ((StringEval) o).value.equals(value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	private class FuncLen extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new IntegerEval(value.size());
		}
	}

	private class FuncCharAt extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException("integer needed", sl);

			long i = ((IntegerEval) args.get(0)).getValue();
			return charAt((int) i);
		}
	}

	private class FuncStartsWith extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);

			String s = ((StringEval) args.get(0)).getValue();
			return BooleanEval.valueOf(getValue().startsWith(s));
		}
	}

	private class FuncEndsWith extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);

			String s = ((StringEval) args.get(0)).getValue();
			return BooleanEval.valueOf(getValue().endsWith(s));
		}
	}

	private class FuncSubstr extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 2)
				throw new ScriptRuntimeException("need 2 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER
					|| args.get(1).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException("integer needed", sl);

			int begin = (int) ((IntegerEval) args.get(0)).getValue();
			int end = (int) ((IntegerEval) args.get(1)).getValue();
			return new StringEval(getValue().substring(begin, end));
		}
	}

	private class FuncToLowerCase extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new StringEval(getValue().toLowerCase());
		}
	}

	private class FuncToUpperCase extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new StringEval(getValue().toUpperCase());
		}
	}

	private class FuncIndexOf extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1 && args.size() != 2)
				throw new ScriptRuntimeException("need 1 or 2 argument", sl);

			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);
			String s = ((StringEval) args.get(0)).getValue();

			int start = 0;
			if (args.size() == 2) {
				if (args.get(1).getType() != IValueEval.EvalType.INTEGER)
					throw new ScriptRuntimeException("integer needed", sl);
				start = (int) ((IntegerEval) args.get(1)).getValue();
			}

			return new IntegerEval(getValue().indexOf(s, start));
		}
	}

	private class FuncLastIndexOf extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1 && args.size() != 2)
				throw new ScriptRuntimeException("need 1 or 2 argument", sl);

			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);
			String s = ((StringEval) args.get(0)).getValue();

			int start = value.size();
			if (args.size() == 2) {
				if (args.get(1).getType() != IValueEval.EvalType.INTEGER)
					throw new ScriptRuntimeException("integer needed", sl);
				start = (int) ((IntegerEval) args.get(1)).getValue();
			}

			return new IntegerEval(getValue().lastIndexOf(s, start));
		}
	}

	private class FuncReplace extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 2)
				throw new ScriptRuntimeException("need 2 argument", sl);

			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);
			String from = ((StringEval) args.get(0)).getValue();

			if (args.get(1).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("integer needed", sl);
			String to = ((StringEval) args.get(1)).getValue();

			return new StringEval(getValue().replace(from, to));
		}
	}

	private class FuncAppend extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1 || args.get(0).getType() != EvalType.STRING)
				throw new ScriptRuntimeException("need 1 string argument", sl);

			StringEval se = (StringEval) args.get(0);
			for (int i = 0, size = se.value.size(); i < size; ++i)
				value.add(se.value.get(i));

			return StringEval.this;
		}
	}

	private class FuncClone extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return StringEval.this.clone();
		}
	}

	@Override
	public IValueEval member(String name, SourceLocation sl) {
		if (name.equals("length"))
			return new FuncLen();
		else if (name.equals("charAt"))
			return new FuncCharAt();
		else if (name.equals("startsWith"))
			return new FuncStartsWith();
		else if (name.equals("endsWith"))
			return new FuncEndsWith();
		else if (name.equals("substr"))
			return new FuncSubstr();
		else if (name.equals("toLowerCase"))
			return new FuncToLowerCase();
		else if (name.equals("toUpperCase"))
			return new FuncToUpperCase();
		else if (name.equals("indexOf"))
			return new FuncIndexOf();
		else if (name.equals("lastIndexOf"))
			return new FuncLastIndexOf();
		else if (name.equals("replace"))
			return new FuncReplace();
		else if (name.equals("append"))
			return new FuncAppend();
		else if (name.equals("clone"))
			return new FuncClone();
		throw new ScriptRuntimeException("string no such member: " + name, sl);
	}
}
