package net.kunmc.lab.elasticentityplugin;

import net.kunmc.lab.elasticentityplugin.entity.ElasticEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Game implements Listener {
    private final Plugin plugin;
    public final GameConfig config;
    private final TaskScheduler taskScheduler;
    private boolean isRunning = false;
    private Location center;
    private int currentRound;
    private double amountOfMobs;
    private final Set<Player> participants = new HashSet<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<ElasticEntity> entityList = new ArrayList<>();
    private final EntityType entityType = EntityType.CREEPER;
    private final Lock lock = new ReentrantLock();

    public Game(Plugin plugin, GameConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.taskScheduler = new TaskScheduler(plugin);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public int start(Location location) {
        if (isRunning) {
            return 1;
        }
        isRunning = true;

        participants.addAll(Bukkit.getOnlinePlayers().stream()
                .filter(u -> !config.spectators.contains(u.getUniqueId()))
                .collect(Collectors.toSet()));
        if (participants.isEmpty()) {
            isRunning = false;
            return 2;
        }

        center = location.clone();
        int y = location.getWorld().getHighestBlockYAt(location) + config.height.value() / 2 + 5;
        center.setY(y);

        currentRound = 0;
        amountOfMobs = config.amountInFirstRound.value();
        entityList.clear();

        generateStage();

        World w = center.getWorld();
        w.setGameRule(GameRule.FALL_DAMAGE, false);
        w.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);

        Location to = center.clone().subtract(5, config.height.value() / 2 - 5, 0);
        participants.forEach(p -> {
            p.spigot().respawn();
            p.setHealth(p.getMaxHealth());
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
            p.setBedSpawnLocation(center, true);
            p.teleport(to);
        });

        nextRound();

        tasks.add(new MainTask().runTaskTimer(plugin, 0, 1));
        tasks.add(new DetectCollisionTask().runTaskTimerAsynchronously(plugin, 0, 0));

        return 0;
    }

    private void generateStage() {
        Location center = this.center.clone();
        int halfOfHeight = config.height.value() / 2;
        int radius = config.lengthOfSide.value() / 2;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z < radius; z++) {
                int finalX = x;
                int finalZ = z;
                taskScheduler.offer(() -> {
                    center.clone().add(finalX, -halfOfHeight, finalZ).getBlock().setType(Material.STONE_BRICKS);
                    center.clone().add(finalX, halfOfHeight, finalZ).getBlock().setType(Material.BLUE_STAINED_GLASS);
                });
            }
        }

        for (int y = -halfOfHeight; y <= halfOfHeight; y++) {
            for (int x = -radius; x < radius; x++) {
                int finalX = x;
                int finalY = y;
                taskScheduler.offer(() -> {
                    center.clone().add(finalX, finalY, -radius).getBlock().setType(Material.BLUE_STAINED_GLASS);
                    center.clone().add(finalX, finalY, radius).getBlock().setType(Material.BLUE_STAINED_GLASS);
                });
            }

            for (int z = -radius; z <= radius; z++) {
                int finalY = y;
                int finalZ = z;
                taskScheduler.offer(() -> {
                    center.clone().add(-radius, finalY, finalZ).getBlock().setType(Material.BLUE_STAINED_GLASS);
                    center.clone().add(radius, finalY, finalZ).getBlock().setType(Material.BLUE_STAINED_GLASS);
                });
            }

            //???????????????????????????
            taskScheduler.offer(() -> {
                int height = -halfOfHeight + 2;
                center.clone().add(-radius + 1, height, -radius + 1).getBlock().setType(Material.LAVA);
                center.clone().add(radius - 1, height, -radius + 1).getBlock().setType(Material.LAVA);
                center.clone().add(-radius + 1, height, radius - 1).getBlock().setType(Material.LAVA);
                center.clone().add(radius - 1, height, radius - 1).getBlock().setType(Material.LAVA);
            });
        }

        for (int x = -radius + 1; x <= radius - 1; x++) {
            for (int z = -radius + 1; z <= radius - 1; z++) {
                for (int y = -halfOfHeight + 1; y <= halfOfHeight - 1; y++) {
                    Block b = center.clone().add(x, y, z).getBlock();
                    if (b.getType() != Material.AIR) {
                        taskScheduler.offer(() -> {
                            b.setType(Material.AIR);
                        });
                    }
                }
            }
        }
    }

    public boolean stop() {
        if (!isRunning) {
            return false;
        }
        isRunning = false;

        participants.clear();
        entityList.forEach(ElasticEntity::remove);

        tasks.forEach(BukkitTask::cancel);

        return true;
    }

    public void nextRound() {
        entityList.forEach(ElasticEntity::remove);
        entityList.clear();

        currentRound++;
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendTitle("Round " + currentRound, "", 10, 40, 10);
        });

        IntStream.range(0, ((int) amountOfMobs))
                .mapToObj(i -> new ElasticEntity(center, entityType, plugin, e -> {
                    ((Creeper) e).setMaxFuseTicks(Integer.MAX_VALUE);
                }))
                .forEach(entityList::add);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < entityList.size(); i++) {
                    ElasticEntity e = entityList.get(i);
                    new BukkitRunnable() {
                        private final Random rnd = new Random();

                        @Override
                        public void run() {
                            Vector direction = new Vector(rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5);
                            e.direction(direction);
                            e.speed(config.speed.value());
                        }
                    }.runTaskLater(plugin, 10 * i);
                }

                amountOfMobs += config.increasePerRound.value();
            }
        }.runTaskLater(plugin, 50);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        runWithLock(() -> {
            if (participants.remove(e.getPlayer())) {
                Bukkit.broadcast(Component.text(ChatColor.YELLOW + e.getPlayer().getName() + "????????????????????????????????????."));
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        runWithLock(() -> {
            Player p = e.getEntity();
            if (participants.remove(p)) {
                p.setGameMode(GameMode.SPECTATOR);

                Bukkit.getOnlinePlayers().forEach(recipient -> {
                    recipient.sendMessage(Component.text(ChatColor.RED + p.getName() + "???????????????"));
                });
            }
        });
    }

    public void changeSpeed(double speed) {
        runWithLock(() -> {
            entityList.forEach(e -> {
                e.speed(speed);
            });
        });
    }

    private void runWithLock(Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    private class MainTask extends BukkitRunnable {
        @Override
        public void run() {
            runWithLock(() -> {
                if (participants.size() == 0) {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.sendTitle(ChatColor.GREEN + "????????????", "", 20, 100, 20);
                        stop();
                    });
                    return;
                }

                if (participants.size() == 1) {
                    Player winner = participants.toArray(new Player[0])[0];
                    String msg = ChatColor.AQUA + "?????? " + winner.getName();
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.sendTitle(msg, "", 20, 100, 20);
                        p.sendMessage(msg);
                        stop();
                    });
                    return;
                }

                if (entityList.stream().anyMatch(ElasticEntity::isRemoved)) {
                    nextRound();
                }

                participants.forEach(p -> {
                    p.setFoodLevel(20);
                });
            });
        }
    }

    private class DetectCollisionTask extends BukkitRunnable {
        @Override
        public void run() {
            runWithLock(() -> {
                Set<Player> dropouts = new HashSet<>();

                for (ElasticEntity e : entityList) {
                    for (Player p : participants) {
                        if (e.isCollideWith(p)) {
                            explode(e, p);

                            Bukkit.getOnlinePlayers().forEach(recipient -> {
                                recipient.sendMessage(Component.text(ChatColor.RED + p.getName() + "???????????????"));
                            });
                            dropouts.add(p);
                            break;
                        }
                    }

                    if (participants.size() - dropouts.size() <= 1) {
                        break;
                    }
                }

                participants.removeAll(dropouts);
            });
        }

        public void explode(ElasticEntity e, Player p) {
            runSynchronous(() -> {
                Location location = e.location();
                World world = location.getWorld();

                world.spawnParticle(Particle.EXPLOSION_LARGE, location, 3);
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.5F);
                e.remove();
                p.setHealth(0.0);

                p.setGameMode(GameMode.SPECTATOR);
            });
        }

        public void runSynchronous(Runnable runnable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTask(plugin);
        }
    }
}
