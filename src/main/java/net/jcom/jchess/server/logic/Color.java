package net.jcom.jchess.server.logic;

public enum Color {
    WHITE, BLACK;

    public Color enemy() {
        return this.equals(WHITE) ? BLACK : WHITE;
    }

    public ChessResult result() {
        return this.equals(WHITE) ? ChessResult.WHITE : ChessResult.BLACK;
    }

    public ChessResult enemyResult() {
        return this.enemy().result();
    }
}
