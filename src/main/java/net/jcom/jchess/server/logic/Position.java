package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.logic.pieces.Piece;
import net.jcom.jchess.server.logic.pieces.PieceType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Position {
    private List<Piece> pieceList;
    private Color current;

    private Coordinate enPassant;

    private int halfMoveClock;
    private int round;

    private String possibleRochades;

    public Position() {
        //a8 to h1
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Position(String fenString) {
        if (!isFENValidSyntax(fenString)) {
            throw new IllegalArgumentException("FEN is not valid");
        }

        this.pieceList = new ArrayList<>();

        fenString = fenString.trim();
        String[] fenArgs = fenString.split(" ");

        String pieceListString = fenArgs[0];
        String currentPlayer = fenArgs[1];
        String possibleRochadesRaw = fenArgs[2];
        String possEnPassant = fenArgs[3];
        String halfMoveCounter = fenArgs[4];
        String nextRound = fenArgs[5];

        parseBoard(pieceListString);
        parseCurrentPlayer(currentPlayer);
        this.possibleRochades = possibleRochadesRaw;
        this.enPassant = Coordinate.parse(possEnPassant);
        this.halfMoveClock = Integer.parseInt(halfMoveCounter);
        this.round = Integer.parseInt(nextRound);
    }

    public Position(Position position) {
        this(position.toFenNotation());
    }

    public Piece getPieceAt(Coordinate coordinate) {
        var optional = this.pieceList.stream().filter(piece -> piece.getCoordinate().equals(coordinate)).findFirst();
        return optional.orElse(null);
    }

    public String toFenNotation() {
        StringBuilder ret = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            int emptySquares = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = getPieceAt(Coordinate.of(col, row));
                if (piece == null) {
                    emptySquares++;
                } else {
                    if (emptySquares != 0) {
                        ret.append(emptySquares);
                        emptySquares = 0;
                    }
                    ret.append(Parser.parsePiece(piece));
                }
            }
            if (emptySquares != 0) {
                ret.append(emptySquares);
            }
            if (row != 7) {
                ret.append("/");
            }
        }
        //First part done
        ret.append(" ");
        //currPLayer
        ret.append(this.current.equals(Color.WHITE) ? "w" : "b").append(" ");

        ret.append(this.toValidRochadeFen()).append(" ");

        ret.append(this.enPassant != null ? this.enPassant.toString() : "-").append(" ");

        ret.append(this.halfMoveClock).append(" ");

        ret.append(this.round);

        return ret.toString();
    }

    public static boolean isFENValidSyntax(String fenString) {
        Pattern compile = Pattern.compile("\\s*([rnbqkpRNBQKP1-8]+/){7}([rnbqkpRNBQKP1-8]+)\\s[bw-]\\s(([kqKQ]{1,4})|(-))\\s(" +
                "([a-h][36])|(-))\\s\\d+\\s\\d+\\s*");
        return compile.matcher(fenString).matches();
    }

    private void parseCurrentPlayer(String currentPlayer) {
        if (currentPlayer.equalsIgnoreCase("b")) {
            this.current = Color.BLACK;
        } else {
            this.current = Color.WHITE;
        }
    }

    private void parseBoard(String pieceListString) {
        int currY = 0, currX;
        for (var row : pieceListString.split("/")) {
            currX = 0;
            for (var tokenCol : row.split("")) {
                Pair<PieceType, Color> atPosition = Parser.parsePiece(tokenCol);
                if (atPosition == null) {
                    currX += Integer.parseInt(tokenCol);
                } else {
                    this.pieceList.add(Parser.getPiece(atPosition, Coordinate.of(currX, currY)));
                    ++currX;
                }
            }
            ++currY;
        }
    }

    private String toValidRochadeFen() {
        StringBuilder ret = new StringBuilder();
        if (this.possibleRochades.contains("K")) {
            ret.append("K");
        }
        if (this.possibleRochades.contains("Q")) {
            ret.append("Q");
        }
        if (this.possibleRochades.contains("k")) {
            ret.append("k");
        }
        if (this.possibleRochades.contains("q")) {
            ret.append("q");
        }
        if (ret.length() == 0) {
            ret.append("-");
        }
        return ret.toString();
    }
}
