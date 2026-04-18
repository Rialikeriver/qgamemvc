package Network;

/**
 * Host-authoritative multiplayer player state.
 *
 * This class represents the authoritative state of a player in multiplayer.
 * It is intentionally simple and mutable so the host can update fields and
 * broadcast them to clients. Clients should treat received instances as
 * read-only snapshots.
 */
public class MP_PlayerState {

    public final String name;
    public String color;
    public boolean connected;
    public boolean ready;
    public int earnings;

    /**
     * Standard constructor for a newly-seen player.
     */
    public MP_PlayerState(String name) {
        this.name = name;
        this.connected = true;
        this.ready = false;
        this.earnings = 0;
    }

    /**
     * Copy constructor used when clients receive PLAYER_LIST updates.
     * Ensures clients do not mutate host-owned objects.
     */
    public MP_PlayerState(MP_PlayerState other) {
        this.name = other.name;
        this.color = other.color;
        this.connected = other.connected;
        this.ready = other.ready;
        this.earnings = other.earnings;
    }

    /**
     * Useful for debugging and logging.
     */
    @Override
    public String toString() {
        return "MP_PlayerState{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", connected=" + connected +
                ", ready=" + ready +
                ", earnings=" + earnings +
                '}';
    }
}
