package tts.vm;

import java.io.*;
import java.util.List;

import tts.eval.*;
import tts.grammar.tree.IOp;
import tts.util.SourceLocation;
import tts.vm.rtexcpt.ExitException;
import tts.vm.rtexcpt.ScriptRuntimeException;

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

			IOp op;
			try {
				op = vm.loadScript(dst, sl);
			} catch (IOException e) {
				throw new ScriptRuntimeException("can not load file:" + path,
						sl);
			}

			vm.pushScriptPath(dst);
			try {
				if (op != null)
					op.eval(vm);
			} finally {
				vm.popScriptPath();
			}
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
				vm.setTextOutput(new FileWriter(f));
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
			throw new ExitException();
		}
	}
}
