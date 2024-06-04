package com.github.xnaut97.gbank.core.framework.menu;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class ElementSound {

    private XSound sound = XSound.UI_BUTTON_CLICK;

    private float volume = 1;

    private float pitch = 1;

    private ElementSound() {

    }

    public ElementSound(XSound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public ElementSound(Sound sound, float volume, float pitch) {
        try {
            this.sound = XSound.matchXSound(sound);
        } catch (Exception ignored) {

        }
        this.volume = volume;
        this.pitch = pitch;
    }

    public static ElementSound useDefault() {
        return new ElementSound();
    }

    public void play(Player player) {
        this.sound.play(player, volume, pitch);
    }

}