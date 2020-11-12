package Parse;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static final int BR_OPEN = 0, BR_CLO = 1, PRINT = 2, P_OPEN = 3, P_CLO = 4,
            SEMI = 5, IF = 6, ELSE = 7, WHILE = 8, TRUE = 9, FALSE = 10, BANG = 11, EOF = 12;

    public Parser(ArrayList<Integer> tokens){
        this.tokens = tokens;
    }

    public int parse() {
        tok = getToken();
        S();

        if (!tokens.isEmpty()) err_flag = 1;

        return err_flag;
    }

    private int err_flag = 0;
    private int tok;
    private List<Integer> tokens;

    private void error(){
        err_flag = 1;
    }

    private int getToken() {
        return tokens.remove(0);
    }

    private void advance() {
        tok = getToken();
    }

    private void eat(int t) {
        if (tok == t) {
            advance();
        } else {
            error();
        }
    }

    private void S() {
        switch (tok) {
            case BR_OPEN:
                eat(BR_OPEN);
                L();
                T_prime();
                break;
            case PRINT:
                eat(PRINT);
                eat(P_OPEN);
                E();
                F_prime();
                Z_prime();
                break;
            case IF:
                eat(IF);
                eat(P_OPEN);
                E();
                G_prime();
                break;
            case WHILE:
                eat(WHILE);
                eat(P_OPEN);
                E();
                X_prime();
                break;
            default:
                error();
        }
    }

    private void L() {
        switch (tok) {
            case BR_OPEN:
            case WHILE:
            case IF:
            case PRINT:
                S();
                L();
                break;
        }
    }

    private void E() {
        switch (tok) {
            case TRUE:
                eat(TRUE);
                break;
            case FALSE:
                eat(FALSE);
                break;
            case BANG:
                eat(BANG);
                E();
                break;
            default:
                error();
        }
    }

    private void B_prime() {
        switch (tok) {
            case ELSE:
                eat(ELSE);
                S();
                break;
            default:
                error();
        }
    }

    private void G_prime() {
        F_prime();
        S();
        B_prime();
    }

    private void T_prime() {
        switch (tok) {
            case BR_CLO:
                eat(BR_CLO);
                break;
            default:
                error();
        }
    }

    private void F_prime() {
        switch (tok) {
            case P_CLO:
                eat(P_CLO);
                break;
            default:
                error();
        }
    }

    private void Z_prime() {
        switch (tok) {
            case SEMI:
                eat(SEMI);
                break;
            default:
                error();
        }
    }

    private void X_prime() {
        F_prime();
        S();
    }
}
