package net.kunmc.lab.elasticentityplugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ElasticEntityPlugin extends JavaPlugin implements Listener {
    public static Game game;

    @Override
    public void onEnable() {
        GameConfig config = new GameConfig(this);
        game = new Game(this, config);

        new BukkitRunnable() {
            @Override
            public void run() {
                game.start(Bukkit.getPlayer("Maru256").getLocation());
            }
        }.runTaskLater(this, 200);
    }

    @Override
    public void onDisable() {
    }
}
