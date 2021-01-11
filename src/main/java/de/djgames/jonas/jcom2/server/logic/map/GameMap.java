package de.djgames.jonas.jcom2.server.logic.map;

import com.google.common.collect.HashBiMap;
import de.djgames.jonas.jcom2.server.generated.*;
import de.djgames.jonas.jcom2.server.logic.unit.LogicHelpers;
import de.djgames.jonas.jcom2.server.logic.unit.Soldier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameMap {
    static final char[] validChars =
            StringUtils.join(Arrays.stream(MapObject.ObjectType.values()).map(o -> o.charValue)
                    .collect(Collectors.toList()), "").toCharArray();
    static final String regexMatcher = regexMatcher();

    public PointData pointData;

    private MapObject[] objectList;
    private int length;
    private int height;
    private String oneDimensionalMapString;
    //array[x * leny + y] anstatt

    private final HashBiMap<Coordinate, Soldier> soldierPositionBiMap = HashBiMap.create();

    public GameMap(InputStream is) throws IOException {
        this(IOUtils.toString(is, StandardCharsets.UTF_8));
    }

    public GameMap(String mapString) {
        initalize(mapString);
    }

    public MapObject getMapObjectAt(int x, int y) {
        return this.getMapObjectAt(new Coordinate(x, y));
    }

    public MapObject getMapObjectAt(Coordinate coordinate) {
        return this.objectList[coordinate.y * this.length + coordinate.x];
    }

    public boolean spawnSoldier(Team team, Coordinate position, UUID soldierId) {
        //needs to be a Spawn
        if (team == null || getMapObjectAt(position).getType().getTeam() != team) {
            return false;
        }

        var unitData = LogicHelpers.generateDefaultUnit(soldierId, team.value() + soldierId.toString());

        Soldier soldier = new Soldier(unitData, team);

        var prevSoldier = this.soldierPositionBiMap.putIfAbsent(position, soldier);
        if (prevSoldier != null) {
            return false;
        }
        return true;
    }

    public GameMapData toGameMapData() {
        var ret = new GameMapData();
        ret.setHeight(this.height);
        ret.setLength(this.length);
        ret.setMapString(this.oneDimensionalMapString);
        return ret;
    }

    public Map<UnitData, PositionData> getSoldiersOfTeam(Team team) {
        return soldierPositionBiMap.values().stream().filter(soldier -> soldier.getTeam() == team).
                collect(Collectors.toMap(soldier -> soldier,
                        soldier -> this.soldierPositionBiMap.inverse().get(soldier).toPositionData()));
    }

    private void initalize(String mapString) {
        this.height = mapString.split("\n").length;
        mapString = mapString.replaceAll(regexMatcher, "");
        this.oneDimensionalMapString = mapString;
        this.objectList = new MapObject[mapString.length()];
        this.length = mapString.length() / this.height;
        char[] charArray = mapString.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];
            this.objectList[i] = new MapObject(c);
        }
    }

    private static String regexMatcher() {
        StringBuilder regexBuilder = new StringBuilder();
        for (char c : validChars) {
            regexBuilder.append(c);
        }
        return String.format("[^%s]", Pattern.quote(regexBuilder.toString()));
    }
}
