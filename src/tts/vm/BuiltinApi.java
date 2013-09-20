package tts.vm;

import java.io.*;
import java.util.List;

import tts.eval.*;
import tts.util.SourceLocation;
import tts.vm.rtexcept.ExitException;
import tts.vm.rtexcept.ScriptRuntimeException;

class BuiltinApi {

	private BuiltinApi() {
	}

	// 更改输出文件
	static class FuncOutput extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("Need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("Need string argument", sl);

			String output = ((StringEval) args.get(0)).getValue();
			File of = new File(output);
			if (!of.isAbsolute()) {
				String parent = new File(sl.file).getParentFile().getAbsolutePath();
				of = new File(parent + "/" + output);
			}

			try {
				f.getVM().setTextOutput(new OutputStreamWriter(new FileOutputStream(of), "UTF-8"));
			} catch (IOException e) {
				throw new ScriptRuntimeException("Can not write to file: "
						+ output, sl);
			}
			return VoidEval.instance;
		}
	}

	// 退出程序
	static class FuncExit extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("Need no argument", sl);
			throw new ExitException(sl);
		}
	}

	static class FuncPrint extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			for (int i = 0, size = args.size(); i < size; ++i) {
				IValueEval ve = args.get(i);
				if (ve instanceof StringEval) {
					System.out.print(((StringEval) ve).getValue());
				} else {
					System.out.print(ve.toString());
				}
			}
			return VoidEval.instance;
		}
	}

	static class FuncPrintln extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			for (int i = 0, size = args.size(); i < size; ++i) {
				IValueEval ve = args.get(i);
				if (ve instanceof StringEval) {
					System.out.print(((StringEval) ve).getValue());
				} else {
					System.out.print(ve.toString());
				}
			}
			System.out.println();
			return VoidEval.instance;
		}
	}

	static class FuncChr extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1 || args.get(0).getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException("Need 1 integer argument", sl);
			long v = ((IntegerEval) args.get(0)).getValue();
			if (v != (char) v)
				throw new ScriptRuntimeException("Value out of range", sl);
			return new StringEval(new String(new char[] { (char) v }));
		}
	}

	static class FuncOrd extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1 || args.get(0).getType() != EvalType.STRING)
				throw new ScriptRuntimeException("Need 1 string argument", sl);
			String v = ((StringEval) args.get(0)).getValue();
			if (v.length() != 1)
				throw new ScriptRuntimeException("String length must be 1", sl);
			return new IntegerEval(v.charAt(0));
		}
	}

	static class FuncTostring extends FunctionEval {
		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("Need 1 argument", sl);

			IValueEval ve = args.get(0);
			if (ve instanceof StringEval)
				return ve;
			return new StringEval(ve.toString());
		}
	}
}
