package net.kunmc.lab.elasticentityplugin.util;

import org.bukkit.util.Vector;

public class VectorUtil {
    public static Vector toUnit(Vector vector) {
        return vector.divide(new Vector(vector.length(), vector.length(), vector.length()));
    }
}
