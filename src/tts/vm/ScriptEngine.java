package tts.vm;

import java.io.OutputStreamWriter;
import java.io.Writer;

import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.IOp;
import tts.token.scanner.TokenScanner;
import tts.token.scanner.TokenStream;
import tts.token.stream.IScanReader;
import tts.util.PrintStreamWriter;

public class ScriptEngine {

	ScriptVM vm = new ScriptVM(new OutputStreamWriter(System.err));
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
		vm.setTextOutput(new PrintStreamWriter(System.out));
		IOp op = gs.all();
		op.eval(vm);
		System.out.flush();
	}
}
