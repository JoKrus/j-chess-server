package de.djgames.jonas.jcom2.server.logic.map;

import com.google.common.collect.HashBiMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameMap {
    static final char[] validChars =
            StringUtils.join(Arrays.stream(MapObject.ObjectType.values()).map(o -> o.charValue)
                    .collect(Collectors.toList()), "").toCharArray();
    static final String regexMatcher = regexMatcher();

    private MapObject[] objectList;
    private int length;
    private int height;
    //array[x * leny + y] anstatt

    private final HashBiMap<Coordinate, Character> characterHashBiMap = HashBiMap.create();

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

    private void initalize(String mapString) {
        this.height = mapString.split("\n").length;
        mapString = mapString.replaceAll(regexMatcher, "");
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
