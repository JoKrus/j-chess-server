package de.djgames.jonas.jcom2.server.logic.unit;

import com.google.common.collect.ImmutableBiMap;
import de.djgames.jonas.jcom2.server.factory.JComMessageFactory;
import de.djgames.jonas.jcom2.server.generated.*;
import de.djgames.jonas.jcom2.server.networking.Player;
import de.djgames.jonas.jcom2.server.settings.Settings;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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

    //TODO add validator as callback?

    public static <T> Pair<T, Integer> getMatchMessage(Player player, Class<T> classToReturn,
                                                       ErrorType ifWrongMessageTypeComes) {
        return getMatchMessage(player, classToReturn, ifWrongMessageTypeComes, 0);
    }

    public static <T> Pair<T, Integer> getMatchMessage(Player player, Class<T> classToReturn,
                                                       ErrorType ifWrongMessageTypeComes,
                                                       int errors) {
        T ret;
        do {
            var receive = player.getCommunicator().receiveMessage();
            ret = getSubMessage(receive, classToReturn);
            errors++;
            if (ret == null) {
                player.getCommunicator().sendMessage(JComMessageFactory.createAcceptMessage(player.getId(),
                        ifWrongMessageTypeComes));
            }
        } while (errors < Settings.getInt(Settings.MATCH_TRIES) && ret == null);
        return Pair.of(ret, errors);
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


    public static Triple<JComMessage, JComMessageType, Integer> getMatchMessage(Player player,
                                                                                List<JComMessageType> validTypesToReturn,
                                                                                ErrorType ifWrongMessageTypeComes) {
        return getMatchMessage(player, validTypesToReturn, ifWrongMessageTypeComes, 0);
    }

    public static Triple<JComMessage, JComMessageType, Integer> getMatchMessage(Player player,
                                                                                List<JComMessageType> validTypesToReturn,
                                                                                ErrorType ifWrongMessageTypeComes, int errors) {
        Pair<JComMessage, JComMessageType> ret = null;
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
                        ifWrongMessageTypeComes));
            }
        } while (errors < Settings.getInt(Settings.MATCH_TRIES) && ret == null);
        if (ret == null) {
            return Triple.of(null, null, errors);
        } else {
            return Triple.of(ret.getLeft(), ret.getRight(), errors);
        }
    }

    public static final ImmutableBiMap<JComMessageType, Class> typeClassMap =
            new ImmutableBiMap.Builder<JComMessageType, Class>()
                    .put(JComMessageType.ACCEPT, AcceptMessage.class)
                    .put(JComMessageType.ACTION, ActionMessage.class)
                    .put(JComMessageType.BEGIN, BeginMessage.class)
                    .put(JComMessageType.DISCONNECT, DisconnectMessage.class)
                    .put(JComMessageType.FINISH_TURN, FinishTurnMessage.class)
                    .put(JComMessageType.GAME_FOUND, GameFoundMessage.class)
                    .put(JComMessageType.GAME_OVER, GameOverMessage.class)
                    .put(JComMessageType.GAME_STATUS_PLAYER, GameStatusPlayerMessage.class)
                    .put(JComMessageType.HEART_BEAT, HeartBeatMessage.class)
                    .put(JComMessageType.LOGIN, LoginMessage.class)
                    .put(JComMessageType.LOGIN_REPLY, LoginReplyMessage.class)
                    .put(JComMessageType.POSITION_SOLDIERS, PositionSoldiersMessage.class)
                    .put(JComMessageType.YOUR_TURN, YourTurnMessage.class)
                    .build();
}
