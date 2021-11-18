package net.kunmc.lab.elasticentityplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TaskScheduler {
    private final Deque<BukkitRunnable> deque = new ConcurrentLinkedDeque<>();
    public int numberOfExecutionsPerSec = 12500;

    public TaskScheduler(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < (numberOfExecutionsPerSec / 20) * (Bukkit.getTPS()[0] / 20); i++) {
                    BukkitRunnable runnable = deque.poll();
                    if (runnable != null) {
                        runnable.runTask(plugin);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 0);
    }

    public void offer(Runnable runnable) {
        deque.offer(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }
}
