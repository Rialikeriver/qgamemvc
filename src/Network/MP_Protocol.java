package Network;

/**
 * Central protocol definition for multiplayer messages.
 * Format: TYPE@SENDER@PAYLOAD
 */
public class MP_Protocol {

    public static final String SEP = "@";

    // Core message types
    public static final String CHAT      = "CHAT";      // CHAT@player@message
    public static final String JOIN      = "JOIN";      // JOIN@player@""
    public static final String LEAVE     = "LEAVE";     // LEAVE@player@""
    public static final String READY     = "READY";     // READY@player@OK

    // Game flow
    public static final String START     = "START";     // START@HOST@""
    public static final String QUESTION  = "QUESTION";  // QUESTION@HOST@payload
    public static final String ANSWER    = "ANSWER";    // ANSWER@player@A/B/C/D
    public static final String TIMER     = "TIMER";     // TIMER@HOST@seconds
    public static final String BOARD     = "BOARD";     // BOARD@HOST@payload
    public static final String WIN       = "WIN";       // WIN@HOST@payload
    public static final String LIFELINE  = "LIFELINE";  // LIFELINE@HOST@type|data|usedBy
    public static final String SCOREBOARD_TIMER = "SCOREBOARD_TIMER";  // SCOREBOARD_TIMER@HOST@secondsRemaining
    public static final String CONTINUE  = "CONTINUE";  // CONTINUE@playerName@
    public static final String PLAYER_LIST = "PLAYER_LIST"; // PLAYER_LIST@HOST@name,color,connected,ready,earnings|...

    // Error / status
    public static final String ERROR     = "ERROR";     // ERROR@HOST@message;

    public static String format(String type, String sender, String payload) {
        return type + SEP + sender + SEP + (payload == null ? "" : payload);
    }

    public static String[] parse(String rawMessage) {
        String[] parts = rawMessage.split(SEP, 3);
        String[] out = new String[3];
        out[0] = parts.length > 0 ? parts[0] : "";
        out[1] = parts.length > 1 ? parts[1] : "";
        out[2] = parts.length > 2 ? parts[2] : "";
        return out;
    }
}
