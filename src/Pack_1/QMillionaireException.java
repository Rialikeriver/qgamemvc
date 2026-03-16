package Pack_1;

/**
 * Custom Exception class as required by A4 specs.
 * Used to encapsulate game-related errors.
 */
public class QMillionaireException extends Exception {
    // The constructor name MUST match the class name
    public QMillionaireException(String message) {
        super("Quantum Error: " + message);
    }
}