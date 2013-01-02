package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.vm.ScriptVM;
import tts.vm.SourceLocation;

public interface IOp {

	IValueEval eval(ScriptVM vm);

	IOp optimize();

	SourceLocation getSourceLocation();
}
