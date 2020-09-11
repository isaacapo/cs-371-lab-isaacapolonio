/****** Header definitions ******/
%{
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

//global variable
#define NUM_OF_STRINGS 32
char strings[NUM_OF_STRINGS][1024];
//addString prototype
char* generateDataSection();
int addString(char *strIn);
// function prototypes from lex
int yyerror(char *s);
int yylex(void);
int debug=0; // set to 1 to turn on extra printing
%}

/* token value data types */
%union { int ival; char* str; }

/* Starting non-terminal */
%start prog
%type <str> function statements statement funcall

/* Token types */
%token <ival> NUMBER COMMA SEMICOLON LPAREN RPAREN LBRACE RBRACE
%token <str> ID STRING

%%
/******* Rules *******/
prog: function
   { 
      printf("\t.text\n%s, \n%s",generateDataSection(), $1);
   }


funcall: ID LPAREN STRING RPAREN
   {
      //printf("!funcall call!\n");
      int sid = addString($3);
      char *code = (char*) malloc(1024);
      sprintf(code,"\tmovl\t$.LC%d, %%edi\n\tcall\t%s\n",sid,$1);	//replace 0 with sid
      //sprintf(code, "[FUNCALL %s, %d]", $3, sid);
      $$ = code;
   }

statement: funcall SEMICOLON
   {
      //printf("!statement call; %s!\n", $1);	
      $$ = $1;
   }

statements:
   {
      $$ = "";
      //printf("!empty statements call!\n");
   }

   | statement statements
   {
      //printf("!statements call: %s + %s !\n",$1, $2);
      char *code = (char*) malloc(1024);
      sprintf(code, "%s\n%s", $1, $2);
      $$ = code;
   }
;
function: ID LPAREN RPAREN LBRACE statements RBRACE
   {
      //printf("!function call!\n");
      char *code = (char*) malloc(1024);
      sprintf(code, "\t.text\n\t.globl\t%s\n\t.type\t%s, @function\n%s:\n\tpushq\t%%rbp\n\tmovq\t%%rsp,%%rbp\n%s\tmovl\t$0, %%eax\n\tpopq\t%%rbp\n\tret\n", $1, $1, $1, $5);
      $$ = code;

   }

%%
/******* Functions *******/
extern FILE *yyin; // from lex

int main(int argc, char **argv)
{
   if (argc==2) {
      yyin = fopen(argv[1],"r");
      if (!yyin) {
         printf("Error: unable to open file (%s)\n",argv[1]);
         return(1);
      }
   }
   return(yyparse());
}

extern int yylineno; // from lex

int yyerror(char *s)
{
   fprintf(stderr, "Error: line %d: %s\n",yylineno,s);
   return 0;
}

int yywrap()
{
   return(1);
}


int addString(char *strIn)
{
   int index = 0;

   while(strings[index][0] != '\0')
   {
      index++;
   }

   strcpy(strings[index], strIn);

   return index;
}
char* generateDataSection()
{
   int index = 0;
   char *codeOut =  malloc(2048);
   strcpy(codeOut, "\t.section\t.rodata\n");
   while(strings[index][0] != '\0')
   {
      sprintf(codeOut, "%s\n.LC%d:\n\t.string %s", codeOut, index, strings[index]);
      index++;
   }

   return codeOut;
}

