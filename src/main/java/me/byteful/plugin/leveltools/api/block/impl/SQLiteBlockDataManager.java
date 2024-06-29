package me.byteful.plugin.leveltools.api.block.impl;

import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;

import java.nio.file.Path;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteBlockDataManager implements BlockDataManager {
    private static final Logger logger = Logger.getLogger(SQLiteBlockDataManager.class.getName());
    private final Connection connection;

    public SQLiteBlockDataManager(Path dbPath) {
        try {
            String url = "jdbc:sqlite:" + dbPath.toString();
            this.connection = DriverManager.getConnection(url);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS blocks (world TEXT, x INTEGER, y INTEGER, z INTEGER, PRIMARY KEY(world, x, y, z))");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize SQLiteBlockDataManager", e);
            throw new RuntimeException("Failed to initialize SQLiteBlockDataManager", e);
        }
    }

    @Override
    public boolean isPlacedBlock(BlockPosition pos) {
        String query = "SELECT 1 FROM blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, pos.getWorld());
            stmt.setInt(2, pos.getX());
            stmt.setInt(3, pos.getY());
            stmt.setInt(4, pos.getZ());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing isPlacedBlock query", e);
            return false;
        }
    }

    @Override
    public void addPlacedBlock(BlockPosition pos) {
        String query = "INSERT OR IGNORE INTO blocks (world, x, y, z) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, pos.getWorld());
            stmt.setInt(2, pos.getX());
            stmt.setInt(3, pos.getY());
            stmt.setInt(4, pos.getZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing addPlacedBlock query", e);
        }
    }

    @Override
    public void removePlacedBlock(BlockPosition pos) {
        String query = "DELETE FROM blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, pos.getWorld());
            stmt.setInt(2, pos.getX());
            stmt.setInt(3, pos.getY());
            stmt.setInt(4, pos.getZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error executing removePlacedBlock query", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing connection", e);
        }
    }
}
