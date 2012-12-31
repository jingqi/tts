package tts.vm;

import java.io.Writer;

import tts.grammar.scanner.GrammarScanner;
import tts.grammar.tree.IOp;
import tts.token.scanner.TokenScanner;
import tts.token.scanner.TokenStream;
import tts.token.stream.IScanReader;

public class ScriptEngine {

	ScriptVM vm = new ScriptVM();
	GrammarScanner gs;
	Writer textOutput;

	public void setScriptInput(IScanReader r) {
		TokenScanner tsn = new TokenScanner(r);
		TokenStream ts = new TokenStream(tsn);
		gs = new GrammarScanner(ts);
	}

	public void setTextOutput(Writer w) {
		textOutput = w;
	}

	public void run() {
		IOp op = gs.all();
		op.eval(vm);
	}
}
