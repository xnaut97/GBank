package com.github.xnaut97.gbank.core.listener;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ListenerManager {

    private final GBankPlugin plugin;

    private final List<AbstractListener> listeners = Lists.newArrayList();

    public ListenerManager(GBankPlugin plugin) {
        this.plugin = plugin;
        listeners.add(new PlayerJoinListener(plugin));
        register();
    }

    public void register() {
        listeners.forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, plugin));
    }

    public void unregister() {
        HandlerList.unregisterAll(plugin);
    }
}
