package ws1415.common.model;

/**
 * Speichert eine Liste der möglichen Rollen, die bei einem Event angenommen werden können. Dies ist
 * ein Duplikat der Enum die auf dem Server existiert, ohne die enthaltenen Privilegien. Dieses
 * Duplikat dient lediglich der zentralen Definition der Rollen innerhalb des common-Moduls.
 * @author Richard Schulze
 */
public enum EventRole {
    HOST,
    MEDIC,
    MARSHALL,
    PARTICIPANT
}
