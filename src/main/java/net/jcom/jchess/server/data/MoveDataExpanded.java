package net.jcom.jchess.server.data;

import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.logic.Parser;
import net.jcom.jchess.server.logic.pieces.PieceType;

public class MoveDataExpanded extends MoveData {
    private final String pieceType;
    private final String rochade; // empty if no rochade, else O-O or O-O-O
    private final String checkSuffix; //empty + or #
    private final String takenPiece;

    public MoveDataExpanded(MoveData moveData, PieceType pieceType, String rochade,
                            boolean takenPiece, boolean check, boolean mate) {
        super();
        setFrom(moveData.getFrom());
        setTo(moveData.getTo());
        setPromotionUnit(moveData.getPromotionUnit());

        this.pieceType = Parser.parsePieceType(pieceType, true).toUpperCase();

        this.rochade = rochade;

        this.takenPiece = takenPiece ? "x" : "-";

        if (mate) {
            this.checkSuffix = "#";
        } else if (check) {
            this.checkSuffix = "+";
        } else {
            this.checkSuffix = "";
        }
    }

    public String getPieceType() {
        return this.pieceType;
    }

    public String getRochade() {
        return this.rochade;
    }

    public String getCheckSuffix() {
        return this.checkSuffix;
    }

    public String toMoveText() {
        StringBuilder ret = new StringBuilder();
        if (!this.rochade.isEmpty()) {
            ret.append(this.rochade);
        } else {
            ret.append(this.pieceType).append(getFrom())
                    .append(this.takenPiece).append(getTo());
            if (this.promotionUnit != null && !this.promotionUnit.isEmpty()) {
                ret.append("=").append(getPromotionUnit().toUpperCase());
            }
        }
        ret.append(this.checkSuffix);
        return ret.toString();
    }
}
