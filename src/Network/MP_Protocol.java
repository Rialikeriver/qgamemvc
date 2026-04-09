package Network;

/**
 * Handles message formatting and parsing based on the @ separator protocol.
 * Follows the pattern: [Message Type]@[Sender/ID]@[Content]
 */
public class MP_Protocol {
    public static final String SEPARATOR = "@";

    // Message Types
    public static final String CHAT = "CHAT";
    public static final String QUESTION = "QUESTION";
    public static final String TIMER = "TIMER";
    public static final String WIN = "WIN";
    public static final String PL_LEFT = "PL_STATUS";

    public static String format(String type, String sender, String content) {
        return type + SEPARATOR + sender + SEPARATOR + content;
    }

    public static String[] parse(String rawMessage) {
        return rawMessage.split(SEPARATOR);
    }
}