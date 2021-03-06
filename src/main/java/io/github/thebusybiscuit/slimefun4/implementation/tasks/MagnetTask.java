package io.github.thebusybiscuit.slimefun4.implementation.tasks;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import io.github.thebusybiscuit.slimefun4.implementation.items.magical.InfusedMagnet;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

/**
 * This {@link PlayerTask} is run when a {@link Player} carries an {@link InfusedMagnet}.
 * It manages the automatic pickup of nearby items.
 * 
 * @author TheBusyBiscuit
 * 
 * @see InfusedMagnet
 *
 */
public class MagnetTask extends PlayerTask {

    private final double radius;

    /**
     * This creates a new {@link MagnetTask} for the given {@link Player} with the given
     * pickup radius.
     * 
     * @param p
     *            The {@link Player} who items should be teleported to
     * @param radius
     *            The radius in which items should be picked up
     */
    public MagnetTask(Player p, double radius) {
        super(p);

        this.radius = radius;
    }

    @Override
    public void executeTask() {
        boolean playSound = false;

        for (Entity n : p.getNearbyEntities(radius, radius, radius)) {
            if (n instanceof Item) {
                Item item = (Item) n;

                if (!SlimefunUtils.hasNoPickupFlag(item) && item.getPickupDelay() <= 0 && p.getLocation().distanceSquared(item.getLocation()) > 0.3) {
                    item.teleport(p.getLocation());
                    playSound = true;
                }
            }
        }

        if (playSound) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.25F, 0.9F);
        }
    }

    @Override
    protected boolean isValid() {
        return super.isValid() && p.getGameMode() != GameMode.SPECTATOR;
    }

}
