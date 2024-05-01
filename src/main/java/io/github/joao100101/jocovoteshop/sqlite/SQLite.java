package io.github.joao100101.jocovoteshop.sqlite;


import io.github.joao100101.jocovoteshop.JocoVoteShop;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.*;

import static org.bukkit.Bukkit.getLogger;

public class SQLite {
    private final String path;

    public SQLite(String path) {
        this.path = path;
    }

    public SQLite() {
        this.path = JocoVoteShop.getInstance().getDataFolder().getAbsolutePath() + "/data.db";
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public void createTable() {
        String sql = "create table if not exists votepoints(uuid varchar(255) not null primary key, totalVotes integer);";
        try (Connection con = getConnection(); Statement statement = con.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            getLogger().warning("Erro ao se conectar com o banco de dados.");
            e.printStackTrace();
        }
    }

    public boolean existInTable(String uuid) {
        String sql = "select uuid from votepoints where uuid = ?";
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, uuid);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            getLogger().warning("Erro ao se conectar com o banco de dados.");
            e.printStackTrace();
        }

        return false;
    }

    public Integer getTotalVotes(OfflinePlayer p) {
        String uuid = p.getUniqueId().toString();
        String sql = "select totalVotes from votepoints where uuid = ?";
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, uuid);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt("totalVotes");
            }
        } catch (SQLException e) {
            getLogger().warning("Erro ao se conectar com o banco de dados.");
            e.printStackTrace();
        }
        return 0;
    }


    private void createUser(String uuid, Integer totalVotes) {
        String sql = "insert into votepoints (uuid, totalVotes) values (?,?)";
        try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            preparedStatement.setString(1, uuid);
            preparedStatement.setInt(2, totalVotes);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            getLogger().warning("Erro ao se conectar com o banco de dados.");
            e.printStackTrace();
        }
    }


    public void setVotes(String uuid, Integer votes) {
        if (existInTable(uuid)) {
            if (votes >= 0) {
                String sql = "update votepoints set totalVotes = ? where uuid = ?";
                try (Connection con = getConnection(); PreparedStatement preparedStatement = con.prepareStatement(sql)) {
                    preparedStatement.setInt(1, votes);
                    preparedStatement.setString(2, uuid);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    getLogger().warning("Erro ao se conectar com o banco de dados.");
                    e.printStackTrace();
                }
            } else {
                createUser(uuid, votes);
            }
        }
    }


    public void addVotes(Player p, Integer votes) {
        if (votes >= 0) {
            setVotes(p.getUniqueId().toString(), getTotalVotes(p) + votes);
        }
    }

    public void removeVotes(Player p, Integer votes) {
        int totalVotes = getTotalVotes(p);
        if (votes >= 0 && (totalVotes - votes) >= 0) {
            setVotes(p.getUniqueId().toString(), totalVotes - votes);
        }
    }


}