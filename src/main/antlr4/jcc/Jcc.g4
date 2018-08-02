grammar Jcc;

program
	: funcDef*
	;

funcDef
	: type IDT LPAREN funcParams? RPAREN block
	;

block
	: LBRACE stmt* RBRACE
	;

stmt
	: varDefStmt
	;

varDefStmt
	: type IDT SEMICOLON
	;

funcParams
	: paramDef ( ',' paramDef )*
	;

paramDef
	: type IDT
	;

type
	: INT
	| VOID
	;

INT : 'int' ;
VOID : 'void' ;

LBRACE : '{' ;	
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACK : '[' ;
RBRACK : ']' ;
SEMICOLON : ';' ;

IDT : [a-z]+ ;

NEWLINE : ('\r' '\n'?|'\n') -> skip ;
WS : [ \t]+ -> skip ;