package de.djgames.jonas.jcom2.server.logic.unit;

import de.djgames.jonas.jcom2.server.generated.Team;
import de.djgames.jonas.jcom2.server.generated.UnitData;

public class Soldier extends UnitData {
    private Team team;

    public Soldier(UnitData unitData, Team team) {
        super();
        this.armor = unitData.getArmor();
        this.healthPoints = unitData.getHealthPoints();
        this.id = unitData.getId();
        this.movementPoints = unitData.getMovementPoints();
        this.name = unitData.getName();
        this.primaryWeapon = unitData.getPrimaryWeapon();
        this.visionDistance = unitData.getVisionDistance();
        this.team = team;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
