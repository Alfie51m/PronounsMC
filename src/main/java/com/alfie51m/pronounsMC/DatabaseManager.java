package com.alfie51m.pronounsMC;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private final PronounsMC plugin;
    private Connection connection;

    public DatabaseManager(PronounsMC plugin) { this.plugin = plugin; }

    public void connect() throws SQLException {
        FileConfiguration config = plugin.getPluginConfig();
        String dbType = config.getString("database.type","mysql").toLowerCase();

        if(dbType.equals("mysql")){
            String host = config.getString("database.host","localhost");
            int port = config.getInt("database.port",3306);
            String dbName = config.getString("database.name","minecraft");
            String user = config.getString("database.user","root");
            String pass = config.getString("database.password","");

            String url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
            connection = DriverManager.getConnection(url,user,pass);
            plugin.getLogger().info("Connected to MySQL database.");
        } else {
            File dbFile = new File(plugin.getDataFolder(),"pronouns.db");
            if(!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
            String url = "jdbc:sqlite:"+dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Connected to SQLite database at "+dbFile.getAbsolutePath());
        }
    }

    public void setup() throws SQLException {
        String dbType = plugin.getPluginConfig().getString("database.type","mysql").toLowerCase();
        String createTableQuery;
        if(dbType.equals("mysql")){
            createTableQuery = "CREATE TABLE IF NOT EXISTS pronouns (uuid VARCHAR(36) PRIMARY KEY, pronoun VARCHAR(100));";
        } else {
            createTableQuery = "CREATE TABLE IF NOT EXISTS pronouns (uuid TEXT PRIMARY KEY, pronoun TEXT);";
        }
        try(Statement stmt = connection.createStatement()){ stmt.executeUpdate(createTableQuery); }
    }

    public String getPronouns(String uuid){
        try(PreparedStatement stmt = connection.prepareStatement("SELECT pronoun FROM pronouns WHERE uuid = ?")){
            stmt.setString(1,uuid);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) return rs.getString("pronoun");
        } catch(SQLException e){ plugin.getLogger().warning("Failed to fetch pronouns: "+e.getMessage()); }
        return null;
    }

    public void setPronouns(String uuid,String pronoun){
        String dbType = plugin.getPluginConfig().getString("database.type","mysql").toLowerCase();
        String query = dbType.equals("mysql") ?
                "INSERT INTO pronouns (uuid,pronoun) VALUES (?,?) ON DUPLICATE KEY UPDATE pronoun=?" :
                "INSERT OR REPLACE INTO pronouns (uuid,pronoun) VALUES (?,?)";

        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,uuid);
            stmt.setString(2,pronoun);
            if(dbType.equals("mysql")) stmt.setString(3,pronoun);
            stmt.executeUpdate();
        } catch(SQLException e){ plugin.getLogger().warning("Failed to set pronouns: "+e.getMessage()); }
    }

    public void resetPronouns(String uuid){
        try(PreparedStatement stmt = connection.prepareStatement("DELETE FROM pronouns WHERE uuid=?")){
            stmt.setString(1,uuid);
            stmt.executeUpdate();
        } catch(SQLException e){ plugin.getLogger().warning("Failed to reset pronouns: "+e.getMessage()); }
    }

    public void close(){
        if(connection!=null) try{ connection.close(); } catch(SQLException e){ plugin.getLogger().warning("Failed to close connection: "+e.getMessage()); }
    }
}
