package tts.trace;

import java.util.ArrayList;

public final class CallingStack {

	private final ArrayList<RuntimeLocation> stack;

	public CallingStack(ArrayList<RuntimeLocation> s) {
		this.stack = s;
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    for (int i = stack.size() - 1; i >= 0; --i) {
	        RuntimeLocation rl = stack.get(i);
	        sb.append("\t" + "at ");
	        sb.append(rl.funcName);
	        sb.append("(");
	        sb.append(rl.sl.file);
	        sb.append(":");
	        sb.append(Integer.toString(rl.sl.line));
	        sb.append(")\n");
	    }
	    return sb.toString();
	}
}
