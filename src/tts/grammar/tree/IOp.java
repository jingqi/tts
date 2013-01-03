package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.util.SourceLocation;
import tts.vm.ScriptVM;

public interface IOp {

	IValueEval eval(ScriptVM vm);

	IOp optimize();

	SourceLocation getSourceLocation();
}
