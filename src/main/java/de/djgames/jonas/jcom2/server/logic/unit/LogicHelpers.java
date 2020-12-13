package de.djgames.jonas.jcom2.server.logic.unit;

import com.google.common.collect.ImmutableBiMap;
import de.djgames.jonas.jcom2.server.factory.JComMessageFactory;
import de.djgames.jonas.jcom2.server.generated.*;
import de.djgames.jonas.jcom2.server.networking.Player;
import de.djgames.jonas.jcom2.server.settings.Settings;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogicHelpers {
    public static UnitData generateDefaultUnit(UUID id, String name) {
        UnitData unitData = new UnitData();
        unitData.setArmor(0);
        unitData.setMovementPoints(5);
        unitData.setVisionDistance(10);
        unitData.setHealthPoints(5);
        unitData.setId(id.toString());
        unitData.setName(name);
        unitData.setPrimaryWeapon(WeaponData.ASSAULT_RIFLE);
        return unitData;
    }

    public static <T> T getMatchMessage(Player player, Class<T> classToReturn, ErrorType ifWrongMessageComes) {
        int errors = 0;
        T ret;
        do {
            var receive = player.getCommunicator().receiveMessage();
            ret = getSubMessage(receive, classToReturn);
            errors++;
            if (ret == null) {
                player.getCommunicator().sendMessage(JComMessageFactory.createAcceptMessage(player.getId(),
                        ifWrongMessageComes));
            }
        } while (errors < Settings.getInt(Settings.MATCH_TRIES) && ret == null);
        return ret;
    }

    private static final Method[] jcomMessageMethods = JComMessage.class.getDeclaredMethods();

    public static <T> T getSubMessage(JComMessage receive, Class<T> classToReturn) {
        T ret;
        var methodThatFits = Arrays.stream(jcomMessageMethods).filter(method ->
                method.getReturnType().equals(classToReturn)).findFirst().get();
        try {
            ret = (T) methodThatFits.invoke(receive);
        } catch (IllegalAccessException | InvocationTargetException e) {
            ret = null;
        }
        return ret;
    }

    public static Pair<JComMessage, JComMessageType> getMatchMessage(Player player,
                                                                     List<JComMessageType> validTypesToReturn,
                                                                     ErrorType ifWrongMessageComes) {
        int errors = 0;
        Pair ret = null;
        var methodsThatFit = Arrays.stream(jcomMessageMethods)
                .map(method -> Pair.of(method, method.getReturnType()))
                .filter(methodPair -> {
                    List<Class> collect = validTypesToReturn.stream().map(typeClassMap::get).collect(Collectors.toList());
                    return collect.contains(methodPair.getRight());
                })
                .collect(Collectors.toList());
        do {
            var receive = player.getCommunicator().receiveMessage();
            for (int i = 0; i < methodsThatFit.size() && ret == null; ++i) {
                try {
                    var receivedType =
                            methodsThatFit.get(i).getLeft().invoke(receive);
                    if (receivedType != null) {
                        ret = Pair.of(receive, typeClassMap.inverse().get(receivedType.getClass()));
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    ret = null;
                }
            }
            errors++;
            if (ret == null) {
                player.getCommunicator().sendMessage(JComMessageFactory.createAcceptMessage(player.getId(),
                        ifWrongMessageComes));
            }
        } while (errors < Settings.getInt(Settings.MATCH_TRIES) && ret == null);
        return ret;
    }

    public static final ImmutableBiMap<JComMessageType, Class> typeClassMap =
            new ImmutableBiMap.Builder<JComMessageType, Class>()
                    .put(JComMessageType.ACCEPT, AcceptMessage.class)
                    .put(JComMessageType.BEGIN, BeginMessage.class)
                    .put(JComMessageType.DISCONNECT, DisconnectMessage.class)
                    .put(JComMessageType.GAME_FOUND, GameFoundMessage.class)
                    .put(JComMessageType.GAME_OVER, GameOverMessage.class)
                    .put(JComMessageType.HEART_BEAT, HeartBeatMessage.class)
                    .put(JComMessageType.LOGIN, LoginMessage.class)
                    .put(JComMessageType.LOGIN_REPLY, LoginReplyMessage.class)
                    .put(JComMessageType.POSITION_SOLDIERS, PositionSoldiersMessage.class)
                    .build();
}
