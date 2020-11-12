package Parse;

import java.util.*;

public class Parse {
    private static final Map<String, Integer> TOKEN_MAP = new HashMap<String, Integer>();
    static{
        TOKEN_MAP.put("{", 0);
        TOKEN_MAP.put("}", 1);
        TOKEN_MAP.put("System.out.println", 2);
        TOKEN_MAP.put("(", 3);
        TOKEN_MAP.put(")", 4);
        TOKEN_MAP.put(";", 5);
        TOKEN_MAP.put("if", 6);
        TOKEN_MAP.put("else", 7);
        TOKEN_MAP.put("while", 8);
        TOKEN_MAP.put("true", 9);
        TOKEN_MAP.put("false", 10);
        TOKEN_MAP.put("!", 11);
    };

    private static final List<String> TERMINALS = new ArrayList<>(
            Arrays.asList(
                    "{",
                    "}",
                    "System.out.println",
                    "(",
                    ")",
                    ";",
                    "if",
                    "else",
                    "while",
                    "true",
                    "false",
                    "!"
            )
    );

    private static final int BR_OPEN = 0, BR_CLO = 1, PRINT = 2, P_OPEN = 3, P_CLO = 4,
            SEMI = 5, IF = 6, ELSE = 7, WHILE = 8, TRUE = 9, FALSE = 10, BANG = 11, EOF = 12;

    private int tok;
    private int err_flag = 0;

    public static void main(String[] args) {
        // Setup scanner and tokens list
        Scanner sc = new Scanner(System.in);
        List<String> tokenStrings = new ArrayList();
        ArrayList<Integer> tokens = new ArrayList<>();
        String program = "";

        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            program = program + s + "\n";
            StringTokenizer st = new StringTokenizer(s, " !(){}", true);
            while (st.hasMoreTokens()) {
                String next = st.nextToken();
                if (!TERMINALS.contains(next)){
                    if (!next.trim().isEmpty()) {
                        System.out.println("Parse.Parse error");
                        return;
                    }
                } else {
                    tokenStrings.add(next);
                }
            }
        }

        for (String token : tokenStrings) {
            tokens.add(TOKEN_MAP.get(token));
        }
        tokens.add(EOF);

        //System.out.println(tokenStrings);
        //System.out.println(tokens);

        // PARSER
        // Parse.Parse tokens
        Parser parser = new Parser(tokens);
        int out = parser.parse();

        if (out == 0) {
            System.out.println("Program parsed successfully");
        } else {
            System.out.println("Parse.Parse error");
        }
    }


}
