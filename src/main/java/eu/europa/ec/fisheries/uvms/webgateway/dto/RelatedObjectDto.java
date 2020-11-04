package eu.europa.ec.fisheries.uvms.webgateway.dto;

import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;

import java.util.HashMap;
import java.util.Map;

public class RelatedObjectDto {
    private Map<String, Note> notes = new HashMap<>();

    private Map<String, PollInfoDto> polls = new HashMap<>();

    private Map<String, MovementDto> positions = new HashMap<>();

    public Map<String, Note> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, Note> notes) {
        this.notes = notes;
    }

    public Map<String, PollInfoDto> getPolls() {
        return polls;
    }

    public void setPolls(Map<String, PollInfoDto> polls) {
        this.polls = polls;
    }

    public Map<String, MovementDto> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, MovementDto> positions) {
        this.positions = positions;
    }
}
