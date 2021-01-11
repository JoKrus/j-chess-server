package de.djgames.jonas.jcom2.server.logic.map;

import de.djgames.jonas.jcom2.server.generated.PositionData;

import java.util.Objects;

public class Coordinate {
    public final int x, y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(PositionData positionData) {
        this(positionData.getPosX(), positionData.getPosY());
    }

    public PositionData toPositionData() {
        PositionData positionData = new PositionData();
        positionData.setPosX(this.x);
        positionData.setPosY(this.y);
        return positionData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return this.x == that.x &&
                this.y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}
