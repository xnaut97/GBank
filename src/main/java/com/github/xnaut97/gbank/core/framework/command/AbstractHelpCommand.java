package com.github.xnaut97.gbank.core.framework.command;

import com.github.xnaut97.gbank.core.utils.ClickableText;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class AbstractHelpCommand extends CommandArgument implements ConsoleExecutor, AutoCompletion {

    private final Map<String, CommandArgument> subCommands;

    private final AbstractCommand handle;

    AbstractHelpCommand(AbstractCommand handle) {
        this.handle = handle;
        this.subCommands = handle.getArguments();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return handle.getName() + ".command.help";
    }

    @Override
    public String getPermissionDescription() {
        return "Access help command.";
    }

    @Override
    public PermissionDefault getPermissionDefault() {
        return PermissionDefault.TRUE;
    }

    @Override
    public String getDescription() {
        return "Shows available commands.";
    }

    @Override
    public String getUsage() {
        return handle.getName() + " help [page]";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("?");
    }

    @Override
    public void playerExecute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            handleCommands(sender, 0);
            return;
        }
        int page = getPage(args[0]);
        handleCommands(sender, page);
    }

    @Override
    public void consoleExecute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            handleCommands(sender, 0);
            return;
        }
        int page = getPage(args[0]);
        handleCommands(sender, page);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            int max = handle.getArguments().size() / handle.getHelpSuggestions();
            return IntStream.rangeClosed(1, max).boxed().map(String::valueOf)
                    .filter(i -> i.startsWith(args[0])).toList();
        }
        return null;
    }

    private int getPage(String str) {
        try {
            int page = Integer.parseInt(str);
            return Math.max(page, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    private void handleCommands(CommandSender sender, int page) {
        List<CommandArgument> filter = Lists.newArrayList();
        subCommands.values().stream()
                .sorted(Comparator.comparing(CommandArgument::getName))
                .forEachOrdered(command -> {
                    boolean hasPermission = command.getPermission() != null || !command.getPermission().isEmpty();
                    if (!hasPermission || sender.hasPermission(command.getPermission())) {
                        filter.stream().filter(c -> c.getName().equalsIgnoreCase(command.getName())).findFirst()
                                .ifPresentOrElse(c -> {
                                }, () -> filter.add(command));
                    }
                });
        int max = Math.min(handle.getHelpSuggestions() * (page + 1), filter.size());
        if (handle.getHelpHeader() != null)
            sender.sendMessage(handle.getHelpHeader().replace("&", "§"));
        sender.sendMessage(" ");
        sender.sendMessage("§e[ ]§7: required params   §5{ }§7: optional params");
        sender.sendMessage(" ");
        for (int i = page * handle.getHelpSuggestions(); i < max; i++) {
            CommandArgument command = filter.get(i);
            TextComponent clickableCommand = createClickableCommand(command);
            if (sender instanceof Player)
                ((Player) sender).spigot().sendMessage(clickableCommand);
            else
                sender.sendMessage("§7■ " + handle.getHelpCommandColor() + handle.getUsage() + ": "
                        + handle.getHelpDescriptionColor() + command.getDescription());

            if (command.showAliases() && command.getAliases() != null && !command.getAliases().isEmpty()) {
                sender.sendMessage("§7└ Aliases: " + command.getAliases().stream()
                        .map(alias -> "§3" + alias).collect(Collectors.joining("§7, ")));
            }
        }
        if (sender instanceof Player player) {
            TextComponent previousPage = createClickableButton("&e&l«",
                    "/" + handle.getName() + " help " + (page - 1),
                    "&7Previous page");
            TextComponent nextPage = createClickableButton("&e&l»",
                    "/" + handle.getName() + " help " + (page + 1),
                    "&7Next page");
            TextComponent pageInfo = createClickableButton(" &e&l" + (page + 1) + " ",
                    null, "&7You're in page " + (page + 1));
            ClickableText spacing = new ClickableText("                       ");
            boolean canNextPage = handle.getHelpSuggestions() * (page + 1) < filter.size();
            if (page < 1) {
                if (canNextPage)
                    player.spigot().sendMessage(spacing.build(), pageInfo, nextPage);
                else
                    player.spigot().sendMessage(spacing.build(), pageInfo);

            } else {
                if (canNextPage)
                    player.spigot().sendMessage(spacing.build(), previousPage, pageInfo, nextPage);
                else
                    player.spigot().sendMessage(spacing.build(), previousPage, pageInfo);
            }
        }
        if (handle.getHelpFooter() != null)
            sender.sendMessage(handle.getHelpFooter().replace("&", "§"));
    }

    private TextComponent createClickableCommand(CommandArgument command) {
        return new ClickableText("&7■ /" + handle.getHelpCommandColor() + command.getUsage() + ": "
                + handle.getHelpDescriptionColor() + command.getDescription())
                .setHoverAction(HoverEvent.Action.SHOW_TEXT, "&7Click to get this command.")
                .setClickAction(ClickEvent.Action.SUGGEST_COMMAND, "/" + command.getUsage())
                .build();
    }

    private TextComponent createClickableButton(String name, String clickAction, String... hoverAction) {
        ClickableText clickableText = new ClickableText(name);
        if (hoverAction.length > 0)
            clickableText.setHoverAction(HoverEvent.Action.SHOW_TEXT, hoverAction);
        if (clickAction != null)
            clickableText.setClickAction(ClickEvent.Action.RUN_COMMAND, clickAction);
        return clickableText.build();
    }
}