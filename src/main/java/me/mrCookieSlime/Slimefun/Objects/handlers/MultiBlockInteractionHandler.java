package me.mrCookieSlime.Slimefun.Objects.handlers;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import io.github.thebusybiscuit.slimefun4.api.exceptions.IncompatibleItemHandlerException;
import io.github.thebusybiscuit.slimefun4.core.MultiBlock;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunMachine;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.MultiBlockMachine;

/**
 * This {@link ItemHandler} is called whenever a {@link Player} interacts with
 * this {@link MultiBlock}.
 * Note that this {@link MultiBlockInteractionHandler} should be assigned to
 * a class that inherits from {@link MultiBlockMachine}.
 * 
 * @author TheBusyBiscuit
 * 
 * @see ItemHandler
 * @see MultiBlock
 * @see MultiBlockMachine
 *
 */
@FunctionalInterface
public interface MultiBlockInteractionHandler extends ItemHandler {

    boolean onInteract(Player p, MultiBlock mb, Block b);

    @Override
    default Optional<IncompatibleItemHandlerException> validate(SlimefunItem item) {
        // Change this to "MultiBlockMachine" once SlimefunMachine was removed or deprecated
        if (!(item instanceof SlimefunMachine)) {
            return Optional.of(new IncompatibleItemHandlerException("Only class inheriting 'MultiBlockMachine' can have a MultiBlockInteractionHandler", item, this));
        }

        return Optional.empty();
    }

    @Override
    default Class<? extends ItemHandler> getIdentifier() {
        return MultiBlockInteractionHandler.class;
    }
}
