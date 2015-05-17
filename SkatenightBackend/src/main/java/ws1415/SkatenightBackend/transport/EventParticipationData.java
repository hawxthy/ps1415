package ws1415.SkatenightBackend.transport;

import java.util.Map;

import ws1415.SkatenightBackend.model.Event;
import ws1415.SkatenightBackend.model.EventRole;

/**
 * Trasportklasse, die ausschließlich die ID und die Mail-Adressen der teilnehmenden Benutzer inkl.
 * Rollen überträgt.
 * @author Richard Schulze
 */
public class EventParticipationData {
    private Long id;
    private Map<String, EventRole> memberList;

    public EventParticipationData(Event event) {
        this.id = event.getId();
        this.memberList = event.getMemberList();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, EventRole> getMemberList() {
        return memberList;
    }

    public void setMemberList(Map<String, EventRole> memberList) {
        this.memberList = memberList;
    }
}
