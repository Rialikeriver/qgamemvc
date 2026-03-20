package Pack_1;

/**
 * Exception type used for game‑related errors in the Quantum Millionaire
 * subsystem. This class exists to satisfy the A4 specification requirement
 * for a custom checked exception and provides a consistent prefix for all
 * game‑level error messages.
 *
 * <p>The constructor prepends a standardized label to the supplied message
 * so that all thrown errors appear with a unified “Quantum Error” prefix
 * when surfaced to logs or UI layers.</p>
 */
public class QMillionaireException extends Exception {

    /**
     * Creates a new exception with a standardized prefix applied to the
     * provided message. The constructor name must match the class name
     * per assignment requirements.
     *
     * @param message the specific error description
     */
    public QMillionaireException(String message) {
        super("Quantum Error: " + message);
    }
}
