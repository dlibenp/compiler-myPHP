/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MemoCompiler.LexicalAnalyzer;

import MemoCompiler.Errors.*;
import MemoCompiler.Stream.*;
import MemoCompiler.SymbolsTable.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author frodo
 */
public class Lexer {

    char c; //currentChar   
    SourceStream input; // Fichero de entrada
    SymbolsTable symbolsTable;
    HashMap<String, TokenKind> keywordsTable; // Tabla donde se registran las palabras reservadas
    ErrorReporter errorReporter; // Modulo de gestion de errores

    public Lexer(SourceStream input, SymbolsTable symbolsTable, ErrorReporter errorReporter) throws IOException {
        //Constructor de la clase Lexer
        this.input = input;
        this.symbolsTable = symbolsTable;
        this.errorReporter = errorReporter;
        c = input.Read();
        keywordsTable = new HashMap<String, TokenKind>(); // Crea la tabla hash
        FillKeywordtable(); //Inicializa la tabla hash
    }

    private void FillKeywordtable() {
        keywordsTable.put("write", TokenKind.Write);
        keywordsTable.put("float", TokenKind.Float);
        keywordsTable.put("int", TokenKind.Int);

        // Palabras clave para las condiciones
        keywordsTable.put("bool", TokenKind.Bool);
        keywordsTable.put("true", TokenKind.True);
        keywordsTable.put("false", TokenKind.False);
        keywordsTable.put("if", TokenKind.If);
        keywordsTable.put("then", TokenKind.Then);
        keywordsTable.put("else", TokenKind.Else);
        
        keywordsTable.put("void", TokenKind.Void);
        keywordsTable.put("leftKey", TokenKind.LeftKey);
        keywordsTable.put("rightKey", TokenKind.RightKey);
        keywordsTable.put("comma", TokenKind.Comma);
        
        //myphp
        keywordsTable.put("dosPuntos", TokenKind.DosPuntos);
        keywordsTable.put("color", TokenKind.Color);
        keywordsTable.put("href", TokenKind.Href);
        keywordsTable.put("var", TokenKind.Var);
        keywordsTable.put("cod", TokenKind.Cod);
        keywordsTable.put("html", TokenKind.Html);
        keywordsTable.put("title", TokenKind.Title);
        keywordsTable.put("body", TokenKind.Body);
        keywordsTable.put("php", TokenKind.Php);
        keywordsTable.put("script", TokenKind.Script);
        keywordsTable.put("languaje", TokenKind.Languaje);
        
    }

    boolean isLetter() {
        return Character.isLetter(c);
    }

    boolean isDigit() {
        return Character.isDigit(c);
    }

    void consume() throws IOException {
        c = input.Read();
    }

    void WS() throws IOException {
        while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            consume();
        }
    }

    Token ID() throws IOException
    {
        Token token = null;
        StringBuilder buf = new StringBuilder();
        int estado = 0;
        boolean scanning = true;
        while (scanning) {
            switch (estado) {
                case 0:
                    if (isLetter()) {
                        buf.append(c);
                        consume();
                        estado = 1;
                    } else {
                        errorReporter.add(new LexicalError(input.getCurrentLine(), "Caracter no esperado" + c + "."));
                        scanning = false;
                    }
                    break;
                case 1:
                    if (c == '_'){
                        buf.append(c);
                        consume();
                        estado = 2;
                    } else{
                        if (isLetter() || isDigit()) {
                            buf.append(c);
                            consume();
                            estado = 1;
                        } else {
                            scanning = false;
                            String lexeme = buf.toString();
                            String lexema = lexeme.toLowerCase();
                            TokenKind key = keywordsTable.get(lexema);
                            if (key != null) {
                                token = new Token(key, lexeme, input.getCurrentLine());
                            } else {

                                int entry = symbolsTable.add(lexeme, TokenKind.Id);
                                token = new Token(TokenKind.Id, lexeme, input.getCurrentLine(), entry);
                            }
                        }
                    }
                case 2:
                    if (isDigit()){
                        buf.append(c);
                        consume();
                        estado = 3;
                    } else{
                        if (c == '_'){
                        buf.append(c);
                        consume();
                        estado = 2;
                        }else{
                         if (isLetter()) {
                        buf.append(c);
                        consume();
                        estado = 1;
                         }else {
                        scanning = false;
                        String lexeme = buf.toString();
                        String lexema = lexeme.toLowerCase();
                        TokenKind key = keywordsTable.get(lexema);
                        if (key != null) {
                            token = new Token(key, lexeme, input.getCurrentLine());
                        } else {

                            int entry = symbolsTable.add(lexeme, TokenKind.Id);
                            token = new Token(TokenKind.Id, lexeme, input.getCurrentLine(), entry);
                        }
                    }
                    }
                    }    
                case 3:
                    if (isDigit()){
                        buf.append(c);
                        consume();
                        estado = 3;
                    } else{
                        if (c == '_'){
                        buf.append(c);
                        consume();
                        estado = 2;
                        }else{
                         if (isLetter()) {
                        buf.append(c);
                        consume();
                        estado = 1;
                         }else {
                        scanning = false;
                        String lexeme = buf.toString();
                        String lexema = lexeme.toLowerCase();
                        TokenKind key = keywordsTable.get(lexema);
                        if (key != null) {
                            token = new Token(key, lexeme, input.getCurrentLine());
                        } else {

                            int entry = symbolsTable.add(lexeme, TokenKind.Id);
                            token = new Token(TokenKind.Id, lexeme, input.getCurrentLine(), entry);
                        }
                    }
                    }
                    }
                    break;
            }
        }
        return token;
       
    }
        
    Token Literal() throws IOException
    {
        Token token = null;
        StringBuilder buf = new StringBuilder();
        int estado = 0;
        boolean scanning = true;
        while (scanning) {
            switch (estado) {
                case 0:
                    if (isDigit()) {
                        buf.append(c);
                        consume();
                        estado = 1;
                    } else if (c == '#') {
                        consume();
                        estado = 4;
                    } else if (c == '"') {
                        consume();
                        estado = 5;
                    } else {
                        errorReporter.add(new LexicalError(input.getCurrentLine(), "Caracter no esperado" + c + "."));
                        scanning = false;
                    }
                break;
                case 1:
                    if (isDigit()) {
                         buf.append(c);
                        consume();
                        estado = 1;
                    }else if(c == '.'){
                         buf.append(c);
                        consume();
                        estado = 2;
                    }else{
                        scanning = false;
                        String lexeme = buf.toString();
                        int entry = symbolsTable.add(lexeme, TokenKind.IntLiteral);
                        token = new Token(TokenKind.IntLiteral, lexeme,  input.getCurrentLine(),entry);
                    }
                break;
                case 2:
                    if (isDigit()) {
                         buf.append(c);
                        consume();
                        estado = 3;
                    }else {
//                        //Aceptando float de la forma 1. , 32. , 5. ,etc 
//                        //por lo que en el autómata el estado 2 sería de aceptación. 
                            scanning = false;
                            String lexeme = buf.toString();
                            int entry = symbolsTable.add(lexeme, TokenKind.FloatLiteral);
                            token = new Token(TokenKind.FloatLiteral, lexeme,  input.getCurrentLine(),entry);
                   
                    }
                break;
                case 3:
                     if (isDigit()) {
                         buf.append(c);
                        consume();
                        estado = 3;
                    }else {
                          scanning = false;
                        String lexeme = buf.toString();
                        int entry = symbolsTable.add(lexeme, TokenKind.FloatLiteral);
                        token = new Token(TokenKind.FloatLiteral, lexeme,  input.getCurrentLine(),entry);
                    }
                break;
                case 4:
                    if (isDigit() || isLetter()) {
                        if (isLetter() && !Character.toString(c).matches("[a-fA-F]")) {
                            errorReporter.add(new LexicalError(input.getCurrentLine(), "Caracter no valido " + c + "."));
                            scanning = false;
                        }
                        buf.append(c);
                        consume();
                        estado = 4;
                        
                    }else{
                        scanning = false;
                        String lexeme = buf.toString();
                        if (lexeme.length()==6) {
                            int entry = symbolsTable.add(lexeme, TokenKind.ColorLiteral);
                            token = new Token(TokenKind.ColorLiteral, lexeme,  input.getCurrentLine(),entry);
                        }else{
                            errorReporter.add(new LexicalError(input.getCurrentLine(), "Longitud incorrecta de " + TokenKind.ColorLiteral + "."));
                            scanning = false;
                        }
                    }
                break;
                case 5:
                    if (isDigit() || isLetter() || c=='.' || c=='/' || c==':') {
                        buf.append(c);
                        consume();
                        estado = 5;
                    }else{
                        if (c=='"') {
                            consume();
                            scanning = false;
                            String lexeme = buf.toString();
                            if (lexeme.equals("php")) {
                                int entry = symbolsTable.add(lexeme, TokenKind.Php);
                                token = new Token(TokenKind.Php, lexeme,  input.getCurrentLine(),entry);
                            }else{
                                int entry = symbolsTable.add(lexeme, TokenKind.HrefLiteral);
                                token = new Token(TokenKind.HrefLiteral, lexeme,  input.getCurrentLine(),entry);                            
                            }
                        }else{
                            errorReporter.add(new LexicalError(input.getCurrentLine(), "Caracter no esperado" + c + "."));
                            scanning = false;
                        }
                    }
                break;
            }
        }
        return token;
    }

    public Token nextToken() throws IOException /*Funcion principal enlace con el parser(Analizador Sintactico)*/ {
        while (c != '\0') {
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                WS(); // skip
            }
            switch (c) { // which token approaches?
                case '+':
                    consume();
                    return new Token(TokenKind.Sum, "+", input.getCurrentLine());
                case '*':
                    consume();
                    return new Token(TokenKind.Multiplication, "*", input.getCurrentLine());
                case '(':
                    consume();
                    return new Token(TokenKind.LeftParen, "(", input.getCurrentLine());
                case ')':
                    consume();
                    return new Token(TokenKind.RigthParen, ")", input.getCurrentLine());
                case ';':
                    consume();
                    return new Token(TokenKind.SemiColon, ";",input.getCurrentLine());
                //myphp
                case ':':
                    consume();
                    return new Token(TokenKind.DosPuntos, ":", input.getCurrentLine());
                case '/':
                    consume();
                    return new Token(TokenKind.Division, "/", input.getCurrentLine());
                case '-':
                    consume();
                    return new Token(TokenKind.Minus, "-", input.getCurrentLine());
                case '"':
                    Token tkLiteralHref = Literal();
                    return (tkLiteralHref != null)? tkLiteralHref : nextToken();
                    
                case '#':
                    Token tkLiteralColor = Literal();
                    return (tkLiteralColor != null)? tkLiteralColor : nextToken();
                case '?':
                    consume();
                    return new Token(TokenKind.Pregunta, "?", input.getCurrentLine());
                    
                //modificado para expresiones repetitivas
                case '{':
                    consume();
                    return new Token(TokenKind.LeftKey, "{",input.getCurrentLine());
                case '}':
                    consume();
                    return new Token(TokenKind.RightKey, "}",input.getCurrentLine());
                case ',':
                    consume();
                    return new Token(TokenKind.Comma, ",",input.getCurrentLine());
                    
                // Operadores nuevos para las condiciones
                 case '\0':
                     break;
                case '=':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenKind.Equals, "==", input.getCurrentLine());
                    } else {
                        return new Token(TokenKind.Assignment, "=", input.getCurrentLine());
                    }
                case '!':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenKind.Differs, "!=", input.getCurrentLine());
                    } else {
                        return new Token(TokenKind.Not, "!", input.getCurrentLine());
                    }
                    
                case '>':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenKind.GreaterThanOrEquals, ">=", input.getCurrentLine());
                    } else {
                        return new Token(TokenKind.GreaterThan, ">", input.getCurrentLine());
                    }
                case '<':
                    consume();
                    if (c == '=') {
                        consume();
                        return new Token(TokenKind.LessThanOrEquals, "<=", input.getCurrentLine());
                    } else {
                        return new Token(TokenKind.LessThan, "<", input.getCurrentLine());
                    }
                case '&':
                    consume();
                    if (c == '&') {
                        consume();
                        return new Token(TokenKind.And, "&&", input.getCurrentLine());
                    } else {
                        errorReporter.add(new LexicalError(input.getCurrentLine(), "Caracter no esperado &"));
                        return nextToken();
                    }
                case '|':
                    consume();
                    if (c == '|') {
                        consume();
                        return new Token(TokenKind.Or, "||", input.getCurrentLine());
                    } else {
                        errorReporter.add(new LexicalError(input.getCurrentLine(), "Caracter no esperado |"));
                        return nextToken();
                    }
               
                default: {
                    if(isLetter() || c == '_'){
                            Token token = ID();// reconoce ID
                            return (token != null)? token : nextToken() ; 
                            
                        }
                        else
                        {
                            if(isDigit()){
                            Token token = Literal();// reconoce Literal
                            return (token != null)? token : nextToken() ; 
                            
                        }else
                            {
                                errorReporter.add(new LexicalError( input.getCurrentLine(), "Caracter no esperado " + c));
                                consume();
                                return nextToken();
                            }  
                    }
                }
            }
        }
        return new Token(TokenKind.EOT, "", input.getCurrentLine());
    }

    public HashMap<String, TokenKind> getKeywordsTable() {
        return keywordsTable;
    }
    
    public ErrorReporter getErrorList() {
        return errorReporter;
    }
}
