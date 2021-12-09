package net.jcom.jchess.server.logic;

public enum ChessResult {
    BLACK, WHITE, DRAW, PLAYING;

    public Color toColor() {
        if (this == WHITE) {
            return Color.WHITE;
        } else {
            return this == BLACK ? Color.BLACK : null;
        }
    }
}
