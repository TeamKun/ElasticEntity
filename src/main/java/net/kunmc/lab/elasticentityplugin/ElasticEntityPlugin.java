package net.kunmc.lab.elasticentityplugin;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElasticEntityPlugin extends JavaPlugin implements Listener {
    public static Game game;

    @Override
    public void onEnable() {
        GameConfig config = new GameConfig(this);
        game = new Game(this, config);
    }

    @Override
    public void onDisable() {
    }
}
