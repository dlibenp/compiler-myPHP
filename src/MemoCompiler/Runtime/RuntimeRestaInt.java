/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MemoCompiler.Runtime;

/**
 *
 * @author orisha
 */
public class RuntimeRestaInt extends RuntimeOperator{
    public RuntimeRestaInt(){
        super();
    }

    @Override
    public void execute(Context context) throws Exception {
        IntValue der = (IntValue) context.getStack().pop();
        IntValue izq = (IntValue) context.getStack().pop();

        IntValue rval = new IntValue(izq.getValue() - der.getValue());
        context.getStack().push(rval);
        context.setCurrent(context.getCurrent() + 1);
    }
}
