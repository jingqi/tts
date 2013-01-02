package tts.eval;

import java.util.ArrayList;
import java.util.List;

import tts.grammar.tree.IOp;
import tts.grammar.tree.binaryop.AssignOp;
import tts.vm.*;

public class UserFunctionEval extends FunctionEval {

	public static class ParamInfo {
		String name;
		VarType type;

		public ParamInfo(String n, VarType vt) {
			this.name = n;
			this.type = vt;
		}
	}

	String module;
	IOp ops;
	ArrayList<ParamInfo> params;

	public UserFunctionEval(String module, IOp ops, ArrayList<ParamInfo> params) {
		this.module = module;
		this.ops = ops;
		this.params = params;
	}

	@Override
	public IValueEval call(List<IValueEval> args, ScriptVM vm, SourceLocation sl) {
		if (args.size() != params.size())
			throw new ScriptRuntimeException(
					"count of argument not match in calling function", sl);

		IValueEval ret = VoidEval.instance;
		vm.enterFrame();
		try {
			// 设置参数
			for (int i = 0, size = params.size(); i < size; ++i) {
				ParamInfo p = params.get(i);
				Variable v = new Variable(p.name, p.type, null);
				AssignOp.assign(v, args.get(i), sl);
				vm.addVariable(p.name, v, sl);
			}

			// 调用函数
			ops.eval(vm);
		} catch (BreakLoopException e) {
			throw new ScriptRuntimeException("Break without loop", e.sl);
		} catch (ContinueLoopException e) {
			throw new ScriptRuntimeException("Continue without loop", e.sl);
		} catch (ReturnFuncException e) {
			ret = e.value;
		}
		vm.leaveFrame();
		return ret;
	}

	@Override
	public String getModuleName() {
		return module;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}
