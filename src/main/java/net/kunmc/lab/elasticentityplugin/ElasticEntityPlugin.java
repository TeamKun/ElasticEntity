package net.kunmc.lab.elasticentityplugin;

import dev.kotx.flylib.FlyLib;
import net.kunmc.lab.configlib.command.ConfigCommand;
import net.kunmc.lab.configlib.command.ConfigCommandBuilder;
import net.kunmc.lab.elasticentityplugin.command.MainCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElasticEntityPlugin extends JavaPlugin {
    public static Game game;

    @Override
    public void onEnable() {
        GameConfig config = new GameConfig(this);
        game = new Game(this, config);

        ConfigCommand configCommand = new ConfigCommandBuilder(config).build();

        FlyLib.create(this, builder -> {
            builder.command(new MainCommand("elastic", configCommand));
        });
    }

    @Override
    public void onDisable() {
    }
}
