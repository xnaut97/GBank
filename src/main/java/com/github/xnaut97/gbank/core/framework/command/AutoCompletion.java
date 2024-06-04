package com.github.xnaut97.gbank.core.framework.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface AutoCompletion {

    List<String> tabComplete(CommandSender sender, String[] args);

}
