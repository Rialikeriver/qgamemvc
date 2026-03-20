/**
 * Core package for the Quantum Millionaire application. This package contains
 * the full MVC implementation for gameplay, profile management, admin tools,
 * and UI rendering. The package is structured around clear separation of
 * responsibilities:
 *
 * <ul>
 *   <li><b>Model classes</b> such as {@link Pack_1.QModel} and {@link Pack_1.Question}
 *       hold deterministic game state, prize logic, lifeline behavior, and
 *       question/answer structures.</li>
 *
 *   <li><b>View classes</b> such as {@link Pack_1.QView},
 *       {@link Pack_1.PlayerMenuView}, and {@link Pack_1.ProfileSelectionView}
 *       define all JavaFX UI layouts, styling hooks, and interactive elements.</li>
 *
 *   <li><b>Controller classes</b> such as {@link Pack_1.QController},
 *       {@link Pack_1.ProfileController}, and
 *       {@link Pack_1.QModeSelectionController} coordinate user actions,
 *       update models, and refresh views.</li>
 *
 *   <li><b>Application entry point</b> {@link Pack_1.QMillionaireMVC} manages
 *       navigation between screens, initializes persistent stores, restores
 *       user sessions, and applies global CSS themes.</li>
 *
 *   <li><b>Utility and support classes</b> such as
 *       {@link Pack_1.QSplash} and {@link Pack_1.QMillionaireException}
 *       provide startup behavior and standardized error handling.</li>
 * </ul>
 *
 * <p>The package follows a strict MVC pattern: models contain no UI logic,
 * views contain no game logic, and controllers act as the only bridge between
 * them. All UI components are styled through the shared <code>style.css</code>
 * file, and overlays (e.g., win/lose screens, popups) are layered using
 * {@link Pack_1.QView#showOverlay(javafx.scene.Node)}.</p>
 *
 * <p>This package forms the complete gameplay and admin experience for
 * Quantum Millionaire.</p>
 */
package Pack_1;
