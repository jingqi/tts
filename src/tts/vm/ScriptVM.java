package tts.vm;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import tts.eval.IValueEval;
import tts.eval.ModuleScopeEval;
import tts.eval.scope.*;
import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.Op;
import tts.grammar.tree.OpList;
import tts.lexer.scanner.LexerScanner;
import tts.lexer.scanner.TokenStream;
import tts.lexer.stream.CharArrayScanReader;
import tts.util.PrintStreamWriter;
import tts.util.SourceLocation;
import tts.vm.BuiltinApi.FuncChr;
import tts.vm.BuiltinApi.FuncExit;
import tts.vm.BuiltinApi.FuncOrd;
import tts.vm.BuiltinApi.FuncOutput;
import tts.vm.BuiltinApi.FuncPrint;
import tts.vm.BuiltinApi.FuncPrintln;
import tts.vm.BuiltinApi.FuncTostring;
import tts.vm.rtexcept.*;

/**
 * 脚本运行虚拟机
 */
public class ScriptVM {

	// 文本输出
	private Writer textOutput;

	// 全局作用域
	private Scope globalScope;

	// 主线程栈帧
	private Frame mainFrame;

	// 其他模块
	private final Map<String, IValueEval> modules = new HashMap<String, IValueEval>();

	public ScriptVM() {
		this(new PrintStreamWriter(System.out));
	}

	public ScriptVM(Writer textOutput) {
		this.textOutput = textOutput;
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

	public void flush() {
		try {
			textOutput.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Frame getMainFrame() {
		return mainFrame;
	}

	/**
	 * 初始化执行环境
	 */
	public void initExecEnv() {
		if (mainFrame != null)
			return;

		globalScope = new Scope(null);
		globalScope.addVariable("output", new EvalSlot(VarType.FUNCTION, new FuncOutput()), SourceLocation.NATIVE);
		globalScope.addVariable("exit", new EvalSlot(VarType.FUNCTION, new FuncExit()), SourceLocation.NATIVE);
		globalScope.addVariable("print", new EvalSlot(VarType.FUNCTION, new FuncPrint()), SourceLocation.NATIVE);
		globalScope.addVariable("println", new EvalSlot(VarType.FUNCTION, new FuncPrintln()), SourceLocation.NATIVE);
		globalScope.addVariable("chr", new EvalSlot(VarType.FUNCTION, new FuncChr()), SourceLocation.NATIVE);
		globalScope.addVariable("ord", new EvalSlot(VarType.FUNCTION, new FuncOrd()), SourceLocation.NATIVE);
		globalScope.addVariable("toString", new EvalSlot(VarType.FUNCTION, new FuncTostring()), SourceLocation.NATIVE);

		mainFrame = new Frame(this);
		mainFrame.pushScope(globalScope);
	}

	public IValueEval importModule(Frame f, String module, SourceLocation sl) {

		initExecEnv();

		IValueEval ret = null;
		File mfile = new File(module);
		if (mfile.isAbsolute()) {
			// 绝对地址，直接加载
			ret = tryImportModule(f, mfile.getParentFile(), mfile.getName(), sl);
		} else {
			// 相对地址，多次尝试

			// 首先尝试脚本所在的目录
			ret = tryImportModule(f, new File(sl.file).getParentFile(), module, sl);

			// 再尝试当前执行路径
			if (ret == null) {
				String cwd = System.getProperty("user.dir");
				ret = tryImportModule(f, new File(cwd), module, sl);
			}
		}

		return ret;
	}

	/**
	 * 尝试在某一个路径下加载模块
	 */
	private IValueEval tryImportModule(Frame f, File parent, String relativePath, SourceLocation sl) {
		File p = new File(parent, relativePath);
		if (!p.exists() && !relativePath.endsWith(".tts"))
			p = new File(parent, relativePath + ".tts");
		if (!p.exists())
			return null;

		String abspath = p.getAbsolutePath();
		p = new File(abspath);
		IValueEval ret = modules.get(abspath);
		if (ret != null)
			return ret;

		Scope s = new Scope(globalScope);
		f.pushScope(s);
		try {
			// 编译代码
			Op op = loadScript(p, sl);

			if (op != null)
				op.eval(f);
		} catch (BreakLoopException e) {
			f.popScope();
			throw new ScriptRuntimeException("Break without loop", e.sl);
		} catch (ContinueLoopException e) {
			f.popScope();
			throw new ScriptRuntimeException("Continue with out loop", e.sl);
		} catch (ReturnFuncException e) {
			f.popScope();
			throw new ScriptRuntimeException("Return without function", e.sl);
		} catch (ScriptRuntimeException e) {
			f.popScope();
			throw e;
		} catch (IOException e) {
			f.popScope();
			throw new RuntimeException(e);
		}
		f.popScope();

		ret = new ModuleScopeEval(s);
		modules.put(abspath, ret);
		return ret;
	}

	/**
	 * 加载脚本
	 */
	public Op loadScript(File dst, SourceLocation sl) throws IOException {
		// 读取文件内容
		if (!dst.exists())
			throw new ScriptRuntimeException("File not exists: " + dst.getName(), sl);

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
		LexerScanner tsn = new LexerScanner(ss, dst.getPath());
		TokenStream ts = new TokenStream(tsn);
		GrammarScanner gs = new GrammarScanner(ts);

		// 词法分析，语法分析，构建语法树
		Op ret = gs.all();

		// 优化语法树
		ret = ret.optimize();
		if (ret == null)
			ret = OpList.VOID; // 空操作
		return ret;
	}
}
