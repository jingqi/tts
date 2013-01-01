package tts.eval;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayEval implements IValueEval {

	ArrayList<IValueEval> values = new ArrayList<IValueEval>();

	public int size() {
		return values.size();
	}

	public IValueEval get(int i) {
		return values.get(i);
	}

	public void add(IValueEval v) {
		values.add(v);
	}

	public void addAll(Collection<IValueEval> vs) {
		values.addAll(vs);
	}

	@Override
	public EvalType getType() {
		return EvalType.ARRAY;
	}
}
