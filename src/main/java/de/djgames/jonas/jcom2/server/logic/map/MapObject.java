package de.djgames.jonas.jcom2.server.logic.map;

import de.djgames.jonas.jcom2.server.generated.Team;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

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
        RED_SPAWN('R', Team.RED), BLUE_SPAWN('B', Team.BLUE), EMPTY('-'), HALF_COVER('H'), COVER('C'),
        CONTROL_POINT('P'), FOG('F');

        private static final Map<Character, ObjectType> map;

        static {
            map = Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(ot -> ot.charValue,
                    ot -> ot)));
        }

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
            ObjectType result = map.get(c);
            if (result == null) {
                logger.error("Could not map char " + c + " to a valid MapObject.");
                return EMPTY;
            }
            return result;
        }
    }
}
