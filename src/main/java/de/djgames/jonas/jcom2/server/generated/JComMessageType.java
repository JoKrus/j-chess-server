//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.10.25 um 04:50:33 PM CET 
//


package de.djgames.jonas.jcom2.server.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für JComMessageType.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="JComMessageType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="AwaitMove"/&gt;
 *     &lt;enumeration value="Move"/&gt;
 *     &lt;enumeration value="MoveInfo"/&gt;
 *     &lt;enumeration value="FinishTurn"/&gt;
 *     &lt;enumeration value="TurnInfo"/&gt;
 *     &lt;enumeration value="Login"/&gt;
 *     &lt;enumeration value="LoginReply"/&gt;
 *     &lt;enumeration value="AwaitSquad"/&gt;
 *     &lt;enumeration value="SubmitSquad"/&gt;
 *     &lt;enumeration value="SquadData"/&gt;
 *     &lt;enumeration value="Accept"/&gt;
 *     &lt;enumeration value="Win"/&gt;
 *     &lt;enumeration value="Disconnect"/&gt;
 *     &lt;enumeration value="SpectatorInfo"/&gt;
 *     &lt;enumeration value="GameStatus"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "JComMessageType")
@XmlEnum
public enum JComMessageType {

    @XmlEnumValue("AwaitMove")
    AWAIT_MOVE("AwaitMove"),
    @XmlEnumValue("Move")
    MOVE("Move"),
    @XmlEnumValue("MoveInfo")
    MOVE_INFO("MoveInfo"),
    @XmlEnumValue("FinishTurn")
    FINISH_TURN("FinishTurn"),
    @XmlEnumValue("TurnInfo")
    TURN_INFO("TurnInfo"),
    @XmlEnumValue("Login")
    LOGIN("Login"),
    @XmlEnumValue("LoginReply")
    LOGIN_REPLY("LoginReply"),
    @XmlEnumValue("AwaitSquad")
    AWAIT_SQUAD("AwaitSquad"),
    @XmlEnumValue("SubmitSquad")
    SUBMIT_SQUAD("SubmitSquad"),
    @XmlEnumValue("SquadData")
    SQUAD_DATA("SquadData"),
    @XmlEnumValue("Accept")
    ACCEPT("Accept"),
    @XmlEnumValue("Win")
    WIN("Win"),
    @XmlEnumValue("Disconnect")
    DISCONNECT("Disconnect"),
    @XmlEnumValue("SpectatorInfo")
    SPECTATOR_INFO("SpectatorInfo"),
    @XmlEnumValue("GameStatus")
    GAME_STATUS("GameStatus");
    private final String value;

    JComMessageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static JComMessageType fromValue(String v) {
        for (JComMessageType c : JComMessageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
