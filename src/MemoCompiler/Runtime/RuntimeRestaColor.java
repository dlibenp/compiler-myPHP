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
public class RuntimeRestaColor extends RuntimeOperator{
    public RuntimeRestaColor(){
        super();
    }

    @Override
    public void execute(Context context) throws Exception {
        ColorValue der = (ColorValue) context.getStack().pop();
        ColorValue izq = (ColorValue) context.getStack().pop();
        
        int valder = Integer.parseInt(der.getValue(), 16);
        int valizq = Integer.parseInt(izq.getValue(), 16);
        int result = valder - valizq;
        String resHez = Integer.toHexString(result);
        
        ColorValue rval = new ColorValue(resHez);
        context.getStack().push(rval);
        context.setCurrent(context.getCurrent() + 1);  
    }
}
