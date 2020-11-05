package eu.europa.ec.fisheries.uvms.webgateway.dto;

import eu.europa.ec.fisheries.schema.exchange.v1.ExchangeLogStatusType;
import eu.europa.ec.fisheries.uvms.asset.client.model.SanePollDto;
import eu.europa.ec.fisheries.uvms.asset.client.model.mt.MobileTerminal;
import eu.europa.ec.fisheries.uvms.movement.model.dto.MovementDto;

public class PollInfoDto {

    SanePollDto pollInfo;

    ExchangeLogStatusType pollStatus;

    MovementDto movement;

    MobileTerminal mobileTerminalSnapshot;

    public PollInfoDto() {
    }

    public PollInfoDto(SanePollDto pollInfo, ExchangeLogStatusType pollStatus, MovementDto movement, MobileTerminal mt) {
        this.pollInfo = pollInfo;
        this.pollStatus = pollStatus;
        this.movement = movement;
        mobileTerminalSnapshot = mt;
    }

    public SanePollDto getPollInfo() {
        return pollInfo;
    }

    public void setPollInfo(SanePollDto pollInfo) {
        this.pollInfo = pollInfo;
    }

    public ExchangeLogStatusType getPollStatus() {
        return pollStatus;
    }

    public void setPollStatus(ExchangeLogStatusType pollStatus) {
        this.pollStatus = pollStatus;
    }

    public MovementDto getMovement() {
        return movement;
    }

    public void setMovement(MovementDto movement) {
        this.movement = movement;
    }

    public MobileTerminal getMobileTerminalSnapshot() {
        return mobileTerminalSnapshot;
    }

    public void setMobileTerminalSnapshot(MobileTerminal mobileTerminalSnapshot) {
        this.mobileTerminalSnapshot = mobileTerminalSnapshot;
    }
}
