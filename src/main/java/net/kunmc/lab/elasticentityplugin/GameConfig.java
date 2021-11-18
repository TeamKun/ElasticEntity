package net.kunmc.lab.elasticentityplugin;

import net.kunmc.lab.configlib.config.BaseConfig;
import net.kunmc.lab.configlib.value.DoubleValue;
import net.kunmc.lab.configlib.value.IntegerValue;
import net.kunmc.lab.configlib.value.UUIDSetValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class GameConfig extends BaseConfig {
    public IntegerValue height = new IntegerValue(15, 5, 100);
    public IntegerValue lengthOfSide = new IntegerValue(40, 10, 100);
    public DoubleValue speed = new DoubleValue(1.0, 0.1, 30.0, d -> {
        ElasticEntityPlugin.game.changeSpeed(d);
    });
    public IntegerValue amountInFirstRound = new IntegerValue(10, 1, 100);
    public DoubleValue increasePerRound = new DoubleValue(1.0, 0.0, 100.0);
    public UUIDSetValue spectators = new UUIDSetValue();

    public GameConfig(@NotNull Plugin plugin) {
        super(plugin, "game");
    }
}
