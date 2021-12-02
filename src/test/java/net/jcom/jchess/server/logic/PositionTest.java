package net.jcom.jchess.server.logic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionTest {
    public static Stream<String> fenTestData() {
        return Stream.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    @ParameterizedTest
    @MethodSource(value = "fenTestData")
    void fenCtorTest(String argument) {
        Position position = new Position(argument);
        assertEquals(argument, position.toFenNotation());
    }

    @ParameterizedTest
    @MethodSource(value = "fenTestData")
    void fenCopyCtorTest(String argument) {
        Position position = new Position(argument);
        Position copyPosition = new Position(position);
        assertEquals(argument, copyPosition.toFenNotation());
    }
}
