package net.jcom.jchess.server.logic;

import java.util.Objects;

public class Coordinate {
    private int x;
    private int y;

    //x = 0 means a
    //y = 0 means 8
    //0/0 = a8
    //7/0 = h8
    //0/7 = a1
    //7/7 = h1

    private Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate parse(String s) {
        if (s.length() != 2) {
            return null;
        } else {
            int possX = s.charAt(0) - 'a';
            int possY = '8' - s.charAt(1);

            return Coordinate.of(possX, possY);
        }
    }

    public static Coordinate of(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            return null;
        }
        return new Coordinate(x, y);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    @Override
    public String toString() {
        return String.valueOf(new char[]{(char) ('a' + this.x), (char) ('8' - this.y)});
    }
}