package io.github.thebusybiscuit.slimefun4.implementation.items.magical;

import org.bukkit.NamespacedKey;
import org.bukkit.block.EnderChest;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.LockedCategory;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;

/**
 * An {@link EnderTalisman} is a special version of {@link Talisman}
 * that works while it is in your {@link EnderChest}.
 * 
 * @author TheBusyBiscuit
 *
 */
class EnderTalisman extends Talisman {

    private static final LockedCategory ENDER_TALISMANS_CATEGORY = new LockedCategory(new NamespacedKey(SlimefunPlugin.instance, "ender_talismans"), new CustomItem(SlimefunItems.ENDER_TALISMAN, "&7Talismans - &aTier II"), 3, Talisman.TALISMANS_CATEGORY.getKey());

    public EnderTalisman(Talisman parent) {
        super(ENDER_TALISMANS_CATEGORY, parent.upgrade(), new ItemStack[] { SlimefunItems.ENDER_LUMP_3, null, SlimefunItems.ENDER_LUMP_3, null, parent.getItem(), null, SlimefunItems.ENDER_LUMP_3, null, SlimefunItems.ENDER_LUMP_3 }, parent.isConsumable(), parent.isEventCancelled(), parent.getSuffix(), parent.getChance(), parent.getEffects());
    }

    @Override
    public SlimefunItemStack upgrade() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createEnderTalisman() {
        // Let's override that, otherwise we would be creating Ender Talismans
        // for every Ender Talisman
    }

    @Override
    public void postRegister() {
        // Let's override that, otherwise we would be creating Ender Talismans
        // for every Ender Talisman
    }
}
