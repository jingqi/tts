package tts.vm;

import java.util.ArrayList;

/**
 * 调用栈有效范围
 */
class Frame {

	// 局部范围
	ArrayList<Scope> scopes = new ArrayList<Scope>();

	public Variable getVariable(String name) {
		for (int i = scopes.size() - 1; i >= 0; --i) {
			Variable ret = scopes.get(i).getVariable(name);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public void enterScope() {
		scopes.add(new Scope());
	}

	public void leaveScope() {
		scopes.remove(scopes.size()  - 1);
	}
}
