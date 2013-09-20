package tts;

import java.io.File;
import java.io.Writer;

import tts.grammar.scanner.GrammarException;
import tts.lexer.scanner.LexerException;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;
import tts.vm.rtexcept.*;

public class ScriptEngine {

	private final ScriptVM vm;

	public ScriptEngine() {
		vm = new ScriptVM();
	}

	public ScriptEngine(Writer textOutput) {
		vm = new ScriptVM(textOutput);
	}

	public void run(File input) {
		try {
			vm.initExecEnv();
			vm.importModule(vm.getMainFrame(), input.getPath(), SourceLocation.NATIVE);
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
		} catch (LexerException e) {
			System.err.println("Lexer exception:");
			System.err.println(e.toString());
		} catch (GrammarException e) {
			System.err.println("Grammar exception:");
			System.err.println(e.toString());
		} catch (ScriptRuntimeException e) {
			System.err.print("Script runtime exception: ");
			System.err.println(e.getMessage());
			System.err.println(e.toString());
		}
	}
}
