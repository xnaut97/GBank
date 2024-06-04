package com.github.xnaut97.gbank.core.framework.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

public abstract class Menu implements InventoryHolder {

    private static Plugin plugin;

    private final Inventory inventory;

    private final String title;

    private MenuElement[] elements;

    private boolean closed;

    public Menu(int row, String title) {
        row = 9 * Math.max(1, Math.min(6, row));
        if (title == null)
            title = "";
        this.inventory = Bukkit.createInventory(this, row, title.replace("&", "§"));
        this.elements = new MenuElement[row];
        this.title = title;
    }

    public static void forceCloseAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (inventory.getHolder() instanceof Menu)
                player.closeInventory();
        });
    }

    private static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName()
                .substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1);
    }

    private static int getVersionNumber() {
        return Integer.parseInt(getVersion().split("_")[1]);
    }

    public static void register(Plugin instance) {
        plugin = instance;
        Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                Inventory inv = e.getInventory();
                try {
                    if (inv.getHolder() != null && inv.getHolder() instanceof Menu)
                        ((Menu) inv.getHolder()).onClose(e);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            @EventHandler
            public void onOpen(InventoryOpenEvent e) {
                Inventory inv = e.getInventory();
                try {
                    if (inv.getHolder() != null && inv.getHolder() instanceof Menu)
                        ((Menu) inv.getHolder()).onOpen(e);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            @EventHandler
            public void onClick(InventoryClickEvent e) {
                Inventory inv = e.getClickedInventory();
                if (inv == null) return;
                try {
                    if (inv.getHolder() != null && inv.getHolder() instanceof Menu)
                        ((Menu) inv.getHolder()).onClick(e);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            @EventHandler
            public void onDrag(InventoryDragEvent event) {
                Inventory inv = event.getInventory();
                try {
                    if (inv.getHolder() != null && inv.getHolder() instanceof Menu)
                        ((Menu) inv.getHolder()).onDrag(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, plugin);

    }

    public static void unregister() {
        forceCloseAll();
        HandlerList.unregisterAll(plugin);
        plugin = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public MenuElement[] getElements() {
        return elements;
    }

    public void pushElements(MenuElement[] elements) {
        this.elements = elements;
    }

    public String getTitle() {
        return this.title;
    }

    @Nonnull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setTitle(Player player, String title, boolean async, long ticks) {
        setTitle(player, title);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                setTitle(player, getTitle());
            }
        };
        if (async)
            runnable.runTaskLaterAsynchronously(plugin, ticks);
        else
            runnable.runTaskLater(plugin, ticks);
    }

    public void setTitle(Player player, String title) {
        InventoryUpdate.updateInventory(getPlugin(), player, title);
    }

    protected final void onClick(InventoryClickEvent event) {
        MenuElement element = getElements()[event.getSlot()];
        if (element == null) return;
        element.onClick(event);
        if (element.getSound() == null) return;
        element.getSound().play((Player) event.getWhoClicked());
    }

    protected final void onOpen(InventoryOpenEvent event) {
        Arrays.stream(getElements()).filter(Objects::nonNull).forEach(element -> element.onOpen(event));
        onOpenActions(event);
    }

    protected final void onClose(InventoryCloseEvent event) {
        closed = true;
        Arrays.stream(getElements()).filter(Objects::nonNull).forEach(element -> element.onClose(event));
        onCloseActions(event);
    }

    protected final void onDrag(InventoryDragEvent event) {
        ItemStack oldCursor = event.getOldCursor();
        ItemStack toUpdate = oldCursor.clone();
        event.getInventorySlots().forEach(slot -> {
            MenuElement element = getElements()[slot];
            if (element == null) return;
            if (!(element instanceof DraggableElement)) {
                ItemStack item = getInventory().getItem(slot);
                Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), () -> {
                    if(item == null) {
                        getInventory().setItem(slot, null);
                        return;
                    }
                    item.setAmount(item.getAmount() - 1);
                }, 1);

                toUpdate.setAmount(toUpdate.getAmount() - 1);
                return;
            }
            ((DraggableElement) element).onDrag(slot, event.getOldCursor(), event.getCursor());
            if (element.getSound() == null) return;
            element.getSound().play((Player) event.getWhoClicked());
        });
        event.setCursor(toUpdate);
    }

    /**
     * Push element to specific slot in inventory
     *
     * @param slot    Slot to push element
     * @param element Element to push
     */
    public void pushElement(int slot, MenuElement element) {
        if (slot < 0 || slot >= getInventory().getSize())
            return;
        getInventory().setItem(slot, element.getItem());
        getElements()[slot] = element;
    }

    /**
     * Open current inventory for player
     *
     * @param player Player to open
     */
    public void open(Player player) {
        open(player, false);
    }

    /**
     * Open other inventory for player
     *
     * @param player    Player to open
     */
    public void open(Player player, boolean async) {
        if (async) Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> player.openInventory(getInventory()));
        else player.openInventory(getInventory());
    }

    public void close(Player player) {
        close(player, false);
    }

    /**
     * Force player to close menu
     *
     * @param player Player to close
     */
    public void close(Player player, boolean async) {
        if (!isOpening(player)) return;
        if (!async) player.closeInventory();
        else Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), player::closeInventory);
    }

    /**
     * Force close for all player is opening this inventory
     */
    public void closeAll(boolean async) {
        getInventory().getViewers().forEach(entity -> close((Player) entity, async));
    }

    public boolean recalculate(int page, int listSize, int slots) {
        return page != 0 && listSize <= page * slots;
    }

    public boolean isMax(int page, int listSize, int slots) {
        return (page + 1) * slots >= listSize;
    }

    /**
     * Triggered when open inventory
     */
    public abstract void onOpenActions(InventoryOpenEvent event);

    /**
     * Triggered when close inventory
     *
     * @param event
     */
    public abstract void onCloseActions(InventoryCloseEvent event);

    public boolean isOpening(Player player) {
        return player.getOpenInventory().getTopInventory().getHolder() instanceof Menu;
    }

}
