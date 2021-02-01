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
public class RuntimeConcatenarHref extends RuntimeOperator{
    public RuntimeConcatenarHref(){
        super();
    }

    @Override
    public void execute(Context context) throws Exception {
        HrefValue der = (HrefValue) context.getStack().pop();
        HrefValue izq = (HrefValue) context.getStack().pop();
        
        HrefValue rval = new HrefValue(der.getValue() + izq.getValue());
        context.getStack().push(rval);
        context.setCurrent(context.getCurrent() + 1);
    }
}
