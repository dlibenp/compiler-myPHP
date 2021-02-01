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
public class ASTExpresionMenos extends ASTExpresionBinary{

    public ASTExpresionMenos(ASTLogicExpression izq, ASTLogicExpression der, int line) {
        super(izq, der, line);
    }

    @Override
    public Object Visit(Visitor visitor) {
        return visitor.visit(this);
    }
    
}
