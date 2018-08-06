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

block returns [BlockNode n]
@init { List<StmtNode> stmts = new ArrayList<>(); }
	: LBRACE ( stmt { stmts.add($stmt.n); } )* RBRACE { $n = new BlockNode(stmts); }
	;

stmt returns [StmtNode n]
	: varDefStmt   { $n = $varDefStmt.n; }
	| varInitStmt  { $n = $varInitStmt.n; }
	| varLetStmt   { $n = $varLetStmt.n; }
	| returnStmt   { $n = $returnStmt.n; }
	| exprStmt     { $n = $exprStmt.n; }
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
	: l=expr op=('*'|'/') r=expr  { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('+'|'-') r=expr  { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| INTLIT                      { $n = new IntLiteralNode($INTLIT.int); }
	| IDT LPAREN exprList? RPAREN { $n = new FuncCallNode($IDT.text, $exprList.ctx == null ? new ArrayList<>() : $exprList.ns); }
	| var                         { $n = $var.n; }
	| LPAREN expr RPAREN          { $n = $expr.n; }
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

MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;

LBRACE : '{' ;	
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACK : '[' ;
RBRACK : ']' ;
SEMICOLON : ';' ;
EQ : '=' ;

IDT : [a-z]+ ;
INTLIT : [0-9]+ ;

NEWLINE : ('\r' '\n'?|'\n') -> skip ;
WS : [ \t]+ -> skip ;