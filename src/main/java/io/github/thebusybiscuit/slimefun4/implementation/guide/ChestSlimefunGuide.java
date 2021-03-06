package io.github.thebusybiscuit.slimefun4.implementation.guide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;

import io.github.thebusybiscuit.cscorelib2.chat.ChatInput;
import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.recipes.MinecraftRecipe;
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.MultiBlock;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.categories.FlexCategory;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideLayout;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.MenuClickHandler;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.LockedCategory;
import me.mrCookieSlime.Slimefun.Objects.Research;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.MultiBlockMachine;
import me.mrCookieSlime.Slimefun.api.Slimefun;

public class ChestSlimefunGuide implements SlimefunGuideImplementation {

    private static final int CATEGORY_SIZE = 36;

    private final int[] recipeSlots = { 3, 4, 5, 12, 13, 14, 21, 22, 23 };
    private final Sound sound;
    private final boolean showVanillaRecipes;

    public ChestSlimefunGuide(boolean vanillaRecipes) {
        if (SlimefunPlugin.getMinecraftVersion().isAtLeast(MinecraftVersion.MINECRAFT_1_14)) {
            sound = Sound.ITEM_BOOK_PAGE_TURN;
            showVanillaRecipes = vanillaRecipes;
        }
        else {
            sound = Sound.ENTITY_BAT_TAKEOFF;
            showVanillaRecipes = false;
        }
    }

    @Override
    public SlimefunGuideLayout getLayout() {
        return SlimefunGuideLayout.CHEST;
    }

    @Override
    public ItemStack getItem() {
        return new CustomItem(new ItemStack(Material.ENCHANTED_BOOK), "&aSlimefun Guide &7(Chest GUI)", "", "&eRight Click &8\u21E8 &7Browse Items", "&eShift + Right Click &8\u21E8 &7Open Settings / Credits");
    }

    protected boolean isSurvivalMode() {
        return true;
    }

    private List<Category> getVisibleCategories(Player p, PlayerProfile profile) {
        List<Category> categories = new LinkedList<>();

        for (Category category : SlimefunPlugin.getRegistry().getCategories()) {
            if (!category.isHidden(p) && (!(category instanceof FlexCategory) || ((FlexCategory) category).isVisible(p, profile, getLayout()))) {
                categories.add(category);
            }
        }

        return categories;
    }

    @Override
    public void openMainMenu(PlayerProfile profile, int page) {
        Player p = profile.getPlayer();
        if (p == null) return;

        if (isSurvivalMode()) {
            profile.getGuideHistory().clear();
        }

        ChestMenu menu = create(p);

        List<Category> categories = getVisibleCategories(p, profile);

        int index = 9;
        createHeader(p, profile, menu);

        int target = (CATEGORY_SIZE * (page - 1)) - 1;

        while (target < (categories.size() - 1) && index < CATEGORY_SIZE + 9) {
            target++;
            Category category = categories.get(target);

            displayCategory(menu, p, profile, category, index);
            index++;
        }

        int pages = target == categories.size() - 1 ? page : (categories.size() - 1) / CATEGORY_SIZE + 1;

        menu.addItem(46, ChestMenuUtils.getPreviousButton(p, page, pages));
        menu.addMenuClickHandler(46, (pl, slot, item, action) -> {
            int next = page - 1;
            if (next != page && next > 0) openMainMenu(profile, next);
            return false;
        });

        menu.addItem(52, ChestMenuUtils.getNextButton(p, page, pages));
        menu.addMenuClickHandler(52, (pl, slot, item, action) -> {
            int next = page + 1;
            if (next != page && next <= pages) openMainMenu(profile, next);
            return false;
        });

        menu.open(p);
    }

    private void displayCategory(ChestMenu menu, Player p, PlayerProfile profile, Category category, int index) {
        if (!(category instanceof LockedCategory) || !isSurvivalMode() || ((LockedCategory) category).hasUnlocked(p, profile)) {
            menu.addItem(index, category.getItem(p));
            menu.addMenuClickHandler(index, (pl, slot, item, action) -> {
                openCategory(profile, category, 1);
                return false;
            });
        }
        else {
            List<String> lore = new ArrayList<>();
            lore.add("");

            for (String line : SlimefunPlugin.getLocal().getMessages(p, "guide.locked-category")) {
                lore.add(ChatColor.RESET + line);
            }

            lore.add("");

            for (Category parent : ((LockedCategory) category).getParents()) {
                lore.add(parent.getItem(p).getItemMeta().getDisplayName());
            }

            menu.addItem(index, new CustomItem(Material.BARRIER, "&4" + SlimefunPlugin.getLocal().getMessage(p, "guide.locked") + " &7- &r" + category.getItem(p).getItemMeta().getDisplayName(), lore.toArray(new String[0])));
            menu.addMenuClickHandler(index, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public void openCategory(PlayerProfile profile, Category category, int page) {
        Player p = profile.getPlayer();
        if (p == null) return;

        if (category instanceof FlexCategory) {
            ((FlexCategory) category).open(p, profile, getLayout());
            return;
        }

        if (isSurvivalMode()) {
            profile.getGuideHistory().add(category, page);
        }

        ChestMenu menu = create(p);
        createHeader(p, profile, menu);

        menu.addItem(1, new CustomItem(ChestMenuUtils.getBackButton(p, "", ChatColor.GRAY + SlimefunPlugin.getLocal().getMessage(p, "guide.back.guide"))));
        menu.addMenuClickHandler(1, (pl, s, is, action) -> {
            openMainMenu(profile, 1);
            return false;
        });

        int index = 9;
        int pages = (category.getItems().size() - 1) / CATEGORY_SIZE + 1;

        menu.addItem(46, ChestMenuUtils.getPreviousButton(p, page, pages));
        menu.addMenuClickHandler(46, (pl, slot, item, action) -> {
            int next = page - 1;
            if (next != page && next > 0) openCategory(profile, category, next);
            return false;
        });

        menu.addItem(52, ChestMenuUtils.getNextButton(p, page, pages));
        menu.addMenuClickHandler(52, (pl, slot, item, action) -> {
            int next = page + 1;
            if (next != page && next <= pages) openCategory(profile, category, next);
            return false;
        });

        int categoryIndex = CATEGORY_SIZE * (page - 1);

        for (int i = 0; i < CATEGORY_SIZE; i++) {
            int target = categoryIndex + i;
            if (target >= category.getItems().size()) break;

            SlimefunItem sfitem = category.getItems().get(target);

            if (Slimefun.isEnabled(p, sfitem, false)) {
                Research research = sfitem.getResearch();

                if (isSurvivalMode() && !Slimefun.hasPermission(p, sfitem, false)) {
                    List<String> message = SlimefunPlugin.getPermissionsService().getLore(sfitem);
                    menu.addItem(index, new CustomItem(Material.BARRIER, sfitem.getItemName(), message.toArray(new String[0])));
                    menu.addMenuClickHandler(index, ChestMenuUtils.getEmptyClickHandler());
                    index++;
                }
                else if (isSurvivalMode() && research != null && !profile.hasUnlocked(research)) {
                    menu.addItem(index, new CustomItem(Material.BARRIER, "&r" + ItemUtils.getItemName(sfitem.getItem()), "&4&l" + SlimefunPlugin.getLocal().getMessage(p, "guide.locked"), "", "&a> Click to unlock", "", "&7Cost: &b" + research.getCost() + " Level(s)"));
                    menu.addMenuClickHandler(index, (pl, slot, item, action) -> {
                        if (!SlimefunPlugin.getRegistry().getCurrentlyResearchingPlayers().contains(pl.getUniqueId())) {
                            if (research.canUnlock(pl)) {
                                if (profile.hasUnlocked(research)) {
                                    openCategory(profile, category, page);
                                }
                                else {
                                    unlockItem(pl, sfitem, () -> openCategory(profile, category, page));
                                }
                            }
                            else {
                                SlimefunPlugin.getLocal().sendMessage(pl, "messages.not-enough-xp", true);
                            }
                        }
                        return false;
                    });

                    index++;
                }
                else {
                    menu.addItem(index, sfitem.getItem());
                    menu.addMenuClickHandler(index, (pl, slot, item, action) -> {
                        try {
                            if (isSurvivalMode()) {
                                displayItem(profile, sfitem, true);
                            }
                            else {
                                if (sfitem instanceof MultiBlockMachine) {
                                    SlimefunPlugin.getLocal().sendMessage(pl, "guide.cheat.no-multiblocks");
                                }
                                else {
                                    pl.getInventory().addItem(sfitem.getItem().clone());
                                }
                            }
                        }
                        catch (Throwable x) {
                            printErrorMessage(pl, x);
                        }

                        return false;
                    });

                    index++;
                }
            }
        }

        menu.open(p);
    }

    @Override
    public void openSearch(PlayerProfile profile, String input, boolean addToHistory) {
        Player p = profile.getPlayer();
        if (p == null) return;

        ChestMenu menu = new ChestMenu(SlimefunPlugin.getLocal().getMessage(p, "guide.search.inventory").replace("%item%", ChatUtils.crop(ChatColor.RESET, input)));
        String searchTerm = input.toLowerCase(Locale.ROOT);

        if (addToHistory) {
            profile.getGuideHistory().add(searchTerm);
        }

        menu.setEmptySlotsClickable(false);
        createHeader(p, profile, menu);
        addBackButton(menu, 1, p, profile);

        int index = 9;
        // Find items and add them
        for (SlimefunItem item : SlimefunPlugin.getRegistry().getEnabledSlimefunItems()) {
            String itemName = ChatColor.stripColor(item.getItemName()).toLowerCase(Locale.ROOT);

            if (index == 44) break;

            if (!itemName.isEmpty() && (itemName.equals(searchTerm) || itemName.contains(searchTerm))) {
                ItemStack itemstack = new CustomItem(item.getItem(), meta -> {
                    List<String> lore = null;
                    Category category = item.getCategory();

                    if (category != null) {
                        ItemStack categoryItem = category.getItem(p);
                        if (categoryItem != null && categoryItem.hasItemMeta() && categoryItem.getItemMeta().hasDisplayName()) {
                            lore = Arrays.asList("", ChatColor.DARK_GRAY + "\u21E8 " + ChatColor.RESET + categoryItem.getItemMeta().getDisplayName());
                        }
                    }

                    meta.setLore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
                });

                menu.addItem(index, itemstack);
                menu.addMenuClickHandler(index, (pl, slot, itm, action) -> {
                    try {
                        if (!isSurvivalMode()) {
                            pl.getInventory().addItem(item.getItem().clone());
                        }
                        else {
                            displayItem(profile, item, true);
                        }
                    }
                    catch (Throwable x) {
                        printErrorMessage(pl, x);
                    }

                    return false;
                });

                index++;
            }
        }

        menu.open(p);
    }

    @Override
    public void displayItem(PlayerProfile profile, ItemStack item, int index, boolean addToHistory) {
        Player p = profile.getPlayer();
        if (p == null) return;

        if (item == null || item.getType() == Material.AIR) return;

        SlimefunItem sfItem = SlimefunItem.getByItem(item);

        if (sfItem != null) {
            displayItem(profile, sfItem, addToHistory);
            return;
        }

        if (!showVanillaRecipes) {
            return;
        }

        Recipe[] recipes = SlimefunPlugin.getMinecraftRecipes().getRecipesFor(item);

        if (recipes.length == 0) {
            return;
        }

        showMinecraftRecipe(recipes, index, item, profile, p, addToHistory);
    }

    private void showMinecraftRecipe(Recipe[] recipes, int index, ItemStack item, PlayerProfile profile, Player p, boolean addToHistory) {
        Recipe recipe = recipes[index];

        ItemStack[] recipeItems = new ItemStack[9];
        RecipeType recipeType = RecipeType.NULL;
        ItemStack result = null;

        Optional<MinecraftRecipe<? super Recipe>> optional = MinecraftRecipe.of(recipe);
        RecipeChoiceTask task = new RecipeChoiceTask();

        if (optional.isPresent()) {
            MinecraftRecipe<?> mcRecipe = optional.get();

            RecipeChoice[] choices = SlimefunPlugin.getMinecraftRecipes().getRecipeShape(recipe);

            if (choices.length == 1 && choices[0] instanceof MaterialChoice) {
                recipeItems[4] = new ItemStack(((MaterialChoice) choices[0]).getChoices().get(0));

                if (((MaterialChoice) choices[0]).getChoices().size() > 1) {
                    task.add(recipeSlots[4], (MaterialChoice) choices[0]);
                }
            }
            else {
                for (int i = 0; i < choices.length; i++) {
                    if (choices[i] instanceof MaterialChoice) {
                        recipeItems[i] = new ItemStack(((MaterialChoice) choices[i]).getChoices().get(0));

                        if (((MaterialChoice) choices[i]).getChoices().size() > 1) {
                            task.add(recipeSlots[i], (MaterialChoice) choices[i]);
                        }
                    }
                }
            }

            recipeType = new RecipeType(mcRecipe);
            result = recipe.getResult();
        }
        else {
            recipeItems = new ItemStack[] { null, null, null, null, new CustomItem(Material.BARRIER, "&4We are somehow unable to show you this Recipe :/"), null, null, null, null };
        }

        ChestMenu menu = create(p);

        if (addToHistory) {
            profile.getGuideHistory().add(item, index);
        }

        displayItem(menu, profile, p, item, result, recipeType, recipeItems, task);

        if (recipes.length > 1) {
            for (int i = 27; i < 36; i++) {
                menu.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }

            menu.addItem(28, ChestMenuUtils.getPreviousButton(p, index + 1, recipes.length), (pl, slot, action, stack) -> {
                if (index > 0) {
                    showMinecraftRecipe(recipes, index - 1, item, profile, p, true);
                }
                return false;
            });

            menu.addItem(34, ChestMenuUtils.getNextButton(p, index + 1, recipes.length), (pl, slot, action, stack) -> {
                if (index < recipes.length - 1) {
                    showMinecraftRecipe(recipes, index + 1, item, profile, p, true);
                }
                return false;
            });
        }

        menu.open(p);

        if (!task.isEmpty()) {
            task.start(menu.toInventory());
        }
    }

    @Override
    public void displayItem(PlayerProfile profile, SlimefunItem item, boolean addToHistory) {
        Player p = profile.getPlayer();
        if (p == null) return;

        ItemStack result = item.getRecipeOutput();
        RecipeType recipeType = item.getRecipeType();
        ItemStack[] recipe = item.getRecipe();

        ChestMenu menu = create(p);
        Optional<String> wiki = item.getWikipage();

        if (wiki.isPresent()) {
            menu.addItem(8, new CustomItem(Material.KNOWLEDGE_BOOK, ChatColor.RESET + SlimefunPlugin.getLocal().getMessage(p, "guide.tooltips.wiki"), "", ChatColor.GRAY + "\u21E8 " + ChatColor.GREEN + SlimefunPlugin.getLocal().getMessage(p, "guide.tooltips.open-category")));
            menu.addMenuClickHandler(8, (pl, slot, itemstack, action) -> {
                pl.closeInventory();
                ChatUtils.sendURL(pl, wiki.get());
                return false;
            });
        }

        RecipeChoiceTask task = new RecipeChoiceTask();

        if (addToHistory) {
            profile.getGuideHistory().add(item);
        }

        displayItem(menu, profile, p, item, result, recipeType, recipe, task);

        if (item instanceof RecipeDisplayItem) {
            displayRecipes(p, profile, menu, (RecipeDisplayItem) item, 0);
        }

        menu.open(p);

        if (!task.isEmpty()) {
            task.start(menu.toInventory());
        }
    }

    private void displayItem(ChestMenu menu, PlayerProfile profile, Player p, Object item, ItemStack output, RecipeType recipeType, ItemStack[] recipe, RecipeChoiceTask task) {
        boolean isSlimefunRecipe = item instanceof SlimefunItem;

        addBackButton(menu, 0, p, profile);

        MenuClickHandler clickHandler = (pl, slot, itemstack, action) -> {
            try {
                if (itemstack != null && itemstack.getType() != Material.BARRIER) {
                    displayItem(profile, itemstack, 0, true);
                }
            }
            catch (Throwable x) {
                printErrorMessage(pl, x);
            }
            return false;
        };

        for (int i = 0; i < 9; i++) {
            ItemStack recipeItem = getDisplayItem(p, isSlimefunRecipe, recipe[i]);
            menu.addItem(recipeSlots[i], recipeItem, clickHandler);

            if (recipeItem != null && item instanceof MultiBlockMachine) {
                for (Tag<Material> tag : MultiBlock.SUPPORTED_TAGS) {
                    if (tag.isTagged(recipeItem.getType())) {
                        task.add(recipeSlots[i], tag);
                        break;
                    }
                }
            }
        }

        menu.addItem(10, recipeType.getItem(p), ChestMenuUtils.getEmptyClickHandler());
        menu.addItem(16, output, ChestMenuUtils.getEmptyClickHandler());
    }

    protected void createHeader(Player p, PlayerProfile profile, ChestMenu menu) {
        for (int i = 0; i < 9; i++) {
            menu.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        // Settings Panel
        menu.addItem(1, ChestMenuUtils.getMenuButton(p));
        menu.addMenuClickHandler(1, (pl, slot, item, action) -> {
            SlimefunGuideSettings.openSettings(pl, pl.getInventory().getItemInMainHand());
            return false;
        });

        // Search feature!
        menu.addItem(7, ChestMenuUtils.getSearchButton(p));
        menu.addMenuClickHandler(7, (pl, slot, item, action) -> {
            pl.closeInventory();
            SlimefunPlugin.getLocal().sendMessage(pl, "guide.search.message");

            ChatInput.waitForPlayer(SlimefunPlugin.instance, pl, msg -> SlimefunGuide.openSearch(profile, msg, isSurvivalMode(), isSurvivalMode()));

            return false;
        });

        for (int i = 45; i < 54; i++) {
            menu.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    private void addBackButton(ChestMenu menu, int slot, Player p, PlayerProfile profile) {
        GuideHistory history = profile.getGuideHistory();

        if (isSurvivalMode() && history.size() > 1) {
            menu.addItem(slot, new CustomItem(ChestMenuUtils.getBackButton(p, "", "&rLeft Click: &7Go back to previous Page", "&rShift + left Click: &7Go back to Main Menu")));

            menu.addMenuClickHandler(slot, (pl, s, is, action) -> {
                if (action.isShiftClicked()) {
                    openMainMenu(profile, 1);
                }
                else {
                    history.goBack(this);
                }
                return false;
            });

        }
        else {
            menu.addItem(slot, new CustomItem(ChestMenuUtils.getBackButton(p, "", ChatColor.GRAY + SlimefunPlugin.getLocal().getMessage(p, "guide.back.guide"))));
            menu.addMenuClickHandler(slot, (pl, s, is, action) -> {
                openMainMenu(profile, 1);
                return false;
            });
        }
    }

    private static ItemStack getDisplayItem(Player p, boolean isSlimefunRecipe, ItemStack item) {
        if (isSlimefunRecipe) {
            SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
            if (slimefunItem == null) return item;

            String lore = Slimefun.hasPermission(p, slimefunItem, false) ? "&rNeeds to be unlocked elsewhere" : "&rNo Permission";
            return Slimefun.hasUnlocked(p, slimefunItem, false) ? item : new CustomItem(Material.BARRIER, ItemUtils.getItemName(item), "&4&l" + SlimefunPlugin.getLocal().getMessage(p, "guide.locked"), "", lore);
        }
        else {
            return item;
        }
    }

    private void displayRecipes(Player p, PlayerProfile profile, ChestMenu menu, RecipeDisplayItem sfItem, int page) {
        List<ItemStack> recipes = sfItem.getDisplayRecipes();

        if (!recipes.isEmpty()) {
            menu.addItem(53, null);

            if (page == 0) {
                for (int i = 27; i < 36; i++) {
                    menu.replaceExistingItem(i, new CustomItem(ChestMenuUtils.getBackground(), sfItem.getRecipeSectionLabel(p)));
                    menu.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
                }
            }

            int pages = (recipes.size() - 1) / 18 + 1;

            menu.replaceExistingItem(28, ChestMenuUtils.getPreviousButton(p, page + 1, pages));
            menu.addMenuClickHandler(28, (pl, slot, itemstack, action) -> {
                if (page > 0) {
                    displayRecipes(pl, profile, menu, sfItem, page - 1);
                    pl.playSound(pl.getLocation(), sound, 1, 1);
                }

                return false;
            });

            menu.replaceExistingItem(34, ChestMenuUtils.getNextButton(p, page + 1, pages));
            menu.addMenuClickHandler(34, (pl, slot, itemstack, action) -> {
                if (recipes.size() > (18 * (page + 1))) {
                    displayRecipes(pl, profile, menu, sfItem, page + 1);
                    pl.playSound(pl.getLocation(), sound, 1, 1);
                }

                return false;
            });

            int inputs = 36;
            int outputs = 45;

            for (int i = 0; i < 18; i++) {
                int slot;

                if (i % 2 == 0) {
                    slot = inputs;
                    inputs++;
                }
                else {
                    slot = outputs;
                    outputs++;
                }

                addDisplayRecipe(menu, profile, recipes, slot, i, page);
            }
        }
    }

    private void addDisplayRecipe(ChestMenu menu, PlayerProfile profile, List<ItemStack> recipes, int slot, int i, int page) {
        if ((i + (page * 18)) < recipes.size()) {
            ItemStack item = recipes.get(i + (page * 18));

            // We want to clone this item to avoid corrupting the original
            // but we wanna make sure no stupid addon creator sneaked some nulls in here
            if (item != null) item = item.clone();

            menu.replaceExistingItem(slot, item);

            if (page == 0) {
                menu.addMenuClickHandler(slot, (pl, s, itemstack, action) -> {
                    displayItem(profile, itemstack, 0, true);
                    return false;
                });
            }
        }
        else {
            menu.replaceExistingItem(slot, null);
            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    private ChestMenu create(Player p) {
        ChestMenu menu = new ChestMenu(SlimefunPlugin.getLocal().getMessage(p, "guide.title.main"));

        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), sound, 1, 1));
        return menu;
    }

    private void printErrorMessage(Player p, Throwable x) {
        p.sendMessage(ChatColor.DARK_RED + "An internal server error has occured. Please inform an admin, check the console for further info.");
        Slimefun.getLogger().log(Level.SEVERE, "An error has occured while trying to open a SlimefunItem in the guide!", x);
    }

}
