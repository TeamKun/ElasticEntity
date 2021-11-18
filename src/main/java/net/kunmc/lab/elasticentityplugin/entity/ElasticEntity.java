package net.kunmc.lab.elasticentityplugin.entity;

import net.kunmc.lab.elasticentityplugin.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class ElasticEntity {
    private final Entity entity;
    private Vector direction = new Vector(0, 0, 0);
    private double speed = 0.0;

    public ElasticEntity(Location location, EntityType entityType, Plugin plugin, Consumer<Entity> consumer) {
        this(location.getWorld().spawnEntity(location, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
            e.setGravity(false);
            e.setInvulnerable(true);
            consumer.accept(e);
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
        BoundingBox boundingBox = this.entity.getBoundingBox().expand(0.25);
        return boundingBox.overlaps(entity.getBoundingBox());
    }

    public boolean isCollideWith(Block block) {
        BoundingBox boundingBox = this.entity.getBoundingBox()
                .expand(1.0)
                .expandDirectional(0, -1.0, 0);
        return boundingBox.overlaps(block.getBoundingBox());
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
                entity.setVelocity(calcVelocity());
                return;
            }
            if (!isCollideWith(rayTraceResult.getHitBlock())) {
                entity.setVelocity(calcVelocity());
                return;
            }

            Vector normal = rayTraceResult.getHitBlockFace().getDirection();
            direction = VectorUtil.toUnit(direction.clone().add(direction.clone().multiply(-1).multiply(normal).multiply(2).multiply(normal)));

            entity.setVelocity(calcVelocity());
        }

        public Vector calcVelocity() {
            return direction.clone().multiply(speed);
        }

        public RayTraceResult rayTrace() {
            if (direction.length() == 0) {
                return null;
            }

            BoundingBox boundingBox = entity.getBoundingBox();
            World w = entity.getWorld();

            return w.rayTraceBlocks(boundingBox.getCenter().toLocation(w), direction, 5);
        }
    }
}
