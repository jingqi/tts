package tts.grammar.tree.declare;

import tts.eval.IValueEval;
import tts.eval.StringEval;
import tts.grammar.tree.Op;
import tts.scope.Variable;
import tts.scope.VarType;
import tts.trace.SourceLocation;
import tts.vm.Frame;
import tts.vm.rtexcept.ScriptRuntimeException;

public final class ImportOp extends Op {

	private Op path;
	private final String as;

	public ImportOp(Op path, String as, SourceLocation sl) {
		super(sl);
		this.path = path;
		this.as = as;
	}

	private static String getAsName(String s) {
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
		// evaluate path string value
		IValueEval ve = path.eval(f);
		if (!(ve instanceof StringEval))
			throw new ScriptRuntimeException("String value expected", getSourceLocation());
		String p = ((StringEval) ve).getValue();

		// import
		IValueEval vv = f.getVM().importModule(f, p, getSourceLocation());
		String as_name = as;
		if (as_name == null)
			as_name = getAsName(p);
		Variable v = new Variable(VarType.VAR, vv);
		f.addVariable(as_name, v, getSourceLocation());
		return vv;
	}

	@Override
	public Op optimize() {
		path = path.optimize();
		return this;
	}

	@Override
	public String toString() {
		if (as == null)
			return "import " + path.toString() + ";";
		return "import " + path.toString() + " as " + as + ";";
	}
}
