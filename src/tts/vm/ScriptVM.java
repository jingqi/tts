package tts.vm;

import java.io.*;
import java.util.*;

import tts.eval.FunctionEval;
import tts.eval.IValueEval;
import tts.grammar.scanner.GrammarException;
import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.IOp;
import tts.grammar.tree.OpList;
import tts.token.scanner.*;
import tts.token.stream.CharArrayScanReader;
import tts.token.stream.IScanReader;
import tts.util.PrintStreamWriter;

/**
 * 脚本运行虚拟机
 */
public class ScriptVM {

	// 文本输出
	private Writer textOutput;

	// 全局变量
	private final Map<String, Variable> globalVars = new HashMap<String, Variable>();

	// 帧
	private static class Frame {
		Map<String, Variable> localValues = new HashMap<String, Variable>();
	}

	// 帧栈
	private final List<Frame> frames = new ArrayList<Frame>();

	// 当前脚本路径
	private File currentScriptPath;
	private final Stack<File> scriptPathStack = new Stack<File>();

	// 已经加载的脚本文件
	private final Map<String, IOp> loadedFiles = new HashMap<String, IOp>();

	// 调用栈
	private static class CallFrame {
		SourceLocation sl;
		String module;

		public CallFrame(SourceLocation sl, String module) {
			this.sl = sl;
			this.module = module;
		}
	}

	private final Stack<CallFrame> callFrames = new Stack<CallFrame>();

	// 当前模块
	private String currentModule;

	public ScriptVM() {
		this(new PrintStreamWriter(System.out));
	}

	public ScriptVM(Writer textOutput) {
		this.textOutput = textOutput;
	}

	// 进入帧
	public void enterFrame() {
		frames.add(new Frame());
	}

	// 退出帧
	public void leaveFrame() {
		frames.remove(frames.size() - 1);
	}

	// 添加变量定义
	public void addVariable(String name, Variable var, SourceLocation sl) {
		Map<String, Variable> pool;
		if (frames.size() == 0)
			pool = globalVars;
		else
			pool = frames.get(frames.size() - 1).localValues;

		if (pool.containsKey(name))
			throw new ScriptRuntimeException("variable " + name + " redefined",
					sl);

		pool.put(name, var);
	}

	// 获取变量的值
	public Variable getVariable(String name, SourceLocation sl) {
		for (int i = frames.size() - 1; i >= 0; --i) {
			Map<String, Variable> pool = frames.get(i).localValues;
			if (pool.containsKey(name))
				return pool.get(name);
		}

		if (globalVars.containsKey(name))
			return globalVars.get(name);

		throw new ScriptRuntimeException("variable " + name + " not found", sl);
	}

	// 设置文本输出流
	public void setTextOutput(Writer w) {
		textOutput = w;
	}

	// 输出文本
	public void writeText(String s) {
		try {
			textOutput.write(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// 当前脚本路径压栈，换成相对的另一个路径
	public void pushScriptPath(File path) {
		scriptPathStack.push(currentScriptPath);
		currentScriptPath = path;
	}

	// 脚本路径从栈中弹出
	public void popScriptPath() {
		currentScriptPath = scriptPathStack.pop();
	}

	// 调用栈压栈
	public void pushCallFrame(SourceLocation sl, String nextModule) {
		callFrames.push(new CallFrame(sl, currentModule));
		currentModule = nextModule;
	}

	// 调用栈弹出
	public void popCallFrame() {
		CallFrame cf = callFrames.pop();
		currentModule = cf.module;
	}

	// 当前脚本路径
	public File getCurrentScriptPath() {
		return currentScriptPath;
	}

	// 加载脚本
	public IOp loadScript(File dst, SourceLocation sl) throws IOException {
		// 从缓存加载
		IOp ret = loadedFiles.get(dst.getAbsolutePath());
		if (ret != null)
			return ret;

		// 读取文件内容
		if (!dst.exists())
			throw new ScriptRuntimeException("file not exists: "
					+ dst.getName(), sl);
		FileReader fr = new FileReader(dst);
		CharArrayScanReader ss = new CharArrayScanReader();
		char[] buf = new char[4096];
		int readed = 0;
		while ((readed = fr.read(buf)) > 0) {
			ss.write(buf, 0, readed);
		}
		fr.close();

		// 解析文件
		ss.seek(0);
		TokenScanner tsn = new TokenScanner(ss, dst.getName());
		TokenStream ts = new TokenStream(tsn);
		GrammarScanner gs = new GrammarScanner(ts);

		// 词法分析，语法分析，构建语法树
		ret = gs.all();

		// 优化语法树
		ret = ret.optimize();
		if (ret == null)
			ret = OpList.VOID; // 空操作
		loadedFiles.put(dst.getAbsolutePath(), ret);
		return ret;
	}

	// 退出程序
	private static class FuncExit extends FunctionEval {

		@Override
		public IValueEval call(List<IValueEval> args, ScriptVM vm,
				SourceLocation sl) {
			throw new ExitException();
		}
	}

	// 初始化执行环境
	private void initExecEnv() {
		globalVars.clear();
		frames.clear();
		currentScriptPath = null;
		scriptPathStack.clear();
		loadedFiles.clear();
		callFrames.clear();
		currentModule = "<module>";

		// 默认全局变量
		addVariable("exit", new Variable("exit", VarType.FUNCTION,
				new FuncExit()), SourceLocation.NATIVE);
	}

	// 调用栈
	private String callingStack() {
		StringBuilder sb = new StringBuilder();
		for (int i = callFrames.size() - 1; i >= 0; --i) {
			CallFrame cf = callFrames.get(i);
			sb.append("\t").append("at ").append(cf.module).append("(")
					.append(cf.sl.file).append(":").append(cf.sl.line)
					.append(")\n");
		}
		return sb.toString();
	}

	// 脚本入口
	public void run(File script) {
		initExecEnv();
		currentScriptPath = script;

		try {
			IOp op = loadScript(script, SourceLocation.NATIVE);
			op.eval(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (BreakLoopException e) {
			System.err.println("Break without loop:");
			System.err.println(e.toString());
		} catch (ContinueLoopException e) {
			System.err.println("Continue without loop:");
			System.err.println(e.toString());
		} catch (ReturnFuncException e) {
			System.err.println("Return statement without function:");
			System.err.println(e.toString());
		} catch (ExitException e) {
			return;
		} catch (ScannerException e) {
			System.err.println("Scanner exception:");
			System.err.println(e.toString());
		} catch (GrammarException e) {
			System.err.println("Grammar exception:");
			System.err.println(e.toString());
		} catch (ScriptRuntimeException e) {
			System.err.println("Script runtime exception:");
			System.err.println(e.toString(currentModule));
			System.err.println(callingStack());
		}
	}

	// 内存代码入口
	public void run(IScanReader r) {
		initExecEnv();

		try {
			TokenScanner tsn = new TokenScanner(r, "<memory>");
			TokenStream ts = new TokenStream(tsn);
			GrammarScanner gs = new GrammarScanner(ts);
			IOp op = gs.all();
			op = op.optimize();
			if (op != null)
				op.eval(this);
		} catch (BreakLoopException e) {
			System.err.println("Break without loop:");
			System.err.println(e.toString());
		} catch (ContinueLoopException e) {
			System.err.println("Continue without loop:");
			System.err.println(e.toString());
		} catch (ReturnFuncException e) {
			System.err.println("Return statement without function:");
			System.err.println(e.toString());
		} catch (ExitException e) {
			return;
		} catch (ScannerException e) {
			System.err.println("Scanner exception:");
			System.err.println(e.toString());
		} catch (GrammarException e) {
			System.err.println("Grammar exception:");
			System.err.println(e.toString());
		} catch (ScriptRuntimeException e) {
			System.err.println("Script runtime exception:");
			System.err.println(e.toString(currentModule));
			System.err.println(callingStack());
		}
	}
}
