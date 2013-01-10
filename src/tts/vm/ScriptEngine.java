package tts.vm;

import java.io.File;
import java.io.Writer;

import tts.lexer.stream.IScanReader;

public class ScriptEngine {

	final ScriptVM vm;

	public ScriptEngine() {
		vm = new ScriptVM();
	}

	public ScriptEngine(Writer textOutput) {
		vm = new ScriptVM(textOutput);
	}

	// 设置文本输出流
	public void setTextOutput(Writer w) {
		vm.setTextOutput(w);
	}

	// 脚本入口
	public void run(File script) {
		vm.run(script);
	}

	// 内存代码入口
	public void run(IScanReader r) {
		vm.run(r);
	}
}
