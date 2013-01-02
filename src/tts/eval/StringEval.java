package tts.eval;

import java.util.List;

import tts.vm.*;

public final class StringEval extends ObjectEval {

	private String value;

	public StringEval(String s) {
		value = s;
	}

	public String getValue() {
		return value;
	}

	public IValueEval charAt(int i) {
		return new StringEval(value.substring(i, i + 1));
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
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

	private class FuncLen extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new IntegerEval(value.length());
		}
	}

	private class FuncCharAt extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
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
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);

			String s = ((StringEval) args.get(0)).getValue();
			return BooleanEval.valueOf(value.startsWith(s));
		}
	}

	private class FuncEndsWith extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);

			String s = ((StringEval) args.get(0)).getValue();
			return BooleanEval.valueOf(value.endsWith(s));
		}
	}

	private class FuncSubstr extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 2)
				throw new ScriptRuntimeException("need 2 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.INTEGER
					|| args.get(1).getType() != IValueEval.EvalType.INTEGER)
				throw new ScriptRuntimeException("integer needed", sl);

			int begin = (int) ((IntegerEval) args.get(0)).getValue();
			int end = (int) ((IntegerEval) args.get(1)).getValue();
			return new StringEval(value.substring(begin, end));
		}
	}

	private class FuncToLowerCase extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new StringEval(value.toLowerCase());
		}
	}

	private class FuncToUpperCase extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need 0 argument", sl);

			return new StringEval(value.toUpperCase());
		}
	}

	private class FuncIndexOf extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
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

			return new IntegerEval(value.indexOf(s, start));
		}
	}

	private class FuncLastIndexOf extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1 && args.size() != 2)
				throw new ScriptRuntimeException("need 1 or 2 argument", sl);

			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("string needed", sl);
			String s = ((StringEval) args.get(0)).getValue();

			int start = value.length();
			if (args.size() == 2) {
				if (args.get(1).getType() != IValueEval.EvalType.INTEGER)
					throw new ScriptRuntimeException("integer needed", sl);
				start = (int) ((IntegerEval) args.get(1)).getValue();
			}

			return new IntegerEval(value.lastIndexOf(s, start));
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
		throw new ScriptRuntimeException("string no such member: " + name, sl);
	}
}
