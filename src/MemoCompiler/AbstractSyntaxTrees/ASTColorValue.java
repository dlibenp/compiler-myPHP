/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MemoCompiler.AbstractSyntaxTrees;

/**
 *
 * @author orisha
 */
public class ASTColorValue extends ASTSymbol{

    public ASTColorValue(int entry, String lexeme, int line) {
        super(entry, lexeme, line);
    }

    @Override
    public Object Visit(Visitor visitor) {
        return visitor.visit(this);
    }
    
}