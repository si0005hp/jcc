grammar Jcc;

@header {
import jcc.ast.*;
import jcc.type.*;
}

@parser::members {
	private final List<StrLiteralNode> strs = new ArrayList<>();
}

program returns [ProgramNode n]
@init { List<FuncDefNode> funcDefs = new ArrayList<>(); }
	: ( funcDef { funcDefs.add($funcDef.n); } )* 
	  {
	   $n = new ProgramNode(strs, funcDefs);
	  }
	;

funcDef returns [FuncDefNode n]
	: type IDT LPAREN funcParams? RPAREN block
	  {
	  	$n = new FuncDefNode($type.t, $IDT.text, $funcParams.ctx == null ? new ArrayList<>() : $funcParams.e, $block.n);
	  }
	;

stmt returns [StmtNode n]
	: varDefStmt    { $n = $varDefStmt.n; }
	| returnStmt    { $n = $returnStmt.n; }
	| exprStmt      { $n = $exprStmt.n; }
	| ifStmt        { $n = $ifStmt.n; }
	| forStmt       { $n = $forStmt.n; }
	| whileStmt     { $n = $whileStmt.n; }
	| breakStmt     { $n = $breakStmt.n; }
	| continueStmt  { $n = $continueStmt.n; }
	| block         { $n = $block.n; }
	;

varDefStmt returns [VarDefNode n]
	: type IDT SEMICOLON  { $n = new VarDefNode($type.t, $IDT.text); }
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

forStmt returns [ForNode n]
	: FOR LPAREN init=expr? SEMICOLON cond=expr? SEMICOLON step=expr? RPAREN body=stmt
	  {
	  	$n = new ForNode($init.ctx == null ? null : $init.n,
	  		$cond.ctx == null ? null : $cond.n,
	  		$step.ctx == null ? null : $step.n,
	  		$body.n);
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

block returns [BlockNode n]
@init { List<StmtNode> stmts = new ArrayList<>(); }
	: LBRACE ( stmt { stmts.add($stmt.n); } )* RBRACE { $n = new BlockNode(stmts); }
	;

funcParams returns [List<FuncDefNode.ParamDef> e]
@init { $e = new ArrayList<>(); }
	: paramDef  { $e.add($paramDef.e); } ( ',' paramDef { $e.add($paramDef.e); } )*
	;

paramDef returns [FuncDefNode.ParamDef e]
	: type IDT  { $e = new FuncDefNode.ParamDef($type.t, $IDT.text); }
	;

exprList returns [List<ExprNode> ns]
@init { $ns = new ArrayList<>(); }
	: expr { $ns.add($expr.n); } (',' expr { $ns.add($expr.n); } )*
	;

expr returns [ExprNode n]
	: derefer                                         { $n = $derefer.n; }
	| '&' v=var                                       { $n = new AddressNode($v.n); }
	| l=expr op=('*'|'/'|'%') r=expr                  { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('+'|'-') r=expr                      { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('<<'|'>>') r=expr                    { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| l=expr op=('=='|'!='|'>'|'<'|'>='|'<=') r=expr  { $n = new BinOpNode($op.type, $l.n, $r.n); }
	| INTLIT                                          { $n = new IntLiteralNode(IntegerType.of(CType.INT), $INTLIT.int); }
	| CHARLIT                                         { $n = new IntLiteralNode(IntegerType.of(CType.CHAR), StrUtils.characterCode($CHARLIT.text)); }
	| STRLIT                                          
	  { 
	  	StrLiteralNode s = new StrLiteralNode(StrUtils.stringValue($STRLIT.text));
	  	strs.add(s);
	  	$n = s;
	  }
	| IDT LPAREN exprList? RPAREN                     { $n = new FuncCallNode($IDT.text, $exprList.ctx == null ? new ArrayList<>() : $exprList.ns); }
	| var                                             { $n = $var.n; }
	| LPAREN expr RPAREN                              { $n = $expr.n; }
	| varInit                                         { $n = $varInit.n; }
	| assign                                          { $n = $assign.n; }
	;

assign returns [AssignNode n]
	: var '=' v=expr      { $n = new AssignNode($var.n, $v.n); }
	| derefer '=' v=expr  { $n = new AssignNode($derefer.n, $v.n); }
	;

varInit returns [VarInitNode n]
	: type IDT '=' expr  { $n = new VarInitNode(new VarDefNode($type.t, $IDT.text), $expr.n); }
	| arrInit           { $n = $arrInit.n; }
	;

arrInit returns [VarInitNode n]
	: type IDT LBRACK INTLIT? RBRACK '=' LBRACE exprList? RBRACE  
	  { 
	  	ArrLiteralNode an = new ArrLiteralNode($type.t, $exprList.ctx == null ? new ArrayList<>() : $exprList.ns);
	  	int size = $INTLIT == null ? an.getElems().size() : $INTLIT.int;
	  	$n = new VarInitNode(new VarDefNode(ArrayType.of($type.t, size), $IDT.text), an); 
	  }
	| type IDT LBRACK INTLIT? RBRACK '=' STRLIT  
	  { 
	  	ArrLiteralNode an = new ArrLiteralNode($type.t, ParseUtils.strToIntLiteralNodes(StrUtils.stringValue($STRLIT.text)));
	  	int size = $INTLIT == null ? an.getElems().size() : $INTLIT.int;
	  	$n = new VarInitNode(new VarDefNode(ArrayType.of($type.t, size), $IDT.text), an);
	  }
	;

derefer returns [DereferNode n]
	: '*' v=var  { $n = new DereferNode($v.n); }
	;

var returns [VarRefNode n]
	: IDT  { $n = new VarRefNode($IDT.text); }
	;

type returns [Type t]
	: cType        { $t = IntegerType.of($cType.t); }
	| bt=type '*'  { $t = PointerType.of($bt.t); }
	;

cType returns [CType t]
	: 
		(
	      ct=INT
		| ct=CHAR
		| ct=VOID
		)
	  {
	  	$t = CType.of($ct.text);
	  }
	;


INT : 'int' ;
CHAR : 'char' ;
VOID : 'void' ;
RETURN : 'return' ;
IF : 'if' ;
ELSE : 'else' ;
FOR : 'for' ;
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

EQ : '==' ;
NEQ : '!=' ;
GT : '>' ;
LT : '<' ;
GTE : '>=' ;
LTE : '<=' ;

IDT : [a-z]+ ;
INTLIT : [0-9]+ ;
STRLIT : '"' ('""'|~'"')* '"' ;
CHARLIT : '\'' (~'\'')+ '\'' ;

NEWLINE : ('\r' '\n'?|'\n') -> skip ;
WS : [ \t]+ -> skip ;