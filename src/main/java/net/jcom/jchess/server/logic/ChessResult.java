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

    public String toPgnResult() {
        switch (this) {
            case BLACK:
                return "0-1";
            case WHITE:
                return "1-0";
            case DRAW:
                return "1/2-1/2";
            case PLAYING:
                return "*";
        }
        return "";
    }
}
