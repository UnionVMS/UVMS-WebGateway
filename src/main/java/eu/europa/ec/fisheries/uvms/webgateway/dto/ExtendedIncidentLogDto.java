package eu.europa.ec.fisheries.uvms.webgateway.dto;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.uvms.asset.client.model.Note;
import eu.europa.ec.fisheries.uvms.incident.model.dto.IncidentLogDto;
import eu.europa.ec.fisheries.uvms.movement.client.model.MicroMovement;

import java.util.HashMap;
import java.util.Map;

public class ExtendedIncidentLogDto {

    private Map<Long, IncidentLogDto> incidentLogs;

    private Map<String, Note> notes = new HashMap<>();

    private Map<String, ExchangeLogStatusType> polls = new HashMap<>();

    private Map<String, MicroMovement> positions = new HashMap<>();

    public ExtendedIncidentLogDto() {
        incidentLogs = new HashMap<>();
    }

    public ExtendedIncidentLogDto(int amountOfLogs) {
        incidentLogs = new HashMap<>(amountOfLogs);
    }

    public Map<Long, IncidentLogDto> getIncidentLogs() {
        return incidentLogs;
    }

    public void setIncidentLogs(Map<Long, IncidentLogDto> incidentLogs) {
        this.incidentLogs = incidentLogs;
    }

    public Map<String, Note> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, Note> notes) {
        this.notes = notes;
    }

    public Map<String, ExchangeLogStatusType> getPolls() {
        return polls;
    }

    public void setPolls(Map<String, ExchangeLogStatusType> polls) {
        this.polls = polls;
    }

    public Map<String, MicroMovement> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, MicroMovement> positions) {
        this.positions = positions;
    }
}
