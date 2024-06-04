package com.github.xnaut97.gbank.core.framework.menu;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuElement {

    private final ItemStack item;

    private ElementSound sound;

    public MenuElement(Material item, int amount) {
        this.item = new ItemStack(item, amount);
    }

    public MenuElement(Material item) {
        this.item = new ItemStack(item);
    }

    public MenuElement(ItemStack item, String name) {
        this.item = item == null ? new ItemStack(Material.AIR) : item;
        ItemMeta meta = getItem().getItemMeta();
        if(meta == null) return;
        if(name == null) return;
        meta.setDisplayName(name);
        getItem().setItemMeta(meta);
    }

    public MenuElement(Material material, String name) {
        this(new ItemStack(material), name);
    }

    public MenuElement(ItemStack item) {
        this(item, null);
    }

    public void onClick(InventoryClickEvent event) {}

    public void onClose(InventoryCloseEvent event) {}

    public void onOpen(InventoryOpenEvent event) {}

    public ItemStack getItem() {
        return this.item;
    }

    public ElementSound getSound() {
        return sound;
    }

    public MenuElement setSound(XSound sound) {
        return setSound(sound, 1, 1);
    }

    public MenuElement setSound(XSound sound, float volume, float pitch) {
        this.sound = new ElementSound(sound, volume, pitch);
        return this;
    }

}