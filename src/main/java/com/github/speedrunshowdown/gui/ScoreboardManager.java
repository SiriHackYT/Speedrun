package com.github.speedrunshowdown.gui;

import java.util.ArrayList;

import com.github.speedrunshowdown.SpeedrunShowdown;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardManager {
    private SpeedrunShowdown plugin;
    private Scoreboard scoreboard;
    private Objective objective;

    public ScoreboardManager() {
        plugin = SpeedrunShowdown.getInstance();

        // Get scoreboard
        scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        // Unregister all teams
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }

        // Unregister all objectives
        for (Objective objective : scoreboard.getObjectives()) {
            objective.unregister();
        }

        // Create teams
        for (String key : plugin.getConfig().getConfigurationSection("teams").getKeys(false)) {
            ChatColor color;
            try {
                color = ChatColor.valueOf(plugin.getConfig().getString("teams." + key));
            }
            catch (IllegalArgumentException e) {
                color = ChatColor.RESET;
            }

            Team team = scoreboard.registerNewTeam(key);
            team.setColor(color);
        }

        // Register new objective
        objective = scoreboard.registerNewObjective(
            "sidebar",
            "dummy",
            ChatColor.YELLOW.toString() + ChatColor.BOLD + "SPEEDRUN SHOWDOWN"
        );

        // Score scoreboard
        show();

        // Set scoreboard to all players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }

    public void show() {
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void clear() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void update() {
        // Update the sidebar
        ArrayList<String> scores = new ArrayList<>();
        String newLine = "";
        scores.add(newLine += " ");
        scores.add(" ");
        if (plugin.isSuddenDeath()){
            scores.add("Objectif : §aTuer le dragon");
        }else{
            scores.add(
                    "Mort subite dans " + ChatColor.GREEN +
                            String.format("%02d:%02d", plugin.getTimer() / 60, plugin.getTimer() % 60)
            );
        }
        scores.add(newLine += " ");
        for (String key : plugin.getConfig().getConfigurationSection("teams").getKeys(false)) {
            Team team = scoreboard.getTeam(key);

            // If team is not null and has entries, display team
            if (team != null && team.getEntries().size() != 0) {
                for (String entry : team.getEntries()) {
                    Player player = plugin.getServer().getPlayer(entry);
                    if (player != null && player.getGameMode() != GameMode.SPECTATOR) {
                        String mark = " " + ChatColor.WHITE + entry + ChatColor.GREEN + " \u2714";// Green check mark
                        scores.add(
                                team.getColor().toString() + ChatColor.BOLD + team.getDisplayName().toUpperCase() + mark);
                    }
                }
                // Add all living players to scoreboard
                // Add all dead players to scoreboard
                for (String entry : team.getEntries()) {
                    Player player = plugin.getServer().getPlayer(entry);

                    if (player == null || player.getGameMode() == GameMode.SPECTATOR) {
                        scores.add(
                            ChatColor.GRAY.toString() + ChatColor.ITALIC +
                            entry + ChatColor.RED + " \u2718" // Red X
                        );
                    }
                }
               // scores.add(newLine += " ");
                scores.add(" ");
                scores.add(ChatColor.GOLD + "CrafTok.fr");
            }
        }
        
        // Get rid of old scores
        for (String entry : scoreboard.getEntries()) {
            if (scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(entry).getScore() > scores.size()) {
                scoreboard.resetScores(entry);
            }
        }
        
        // Display scores
        for (int i = 0; i < scores.size(); i++) {
            replaceScore(scoreboard.getObjective(DisplaySlot.SIDEBAR), scores.size() - i, scores.get(i));
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Team setPlayerTeam(Player player) {
        for (Team team : scoreboard.getTeams()) {
            if (team.getSize() == 0){
                team.addEntry(player.getName());
                    return team;
            }
        }
        return null;
    }

    public String getStringTeam(Player player) {
        String resultat = "";
        for (Team team : scoreboard.getTeams()){
            if (team.hasEntry(player.getName())){
                resultat = team.getColor().toString();
            }
        }
        return resultat;
    }

    public Team getTeam(Player player) {
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                return team;
            }
        }
        return null;
    }

    public static String getEntryFromScore(Objective objective, int score) {
        // If objective does not exist or no score is taken, return null
        if(objective == null || !hasScoreTaken(objective, score)) {
            return null;
        }
        // Otherwise, loop over all entries
        for (String entry : objective.getScoreboard().getEntries()) {
            // If entry matches score, return entry
            if(objective.getScore(entry).getScore() == score) {
                return objective.getScore(entry).getEntry();
            }
        }
        // Otherwise, return null
        return null;
    }

    public static boolean hasScoreTaken(Objective objective, int score) {
        // Loop over all entries
        for (String entry : objective.getScoreboard().getEntries()) {
            // If entry has correct score, return true
            if(objective.getScore(entry).getScore() == score) {
                return true;
            }
        }
        // Otherwise, return false
        return false;
    }

    public static void replaceScore(Objective objective, int score, String name) {
        // Check if the score is taken
        if(hasScoreTaken(objective, score)) {
            // If the entry contains the same entry, don't change anything
            if(getEntryFromScore(objective, score).equalsIgnoreCase(name)) {
                return;
            }
            // Else reset the scoree
            else {
                objective.getScoreboard().resetScores(getEntryFromScore(objective, score));
            }
        }
        // Set new score
        objective.getScore(name).setScore(score);
    }
}
