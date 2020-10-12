import java.util.*;

public class Parse {
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

    public static void main(String[] args) {
        // Setup scanner and tokens list
        Scanner sc = new Scanner(System.in);
        List<String> tokens = new ArrayList();
        String program = "";

        // SCANNER
        // tokenize entire program
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            program = program + s + "\n";
            StringTokenizer st = new StringTokenizer(s, " !(){}", true);
            while (st.hasMoreTokens()) {
                String next = st.nextToken();
                if (!TERMINALS.contains(next)){
                    if (!next.trim().isEmpty()) {
                        System.out.println("Parse error");
                        return;
                    }
                } else {
                    tokens.add(next);
                }
            }
        }

        // PARSER
        // Parse tokens

    }
}
