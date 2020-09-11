/****** Header definitions ******/
%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifndef LEXONLY
// definitions auto-created by yacc (when using yacc)
#include "y.tab.h"
#else
// explicit definitions for standalone mode
typedef union { int ival; char* str; } yystype;
#define YYSTYPE yystype
yystype yylval;
#define NUMBER  1
#define COMMA   2
#define LPAREN  3
#define RPAREN  4
#define LBRACE  5
#define RBRACE  6
#define SEMICOLON 7
#define STRING  8
#define ID		 9
#endif
extern int debug; // declared and set in parser.y
%}

%option yylineno

/****** Token Patterns ******/
%%
[ \t\n\r]* { /* skipping white space */ }
[0-9]+   {
           if (debug) printf("lex number: (%s)\n", yytext);
            yylval.ival = strtol(yytext,0,10);
            return(NUMBER);
         }


[a-zA-Z_][a-zA-Z0-9_]*  {			
            if (debug) printf("lex identifier: (%s)\n", yytext);
            // creating a copy of the string is important, but
            // in this small program we are leaking this memory,
            // we don't clean it up; needs fixed in bigger program!
            yylval.str = strdup(yytext);
            return(ID);

			}




\,       {
            if (debug) printf("lex comma\n");
            yylval.ival = yytext[0];
            return(COMMA);
         }
\(       {
            if (debug) printf("lex lparen\n");
            yylval.ival = yytext[0];
            return(LPAREN);
         }
\)       {
            if (debug) printf("lex rparen\n");
            yylval.ival = yytext[0];
            return(RPAREN);
         }
\{       {
            if (debug) printf("lex lbrace\n");
            yylval.ival = yytext[0];
            return(LBRACE);
         }
\}       {
            if (debug) printf("lex rbrace\n");
            yylval.ival = yytext[0];
            return(RBRACE);
         }
\;       {
            if (debug) printf("lex semicolon\n");
            yylval.ival = yytext[0];
            return(SEMICOLON);
         }
\".*\" {

            if (debug) printf("lex string: (%s)\n", yytext);
            // creating a copy of the string is important, but
            // in this small program we are leaking this memory,
            // we don't clean it up; needs fixed in bigger program!
            yylval.str = strdup(yytext);
            return(STRING);
         }

%%

#ifdef LEXONLY

// A main for standalone testing (uses just stdin as input)
int main(int argc, char **argv) 
{
   do {
      yylex();
   } while (1);
   // while (!feof(stdin)); doesn't work if redirecting a file
   // in because the file is read in and buffered, and eof is
   // true too early, causing lex to stop; rather, use while(1)
   // and then do a hard exit in yywrap(); ugly, but works!
   return 0;
}

int yywrap()
{
   exit(0); // hard exit!
   //return(1);
}

#endif // LEXONLY
