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
public class RuntimeRestaFloat extends RuntimeOperator{
    public RuntimeRestaFloat(){
        super();
    }

    @Override
    public void execute(Context context) throws Exception {
        FloatValue der = (FloatValue) context.getStack().pop();
        FloatValue izq = (FloatValue) context.getStack().pop();

        FloatValue rval = new FloatValue(izq.getValue() - der.getValue());
        context.getStack().push(rval);
        context.setCurrent(context.getCurrent() + 1);
    }
}
