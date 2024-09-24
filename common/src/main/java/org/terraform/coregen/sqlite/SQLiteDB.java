package org.terraform.coregen.sqlite;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.main.TerraformGeneratorPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SQLiteDB {
    private static final Set<String> PREPARED_WORLDS = new HashSet<>();
    /***
     * Welcome to my failed frankenstein experiment that did not work
     * even in the slightest. Be free to marvel at the rubberband code
     * that I have graced upon this Earth.
     */
    private static SQLiteDB i;

    public static @NotNull SQLiteDB get() {
        if (i == null) {
            i = new SQLiteDB();
        }
        return i;
    }

    /**
     * Ensures that the database and all relevant tables exist.
     */
    public static void createTableIfNotExists(String world) {
        if (PREPARED_WORLDS.contains(world)) {
            return;
        }
        Connection conn = null;
        String dir = "plugins" + File.separator + "TerraformGenerator" + File.separator + world + ".db";
        try {
            // db parameters  
            String url = "jdbc:sqlite:" + dir;
            // create a connection to the database & create the table

            // Create Chunks Table
            conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS CHUNKS "
                         + "(CHUNK STRING PRIMARY KEY     NOT NULL,"
                         + " POPULATED           BOOLEAN     NOT NULL); ";
            stmt.executeUpdate(sql);
            stmt.close();

            // Create BlockData table
            stmt = conn.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS BLOCKDATA "
                  + "(CHUNK STRING NOT NULL,"
                  + "COORDS STRING NOT NULL,"
                  + " DATA STRING NOT NULL,"
                  + "PRIMARY KEY (CHUNK,COORDS)); ";
            stmt.executeUpdate(sql);
            stmt.close();
            PREPARED_WORLDS.add(world);
        }
        catch (SQLException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
        finally {
            closeConn(conn);
        }
    }

    private static void closeConn(@Nullable Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        }
        catch (SQLException ex) {
            TerraformGeneratorPlugin.logger.stackTrace(ex);
        }
    }

    /**
     * Create an entry in the BlocData table
     */
    public void updateBlockData(String world, int chunkX, int chunkZ, int x, int y, int z, @NotNull BlockData data) {
        createTableIfNotExists(world);
        String dir = "plugins" + File.separator + "TerraformGenerator" + File.separator + world + ".db";
        String CHUNK = chunkX + "," + chunkZ;
        String COORDS = x + "," + y + "," + z;
        String DATA = data.toString();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + dir);
            c.setAutoCommit(false);

            Statement stmt = c.createStatement();
            String sql = "DELETE from BLOCKDATA where CHUNK='" + CHUNK + "' and" + " COORDS='" + COORDS + "';";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO BLOCKDATA (CHUNK,COORDS,DATA) "
                  + "VALUES ('"
                  + CHUNK
                  + "', '"
                  + COORDS
                  + "', '"
                  + DATA
                  + "');";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        }
        catch (Exception e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    /**
     * Returns a boolean array. Index 0 is whether or not the chunk exists
     * in the table. Index 1 is whether or not the chunk was populated yet.
     */
    public boolean[] fetchFromChunks(String world, int chunkX, int chunkZ) {
        createTableIfNotExists(world);
        String dir = "plugins" + File.separator + "TerraformGenerator" + File.separator + world + ".db";
        String key = chunkX + "," + chunkZ;
        boolean[] queryReply = {false, false};
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + dir);
            c.setAutoCommit(false);

            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM CHUNKS WHERE CHUNK='" + key + "';");
            if (rs.next()) {
                queryReply = new boolean[] {true, rs.getBoolean("POPULATED")};
            }
            else {
                queryReply = new boolean[] {false, false};
            }
            rs.close();
            stmt.close();
            c.close();
        }
        catch (Exception e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            // Bukkit.getLogger().severe(e.getClass().getName() + "[" + e.getCause() +"]" + ":" + e.getMessage() );
        }

        return queryReply;
    }

    /**
     * Create or update an entry in the Chunk table
     */
    public void putChunk(String world, int chunkX, int chunkZ, boolean populated) {
        createTableIfNotExists(world);
        String dir = "plugins" + File.separator + "TerraformGenerator" + File.separator + world + ".db";
        try {
            Class.forName("org.sqlite.JDBC");
            Connection c = DriverManager.getConnection("jdbc:sqlite:" + dir);
            c.setAutoCommit(false);

            Statement stmt = c.createStatement();
            String key = chunkX + "," + chunkZ;
            String sql = "DELETE from CHUNKS where CHUNK='" + key + "';";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO CHUNKS (CHUNK,POPULATED) " + "VALUES ('" + key + "', '" + populated + "');";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        }
        catch (Exception e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }
}
