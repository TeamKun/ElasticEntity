package net.kunmc.lab.elasticentityplugin.entity;

import net.kunmc.lab.elasticentityplugin.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ElasticEntity {
    private final Entity entity;
    private Vector direction = new Vector(0, 0, 0);
    private double speed = 0.0;

    public ElasticEntity(Location location, EntityType entityType, Plugin plugin) {
        this(location.getWorld().spawnEntity(location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
            e.setGravity(false);
            e.setInvulnerable(true);
            if (e instanceof LivingEntity) {
                ((LivingEntity) e).setAI(false);
            }
        }), plugin);
    }

    public ElasticEntity(Entity entity, Plugin plugin) {
        this.entity = entity;

        new MoveTask().runTaskTimerAsynchronously(plugin, 0, 0);
    }

    public Vector velocity() {
        return direction.clone().multiply(speed);
    }

    public void velocity(Vector velocity) {
        direction(velocity);
        speed(velocity.length());
    }

    public Vector direction() {
        return direction.clone();
    }

    public void direction(Vector direction) {
        this.direction = VectorUtil.toUnit(direction.clone());
    }

    public double speed() {
        return speed;
    }

    public void speed(double speed) {
        this.speed = speed;
    }

    public boolean isCollideWith(Entity entity) {
        return isCollideWith(entity.getBoundingBox());
    }

    public boolean isCollideWith(Block block) {
        return isCollideWith(block.getBoundingBox());
    }

    private boolean isCollideWith(BoundingBox boundingBox) {
        BoundingBox entityBoundingBox = entity.getBoundingBox().expand(0.25);
        return entityBoundingBox.overlaps(boundingBox);
    }

    public void remove() {
        entity.remove();
    }

    public Location location() {
        return entity.getLocation();
    }

    public boolean isRemoved() {
        return entity.isDead();
    }

    private class MoveTask extends BukkitRunnable {
        @Override
        public void run() {
            if (entity.isDead()) {
                this.cancel();
                return;
            }

            RayTraceResult rayTraceResult = rayTrace();
            if (rayTraceResult == null || rayTraceResult.getHitBlock() == null) {
                entity.teleportAsync(nextLocation());
                return;
            }
            if (!isCollideWith(rayTraceResult.getHitBlock())) {
                entity.teleportAsync(nextLocation());
                return;
            }

            Vector normal = rayTraceResult.getHitBlockFace().getDirection();
            direction = VectorUtil.toUnit(direction.clone().add(direction.clone().multiply(-1).multiply(normal).multiply(2).multiply(normal)));

            entity.teleportAsync(nextLocation());
        }

        public Location nextLocation() {
            return entity.getLocation().add(direction.clone().multiply(speed / 20));
        }

        public RayTraceResult rayTrace() {
            if (direction.length() == 0) {
                return null;
            }

            BoundingBox boundingBox = entity.getBoundingBox();
            World w = entity.getWorld();

            return w.rayTraceBlocks(boundingBox.getCenter().toLocation(w), direction, 3);
        }
    }
}
