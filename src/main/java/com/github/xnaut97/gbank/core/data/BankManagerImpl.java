package com.github.xnaut97.gbank.core.data;

import com.github.xnaut97.gbank.api.*;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.GBankPluginImpl;
import com.github.xnaut97.gbank.core.utils.AbstractDatabase;
import com.github.xnaut97.gbank.core.utils.AbstractDatabase.DatabaseInsertion;
import com.github.xnaut97.gbank.core.utils.AbstractDatabase.MySQL;
import com.github.xnaut97.gbank.core.utils.ItemCreator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankManagerImpl implements BankManager {

    private final GBankPlugin plugin;

    private final Map<String, BankCurrency> currencies = Maps.newHashMap();

    private final Map<UUID, BankAccount> accounts = Maps.newHashMap();

    public BankManagerImpl(GBankPlugin plugin) {
        this.plugin = plugin;
        loadCurrencies();
        loadAccounts();
        registerOnline();
    }

    private void registerOnline() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            BankAccount account = getAccount(player);
            if(account == null) {
                account = new BankAccountImpl(player);
                for (BankCurrency currency : getCurrencies()) {
                    account.setBalance(currency, 0);
                }
                addAccount(account);
                plugin.getLogger().warning("Register new account for player '" + player.getName() + "'");
            }
        });
    }

    private void loadCurrencies() {
        File file = new File(plugin.getDataFolder() + "/currencies.yml");
        if(!file.exists()) {
            plugin.getLogger().severe("Could not load any currencies due to file not found!");
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.getKeys(false).forEach(id -> {
            String name = config.getString(id + ".name", null);
            String prefix = config.getString(id + ".prefix", null);
            Material material = Material.matchMaterial(config.getString(id + ".icon.item", "STONE"));
            double tax = Double.parseDouble(config.getString(id + ".tax", "0").replace("%", ""));
            if(name != null && prefix != null && material != null) {
                ItemCreator creator = new ItemCreator(material);
                if(material == Material.PLAYER_HEAD) {
                    String texture = config.getString(id + ".icon.texture", null);
                    if(texture != null)
                        creator.setTexture(texture);
                }
                this.currencies.putIfAbsent(id, new BankCurrencyImpl(id, name, prefix, creator.build(), tax));
            }
        });
        if(!currencies.isEmpty())
            plugin.getLogger().warning("Loaded " + currencies.size() + " currencies!");
    }

    @Override
    public List<BankCurrency> getCurrencies() {
        return Lists.newArrayList(this.currencies.values());
    }

    @Override
    public BankCurrency getCurrency(String id) {
        return this.currencies.getOrDefault(id, null);
    }

    @Override
    public List<BankAccount> getAccounts() {
        return Lists.newArrayList(this.accounts.values());
    }

    @Override
    @Nullable
    public BankAccount getAccount(OfflinePlayer player) {
        return getAccount(player.getUniqueId());
    }

    @Override
    public BankAccount getAccount(UUID uuid) {
        return this.accounts.getOrDefault(uuid, null);
    }

    @Override
    public BankAccount getAccount(String playerName) {
        return this.accounts.values().stream().filter(account -> account.getPlayerName().equals(playerName)).findAny().orElse(null);
    }

    @Override
    public void addAccount(BankAccount account) {
        this.accounts.putIfAbsent(account.getPlayerUniqueId(), account);
    }

    @Override
    public void deleteAccount(OfflinePlayer player) {
        this.accounts.remove(player.getUniqueId());
    }

    @Override
    public void deleteAccount(UUID uuid) {
        this.accounts.remove(uuid);
    }

    @Override
    public void deleteAccount(String playerName) {
        BankAccount account = getAccount(playerName);
        if(account != null)
            deleteAccount(account.getPlayerUniqueId());
    }

    @Override
    public void saveAccount(BankAccount account) {
        MySQL database = ((GBankPluginImpl) plugin).getDatabase();
        if(database != null && database.isConnected()) saveSQL(account);
        else saveJson(account);
    }

    private void saveJson(BankAccount account) {
        try {
            JsonArray mainArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", account.getPlayerUniqueId().toString());
            jsonObject.addProperty("name", account.getPlayerName());

            JsonArray balanceArray = new JsonArray();
            account.getAllBalance().forEach(balance -> {
                JsonObject object = new JsonObject();
                object.addProperty("id", balance.getCurrency().getId());
                object.addProperty("amount", balance.getAmount());
                balanceArray.add(object);
            });
            jsonObject.add("balance", balanceArray);
            mainArray.add(jsonObject);

            File file = new File(plugin.getDataFolder() + "/userdata/" + account.getPlayerUniqueId().toString() + ".json");
            if(!file.exists()) file.createNewFile();
            try(PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(mainArray);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveSQL(BankAccount account) {
        MySQL database = ((GBankPluginImpl) plugin).getDatabase();
        String uuid = account.getPlayerUniqueId().toString();
        ((GBankPluginImpl) plugin).getSettings().getOptionalString("database.table-name").ifPresent(tableName -> {
            List<DatabaseInsertion> insertions = Lists.newArrayList();
            insertions.add(new DatabaseInsertion("uuid", uuid));
            insertions.add(new DatabaseInsertion("balance", account.getAllBalance().stream()
                    .map(balance -> balance.getCurrency().getId() + ":" + balance.getAmount())
                    .collect(Collectors.joining(","))));

            database.addOrUpdate(tableName,
                    new DatabaseInsertion("uuid", uuid),
                    insertions.toArray(new DatabaseInsertion[0]));
        });
    }

    @Override
    public void loadAccounts() {
        MySQL database = ((GBankPluginImpl) plugin).getDatabase();
        if(database != null && database.isConnected()) loadSQL();
        else loadJson();
    }

    @Override
    public void reload() {
        loadCurrencies();
        loadAccounts();
    }

    private void loadJson() {
        try {
            File folder = new File(plugin.getDataFolder() + "/userdata");
            if(!folder.exists()) {
                folder.mkdirs();
                return;
            }
            File[] files = folder.listFiles();
            if(files == null || files.length == 0) return;

            for (File file : files) {
                JsonArray mainArray = (JsonArray) JsonParser.parseReader(new FileReader(file));
                for (JsonElement element : mainArray) {
                    JsonObject jsonObject = (JsonObject) element;

                    UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
                    BankAccount account = new BankAccountImpl(Bukkit.getOfflinePlayer(uuid));
                    // Load balance array
                    JsonArray balanceArray = jsonObject.getAsJsonArray("balance");
                    for (JsonElement jsonElement : balanceArray) {
                        JsonObject balanceObject = (JsonObject) jsonElement;
                        String id = balanceObject.get("id").getAsString();
                        double amount = balanceObject.get("amount").getAsDouble();

                        //Check if currency is registered
                        BankCurrency currency = getCurrency(id);
                        if(currency == null) continue;
                        account.setBalance(currency, amount);
                    }

                    this.accounts.put(uuid, account);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSQL() {
        MySQL database = ((GBankPluginImpl) plugin).getDatabase();
        ((GBankPluginImpl) plugin).getSettings().getOptionalString("database.table-name").ifPresent(tableName -> {
            try (Connection connection = database.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    BankAccount account = new BankAccountImpl(Bukkit.getOfflinePlayer(uuid));
                    String balanceString = rs.getString("balance");

                    Arrays.stream(balanceString.split(",")).forEach(data -> {
                        String[] dataSplit = data.split(":");
                        if(dataSplit.length != 2) return;
                        String id = dataSplit[0];
                        double amount = Double.parseDouble(dataSplit[1]);

                        BankCurrency currency = getCurrency(id);
                        if(currency == null) return;
                        account.setBalance(currency, amount);

                        this.accounts.put(uuid, account);
                    });
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
