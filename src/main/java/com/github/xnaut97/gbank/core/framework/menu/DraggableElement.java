package com.github.xnaut97.gbank.core.framework.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DraggableElement extends MenuElement {

    private boolean draggable;

    public DraggableElement(Material item, int amount) {
        super(item, amount);
    }

    public DraggableElement(Material item) {
        super(item);
    }

    public DraggableElement(ItemStack item, String name) {
        super(item, name);
    }

    public DraggableElement(Material material, String name) {
        super(material, name);
    }

    public DraggableElement(ItemStack item) {
        super(item);
    }

    public boolean isDraggable() {
        return this.draggable;
    }

    public DraggableElement setDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public void onDrag(int slot, @Nullable ItemStack oldCursor, @Nullable ItemStack newCursor) {

    }
}
