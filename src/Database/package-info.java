/**
 * Persistence utilities for loading structured game data. This package
 * provides JSON‑based deserialization for the Quantum Millionaire question
 * bank and supports alternate language files and admin‑edited datasets.
 *
 * <p>The package is intentionally minimal and focused:</p>
 *
 * <ul>
 *   <li>{@link Database.QuestionLoader} — central utility for reading
 *       question lists from JSON files on the classpath. It supports both
 *       default and language‑specific question sets.</li>
 * </ul>
 *
 * <p>All JSON parsing is performed using Jackson, and the resulting
 * {@link Pack_1.Question} objects are consumed by gameplay models,
 * admin tools, and profile restoration logic. The package does not
 * perform any validation or game logic; it simply provides structured
 * data to higher‑level components.</p>
 */
package Database;
