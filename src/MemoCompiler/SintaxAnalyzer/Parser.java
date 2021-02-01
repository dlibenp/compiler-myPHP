/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MemoCompiler.SintaxAnalyzer;

import MemoCompiler.AbstractSyntaxTrees.*;
import MemoCompiler.Errors.ErrorReporter;
import MemoCompiler.Errors.SyntacticError;
import MemoCompiler.LexicalAnalyzer.*;
import MemoCompiler.MemoTypes;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Parser {

    Lexer input; //from were do we get the tokens
    Token lookahead; //current token
    ErrorReporter errorReporter;

    public Parser(Lexer input, ErrorReporter errorReporter) throws IOException {
        this.input = input;
        this.errorReporter = errorReporter;
        lookahead = input.nextToken();
    }

    public Token getLookahead() {
        return lookahead;
    }
    
    private void consume() throws IOException {
        lookahead = input.nextToken();
    }

    public void match(TokenKind tk_expected) throws IOException {
        if (tk_expected == lookahead.getKind()) {
            consume();
        } else {
            errorReporter.add(new SyntacticError(lookahead.getLine(), "Se esperaba el token \"" + tk_expected + "\" y se encontró el token \"" + lookahead.getKind() + "\""));
            recuperate(tk_expected);
            consume();
        }
    }

    public void recuperate(TokenKind tk_expected) throws IOException {
        while (lookahead.getKind() != tk_expected && lookahead.getKind() != TokenKind.EOT) {
            consume();
        }
    }
    private void recuperate(List<TokenKind> tipos) throws IOException{
        while (!tipos.contains(lookahead.getKind()) && lookahead.getKind() != TokenKind.EOT ) {
           this.consume();
        }
    }
     public AST Parse() throws IOException{
         AST ast = Program();
         if(lookahead.getKind() != TokenKind.EOT){
           errorReporter.add(new SyntacticError(lookahead.getLine(), "Error Sintáctico: Se esperaba el token fin de fichero y se encontró el token " + lookahead.getKind())); 
           recuperate(TokenKind.EOT);
         }
         return ast;
     }

    // <program> -> void id() { <instruction> <mas_instr> }
    public ASTProgram Program() throws IOException {
        //inicio <html>
        match(TokenKind.LessThan);
        match(TokenKind.Html);
        match(TokenKind.GreaterThan);
        
        //inicio <title>id
        match(TokenKind.LessThan);
        match(TokenKind.Title);
        match(TokenKind.GreaterThan);
        match(TokenKind.Id);
        //cierre </title>
        match(TokenKind.LessThan);
        match(TokenKind.Division);
        match(TokenKind.Title);
        match(TokenKind.GreaterThan);
        
        //inicio <body>
        match(TokenKind.LessThan);
        match(TokenKind.Body);
        match(TokenKind.GreaterThan);
        
        List<ASTInstruction> instructions = new LinkedList<ASTInstruction>();
        instructions.addAll(ParsePhpTipo());
        
        //cierre </body>
        match(TokenKind.LessThan);
        match(TokenKind.Division);
        match(TokenKind.Body);
        match(TokenKind.GreaterThan);
        //cierre </html>
        match(TokenKind.LessThan);
        match(TokenKind.Division);
        match(TokenKind.Html);
        match(TokenKind.GreaterThan);
        
        
        
        return new ASTProgram(instructions);
    }

    // <instruction> -> <write_instruction> | <declaration_instruction>
    //                 |<assig_instruction> | <if_instruction> | <ParseWhile>
    //                 | <ParseDoWhile> | <ParseFor> | <SemiColon>
    private ASTInstruction Instruction() throws IOException {
        switch(lookahead.getKind()){
            case Write:
                return Write_Instruction();
            case Id:
                return Assig_Instruction();            
            case If:
                // Agregado para las condiciones
                return If_Instruction();
            case SemiColon:
                match(TokenKind.SemiColon);
            default:
                errorReporter.add(new SyntacticError(lookahead.getLine(), 
                     "Instrucción no válida, se esperaba write,  If o Identificador y llegó "+ lookahead.getLexeme()));
                List<TokenKind> list = new ArrayList<TokenKind>();
                list.add(TokenKind.Write);
                list.add(TokenKind.Id);
                list.add(TokenKind.Int);
                list.add(TokenKind.Float);
                list.add(TokenKind.If);
                list.add(TokenKind.SemiColon);
                recuperate(list);
                return new ASTWrongInstruction(lookahead.getLine());
        }
    }
    // <mas_instr>  ->  <instruction> <mas_instr> | e
    private List<ASTInstruction> Mas_Instr() throws IOException {
         List<ASTInstruction> listInsts = new ArrayList<ASTInstruction>();
         if (lookahead.getKind() == TokenKind.Write || lookahead.getKind() == TokenKind.Read ||
                 lookahead.getKind() == TokenKind.Id || lookahead.getKind() == TokenKind.If ||
                 lookahead.getKind() == TokenKind.SemiColon ||
                 lookahead.getKind() == TokenKind.Float || lookahead.getKind() == TokenKind.Bool || 
                 lookahead.getKind() == TokenKind.Int) {
            listInsts.add(Instruction());
            listInsts.addAll(Mas_Instr());
        }
        return listInsts;
    }

    // <assig_instruction> -> tk_id tk_op_asig <expression>;
    private ASTInstructionAssig Assig_Instruction() throws IOException {
        ASTIdentifierReference idref = new ASTIdentifierReference(lookahead.getLexeme(), lookahead.getEntry(), lookahead.getLine());
        match(TokenKind.Id);
        int line =  lookahead.getLine();
        match(TokenKind.Assignment);
        ASTLogicExpression expression = Expression();
        match(TokenKind.SemiColon);
        idref.setType(expression.getType());
        return new ASTInstructionAssig(idref, expression, line);
    }

    // <declaration> -> <type> tk_id ;
    private ASTInstruction Declaration_Instruction() throws IOException {
        MemoTypes type=null;
        ASTInstructionDec dcl= null ;
        if (IsType(lookahead.getKind())) {
            type = GetType(lookahead.getKind());
            match(lookahead.getKind());
        }
        int line = lookahead.getLine();
        if (lookahead.getKind() == TokenKind.Id) {
            dcl = new ASTInstructionDec(type, new ASTIdentifierDeclaration(lookahead.getLexeme(), lookahead.getEntry(), line), line);
        }
        match(TokenKind.Id);
        match(TokenKind.SemiColon);
        return (dcl != null) ? dcl : new ASTWrongInstruction(line) ;
    }
    
    //<var> --> [var <declarations>]
    private List<ASTInstructionDec> ParseVariable() throws IOException{
        if (IsType(lookahead.getKind())) {
            return ParseDeclaraciones();
        }
        return new ArrayList<ASTInstructionDec>();
    }
    
    //<variables> --> id <others_variables>
    private List<ASTIdentifierDeclaration> ParseVariables() throws IOException{
        List<ASTIdentifierDeclaration> identifiers = new ArrayList<ASTIdentifierDeclaration>();
        if (lookahead.getKind() == TokenKind.Id)
            identifiers.add(new ASTIdentifierDeclaration(lookahead.getLexeme(), 
                    lookahead.getEntry(), lookahead.getLine()));
        match(TokenKind.Id);
        identifiers.addAll(ParseOthersVariables());
        return identifiers;
    }
    
    //<others_variables> --> [, <variables>]
    private List<ASTIdentifierDeclaration> ParseOthersVariables() throws IOException{
        if (lookahead.getKind() == TokenKind.Comma) {
            match(TokenKind.Comma);
            return ParseVariables();
        }
        return new ArrayList<ASTIdentifierDeclaration>();
    }
    
    //<declarations> --> [<declaration> <declarations>]
    private List<ASTInstructionDec> ParseDeclaraciones() throws IOException{
        List<ASTInstructionDec> varDeclarations = new ArrayList<ASTInstructionDec>();
        if (lookahead.getKind() == TokenKind.Id) {
            varDeclarations.addAll(ParseDeclaracion());
            varDeclarations.addAll(ParseDeclaraciones());
        }
        return varDeclarations;
    }
    
    //<declaration> --> <variables> : <type>  ;
    private List<ASTInstructionDec> ParseDeclaracion() throws IOException{
        List<ASTInstructionDec> varDeclarations = new ArrayList<ASTInstructionDec>();
        
        List<ASTIdentifierDeclaration> indentifiers = ParseVariables();
        match(TokenKind.DosPuntos);
        MemoTypes type = ParseType();        
        
        for (ASTIdentifierDeclaration indent : indentifiers) {
            varDeclarations.add(new ASTInstructionDec(type, indent, indent.getLine()));
        }
        match(TokenKind.SemiColon);
        return varDeclarations;
    }
    
    //<type> --> int | float | bool | Color | Href
    private MemoTypes ParseType() throws IOException{
        MemoTypes type = GetType(lookahead.getKind());
        if (IsType(lookahead.getKind()))
            match(lookahead.getKind());
            
        return type;
    }

    // <write_instruction> ->  tk_write <expression> 
    private ASTInstructionWrite Write_Instruction() throws IOException {
        int line =  lookahead.getLine();
        match(TokenKind.Write);
        return new ASTInstructionWrite(Expression(), line);
    }

    // Método agregado para las condiciones
    // <if_instruction> ->  tk_if tk_( <logic_expression> tk_) tk_then <instruction> <else_part>
    private ASTInstructionIf If_Instruction() throws IOException {
        int ifPosition = lookahead.getLine();
        match(TokenKind.If);
        match(TokenKind.LeftParen);
        ASTLogicExpression condition = Logic_Expression();
        match(TokenKind.RigthParen);
        match(TokenKind.Then);
        return new ASTInstructionIf(condition, Instruction(), Else_Part(), ifPosition);
    }

    // Método agregado para las condiciones
    // <else_part> -> tk_else <instruction> |
    private ASTInstruction Else_Part() throws IOException {
        if (lookahead.getKind() == TokenKind.Else) {
            match(TokenKind.Else);
            return Instruction();
        } else {
            return null;
        }
    }

    // <expression> -> <termino> <mas_termino>
    private ASTLogicExpression Expression() throws IOException {
        ASTLogicExpression term = Termino();
        return Mas_Termino(term);
    }

    // <mas_termino> -> tk_op_sum <termino> <mas_termino> | e
    private ASTLogicExpression Mas_Termino(ASTLogicExpression term) throws IOException {
        if (lookahead.getKind() == TokenKind.Sum) {
            int line = lookahead.getLine();
            match(TokenKind.Sum);
            ASTLogicExpression izq = term;
            ASTLogicExpression der = Termino();
            if (izq.getType()==MemoTypes.Href || der.getType()==MemoTypes.Href) {
                return Mas_Termino(new ASTExpresionConcatenar(izq, der, line ));
            }
            return Mas_Termino(new ASTExpresionSuma(izq, der, line ));
        }else if (lookahead.getKind() == TokenKind.Minus) {
            int line = lookahead.getLine();
            match(TokenKind.Minus);
            ASTLogicExpression izq = term;
            ASTLogicExpression der = Termino();
            return Mas_Termino(new ASTExpresionMenos(izq, der, line ));
        }
        return term;
    }
    // <termino> -> <factor> <mas_factor>
    private ASTLogicExpression Termino() throws IOException {
        ASTLogicExpression factor = Factor();
        return Mas_Factor(factor);
    }

    // <mas_factor> -> tk_op_por <factor> <mas_factor> | e
    private ASTLogicExpression Mas_Factor(ASTLogicExpression factor) throws IOException {
        if (lookahead.getKind() == TokenKind.Multiplication) {
            int line = lookahead.getLine();
            match(TokenKind.Multiplication);
            ASTLogicExpression izq = factor;
            ASTLogicExpression der = Factor();
            return Mas_Factor(new ASTExpresionMult(izq, der, line));
        }
        else if (lookahead.getKind() == TokenKind.Division) {
            int line = lookahead.getLine();
            match(TokenKind.Division);
            ASTLogicExpression izq = factor;
            ASTLogicExpression der = Factor();
            return Mas_Factor(new ASTExpresionDivision(izq, der, line));
        }
        return factor;
    }
    // <factor> ->  !<logic_expression>| <factor2>
     private ASTLogicExpression Factor() throws IOException {
        ASTLogicExpression factor;
        switch(lookahead.getKind()){
            case Not:
                match(TokenKind.Not);
                factor = Logic_Expression();
            break;
            case LeftParen:
            case Id:
            case FloatLiteral:
            case IntLiteral:
            case ColorLiteral:
            case SigNumero:
            case HrefLiteral:
            case Comilla:
            case True:
            case False:
                 factor = Factor2();
            break;
            default:
                factor = new ASTWrongExpression(lookahead.getLine());
                errorReporter.add(new SyntacticError(lookahead.getLine(), "Expresión Incorrecta"));
                recuperate(TokenKind.SemiColon);
               
        }
        return factor;
     }
    // <factor2> ->  (<logic_expression>)| tk_id | tk_val_int | tk_val_float | tk_true | tk_false
    private ASTLogicExpression Factor2() throws IOException {
        ASTLogicExpression factor;
        switch (lookahead.getKind()) {
            case Id:
                factor = new ASTIdentifierValue(lookahead.getLexeme(), lookahead.getEntry(), lookahead.getLine());
                match(TokenKind.Id);
                break;
            case FloatLiteral:
                factor = new ASTFloatValue(lookahead.getEntry(), lookahead.getLexeme(), lookahead.getLine());
                match(TokenKind.FloatLiteral);
                break;
            case IntLiteral:
                factor = new ASTIntValue(lookahead.getEntry(), lookahead.getLexeme(), lookahead.getLine());
                match(TokenKind.IntLiteral);
                break;
            case ColorLiteral:
                factor = new ASTColorValue(lookahead.getEntry(), lookahead.getLexeme(), lookahead.getLine());
                factor.setType(MemoTypes.Color);
                match(TokenKind.ColorLiteral);
                break;
            case HrefLiteral:
                factor = new ASTHrefValue(lookahead.getEntry(), lookahead.getLexeme(), lookahead.getLine());
                factor.setType(MemoTypes.Href);
                match(TokenKind.HrefLiteral);
                break;
            case True:
                factor = new ASTBoolValue(lookahead.getLexeme(), lookahead.getLine());
                match(TokenKind.True);
                break;
            case False:
                factor = new ASTBoolValue(lookahead.getLexeme(), lookahead.getLine());
                match(TokenKind.False);
                break;
            case LeftParen:
                match(TokenKind.LeftParen);
                factor = Logic_Expression();
                match(TokenKind.RigthParen);
                break;
            default:
                factor = new ASTWrongExpression(lookahead.getLine());
                errorReporter.add(new SyntacticError(lookahead.getLine(), "Expresión Incorrecta"));
                recuperate(TokenKind.SemiColon);
                
        }
        return factor;
    }

    // Método agregado para las condiciones
    // <logic_expression> -> <logic_term> <more_logic_terms>
    private ASTLogicExpression Logic_Expression() throws IOException {
        ASTLogicExpression term = Logic_Term();
        return More_Logic_Terms(term);
    }

    // Método agregado para las condiciones
    // <mas_termino> -> tk_op_or <termino> <mas_termino> | e
    private ASTLogicExpression More_Logic_Terms(ASTLogicExpression term) throws IOException {
        if (lookahead.getKind() == TokenKind.Or) {
            int line =  lookahead.getLine();
            match(TokenKind.Or);
            ASTLogicExpression izq = term;
            ASTLogicExpression der = Logic_Term();
            return More_Logic_Terms(new ASTLogicExpressionOr(izq, der, line));
        }
        return term;
    }

    // Método agregado para las condiciones
    // <logic_term> -> <logic_factor> <more_logic_factor>
    private ASTLogicExpression Logic_Term() throws IOException {
        ASTLogicExpression factor = Logic_Factor();
        return More_Logic_Factor(factor);
    }

    // Método agregado para las condiciones
    // <more_logic_factor> -> tk_op_and <logic_factor> <more_logic_factor> | e
    private ASTLogicExpression More_Logic_Factor(ASTLogicExpression factor) throws IOException {
        if (lookahead.getKind() == TokenKind.And) {
            int line =  lookahead.getLine();
            match(TokenKind.And);
            ASTLogicExpression izq = factor;
            ASTLogicExpression der = Logic_Factor();
            return More_Logic_Factor(new ASTLogicExpressionAnd(izq, der, line));
        }
        return factor;
    }
    //<logic_factor> -> <expression_relational> <more_exp_relational> 
    private ASTLogicExpression Logic_Factor() throws IOException {
        ASTLogicExpression expression_relational = expresion_relacional();
        return more_expresion_relacional(expression_relational);
    } 
    
     //<more_exp_relational> -> tk_op_equal <expression_relational> <more_expresion_relacional>| tk_op_differs <expression_relational> <more_expresion_relacional>| e
    private ASTLogicExpression more_expresion_relacional(ASTLogicExpression izq) throws IOException {
      if(lookahead.getKind() == TokenKind.Equals){
          int line = lookahead.getLine();
          match(TokenKind.Equals);
          ASTLogicExpression der = expresion_relacional();
          return more_expresion_relacional(new ASTLogicExpressionEquals((ASTLogicExpression)izq, (ASTLogicExpression)der, line));
      }else if(lookahead.getKind() == TokenKind.Differs){
          int line = lookahead.getLine();
          match(TokenKind.Differs);
          ASTLogicExpression der = expresion_relacional();
          return more_expresion_relacional(new ASTLogicExpressionDiffers((ASTLogicExpression)izq, (ASTLogicExpression)der, line));
      }
      return izq;
    } 
    
   //<expression_relational> -> <termino_relacional><more_term_relacional>
    private ASTLogicExpression expresion_relacional() throws IOException {
       ASTLogicExpression termino_relacional =  termino_relacional();
       return more_termino_relacional(termino_relacional);
    } 
   //<termino_relacional> -> <Expression> 
    private ASTLogicExpression termino_relacional() throws IOException {
       return Expression();
    } 
    //<more_term_relacional> -> tk_op_great <termino_relacional><more_term_relacional> | tk_op_less <termino_relacional><more_term_relacional> |
    //                          tk_op_greatEq <termino_relacional><more_term_relacional> | tk_op_lessEq <termino_relacional><more_term_relacional> 
    //                          | e
    private ASTLogicExpression more_termino_relacional(ASTLogicExpression izq) throws IOException {
       if(lookahead.getKind() == TokenKind.GreaterThan){
           int line = lookahead.getLine();
           match(TokenKind.GreaterThan);
           ASTLogicExpression der =  termino_relacional();
           return more_termino_relacional(new ASTLogicExpressionGreaterThan((ASTLogicExpression)izq, (ASTLogicExpression)der, line));
       }else if(lookahead.getKind() == TokenKind.GreaterThanOrEquals){
           int line = lookahead.getLine();
           match(TokenKind.GreaterThanOrEquals);
           ASTLogicExpression der =  termino_relacional();
           return more_termino_relacional(new ASTLogicExpressionGreaterThanOrEquals((ASTLogicExpression)izq, (ASTLogicExpression)der, line));
       }else if(lookahead.getKind() == TokenKind.LessThan){
           int line = lookahead.getLine();
           match(TokenKind.LessThan);
           ASTLogicExpression der =  termino_relacional();
           return more_termino_relacional(new ASTLogicExpressionLessThan((ASTLogicExpression)izq, (ASTLogicExpression)der, line));
       }else if(lookahead.getKind() == TokenKind.LessThanOrEquals){
           int line = lookahead.getLine();
           match(TokenKind.LessThanOrEquals);
           ASTLogicExpression der =  termino_relacional();
           return more_termino_relacional(new ASTLogicExpressionLessThanOrEquals((ASTLogicExpression)izq, (ASTLogicExpression)der, line));
       }
       return izq;
    }
    
    
    private MemoTypes GetType(TokenKind tokenKind){
        switch(tokenKind){
            case Bool:
                return MemoTypes.Bool;
            case Int:
                return MemoTypes.Int;
            case Float:
                return MemoTypes.Float;
            case Color:
                return MemoTypes.Color;
            case Href:
                return MemoTypes.Href;
            default:
                return MemoTypes.Undefined;
        }
    }
    
    private boolean IsType(TokenKind tokenKind){
        return tokenKind == TokenKind.Bool || tokenKind == TokenKind.Int || 
                tokenKind == TokenKind.Float || tokenKind == TokenKind.Color || 
                tokenKind == TokenKind.Href;
    }
    
    
    /*Agregado pa myPHP*/
    private List<ASTInstruction> ParsePhpTipo() throws IOException{
        List<ASTInstruction> instructions = new LinkedList<ASTInstruction>();
        
        int line =  lookahead.getLine();
        match(TokenKind.LessThan);
        
        if (lookahead.getKind() == TokenKind.Pregunta) {
            match(TokenKind.Pregunta);
            if (lookahead.getKind() == TokenKind.Php) {
                match(TokenKind.Php);
            }
            match(TokenKind.LessThan);
            match(TokenKind.Var);
            match(TokenKind.GreaterThan);
            //<var>
            instructions.addAll(ParseDeclaraciones());

            match(TokenKind.LessThan);
            match(TokenKind.Cod);
            match(TokenKind.GreaterThan);
            //<cod>
            instructions.addAll(Mas_Instr());

            match(TokenKind.Pregunta);
            match(TokenKind.GreaterThan);
        }
        else if (lookahead.getKind() == TokenKind.Script){
            //<script languaje="php">
            match(TokenKind.Script);
            match(TokenKind.Languaje);
            match(TokenKind.Assignment);
            //match(TokenKind.Comilla);
            match(TokenKind.Php);
            //match(TokenKind.Comilla);
            match(TokenKind.GreaterThan);
            
            match(TokenKind.LessThan);
            match(TokenKind.Var);
            match(TokenKind.GreaterThan);
            //<var>
            instructions.addAll(ParseDeclaraciones());

            match(TokenKind.LessThan);
            match(TokenKind.Cod);
            match(TokenKind.GreaterThan);
            //<cod>
            instructions.addAll(Mas_Instr());
            
            //</script>
            match(TokenKind.LessThan);
            match(TokenKind.Division);
            match(TokenKind.Script);
            match(TokenKind.GreaterThan);
        }
                
        return instructions;
    }
    
}
