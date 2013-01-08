package tts.vm;

import java.io.*;
import java.util.*;

import tts.grammar.scanner.GrammarException;
import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.IOp;
import tts.grammar.tree.OpList;
import tts.token.scanner.*;
import tts.token.stream.CharArrayScanReader;
import tts.token.stream.IScanReader;
import tts.util.*;
import tts.vm.BuiltinApi.FuncChr;
import tts.vm.BuiltinApi.FuncEval;
import tts.vm.BuiltinApi.FuncExit;
import tts.vm.BuiltinApi.FuncOrd;
import tts.vm.BuiltinApi.FuncOutput;
import tts.vm.BuiltinApi.FuncPrint;
import tts.vm.BuiltinApi.FuncPrintln;
import tts.vm.BuiltinApi.FuncTostring;
import tts.vm.rtexcpt.*;

/**
 * 脚本运行虚拟机
 */
public class ScriptVM {

	// 文本输出
	private Writer textOutput;

	// 调用栈变量(最前面是全局帧，最后面是当前执行帧)
	private final ArrayList<Frame> frames = new ArrayList<Frame>();

	// 调用栈位置
	private final ArrayList<RuntimeLocation> callingStack = new ArrayList<RuntimeLocation>();

	// 当前脚本路径
	private File currentScriptPath;
	private final Stack<File> scriptPathStack = new Stack<File>();

	// 已经加载的脚本文件
	private final Map<String, IOp> loadedFiles = new HashMap<String, IOp>();

	// 当前模块
	private String currentModule;

	public ScriptVM() {
		this(new PrintStreamWriter(System.out));
	}

	public ScriptVM(Writer textOutput) {
		this.textOutput = textOutput;
	}

	// 添加变量定义
	public void addVariable(String name, Variable var, SourceLocation sl) {
		List<Scope> ls = frames.get(frames.size() - 1).scopes;
		Scope s = ls.get(ls.size() - 1);
		if (s.variables.containsKey(name))
			throw new ScriptRuntimeException("variable " + name + " redefined",
					sl);

		s.variables.put(name, var);
	}

	// 获取变量的值
	public Variable getVariable(String name, SourceLocation sl) {
		// 局部变量
		Variable v = frames.get(frames.size() - 1).getVariable(name);
		if (v != null)
			return v;

		// 全局变量
		if (frames.size() > 1) {
			v = frames.get(0).getVariable(name);
			if (v != null)
				return v;
		}

		throw new ScriptRuntimeException("variable " + name + " not found", sl);
	}

	// 设置文本输出流
	public void setTextOutput(Writer w) {
		if (textOutput != null) {
			try {
				textOutput.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

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

	// 进入脚本文件
	public void enterScriptFile(File path) {
		scriptPathStack.push(currentScriptPath.getAbsoluteFile());
		currentScriptPath = path.getAbsoluteFile();
	}

	// 退出脚本文件
	public void leaveScriptFile() {
		currentScriptPath = scriptPathStack.pop();
	}

	// 进入函数
	public void enterFrame(SourceLocation sl, String nextModule) {
		Frame f = new Frame();
		frames.add(f);
		f.enterScope();
		pushFrameLocation(sl, nextModule);
	}

	// 退出函数
	public void leaveFrame() {
		Frame f = frames.remove(frames.size() - 1);
		f.leaveScope();
		popFrameLocation();
	}

	// 记录调用栈
	public void pushFrameLocation(SourceLocation sl, String nextModule) {
		callingStack.add(new RuntimeLocation(sl, currentModule));
		currentModule = nextModule;
	}

	// 弹出调用栈
	public void popFrameLocation() {
		RuntimeLocation rl = callingStack.remove(callingStack.size() - 1);
		currentModule = rl.module;
	}

	// 进入代码段
	public void enterScope() {
		frames.get(frames.size() - 1).enterScope();
	}

	// 退出代码段
	public void leaveScope() {
		frames.get(frames.size() - 1).leaveScope();
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
		Reader fr = new InputStreamReader(new FileInputStream(dst), "UTF-8");
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

	// 初始化执行环境
	private void initExecEnv() {
		frames.clear();
		enterFrame(SourceLocation.NATIVE, SourceLocation.NATIVE_MODULE);
		currentScriptPath = null;
		scriptPathStack.clear();
		loadedFiles.clear();
		currentModule = "<module>";

		// 默认全局变量
		addVariable("eval", new Variable("eval", VarType.FUNCTION,
				new FuncEval()), SourceLocation.NATIVE);
		addVariable("output", new Variable("output", VarType.FUNCTION,
				new FuncOutput()), SourceLocation.NATIVE);
		addVariable("exit", new Variable("exit", VarType.FUNCTION,
				new FuncExit()), SourceLocation.NATIVE);
		addVariable("print", new Variable("print", VarType.FUNCTION,
				new FuncPrint()), SourceLocation.NATIVE);
		addVariable("println", new Variable("println", VarType.FUNCTION,
				new FuncPrintln()), SourceLocation.NATIVE);
		addVariable("chr",
				new Variable("chr", VarType.FUNCTION, new FuncChr()),
				SourceLocation.NATIVE);
		addVariable("ord",
				new Variable("ord", VarType.FUNCTION, new FuncOrd()),
				SourceLocation.NATIVE);
		addVariable("toString", new Variable("toString", VarType.FUNCTION,
				new FuncTostring()), SourceLocation.NATIVE);
	}

	// 调用栈
	private String callingStack() {
		StringBuilder sb = new StringBuilder();
		for (int i = callingStack.size() - 1; i >= 1; --i) {
			RuntimeLocation rl = callingStack.get(i);
			sb.append("\t").append("at ").append(rl.module).append("(")
					.append(rl.sl.file).append(":").append(rl.sl.line)
					.append(")\n");
		}
		return sb.toString();
	}

	// 脚本入口
	public void run(File script) {
		initExecEnv();
		currentScriptPath = script.getAbsoluteFile();

		try {
			IOp op = loadScript(script, SourceLocation.NATIVE);
			op.eval(this);
			textOutput.flush();
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
			enterFrame(e.sl, SourceLocation.NATIVE_MODULE);
			System.err.print("Script runtime exception: ");
			System.err.println(e.getMessage());
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
			textOutput.flush();
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
			enterFrame(e.sl, SourceLocation.NATIVE_MODULE);
			System.err.print("Script runtime exception: ");
			System.err.println(e.getMessage());
			System.err.println(callingStack());
		}
	}
}
