/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MemoCompiler.AbstractSyntaxTrees;

/**
 *
 * @author yudanis
 */
public class ASTWrongExpression extends  ASTExpresion{

    public ASTWrongExpression(int line) {
        super(line);
    }

    @Override
    public Object Visit(Visitor visitor) {
        return visitor.visit(this);
    }
    
}
