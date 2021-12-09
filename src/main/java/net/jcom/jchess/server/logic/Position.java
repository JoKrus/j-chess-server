package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.logic.pieces.King;
import net.jcom.jchess.server.logic.pieces.Piece;
import net.jcom.jchess.server.logic.pieces.PieceHelper;
import net.jcom.jchess.server.logic.pieces.PieceType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Position {
    private List<Piece> pieceList;
    private Color current;
    private Coordinate enPassant;
    private int halfMoveClock;
    private int round;
    private String possibleRochades;
    private HashMap<String, Integer> previousPositions;

    public Position() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Position(String fenString) {
        if (!isFENValidSyntax(fenString)) {
            throw new IllegalArgumentException(String.format("FEN is not valid: %s", fenString));
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
        this.parseBoard(pieceListString);
        this.parseCurrentPlayer(currentPlayer);
        this.possibleRochades = possibleRochadesRaw;
        this.enPassant = Coordinate.parse(possEnPassant);
        this.halfMoveClock = Integer.parseInt(halfMoveCounter);
        this.round = Integer.parseInt(nextRound);
        this.previousPositions = new HashMap<>();
    }

    public Position(Position position) {
        this(position.toFenNotation());
        this.previousPositions = new HashMap(position.previousPositions);
    }

    public static boolean isFENValidSyntax(String fenString) {
        Pattern compile = Pattern.compile("\\s*([rnbqkpRNBQKP1-8]+/){7}([rnbqkpRNBQKP1-8]+)\\s[bw-]\\s(([kqKQ]{1,4})|(-))\\s(([a-h][36])|(-))\\s\\d+\\s\\d+\\s*");
        return compile.matcher(fenString).matches();
    }

    public Piece getPieceAt(Coordinate coordinate) {
        Optional<Piece> optional = this.pieceList.stream().filter((piece) -> piece.getCoordinate().equals(coordinate)).findFirst();
        return optional.orElse(null);
    }

    //current = player that is about to make his move
    public ChessResult isOverBasedOnPosition() {
        //wins
        //checkmate, current can not prevent the king being taken
        ChessResult overBasedOnCheckmate = checkCheckmate();
        if (overBasedOnCheckmate != ChessResult.PLAYING) {
            return overBasedOnCheckmate;
        }

        //draws

        //stalemate, current can not play a valid move
        ChessResult overBasedOnStalemate = checkStalemate();
        if (overBasedOnStalemate != ChessResult.PLAYING) {
            return overBasedOnStalemate;
        }
        //not enough material
        // - king vs king
        // - king vs 1 bishop or knight
        // - king vs 2 knights
        // - king with minor vs king with minor
        ChessResult overBasedOnMaterial = checkMaterial();
        if (overBasedOnMaterial != ChessResult.PLAYING) {
            return overBasedOnMaterial;
        }
        // 50 move rule / forced at 75
        ChessResult overBasedOnMoveRule = checkMoveRule();
        if (overBasedOnMoveRule != ChessResult.PLAYING) {
            return overBasedOnMoveRule;
        }
        // threefold repetition (save all played fens) pieces occupy same places, same player to move, castling
        //     rights are the same, and en passant as well
        ChessResult overBasedOnRepetition = checkRepetition();
        if (overBasedOnRepetition != ChessResult.PLAYING) {
            return overBasedOnRepetition;
        }

        return ChessResult.PLAYING;
    }

    private ChessResult checkStalemate() {
        //checkmate if with every move possible, current would still be in check
        if (playerInCheck(this.current).isEmpty() && generateAllMoves(this.current).isEmpty()) {
            return ChessResult.DRAW;
        }

        return ChessResult.PLAYING;
    }

    private ChessResult checkCheckmate() {
        //checkmate if with every move possible, current would still be in check
        if (!playerInCheck(this.current).isEmpty() && generateAllMoves(this.current).isEmpty()) {
            return this.current.enemyResult();
        }

        return ChessResult.PLAYING;
    }

    private ChessResult checkMaterial() {
        boolean someMaterialIssuesOccur = false;

        if (this.pieceList.size() == 4 &&
                this.pieceList.stream().filter(piece -> piece.getPieceType() == PieceType.KNIGHT).count() == 2 &&
                this.pieceList.stream().collect(Collectors.groupingBy(Piece::getColor, Collectors.counting())).containsValue(3L)) {
            // 2 Knights of the same color
            someMaterialIssuesOccur = true;
        } else if (this.pieceList.size() == 4 &&
                this.pieceList.stream().filter(piece -> piece.getPieceType() == PieceType.KNIGHT || piece.getPieceType() == PieceType.BISHOP).count() == 2 &&
                this.pieceList.stream().collect(Collectors.groupingBy(Piece::getColor, Collectors.counting())).containsValue(2L)) {
            //2 bishops and or knights in the game and only 2 units per team
            someMaterialIssuesOccur = true;
        } else if (this.pieceList.size() == 3 &&
                this.pieceList.stream().filter(piece -> piece.getPieceType() == PieceType.KNIGHT || piece.getPieceType() == PieceType.BISHOP).count() == 1) {
            //one minor piece on the board
            someMaterialIssuesOccur = true;
        } else if (this.pieceList.size() == 2) {
            //only kings
            someMaterialIssuesOccur = true;
        }

        return someMaterialIssuesOccur ? ChessResult.DRAW : ChessResult.PLAYING;
    }

    private ChessResult checkMoveRule() {
        return this.halfMoveClock >= 75 ? ChessResult.DRAW : ChessResult.PLAYING;
    }

    ChessResult checkRequestedMoveRule() {
        return this.halfMoveClock >= 50 ? ChessResult.DRAW : ChessResult.PLAYING;
    }

    public List<Piece> playerInCheck(Color color) {
        King kingToCheck = null;
        for (var piece : this.getPieceList(color)) {
            if (piece.getPieceType() == PieceType.KING && piece.getColor().equals(color)) {
                kingToCheck = (King) piece;
                break;
            }
        }

        if (kingToCheck == null) {
            throw new IllegalStateException("No King on the board");
        }

        List<Piece> piecesThatPutKingInCheck = new ArrayList<>();
        for (var piece : this.getPieceList(color.enemy())) {
            if (piece.possibleToMoveToUnchecked(this, true).stream()
                    .map((moveData) -> Coordinate.parse(moveData.getTo())).collect(Collectors.toList())
                    .contains(kingToCheck.getCoordinate())) {
                piecesThatPutKingInCheck.add(piece);
            }
        }

        return piecesThatPutKingInCheck;
    }

    private ChessResult checkRepetition() {
        return this.previousPositions.getOrDefault(this.toFenNotationMinusHalfMovesAndRound(), 0) >= 3 ? ChessResult.DRAW : ChessResult.PLAYING;
    }

    public List<Piece> canMoveToSquare(Coordinate square, Color color) {
        List<Piece> ret = new ArrayList<>();
        for (var piece : this.getPieceList(color)) {
            if (piece.possibleToMoveToUnchecked(this, true).stream()
                    .map((moveData) -> Coordinate.parse(moveData.getTo())).collect(Collectors.toList()).contains(square)) {
                ret.add(piece);
            }
        }
        return ret;
    }

    public boolean checkIfLegalMove(MoveData moveData) {
        Coordinate from = Coordinate.parse(moveData.getFrom());
        Coordinate to = Coordinate.parse(moveData.getTo());
        if (from == null || to == null) {
            return false;
        }

        Piece moving = this.getPieceAt(from);
        if (moving == null || moving.getColor() != this.getCurrent()) {
            return false;
        }

        List<MoveData> possibleTos = moving.possibleToMoveTo(this);
        return possibleTos.contains(moveData);
    }

    public void playMove(MoveData moveData) {
        this.playMove(moveData, false);
    }

    public List<MoveData> generateAllMoves(Color color) {
        List<MoveData> ret = new ArrayList<>();
        for (var piece : this.getPieceList(color)) {
            ret.addAll(piece.possibleToMoveTo(this));
        }
        return ret;
    }

    public void playMove(MoveData moveData, boolean force) {
        Coordinate from = Coordinate.parse(moveData.getFrom());
        Coordinate to = Coordinate.parse(moveData.getTo());
        if (from == null || to == null) {
            throw new IllegalArgumentException("Out of bounds");
        }
        Piece moving = getPieceAt(from);
        if (moving == null || moving.getColor() != this.getCurrent()) {
            throw new IllegalArgumentException("No valid piece at position");
        }
        if (!force) {
            List<MoveData> possibleTos = moving.possibleToMoveTo(this);
            if (!possibleTos.contains(moveData)) {
                throw new IllegalArgumentException("Not a valid move");
            }
        }

        int endLine = moving.getColor().equals(Color.WHITE) ? 0 : 7;

        int yDistance = from.getY() - to.getY();
        int xDistance = from.getX() - to.getX();

        this.handleRochade(from, to, moving, xDistance);
        this.handlePieceTakingAndHalfMoveClock(to, moving);
        this.handleSettingEnPassant(from, moving, yDistance);
        this.handleSettingRochadeString(from, to);

        moving.setCoordinate(to);
        this.handlePromotion(moveData, to, moving, endLine);

        this.current = this.current.enemy();
        if (this.current == Color.WHITE) {
            ++this.round;
        }

        this.previousPositions.put(this.toFenNotationMinusHalfMovesAndRound(),
                this.previousPositions.getOrDefault(this.toFenNotationMinusHalfMovesAndRound(), 0) + 1);
    }

    private void handleRochade(Coordinate from, Coordinate to, Piece moving, int xDistance) {
        if (moving.getPieceType() == PieceType.KING && Math.abs(xDistance) == 2) {
            Pair<Piece, Coordinate> rookMove = PieceHelper.getRochadeRook(this, from, to);
            rookMove.getLeft().setCoordinate(rookMove.getRight());
        }
    }

    private void handleSettingRochadeString(Coordinate from, Coordinate to) {
        this.possibleRochades = PieceHelper.modifyRochadeString(this.possibleRochades, from, to);
    }

    private void handlePromotion(MoveData moveData, Coordinate to, Piece moving, int endLine) {
        //Promotion
        if (moving.getPieceType().equals(PieceType.PAWN) && to.getY() == endLine) {
            Piece newPiece = Parser.parsePromotionUnit(moveData.getPromotionUnit(), moving.getColor());
            newPiece.setCoordinate(to);
            this.pieceList.remove(moving);
            this.pieceList.add(newPiece);
        }
    }

    private void handleSettingEnPassant(Coordinate from, Piece moving, int yDistance) {
        if (moving.getPieceType() == PieceType.PAWN) {
            this.halfMoveClock = 0;
            if (Math.abs(yDistance) == 2) {
                int dir = -Integer.signum(yDistance);
                this.enPassant = Coordinate.of(from.getX(), from.getY() + dir);

            } else {
                this.enPassant = null;
            }
        } else {
            this.enPassant = null;
        }
    }

    private void handlePieceTakingAndHalfMoveClock(Coordinate to, Piece moving) {
        if (moving.getPieceType() != PieceType.PAWN) {
            ++this.halfMoveClock;
        } else {
            this.halfMoveClock = 0;
        }

        var possTaken = this.getPieceAt(to);
        if (possTaken == null && moving.getPieceType() == PieceType.PAWN) {
            if (to.equals(getEnPassant())) {
                int dirY = to.getY() == 2 ? 3 : 4;
                possTaken = this.getPieceAt(Coordinate.of(to.getX(), dirY));
            }
        }

        if (possTaken != null) {
            var takenPiece = possTaken;
            this.pieceList.remove(takenPiece);
            /*
            logger.info(String.format("%s %s took %s %s", moving.getColor(), moving.getPieceType(),
                    takenPiece.getColor(), takenPiece.getPieceType()));
            */
            //Reset if piece is taken
            this.halfMoveClock = 0;
        }
    }

    public String toFenNotation() {
        StringBuilder ret = new StringBuilder();

        for (int row = 0; row < 8; ++row) {
            int emptySquares = 0;
            for (int col = 0; col < 8; ++col) {
                Piece piece = this.getPieceAt(Coordinate.of(col, row));
                if (piece == null) {
                    ++emptySquares;
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

    public List<Piece> getPieceList() {
        return this.pieceList;
    }

    private String toFenNotationMinusHalfMovesAndRound() {
        return Arrays.stream(this.toFenNotation().split(" ")).limit(4L).collect(Collectors.joining(" "));
    }

    public Color getCurrent() {
        return this.current;
    }

    public Coordinate getEnPassant() {
        return this.enPassant;
    }

    public int getHalfMoveClock() {
        return this.halfMoveClock;
    }

    public int getRound() {
        return this.round;
    }

    public String getPossibleRochades() {
        return this.possibleRochades;
    }

    public List<Piece> getPieceList(Color color) {
        return this.pieceList.stream().filter((piece) -> piece.getColor() == color).collect(Collectors.toList());
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int y = 0; y < 8; ++y) {
            s.append("|");
            for (int x = 0; x < 8; ++x) {
                if (this.getPieceAt(Coordinate.of(x, y)) != null) {
                    s.append(Parser.parsePiece(this.getPieceAt(Coordinate.of(x, y))));
                } else {
                    s.append(" ");
                }
                s.append("|");
            }
            s.append("\n");
        }

        return s.toString();
    }
}
