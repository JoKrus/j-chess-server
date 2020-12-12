package de.djgames.jonas.jcom2.server.logic.map;

import de.djgames.jonas.jcom2.server.generated.Team;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class MapObject {
    private final ObjectType type;

    public MapObject(char c) {
        this.type = ObjectType.getTypeByChar(c);
    }

    public ObjectType getType() {
        return this.type;
    }

    public enum ObjectType {
        RED_SPAWN('R', Team.RED), BLUE_SPAWN('B', Team.BLUE), EMPTY('-'), HALF_COVER('H'), COVER('C'), CONTROL_POINT('P');

        public final char charValue;
        private Team team = null;

        ObjectType(char charValue) {
            this.charValue = charValue;
        }

        ObjectType(char charValue, Team team) {
            this(charValue);
            this.team = team;
        }

        public Team getTeam() {
            return this.team;
        }

        public static ObjectType getTypeByChar(char c) {
            switch (c) {
                case 'R':
                    return RED_SPAWN;
                case 'B':
                    return BLUE_SPAWN;
                case '-':
                    return EMPTY;
                case 'H':
                    return HALF_COVER;
                case 'C':
                    return COVER;
                case 'P':
                    return CONTROL_POINT;
                default:
                    logger.error("Could not map char " + c + " to a valid MapObject.");
                    return EMPTY;
            }
        }
    }
}
