package com.github.xnaut97.gbank.core.framework.menu.type;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.gbank.core.framework.menu.Menu;
import com.github.xnaut97.gbank.core.framework.menu.MenuElement;
import com.github.xnaut97.gbank.core.utils.ItemCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

@Getter
public abstract class PaginationMenu<T> extends Menu {

    @Getter(AccessLevel.PRIVATE)
    private int[] borderSlots =
            {36, 27, 18, 9,
                    0, 1, 2, 3, 4, 5, 6, 7, 8,
                    17, 26, 35, 44,
                    45, 46, 47, 48, 49, 50, 51, 52, 53};

    @Getter(AccessLevel.PRIVATE)
    private int[] itemSlots =
            {10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34,
                    37, 38, 39, 40, 41, 42, 43};

    private int page;

    protected Player player;

    @Setter
    private boolean updateInstantly = true;

    @Setter
    private long updateTick = 1;

    @Getter(AccessLevel.NONE)
    private BukkitRunnable runnable;

    private boolean exit;

    public PaginationMenu(int page, int row, String title) {
        super(9 * row, title);
        this.page = page;
        setup();
    }

    private void setupBackground() {
        for (int slot : borderSlots) {
            pushElement(slot, new MenuElement(getBorderItem(), " ") {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.setCancelled(true);
                }
            });
        }
        for (int slot : itemSlots) {
            pushElement(slot, new MenuElement(getPlaceholderItem(), " ") {
                @Override
                public void onClick(InventoryClickEvent event) {
                    event.setCancelled(true);
                }
            });
        }
    }

    private void setup() {
        setupBackground();
        this.runnable = new BukkitRunnable() {
            private long count = 0;

            @Override
            public void run() {
                count++;
                if (count == Long.MAX_VALUE)
                    count = 0;
                if (count % updateTick > 0)
                    return;
                if (count / updateTick > 1 && !updateInstantly)
                    return;
                update();
            }
        };
    }

    public void update() {
        List<T> validated = getObjects().stream()
                .filter(this::onValidate).toList();
        for (int i = 0; i < getItemSlots().length; i++) {
            int index = (getPage() + 1) * i;
            if (index >= validated.size()) {
                pushElement(getItemSlots()[i], new MenuElement(fillOtherSlotWhenFull() == null
                        ? new ItemStack(Material.AIR) : fillOtherSlotWhenFull(), " ") {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        event.setCancelled(true);
                    }
                });
                continue;
            }
            T object = validated.get(index);

            pushElement(getItemSlots()[i], getObjectItem(index, object));
        }

        pushElement(getPreviousButtonSlot(), getPage() == 0
                ? new MenuElement(getBorderItem(), " ") {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
            }
        }
                : new MenuElement(getPreviousButton()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                onPreviousButtonClick(event);
                previous();
            }
        });

        pushElement(getInfoButtonSlot(), new MenuElement(getInfoButton()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                onInfoButtonClick(event);
            }
        });

        boolean reachMax = getPage() == getMaxPage();
        pushElement(getNextButtonSlot(), reachMax
                ? new MenuElement(getBorderItem(), " ") {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
            }
        }
                : new MenuElement(getNextButton()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                onNextButtonClick(event);
                next();
            }
        });
        onIndexComplete();
    }

    public void setPage(int page) {
        this.page = Math.max(0, Math.min(getMaxPage(), page));
    }

    public void next() {
        this.page = Math.min(page + 1, getMaxPage());
    }

    public void previous() {
        this.page = Math.max(page - 1, 0);
    }

    public int getMaxPage() {
        return (int) getObjects().stream().filter(this::onValidate).count()
                / itemSlots.length;
    }

    public ItemStack getBorderItem() {
        return XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
    }

    public ItemStack getPlaceholderItem() {
        return XMaterial.GRAY_STAINED_GLASS_PANE.parseItem();
    }

    @Override
    public void onOpenActions(InventoryOpenEvent event) {
        this.player = (Player) event.getPlayer();
        if (this.runnable != null)
            this.runnable.runTaskTimerAsynchronously(getPlugin(), 0, 1);
    }

    @Override
    public void onCloseActions(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (getPlayer() != null && player.getUniqueId().equals(getPlayer().getUniqueId()))
            exit = true;
        if (this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        }
    }

    public int getPreviousButtonSlot() {
        return 48;
    }

    public ItemStack getPreviousButton() {
        return new ItemCreator(Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem()))
                .setDisplayName("&e« PREVIOUS")
                .addLore("&7Back to page &e" + getPage())
                .setTexture("86971dd881dbaf4fd6bcaa93614493c612f869641ed59d1c9363a3666a5fa6")
                .build();
    }

    public void onPreviousButtonClick(InventoryClickEvent event) {
    }

    public int getInfoButtonSlot() {
        return 49;
    }

    public ItemStack getInfoButton() {
        return new ItemCreator(Objects.requireNonNull(XMaterial.PAPER.parseItem()))
                .setDisplayName("&ePage " + (getPage() + 1)).build();
    }

    public void onInfoButtonClick(InventoryClickEvent event) {
    }

    public int getNextButtonSlot() {
        return 50;
    }

    public ItemStack getNextButton() {
        return new ItemCreator(Objects.requireNonNull(XMaterial.PLAYER_HEAD.parseItem()))
                .setDisplayName("&eNEXT »")
                .addLore("&7Go to page &e" + (getPage() + 2))
                .setTexture("f32ca66056b72863e98f7f32bd7d94c7a0d796af691c9ac3a9136331352288f9")
                .build();
    }

    public void onNextButtonClick(InventoryClickEvent event) {
    }

    public boolean onValidate(T object) {
        return true;
    }

    public abstract List<T> getObjects();

    public abstract MenuElement getObjectItem(int index, T object);

    public ItemStack fillOtherSlotWhenFull() {
        return null;
    }

    public void onIndexComplete() {
    }
}