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
public class HrefValue extends RuntimeValue<String>{
    
    public HrefValue(String value) {
        super(value);
    }
    
    public HrefValue() {
        super(new String());
    }
    
}
