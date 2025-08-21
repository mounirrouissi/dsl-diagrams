grammar Aiva;

// Parser Rules
script: track;

track: 'START_TRACK' IDENTIFIER state+ 'END_TRACK'?;

state: IDENTIFIER '.' IDENTIFIER action+;

action:
    'CALL_FUNCTION_SWITCH' IDENTIFIER IDENTIFIER '->' IDENTIFIER branch+
    | 'SEND_TEMPLATE' IDENTIFIER
    | 'GOTO' IDENTIFIER;

branch: IDENTIFIER ':' IDENTIFIER;

// Lexer Rules (Tokens)
IDENTIFIER: [a-zA-Z_@][a-zA-Z_0-9]*;
WS: [ \t\r\n]+ -> skip; // Skip whitespace