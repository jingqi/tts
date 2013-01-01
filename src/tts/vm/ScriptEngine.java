package tts.vm;

import java.io.Writer;

import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.IOp;
import tts.token.scanner.TokenScanner;
import tts.token.scanner.TokenStream;
import tts.token.stream.IScanReader;
import tts.util.PrintStreamWriter;

public class ScriptEngine {

	ScriptVM vm = new ScriptVM(new PrintStreamWriter(System.out));
	GrammarScanner gs = null;

	public void setScriptInput(IScanReader r) {
		TokenScanner tsn = new TokenScanner(r);
		TokenStream ts = new TokenStream(tsn);
		gs = new GrammarScanner(ts);
	}

	public void setTextOutput(Writer w) {
		vm.setTextOutput(w);
	}

	public void run() {
		// 词法分析，语法分析，构建语法树
		IOp op = gs.all();

		// 优化语法树
		op = op.optimize();

		// 求值
		if (op != null)
			op.eval(vm);
	}
}
