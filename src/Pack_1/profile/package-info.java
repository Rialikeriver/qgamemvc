/**
 * Persistence and user‑management subsystem for Quantum Millionaire.
 * This package contains all classes responsible for storing, loading,
 * and tracking player profiles, game statistics, and session state.
 *
 * <p>The subsystem is designed around a clean separation of concerns:</p>
 *
 * <ul>
 *   <li><b>User data model</b> — {@link Pack_1.profile.User} stores all
 *       persistent player attributes, including progress, lifeline usage,
 *       money totals, and win/loss records.</li>
 *
 *   <li><b>Session management</b> — {@link Pack_1.profile.Session} tracks
 *       the currently active user during gameplay and allows controllers
 *       to restore progress or update statistics mid‑game.</li>
 *
 *   <li><b>Persistence layer</b> — {@link Pack_1.profile.JsonUserStore}
 *       and {@link Pack_1.profile.JsonStatsStore} handle JSON‑based
 *       serialization of user profiles and cumulative statistics.</li>
 *
 *   <li><b>Coordinator</b> — {@link Pack_1.profile.UserManager} provides
 *       a unified API for creating users, deleting users, updating progress,
 *       recording game results, and saving all data to disk.</li>
 * </ul>
 *
 * <p>The package is intentionally UI‑agnostic. Controllers in
 * <code>Pack_1</code> interact with this subsystem through
 * {@link Pack_1.profile.UserManager} and {@link Pack_1.profile.Session},
 * ensuring that profile persistence remains consistent across admin tools,
 * player menus, and gameplay.</p>
 *
 * <p>All data is stored in JSON format to support easy inspection and
 * modification during development, while maintaining a stable schema for
 * long‑term persistence.</p>
 */
package Pack_1.profile;
