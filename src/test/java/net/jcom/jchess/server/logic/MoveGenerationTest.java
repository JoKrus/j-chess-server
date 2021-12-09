package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.generated.MoveData;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveGenerationTest {

    public static Stream<Triple<String, Integer, Long>> moveGenTestData() {
        return Stream.of(
                Triple.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 1, 20L),
                Triple.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2, 400L),
//                Triple.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 3, 8_902L),
                Triple.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 1, 48L),
                Triple.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 2, 2_039L),
//                Triple.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97_862L),
                Triple.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/1R2K2R b Kkq - 1 1", 2, 1_969L),
                Triple.of("r3k2r/p2pqpb1/bn2pnp1/2pPN3/1p2P3/2N2Q1p/PPPBBPPP/1R2K2R w Kkq c6 0 2", 1, 47L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 1, 44L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 2, 1_486L),
//                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62_379L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/P7/1PP1NnPP/RNBQK2R b KQ - 0 8", 2, 1_373L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/P7/1PP1N1PP/RNBQK2n w Q - 0 9", 1, 40L),
//                Triple.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3, 9_467L),
                //Triple.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBPPP3/q4N2/Pp4PP/R2Q1RK1 b kq - 0 1", 3, 72_051L),
                Triple.of("r3k2r/Pp1p1ppp/1b3nbN/nPp5/BBPPP3/q4N2/Pp4PP/R2Q1RK1 w kq c6 0 2", 2, 1_730L),
                Triple.of("r3k2r/Pp1p1ppp/1bP2nbN/n7/BBPPP3/q4N2/Pp4PP/R2Q1RK1 b kq - 0 2", 1, 45L)
                //Triple.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 4, 422_333L)
                //Runs through but needs over 3 hours
                //Triple.of("r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1 ", 5, 15_833_292L)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "moveGenTestData")
    void moveGenerationTest(Triple<String, Integer, Long> arguments) {
        Position position = new Position(arguments.getLeft());

        List<MoveData> ret = new ArrayList<>();

        long resNewApproach = 0;

        List<MoveData> depth1Ret = new ArrayList<>();
        var depth1 = moveGenTestRecursive(position, 1, depth1Ret);

        HashMap<MoveData, Long> initMoveToPosAfter = new HashMap<>();

        for (int i = 0; i < depth1Ret.size(); i++) {
            Position modified = new Position(position);
            modified.playMove(depth1Ret.get(i), true);
            var resDepth1 = moveGenTestRecursive(modified, arguments.getMiddle() - 1, ret);
            resNewApproach += resDepth1;
            initMoveToPosAfter.put(depth1Ret.get(i), resDepth1);
        }

        var res = moveGenTestRecursive(position, arguments.getMiddle(), ret);

        initMoveToPosAfter.entrySet().stream().map(moveDataLongEntry -> Parser.moveDataToString(moveDataLongEntry
                .getKey()) + ": " + moveDataLongEntry.getValue()).sorted().forEach(System.out::println);

        assertEquals(arguments.getRight(), resNewApproach);
        assertEquals(arguments.getRight(), res);
        //assertEquals(CollectionUtils.getCardinalityMap(arguments.getRight()), CollectionUtils.getCardinalityMap
        // (calculatedPositions));
    }

    private long moveGenTestRecursive(Position position, int depth, List<MoveData> ret) {
        if (depth == 0) {
            return 1;
        }
        List<MoveData> moveData = position.generateAllMoves(position.getCurrent());
        long numPos = 0;
        for (var data : moveData) {
            Position newPosition = new Position(position);
            newPosition.playMove(data, true);
            ret.add(data);
            numPos += moveGenTestRecursive(newPosition, depth - 1, ret);
        }
        return numPos;
    }
}
