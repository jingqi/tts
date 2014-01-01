package tts.grammar.tree;

import tts.eval.IValueEval;
import tts.eval.scope.EvalSlot;
import tts.eval.scope.VarType;
import tts.util.SourceLocation;
import tts.vm.Frame;

public final class ImportOp extends Op {

	private final String path;

	public ImportOp(String path, SourceLocation sl) {
		super(sl);
		this.path = path;
	}

	private static String getFileName(String s) {
		int i = s.lastIndexOf('\\');
		if (i >= 0)
			s = s.substring(i);
		i = s.lastIndexOf('/');
		if (i >= 0)
			s = s.substring(i);
		i = s.lastIndexOf('.');
		if (i >= 0)
			s = s.substring(0, i);
		return s;
	}

	@Override
	public IValueEval eval(Frame f) {
		IValueEval vv = f.getVM().importModule(f, path, getSourceLocation());
		String as_name = getFileName(path);
		EvalSlot v = new EvalSlot(VarType.VAR, vv);
		f.addVariable(as_name, v, getSourceLocation());
		return vv;
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder("import ");
	    sb.append(path);
	    return sb.toString();
	}
}
