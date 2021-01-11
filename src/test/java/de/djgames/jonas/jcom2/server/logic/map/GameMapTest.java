package de.djgames.jonas.jcom2.server.logic.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GameMapTest {

    @Test
    public void validChars() {
        char[] expected = new char[]{'R', 'B', '-', 'H', 'C', 'P', 'F'};
        Assertions.assertArrayEquals(expected, GameMap.validChars);
    }

    @Test
    public void regexMatcher() {
        Assertions.assertEquals("[^\\QRB-HCPF\\E]", GameMap.regexMatcher);
    }

    @Test
    public void mapParsing() {
        String mapString = "R-HCPB";
        var map = new GameMap(mapString);
        Assertions.assertEquals(map.getMapObjectAt(0, 0).getType(), MapObject.ObjectType.RED_SPAWN);
        Assertions.assertEquals(map.getMapObjectAt(1, 0).getType(), MapObject.ObjectType.EMPTY);
        Assertions.assertEquals(map.getMapObjectAt(2, 0).getType(), MapObject.ObjectType.HALF_COVER);
        Assertions.assertEquals(map.getMapObjectAt(3, 0).getType(), MapObject.ObjectType.COVER);
        Assertions.assertEquals(map.getMapObjectAt(4, 0).getType(), MapObject.ObjectType.CONTROL_POINT);
        Assertions.assertEquals(map.getMapObjectAt(5, 0).getType(), MapObject.ObjectType.BLUE_SPAWN);
    }

    @Test
    public void regexTest1() {
        String mapString = "R-CHPB\n" +
                "R-HCPB";

        mapString = mapString.replaceAll(GameMap.regexMatcher, "");

        Assertions.assertEquals("R-CHPBR-HCPB", mapString);
    }

}
