package com.github.xnaut97.gbank.core;

import com.github.xnaut97.gbank.api.GBankAPI;
import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.command.CommandManager;
import com.github.xnaut97.gbank.core.data.BankManagerImpl;
import com.github.xnaut97.gbank.core.framework.menu.Menu;
import com.github.xnaut97.gbank.core.listener.ListenerManager;
import com.github.xnaut97.gbank.core.thread.BankOnlineReward;
import com.github.xnaut97.gbank.core.utils.AbstractDatabase.DatabaseElement;
import com.github.xnaut97.gbank.core.utils.AbstractDatabase.MySQL;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Getter
public class GBankPluginImpl extends JavaPlugin implements GBankPlugin {

    private BankManager bankManager;

    private CommandManager commandManager;

    private ListenerManager listenerManager;

    private YamlDocument settings, language, command;

    private MySQL database;

    private GBankAPI api;

    private BankOnlineReward bankOnlineReward;

    @Override
    public void onEnable() {
        Menu.register(this);
        saveResource("currencies.yml", false);
        reloadDocuments();
        this.api = new GBankInternalAPI(this);
        GBankAPIProvider.setInstance(api);
        this.bankManager = new BankManagerImpl(this);
        this.commandManager = new CommandManager(this);
        this.listenerManager = new ListenerManager(this);
        this.bankOnlineReward = new BankOnlineReward(this);
    }

    @Override
    public void onDisable() {
        if(commandManager != null) commandManager.unregister();
        if(listenerManager != null) listenerManager.unregister();
        if(bankOnlineReward != null && Bukkit.getScheduler().isCurrentlyRunning(bankOnlineReward.getTaskId()))
            bankOnlineReward.cancel();
    }

    @Override
    public BankManager getBankManager() {
        return this.bankManager;
    }

    private void reloadDocuments() {
        try {
            this.settings = YamlDocument.create(new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());


            this.language = YamlDocument.create(new File(getDataFolder(), "language.yml"),
                    Objects.requireNonNull(getResource("language.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("lang-version")).build());

            this.command = YamlDocument.create(new File(getDataFolder(), "commands.yml"),
                    Objects.requireNonNull(getResource("commands.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("command-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupDatabase() {
        boolean toggle = getConfig().getBoolean("database.toggle", true);
        if (!toggle)
            return;
        String username = getConfig().getString("database.username", "root");
        String password = getConfig().getString("database.password", "password");
        String name = getConfig().getString("database.name", "authenticator");
        String host = getConfig().getString("database.host", "localhost");
        String port = getConfig().getString("database.port", "3306");
        String tableName = getConfig().getString("database.table-name", "user");
        int poolSize = getConfig().getInt("database.pool.max-pool-size", 10);
        int timeout = getConfig().getInt("database.pool.timeout", 5000);
        int idleTimeout = getConfig().getInt("database.pool.idle-timeout", 600000);
        int lifeTime = getConfig().getInt("database.pool.max-life-time", 1800000);
        this.database = new MySQL(this, username, password, name, host, port, poolSize, timeout, idleTimeout, lifeTime);
        if (!this.database.isConnected()) {
            getLogger().info("Use local cache instead.");
            return;
        }
        this.database.createTable(tableName,
                new DatabaseElement("uuid", DatabaseElement.Type.VAR_CHAR, true),
                new DatabaseElement("player_name", DatabaseElement.Type.LONG_TEXT));
    }
}
