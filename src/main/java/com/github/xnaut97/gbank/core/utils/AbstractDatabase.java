package com.github.xnaut97.gbank.core.utils;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractDatabase {

    private final Plugin plugin;

    private final String username;

    private String password;

    private final String name;

    private String host;

    private boolean connected = false;

    public AbstractDatabase(Plugin plugin, String username, String password, String name, String host) {
        this.plugin = plugin;
        this.username = username;
        if (!password.isEmpty())
            this.password = password;
        this.name = name;
        this.host = host;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public boolean isConnected() {
        return this.connected;
    }

    protected void setConnected(boolean connected) {
        this.connected = connected;
    }

    protected abstract void connect();

    public static abstract class DeprecatedDatabase extends AbstractDatabase {
        private final String port;

        private final HikariConfig hikariConfig;

        private HikariDataSource dataSource;

        public DeprecatedDatabase(Plugin plugin, String username, String password, String name, String host, String port, int poolSize, int timeout, int idleTimeout, int lifeTime) {
            super(plugin, username, password, name, host);
            this.port = port;

            hikariConfig = new HikariConfig();
            String url = "jdbc:mysql://" + getHost() + (getPort().equalsIgnoreCase("default") ? ":3306" : ":" + getPort()) + "/" + getName();
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(getUsername());
            hikariConfig.setPassword(getPassword());
            hikariConfig.setMaximumPoolSize(poolSize);
            hikariConfig.setConnectionTimeout(timeout);
            hikariConfig.setIdleTimeout(idleTimeout);
            hikariConfig.setMaxLifetime(lifeTime);

            connect();
        }

        public HikariDataSource getDataSource(){
            return dataSource;
        }

        public String getPort() {
            return this.port;
        }

        public Connection getConnection() throws SQLException {
            return dataSource.getConnection();
        }

        public void connect() {
            try {
                getPlugin().getLogger().info("Connecting to database...");

                dataSource = new HikariDataSource(hikariConfig);

                getPlugin().getLogger().info("Connected to database success!");
                setConnected(true);
            } catch (Exception e) {
                getPlugin().getLogger().severe("Couldn't connect to database!");
            }
        }

        public boolean createDatabase(String databaseName) {
            try {
                if (hasDatabase(databaseName))
                    return false;
                try (Connection connection = getConnection()){
                    connection.createStatement().executeUpdate("CREATE DATABASE " + databaseName);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean hasDatabase(String databaseName) {
            try (Connection connection = getConnection()){
                ResultSet set = connection.getMetaData().getCatalogs();
                boolean exist = false;
                while (set.next()) {
                    String name = set.getString(1);
                    if (name.equals(databaseName)) {
                        exist = true;
                        break;
                    }
                }
                return exist;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean createTable(String name, DatabaseElement... elements) {
            try {
                if (!isConnected())
                    return false;
                StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + name + "` (");
                for (DatabaseElement element : elements) {
                    StringBuilder typeBuilder = new StringBuilder(element.getType().name().replace("_", ""));
                    if (element.getType() == DatabaseElement.Type.VAR_CHAR)
                        typeBuilder.append("(255)");
                    if (element.isPrimary) {
                        builder.append("`").append(element.getName()).append("` ")
                                .append(typeBuilder).append(" NOT NULL ");
                    } else {
                        builder.append("`").append(element.getName()).append("` ")
                                .append(typeBuilder).append(" NULL ");
                    }
                    if(element.isPrimary) builder.append(" PRIMARY KEY");
                    builder.append(", ");
                }
                builder.delete(builder.length() - 2, builder.length());
                builder.append(");");
                Connection connection = getConnection();
                connection.createStatement().executeUpdate(builder.toString());
                connection.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public List<String> getTables() {
            try {
                ResultSet rs = getConnection().getMetaData().getTables(null, null, "%", null);
                List<String> list = Lists.newArrayList();
                while (rs.next())
                    list.add(rs.getString(3));
                return list;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public boolean has(String table, String toFind, String key, Object value) {
            try (Connection connection = getConnection()){
                String query = "SELECT `" + toFind + "` FROM `" + table + "` WHERE `" + key + "`='" + value + "'";
                boolean has = connection.createStatement().executeQuery(query).next();
                connection.close();
                return has;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean add(String table, DatabaseInsertion... insertions) {
            try (Connection connection = getConnection()){
                StringBuilder query = new StringBuilder("INSERT INTO `" + table + "` (");
                String queryKey = Arrays.stream(insertions).map(i -> "`" + i.getKey() + "`").collect(Collectors.joining(", "));
                String queryValue = Arrays.stream(insertions).map(i -> "'" + i.getValue() + "'").collect(Collectors.joining(", "));
                query.append(queryKey).append(") VALUES (").append(queryValue).append(")");
                query.append(" ON DUPLICATE KEY UPDATE ");
                for (DatabaseInsertion insertion: insertions) {
                    if (insertion.getKey().equalsIgnoreCase("uuid")) {
                        continue;
                    }
                    query.append("`").append(insertion.getKey()).append("`").append("=").append("'").append(insertion.getValue()).append("'").append(", ");
                }
                query.delete(query.length() - 2, query.length());
                connection.prepareStatement(query.toString()).executeUpdate();
                connection.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean addOrUpdate(String table, DatabaseInsertion check, DatabaseInsertion... insertions) {
//            if (check != null) {
//                boolean found = has(table, check.getKey(), check.getKey(), check.getValue());
//                if (found)
//                    return update(table, check, insertions);
//            }
            return add(table, insertions);
        }

        public boolean remove(String table, String key, Object value) {
            try (Connection connection = getConnection()){
                String query = "DELETE FROM `" + table + "` WHERE `" + key + "`='" + value + "'";
                connection.createStatement().executeUpdate(query);
                connection.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean update(String table, DatabaseInsertion toUpdate, DatabaseInsertion... insertions) {
            try (Connection connection = getConnection()){
                StringBuilder query = new StringBuilder("UPDATE `" + table + "` SET ");
                for (DatabaseInsertion e : insertions)
                    query.append("`").append(e.getKey()).append("`='").append(e.getValue()).append("', ");
                query.delete(query.length() - 2, query.length()).append(" ");
                query.append("WHERE `").append(toUpdate.getKey()).append("`='").append(toUpdate.getValue()).append("';");
                connection.createStatement().executeUpdate(query.toString());
                connection.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public static class MySQL extends DeprecatedDatabase {
        public MySQL(Plugin plugin, String username, String password, String name, String host, String port, int poolSize, int timeout, int idleTimeout, int lifeTime) {
            super(plugin, username, password, name, host, port, poolSize, timeout, idleTimeout, lifeTime);
        }
    }

    public static class DatabaseInsertion {
        private String key;

        private Object value;

        public DatabaseInsertion(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }
    }

    public static class DatabaseElement {
        private final String name;

        private final Type type;

        private final boolean isPrimary;

        public DatabaseElement(String name, Type type) {
            this(name, type, false);
        }

        public DatabaseElement(String name, Type type, boolean isPrimary) {
            this.name = name;
            this.type = type;
            this.isPrimary = isPrimary;
        }

        public String getName() {
            return this.name;
        }

        public Type getType() {
            return this.type;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public enum Type {
            CHAR, VAR_CHAR, BINARY, VAR_BINARY, LONG_TEXT, BIT, TINY_INT, BOOL, BOOLEAN, SMALL_INT, MEDIUM_INT, INT, INTEGER, BIG_INT, FLOAT, DOUBLE, DECIMAL;
        }
    }
}
