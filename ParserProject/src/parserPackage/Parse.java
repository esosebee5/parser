package parserPackage;

import java.io.BufferedReader;
import java.io.FileReader;

/**
  * Parser
  * Eric Sosebee
  * CSCI 4200-DA
  * Dr. Abi Salimi
  */

public class Parse {
    private static boolean errorState;

    private static int charClass;
    private static char[] lexeme;
    private static char nextChar;
    private static int lexLen;
    private static Token nextToken;
    private static int index;

    private static final int LETTER = 0;
    private static final int DIGIT = 1;
    private static final int UNKNOWN = 99;
    private static final int EOF = 100;
    private static final int MAX_LEXEME_SIZE = 100;

    // something for files
    private static String fileData = "";
    private static final String headline = "Eric Sosebee, CSCI4200-DA, Fall 2018, Lexical Analyzer";
    private static final String border
            = "********************************************************************************";

    public static void main(String[] args) {
        System.out.println(headline);
//        String fileName = "lexInput.txt";
        String fileName = "src\\parserPackage\\statements.txt";

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            while (line != null) {
                fileData = line;

                if (!fileData.isEmpty()) {
                    System.out.println(border);
                    System.out.println("Parsing the statement: " + fileData);
                    fileData = fileData.replace(" ", "");

                    errorState = false;
                    index = 0;
                    getChar();
                    do {
                        lex();
                        if (nextToken == Token.ASSIGN_OP) {
//                            lex();
                            assign();
                        }
                    } while ((nextToken != Token.END_OF_FILE));
                }

                if (errorState) {
                    System.out.println("\nTHIS LINE HAS A SYNTAX ERROR.");
                }

                line = bufferedReader.readLine();
            }

            System.out.println(border);
            printNextTokenLexeme(nextToken, lexeme);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * a method to output the next token and its corresponding lexeme
     * @param token The next token
     * @param lexArr the lexeme that the token represents
     */
    private static void printNextTokenLexeme(Token token, char[] lexArr) {
        System.out.printf("Next token is: %-15s %s %s\n", token, "Next lexeme is ",
                stringifyArray(lexArr));
    }

    /**
     * addChar - a function to add nextChar to lexeme
     */
    private static void addChar() {
        if (lexLen <= MAX_LEXEME_SIZE-2) {
            lexeme[lexLen++] = nextChar;
            lexeme[lexLen] = 0;
        } else {
            System.out.println("Error - lexeme is too long.");
        }
    }

    /**
     * getChar - a function to get the next character of
     *  input and determine its character class
     */
    private static void getChar() {
        try {
            nextChar = getNextCharFrom(fileData);
        } catch (StringIndexOutOfBoundsException e) {
            charClass = Token.END_OF_FILE.getId();
            return;
        }
        if (Character.isAlphabetic(nextChar)) {
            charClass = LETTER;
        } else if (Character.isDigit(nextChar)) {
            charClass = DIGIT;
        } else charClass = UNKNOWN;
    }

    private static char getNextCharFrom(String fileData) {
        char nextChar = fileData.charAt(index);
        index++;
        return nextChar;
    }

    /**
     * getNonBlank - a function to call getChar until it
     * returns a non-whitespace character
     */
    private static void getNonBlank() {
        while (Character.isSpaceChar(nextChar)) {
            getChar();
        }
    }

    /**
     *  lex - a simple lexical lexicalAnalyzerPackage.LexicalAnalyzer for arithmetic
     *  expressions
     */
    public static Token lex() {
//    public static int lex() {
        lexeme = new char[MAX_LEXEME_SIZE]; //make sure to reset lexeme to empty before you try to fill it again
        lexLen = 0;
        getNonBlank();
        switch (charClass) {
            /* Parse identifiers */
            case LETTER:
                addChar();
                getChar();
                while (charClass == LETTER || charClass == DIGIT) {
                    addChar();
                    getChar();
                }
                nextToken = Token.IDENT;
                break;

            /* Parse integer literals */
            case DIGIT:
                addChar();
                getChar();
                while (charClass == DIGIT) {
                    addChar();
                    getChar();
                }
                nextToken = Token.INT_LIT;
                break;

            /* Parentheses and operators */
            case UNKNOWN:
                lookup(nextChar);
                getChar();
                break;

            /* EOF */
            case EOF:
                nextToken = Token.END_OF_FILE;
                lexeme[0] = 'E';
                lexeme[1] = 'O';
                lexeme[2] = 'F';
                lexeme[3] = 0;
                break;
        } /* End of switch */

        if(nextToken != Token.END_OF_FILE) {
            printNextTokenLexeme(nextToken, lexeme);
        }

        return nextToken;
    }   /* End of function lex */

    /**
     * lookup - a function to lookup operators and parentheses
     * and return the token
     */
    private static Token lookup(char ch) {
        switch (ch) {
            case '(':
                addChar();
                nextToken = Token.LEFT_PAREN;
                break;
            case ')':
                addChar();
                nextToken = Token.RIGHT_PAREN;
                break;
            case '+':
                addChar();
                nextToken = Token.ADD_OP;
                break;
            case '-':
                addChar();
                nextToken = Token.SUB_OP;
                break;
            case '*':
                addChar();
                nextToken = Token.MULT_OP;
                break;
            case '/':
                addChar();
                nextToken = Token.DIV_OP;
                break;
            case '=':
                addChar();
                nextToken = Token.ASSIGN_OP;
                break;
            default:
                addChar();
                nextToken = Token.END_OF_FILE;
                break;
        }
        return nextToken;
    }

    private static String stringifyArray(char[] arr) {
        String s = "";
        for (char a : arr) {
            s += a;
        }
        return s;
    }

    /***************************************
     ********** PARSER CODE BELOW **********
     ***************************************/

    /**
     * assign
     * parses strings in the language generated by the rule:
     * <assign> -> id = <expr>
     */
    private static void assign() {
        System.out.println("Enter <assign>");

        lex();
        expr();

        System.out.println("Exit <assign>");
    }

    /**
     * expr
     * parses strings in the language generated by the rule:
     * <expr> -> <term> {(+ | -) <term>}
     */
    private static void expr() {
        System.out.println("Enter <expr>");

        // parse the first term:
        term();

        // as long as the next token is + or -,
        // get the next token and parse the next term:
        while (nextToken == Token.ADD_OP || nextToken == Token.SUB_OP) {
            lex();
            term();
        }

        System.out.println("Exit <expr>");
    }

    /**
     * term
     * parses strings in the language generated by the rule:
     * <term> -> <factor> {(* | /) <factor>}
     */
    private static void term() {
        System.out.println("Enter <term>");

        // parse the first factor
        factor();

        // as long as the next token is * or /,
        // get the next token and parse the next factor
        while (nextToken == Token.MULT_OP || nextToken == Token.DIV_OP) {
            lex();
            factor();
        }

        System.out.println("Exit <term>");
    }

    /**
     * factor
     * parses strings in the language generated by the rule:
     * <factor> -> id | int_constant | ( <expr> )
     */
    private static void factor() {
        System.out.println("Enter <factor>");

        // determine which RHS
        if (nextToken == Token.IDENT || nextToken == Token.INT_LIT) {
            // get the next token
            lex();
        }

        // If the RHS is ( <expr> ),
        // call lex to pass over the left parenthesis,
        // call expr, and check for the right parenthesis
        else {
            if (nextToken == Token.LEFT_PAREN) {
                lex();
                expr();
                if (nextToken == Token.RIGHT_PAREN) {
                    lex();
                } else {
                    error();
                }
            }

            // it was not an id, an integer literal, or a left parenthesis:
            else {
                error();
            }
        }

        System.out.println("Exit <factor>");
    }

    /**
     * error
     * Handles improper syntax with a message
     */
    private static void error() {
        errorState = true;
        System.out.println("There is a syntax error here.");
    }
}

enum Token {
    INT_LIT(10),
    IDENT(11),
    ASSIGN_OP(20),
    ADD_OP(21),
    SUB_OP(22),
    MULT_OP(23),
    DIV_OP(24),
    LEFT_PAREN(25),
    RIGHT_PAREN(26),
    END_OF_FILE(100);

    Token(int id) {
        this.id = id;
    }

    private int id;
    public int getId() {
        return this.id;
    }

    }
