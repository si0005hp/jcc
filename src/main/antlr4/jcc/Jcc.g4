grammar Jcc;

@header {
import jcc.ast.*;
// import java.util.*;
// import java.util.stream.*;
}

@parser::members {
}

program returns [ProgramNode n]
@init { List<FuncDefNode> funcDefs = new ArrayList<>(); }
	: ( funcDef { funcDefs.add($funcDef.n); } )* { $n = new ProgramNode(funcDefs); }
	;

funcDef returns [FuncDefNode n]
	: type IDT LPAREN funcParams? RPAREN block
	  {
	  	$n = new FuncDefNode(CType.of($type.text), $IDT.text, $funcParams.ctx == null ? new ArrayList<>() : $funcParams.e, $block.n);
	  }
	;

stmt returns [StmtNode n]
	: varDefStmt    { $n = $varDefStmt.n; }
	| varInitStmt   { $n = $varInitStmt.n; }
	| varLetStmt    { $n = $varLetStmt.n; }
	| returnStmt    { $n = $returnStmt.n; }
	| exprStmt      { $n = $exprStmt.n; }
	| ifStmt        { $n = $ifStmt.n; }
	| whileStmt     { $n = $whileStmt.n; }
	| breakStmt     { $n = $breakStmt.n; }
	| continueStmt  { $n = $continueStmt.n; }
	| printfStmt    { $n = $printfStmt.n; }
	| block         { $n = $block.n; }
	;

varDefStmt returns [VarDefNode n]
	: type IDT SEMICOLON  { $n = new VarDefNode(CType.of($type.text), $IDT.text); }
	;

varInitStmt returns [VarInitNode n]
	: type IDT EQ expr SEMICOLON  { $n = new VarInitNode(new VarDefNode(CType.of($type.text), $IDT.text), $expr.n); }
	;

varLetStmt returns [VarLetNode n]
	: var EQ expr SEMICOLON  { $n = new VarLetNode($var.n, $expr.n); }
	;

returnStmt returns [ReturnNode n]
	: RETURN expr SEMICOLON  { $n = new ReturnNode($expr.n); }
	| RETURN SEMICOLON       { $n = new ReturnNode(null); }
	;

exprStmt returns [ExprStmtNode n]
	: expr SEMICOLON  { $n = new ExprStmtNode($expr.n); }
	;

ifStmt returns [IfNode n]
	: IF LPAREN cond=expr RPAREN thenBody=stmt ( ELSE elseBody=stmt )?
	  {
	  	$n = new IfNode($cond.n, $thenBody.n, $elseBody.ctx == null ? null : $elseBody.n);
	  }
	;

whileStmt returns [WhileNode n]
	: WHILE LPAREN cond=expr RPAREN body=stmt  { $n = new WhileNode($cond.n, $body.n); }
	;

breakStmt returns [BreakNode n]
	: BREAK SEMICOLON  { $n = new BreakNode(); }
	;

continueStmt returns [ContinueNode n]
	: CONTINUE SEMICOLON  { $n = new ContinueNode(); }
	;

// Temporal node to compile 'printf' in Jcc
printfStmt returns [PrintfNode n]
@init { List<ExprNode> args = new ArrayList<>(); }
	: 'printf' LPAREN fmtStr=expr ( ',' expr { args.add($expr.n); } )* RPAREN SEMICOLON
	  {
	  	$n = new PrintfNode($fmtStr.n, args);
	  }
	;

block returns [BlockNode n]
@init { List<StmtNode> stmts = new ArrayList<>(); }
	: LBRACE ( stmt { stmts.add($stmt.n); } )* RBRACE { $n = new BlockNode(stmts); }
	;

funcParams returns [List<FuncDefNode.ParamDef> e]
@init { $e = new ArrayList<>(); }
	: paramDef  { $e.add($paramDef.e); } ( ',' paramDef { $e.add($paramDef.e); } )*
	;

paramDef returns [FuncDefNode.ParamDef e]
	: type IDT  { $e = new FuncDefNode.ParamDef(CType.of($type.text), $IDT.text); }
	;

exprList returns [List<ExprNode> ns]
@init { $ns = new ArrayList<>(); }
	: expr { $ns.add($expr.n); } (',' expr { $ns.add($expr.n); } )*
	;

expr returns [ExprNode n]
	: l=expr op=('*'|'/'|'%') r=expr                  { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('+'|'-') r=expr                      { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('<<'|'>>') r=expr                    { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('=='|'!='|'>'|'<'|'>='|'<=') r=expr  { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| INTLIT                                          { $n = new IntLiteralNode($INTLIT.int); }
	| STRLIT                                          { $n = new StrLiteralNode(StrUtils.stringValue($STRLIT.text)); }
	| IDT LPAREN exprList? RPAREN                     { $n = new FuncCallNode($IDT.text, $exprList.ctx == null ? new ArrayList<>() : $exprList.ns); }
	| var                                             { $n = $var.n; }
	| LPAREN expr RPAREN                              { $n = $expr.n; }
	;

var returns [VarRefNode n]
	: IDT  { $n = new VarRefNode($IDT.text); }
	;

type
	: INT
	| VOID
	;


INT : 'int' ;
VOID : 'void' ;
RETURN : 'return' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
BREAK : 'break' ;
CONTINUE : 'continue' ;

MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
MOD : '%' ;
LSHIFT : '<<' ;
RSHIFT : '>>' ;

LBRACE : '{' ;	
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACK : '[' ;
RBRACK : ']' ;
SEMICOLON : ';' ;
EQ : '=' ;

EQEQ : '==' ;
NOTEQ : '!=' ;
GT : '>' ;
LT : '<' ;
GTE : '>=' ;
LTE : '<=' ;

IDT : [a-z]+ ;
INTLIT : [0-9]+ ;
STRLIT : '"' ('""'|~'"')* '"' ;

NEWLINE : ('\r' '\n'?|'\n') -> skip ;
WS : [ \t]+ -> skip ;