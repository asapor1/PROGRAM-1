package compiler;

/*
COURSE: COSC455-102
Assignment: Program 1
Name: Wilson, Grace
Name: Anthony Sapporito


 */
import java.util.logging.Logger;

/*
 * GRAMMAR FOR PROCESSING SIMPLE SENTENCES:
 *
 * <SENTENCE> ::= <NOUN_PHRASE> <VERB_PHRASE> <NOUN_PHRASE> <PREP_PHRASE> <SENTENCE_TAIL> $$
 * <SENTENCE_TAIL> ::= <CONJ> <SENTENCE> | <EOS>
 *
 * <NOUN_PHRASE> ::= <ART> <ADJ_LIST> <NOUN>
 * <ADJ_LIST> ::= <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
 * <ADJ_TAIL> ::= <COMMA> <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
 *
 * <VERB_PHRASE> ::= <ADVERB> <VERB> | <VERB>
 * <PREP_PHRASE> ::= <PREPOSITION> <NOUN_PHRASE> | <<EMPTY>>
 *
 * // *** Terminal Productions (Actual terminals omitted, but they are just the
 * valid words in the language). ***
 *
 * <COMMA> ::= ','
 * <EOS> ::= '.' | '!'
 *
 * <ADJECTIVE> ::= ...adjective list...
 * <ADVERB> ::= ...adverb list...
 * <ART> ::= ...article list...
 * <CONJ> ::= ...conjunction list...
 * <NOUN> ::= ...noun list...
 * <PREPOSITION> ::= ...preposition list...
 * <VERB> ::= ...verb list....
 */

/**
 * The Syntax Analyzer.
 * <p>
 * ************** NOTE: REQUIRES JAVA 11 OR ABOVE! ******************
 */
public class Parser {

    // The lexer which will provide the tokens
    private final LexicalAnalyzer lexer;

    // The actual "code generator"
    private final CodeGenerator codeGenerator;

    /**
     * This is the constructor for the Parser class which
     * accepts a LexicalAnalyzer and a CodeGenerator object as parameters.
     *
     * @param lexer         The Lexer Object
     * @param codeGenerator The CodeGenerator Object
     */
    public Parser(LexicalAnalyzer lexer, CodeGenerator codeGenerator) {
        this.lexer = lexer;
        this.codeGenerator = codeGenerator;

        // Change this to automatically prompt to see the Open WebGraphViz dialog or not.
        MAIN.PROMPT_FOR_GRAPHVIZ = true;
    }

    /*
     * Since the "Compiler" portion of the code knows nothing about the start rule,
     * the "analyze" method must invoke the start rule.
     *
     * Begin analyzing...
     */
    void analyze() {
        try {
            // Generate header for our output
            TreeNode startNode = codeGenerator.writeHeader("PARSE TREE");

            // THIS IS OUR START RULE
            this.beginParsing(startNode);

            // generate footer for our output
            codeGenerator.writeFooter();

        } catch (ParseException ex) {
            final String msg = String.format("%s\n", ex.getMessage());
            Logger.getAnonymousLogger().severe(msg);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is just an intermediate method to make it easy to change the start rule of the grammar.
     *
     * @param parentNode The parent node for the parse tree
     * @throws ParseException If there is a syntax error
     */
    private void beginParsing(final TreeNode parentNode) throws ParseException {
        // Invoke the start rule.
        // TODO: Change if necessary!
        PROGRAM(parentNode);
    }
    
    
    // <PROGRAM> ::= <STMT_LIST>
    void PROGRAM(final TreeNode fromNode) throws ParseException {
        final var nodeName = codeGenerator.addNonTerminalToTree(fromNode);

        STMT_LIST(nodeName); //$$ is not important (top of stack)
    }

    // <STMT_LIST> ::= <STMT> <STMT_LIST> | EOS
    void STMT_LIST(final Object nodeName) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(nodeName);

        if (lexer.isCurrentToken(ID) || lexer.isCurrentToken(READ) || lexer.isCurrentToken(WRITE) || lexer.isCurrentToken(IF) || lexer.isCurrentToken(WHILE) || lexer.isCurrentToken(DO)) {
            STMT(treeNode);
            STMT_LIST(treeNode);
        }

        else {
            EMPTY(treeNode);
        }
    }

    // <STMT> ::= <ID> <(TOKEN_Equal)> <EXPR> | <READ ID> | <WRITE EXPR> | IF CONDITION THEN STMT_LIST | WHILE CONDITION DO STMT_LIST OD \ DO UNTIL??
    void STMT(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(ID)) {
            ID_TERMINAL(treeNode);
            EQUAL_TERMINAL(treeNode);
            EXPR(treeNode);
        }
       else if (lexer.isCurrentToken(READ)) {
            READ_TERMINAL(treeNode);
            ID_TERMINAL(treeNode);
        }
       else if (lexer.isCurrentToken(WRITE)){
           WRITE_TERMINAL(treeNode);
           EXPR(treeNode);
        }
       else if (lexer.isCurrentToken(IF)) {
            IF_TERMINAL(treeNode);
            CONDITION(treeNode);
            THEN_TERMINAL(treeNode);
            STMT_LIST(treeNode);
            FI_TERMINAL(treeNode);
        }
       else if (lexer.isCurrentToken(WHILE)){
           WHILE_TERMINAL(treeNode);
           CONDITION(treeNode);
           DO_TERMINAL(treeNode);
           STMT_LIST(treeNode);
           OD_TERMINAL(treeNode);
        } else if (lexer.isCurrentToken(DO)) {
          DO_TERMINAL(treeNode);
          STMT_LIST(treeNode);
          UNTIL_TERMINAL(treeNode);
          CONDITION(treeNode);
        }
        else raiseException("Stmt error", fromNode);

    }

    // <EXPR> ::= <TERM> <TERM_TAIL>
    void EXPR(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        TERM(treeNode);
        TERM_TAIL(treeNode);
    }

    // <TERM_TAIL> ::= <ADD_OP> <TERM> <TERM_TAIL> | <<EMPTY>>
    void TERM_TAIL(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(ADD)) {
            ADD_OP(treeNode);
            TERM(treeNode);
            TERM_TAIL(treeNode);
        } else {
            EMPTY(treeNode);
        }
    }

    // <TERM> ::= <FACTOR> <FACTOR_TAIL>
    void TERM(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        FACTOR(treeNode);
        FACTOR_TAIL(treeNode);
    }

    // <FACTOR_TAIL> ::= <MULT_OP> <FACTOR> <FACTOR_TAIL> | <<EMPTY>>
    void FACTOR_TAIL(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(MULT)) {
            MULT_OP(treeNode);
            FACTOR(treeNode);
            FACTOR_TAIL(treeNode);
        }
        else {
            EMPTY(treeNode);
        }
    }

    // <FACTOR> ::= <(> <EXPR> <)> | <ID> | <NUMBER>
    void FACTOR(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);

        if (lexer.isCurrentToken(PARA_OPEN)) {
            PARA_OPEN_TERMINAL(treeNode);
            EXPR(treeNode);
            PARA_CLOSE_TERMINAL(treeNode);
        }
        else if(lexer.isCurrentToken(ID)) {
            ID_TERMINAL(treeNode);
        }
        else if (lexer.isCurrentToken(NUMBER)){
            NUMBER_TERMINAL(treeNode);
        }
        else raiseException("Factor error", fromNode);
    }

    // <ADD_OP> ::= <+> | <->
    void ADD_OP(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);
        if (lexer.isCurrentToken(ADD)) {
            ADD_TERMINAL(treeNode);
        }
        else raiseException("ADD_OP error", fromNode);
        }


    // <MULT_OP> ::= <*> | </>
    void MULT_OP(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);
        if (lexer.isCurrentToken(MULT)){
            MULT_TERMINAL(treeNode);
        }
        else raiseException("Mult error", fromNode);
    }

    // <CONDITION> ::= <EXPR> <RELATION> <EXPR>
    void CONDITION(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);
        EXPR(treeNode);
        RELATION(treeNode);
        EXPR(treeNode);
    }

    // <RELATION> ::= <<> | <>> | <<=> | <=>> | <=> | <!=>
    void RELATION(final TreeNode fromNode) throws ParseException {
        final var treeNode = codeGenerator.addNonTerminalToTree(fromNode);
        RELATION_TERMINAL(treeNode);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // For the sake of completeness, each terminal-token has its own method,
    // though they all do the same thing here.  In a "REAL" program, each terminal
    // would likely have unique code associated with it.
    /////////////////////////////////////////////////////////////////////////////////////
    void EMPTY(final TreeNode fromNode) throws ParseException {
        codeGenerator.addEmptyToTree(fromNode);
    }

    // <RELATION_TERMINAL>
    void RELATION_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(RELATION)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Relation Error", fromNode);
        }
    }

    // <READ_TERMINAL>
    void READ_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(READ)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Read Error", fromNode);
        }
    }

    // <WRITE_TERMINAL>
    void WRITE_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(WRITE)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Write Error", fromNode);
        }
    }

    // <ID_TERMINAL>
    void ID_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(ID)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("ID Error", fromNode);
        }
    }

    // <IF_TERMINAL>
    void IF_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(IF)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("IF Error", fromNode);
        }
    }

    // <WHILE_TERMINAL>
    void WHILE_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(WHILE)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("While Error", fromNode);
        }
    }

    // <DO_TERMINAL>
    void DO_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(DO)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Do Error", fromNode);
        }
    }

    // <ADD_TERMINAL>
    void ADD_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(ADD)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Add Error", fromNode);
        }
    }

    // <MULT_TERMINAL>
    void MULT_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(MULT)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Mult Error", fromNode);
        }
    }

    // <PARA_OPEN_TERMINAL>
    void PARA_OPEN_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(PARA_OPEN)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Para_open Error", fromNode);
        }
    }

    // <PARA_CLOSE_TERMINAL>
    void PARA_CLOSE_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(PARA_CLOSE)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Para_close Error", fromNode);
        }
    }

    // <EQUAL_TERMINAL>
    void EQUAL_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(EQUAL)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Equal Error", fromNode);
        }
    }

    // <FI_TERMINAL>
    void FI_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(FI)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("FI Error", fromNode);
        }
    }

    // <OD_TERMINAL>
    void OD_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(OD)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("OD Error", fromNode);
        }
    }

    // <THEN_TERMINAL>
    void THEN_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(THEN)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Then Error", fromNode);
        }
    }

    void UNTIL_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(UNTIL)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Until Error", fromNode);
        }
    }

    // <EOS>
    void EOS_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(EOS)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("an End of Sentence", fromNode);
        }
    }

    // <EOF>
    void EOF_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(EOF)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("End of file", fromNode);
        }
    }

    // <OTHER>
    void OTHER_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(OTHER)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Other Error", fromNode);
        }
    }

    // <NUMBER>
    void NUMBER_TERMINAL(final TreeNode fromNode) throws ParseException {
        if (lexer.isCurrentToken(NUMBER)) {
            addTerminalAndAdvanceToken(fromNode);
        } else {
            raiseException("Number Error", fromNode);
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a an EMPTY terminal node (result of an Epsilon Production) to the parse tree.
     * Mainly, this is just done for better visualizing the complete parse tree.
     *
     * @param parentNode The parent of the terminal node.
     */
    void EMPTY(final TreeNode parentNode) {
        codeGenerator.addEmptyToTree(parentNode);
    }

    /**
     * Match the current token with the expected token.
     * If they match, add the token to the parse tree, otherwise throw an exception.
     *
     * @param parentNode    The parent of the terminal node.
     * @param expectedToken The token to be matched.
     * @throws ParseException Thrown if the token does not match the expected token.
     */
    void MATCH(final TreeNode parentNode, final Token expectedToken) throws ParseException {
        final Token currentToken = lexer.currentToken();

        if (currentToken == expectedToken) {
            var currentLexeme = lexer.getCurrentLexeme();
            this.addTerminalToTree(parentNode, currentToken, currentLexeme);
            lexer.advanceToken();
        } else {
            this.raiseException(expectedToken, parentNode);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a terminal node to the parse tree.
     *
     * @param parentNode    The parent of the terminal node.
     * @param currentToken  The token to be added.
     * @param currentLexeme The lexeme of the token beign added.
     * @throws ParseException Throws a ParseException if the token cannot be added to the tree.
     */
    void addTerminalToTree(final TreeNode parentNode, final Token currentToken, final String currentLexeme) throws ParseException {
        var nodeLabel = "<%s>".formatted(currentToken);
        var terminalNode = codeGenerator.addNonTerminalToTree(parentNode, nodeLabel);

        codeGenerator.addTerminalToTree(terminalNode, currentLexeme);
    }

    /**
     * Raise a ParseException if the input cannot be parsed as defined by the grammar.
     *
     * @param expected   The expected token
     * @param parentNode The token's parent node
     */
    private void raiseException(Token expected, TreeNode parentNode) throws ParseException {
        final var template = "SYNTAX ERROR: '%s' was expected but '%s' was found.";
        final var errorMessage = template.formatted(expected.name(), lexer.getCurrentLexeme());
        codeGenerator.syntaxError(errorMessage, parentNode);
    }
}
