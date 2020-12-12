package de.djgames.jonas.jcom2.server.logic.unit;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.generated.UnitData;
import de.djgames.jonas.jcom2.server.generated.WeaponData;
import de.djgames.jonas.jcom2.server.networking.Player;
import de.djgames.jonas.jcom2.server.settings.Settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

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

    public static <T> T getMatchMessage(Player player, Class<T> classToReturn) {
        int errors = 0;
        T ret;
        Method[] methods = JComMessage.class.getDeclaredMethods();
        var methodThatFits = Arrays.stream(methods).filter(method ->
                method.getReturnType().equals(classToReturn)).findFirst().get();
        do {
            var receive = player.getCommunicator().receiveMessage();
            try {
                ret = (T) methodThatFits.invoke(receive);
            } catch (IllegalAccessException | InvocationTargetException e) {
                ret = null;
            }
        } while (errors < Settings.getInt(Settings.MATCH_TRIES) && ret == null);
        return ret;
    }
}
