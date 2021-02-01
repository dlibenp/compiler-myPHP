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
public class ColorValue extends RuntimeValue<String>{
    
    public ColorValue(String value) {
        super(value);
    }
    
    public ColorValue() {
        super(new String());
    }
    
}
