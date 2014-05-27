package tts.vm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import tts.eval.BooleanEval;
import tts.eval.IValueEval;
import tts.eval.IntegerEval;
import tts.eval.StringEval;
import tts.eval.VoidEval;
import tts.eval.function.FunctionEval;
import tts.trace.SourceLocation;
import tts.vm.rtexcept.ExitException;
import tts.vm.rtexcept.ScriptRuntimeException;

final class BuiltinFuncs {

	private BuiltinFuncs() {
	}

	// 更改输出文件
	static final class Redirect extends FunctionEval {
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
	static final class Exit extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("Need no argument", sl);
			throw new ExitException(sl);
		}
	}

	// 打印
	static final class Print extends FunctionEval {
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

	// 打印
	static final class Println extends FunctionEval {
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

	// 编码转字符
	static final class Chr extends FunctionEval {
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

	// 字符转编码
	static final class Ord extends FunctionEval {
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

	// 字符串化
	static final class ToString extends FunctionEval {
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

	// 断言
	static final class Assert extends FunctionEval {

		@Override
		public IValueEval call(Frame f, List<IValueEval> args, SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("Need 1 boolean argument", sl);

			IValueEval ve = args.get(0);
			if (!(ve instanceof BooleanEval))
				throw new ScriptRuntimeException("Need 1 boolean argument", sl);
			BooleanEval be = (BooleanEval) ve;
			if (!be.getValue())
				throw new ScriptRuntimeException("Asstion failed", sl);
			return VoidEval.instance;
		}
	}
}
