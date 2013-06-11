package tts.vm;

import java.io.*;
import java.util.List;

import tts.eval.*;
import tts.grammar.tree.Op;
import tts.util.SourceLocation;
import tts.vm.rtexcept.*;

class BuiltinApi {

	private BuiltinApi() {
	}

	// 包含其他源文件
	static class FuncEval extends FunctionEval {
		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("need string argument", sl);

			String path = ((StringEval) args.get(0)).getValue();
			File dst = new File(path);
			if (!dst.isAbsolute()) {
				String parent = vm.getCurrentScriptPath().getParentFile()
						.getAbsolutePath();
				dst = new File(parent + "/" + path);
			}

			Op op;
			try {
				op = vm.loadScript(dst, sl);
			} catch (IOException e) {
				throw new ScriptRuntimeException("can not load file:" + path,
						sl);
			}

			vm.enterScriptFile(dst);
			try {
				if (op != null)
					op.eval(vm);
			} catch (ScriptLogicException e) {
				vm.leaveScriptFile();
				throw e;
			}
			vm.leaveScriptFile();
			return VoidEval.instance;
		}
	}

	// 更改输出文件
	static class FuncOutput extends FunctionEval {
		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);
			if (args.get(0).getType() != IValueEval.EvalType.STRING)
				throw new ScriptRuntimeException("need string argument", sl);

			String output = ((StringEval) args.get(0)).getValue();
			File f = new File(output);
			if (!f.isAbsolute()) {
				String parent = vm.getCurrentScriptPath().getParentFile()
						.getAbsolutePath();
				f = new File(parent + "/" + output);
			}

			try {
				vm.setTextOutput(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			} catch (IOException e) {
				throw new ScriptRuntimeException("can not write to file: "
						+ output, sl);
			}
			return VoidEval.instance;
		}
	}

	// 退出程序
	static class FuncExit extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 0)
				throw new ScriptRuntimeException("need no argument", sl);
			throw new ExitException(sl);
		}
	}

	static class FuncPrint extends FunctionEval {
		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
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
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
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
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1 || args.get(0).getType() != EvalType.INTEGER)
				throw new ScriptRuntimeException("need 1 integer argument", sl);
			long v = ((IntegerEval) args.get(0)).getValue();
			if (v != (char) v)
				throw new ScriptRuntimeException("value out of range", sl);
			return new StringEval(new String(new char[] { (char) v }));
		}
	}

	static class FuncOrd extends FunctionEval {
		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1 || args.get(0).getType() != EvalType.STRING)
				throw new ScriptRuntimeException("need 1 string argument", sl);
			String v = ((StringEval) args.get(0)).getValue();
			if (v.length() != 1)
				throw new ScriptRuntimeException("string length must be 1", sl);
			return new IntegerEval(v.charAt(0));
		}
	}

	static class FuncTostring extends FunctionEval {
		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			if (args.size() != 1)
				throw new ScriptRuntimeException("need 1 argument", sl);

			IValueEval ve = args.get(0);
			if (ve instanceof StringEval)
				return ve;
			return new StringEval(ve.toString());
		}
	}
}
