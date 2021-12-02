package net.jcom.jchess.server.logic;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinateTest {


    public static Stream<Triple<Integer, Integer, String>> data() {
        return Stream.of(Triple.of(0, 0, "a8"),
                Triple.of(7, 0, "h8"),
                Triple.of(0, 7, "a1"),
                Triple.of(7, 7, "h1"),
                Triple.of(4, 4, "e4")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "data")
    void parseTest(Triple<Integer, Integer, String> arguments) {
        Coordinate correct = Coordinate.of(arguments.getLeft(), arguments.getMiddle());
        Coordinate test = Coordinate.parse(arguments.getRight());
        assertEquals(correct, test);
    }

    @ParameterizedTest
    @MethodSource(value = "data")
    void toStringTest(Triple<Integer, Integer, String> arguments) {
        String correct = arguments.getRight();
        String test = Coordinate.of(arguments.getLeft(), arguments.getMiddle()).toString();
        assertEquals(correct, test);
    }
}
