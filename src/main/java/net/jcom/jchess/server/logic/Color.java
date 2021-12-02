package net.jcom.jchess.server.logic;

public enum Color {
    WHITE, BLACK;

    public Color enemy() {
        return this.equals(WHITE) ? BLACK : WHITE;
    }
}
