package dev.zenologia.zrestart.api;

/**
 * Identifies how a restart countdown was created.
 */
public enum RestartKind {
    /**
     * A restart countdown created from the configured automatic schedule.
     */
    SCHEDULED,

    /**
     * A restart countdown created by an administrator with {@code /zrestart now}.
     */
    MANUAL
}
