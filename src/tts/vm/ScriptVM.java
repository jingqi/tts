package tts.vm;

import java.io.*;
import java.util.*;

import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.IOp;
import tts.grammar.tree.OpList;
import tts.token.scanner.TokenScanner;
import tts.token.scanner.TokenStream;
import tts.token.stream.CharArrayScanReader;
import tts.token.stream.IScanReader;
import tts.util.PrintStreamWriter;

/**
 * 脚本运行虚拟机
 */
public class ScriptVM {

	// 文本输出
	Writer textOutput;

	// 全局变量
	Map<String, Variable> globalVars = new HashMap<String, Variable>();

	// 帧
	static class Frame {
		Map<String, Variable> localValues = new HashMap<String, Variable>();
	}

	// 帧栈
	List<Frame> frames = new ArrayList<Frame>();

	// 当前路径
	File currentPath;

	// 当前脚本路径
	File currentScriptPath;
	Stack<File> scriptPathStack = new Stack<File>();

	// 已经加载的脚本文件
	Map<String, IOp> loadedFiles = new HashMap<String, IOp>();

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
	public void addVariable(String name, Variable var) {
		Map<String, Variable> pool;
		if (frames.size() == 0)
			pool = globalVars;
		else
			pool = frames.get(frames.size() - 1).localValues;

		if (pool.containsKey(name))
			throw new RuntimeException("variable + " + name + " redefined");

		pool.put(name, var);
	}

	// 获取变量的值
	public Variable getVariable(String name) {
		for (int i = frames.size() - 1; i >= 0; --i) {
			Map<String, Variable> pool = frames.get(i).localValues;
			if (pool.containsKey(name))
				return pool.get(name);
		}

		if (globalVars.containsKey(name))
			return globalVars.get(name);

		throw new RuntimeException("variable " + name + " not found");
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

	// 当前脚本路径
	public File getCurrentScriptPath() {
		return currentScriptPath;
	}

	// 加载脚本
	public IOp loadScript(File dst) throws IOException {
		// 从缓存加载
		IOp ret = loadedFiles.get(dst.getAbsolutePath());
		if (ret != null)
			return ret;

		// 读取文件内容
		if (!dst.exists())
			throw new ScriptRuntimeException("file not exists: "
					+ dst.getName());
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
		TokenScanner tsn = new TokenScanner(ss);
		TokenStream ts = new TokenStream(tsn);
		GrammarScanner gs = new GrammarScanner(ts);

		// 词法分析，语法分析，构建语法树
		ret = gs.all();

		// 优化语法树
		ret = ret.optimize();
		if (ret == null)
			ret = new OpList(); // 空操作
		loadedFiles.put(dst.getAbsolutePath(), ret);
		return ret;
	}

	// 脚本入口
	public void run(File script) {
		currentScriptPath = script;
		scriptPathStack.clear();
		IOp op;
		try {
			op = loadScript(script);
		} catch (IOException e) {
			throw new ScriptRuntimeException();
		}
		op.eval(this);
	}

	// 内存代码入口
	public void run(IScanReader r) {
		currentPath = null;
		scriptPathStack.clear();
		TokenScanner tsn = new TokenScanner(r);
		TokenStream ts = new TokenStream(tsn);
		GrammarScanner gs = new GrammarScanner(ts);
		IOp op = gs.all();
		op = op.optimize();
		if (op != null)
			op.eval(this);
	}
}
