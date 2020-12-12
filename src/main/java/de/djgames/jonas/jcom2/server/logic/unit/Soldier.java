package de.djgames.jonas.jcom2.server.logic.unit;

import de.djgames.jonas.jcom2.server.generated.Team;
import de.djgames.jonas.jcom2.server.generated.UnitData;

public class Soldier extends UnitData {
    private Team team;

    public Soldier(UnitData unitData, Team team) {
        super();
        this.team = team;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
