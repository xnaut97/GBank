package com.github.xnaut97.gbank.core.framework.menu.type;

import com.github.xnaut97.gbank.core.framework.menu.Menu;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RunnableMenu extends Menu {

    private final AtomicBoolean running;

    private final boolean async;

    private final int ticks;

    public RunnableMenu(int size, String title, int ticks, boolean async) {
        super(size, title);
        this.ticks = ticks;
        this.async = async;
        this.running = new AtomicBoolean(true);
        init();
    }

    public boolean isAsync() {
        return async;
    }

    public int getTicks() {
        return ticks;
    }

    private void init() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!running.get()) {
                    cancel();
                    onCancel();
                    return;
                }
                onTick();
            }
        };
        if (async)
            runnable.runTaskTimerAsynchronously(getPlugin(), 0, ticks);
        else
            runnable.runTaskTimer(getPlugin(), 0, ticks);
    }

    @Override
    public void onOpenActions(InventoryOpenEvent event) {
        running.set(true);
    }

    @Override
    public void onCloseActions(InventoryCloseEvent event) {
        running.set(false);
    }

    public abstract void onTick();

    public void onCancel() {
    }

}