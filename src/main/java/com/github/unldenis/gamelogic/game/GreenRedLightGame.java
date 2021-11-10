package com.github.unldenis.gamelogic.game;

import com.github.unldenis.gamelogic.Game;
import com.github.unldenis.gamelogic.GamePlayer;
import com.github.unldenis.gamelogic.GameStatus;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.obj.Region;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class GreenRedLightGame extends Game {

    private Location end_1, end_2;
    private Location specSpawn;

    @Getter private Region endRegion;
    private boolean checkPlayers = false;
    private BukkitTask currentTask;

    /**
     * Constructor of GreenRedLight
     *
     * @param mainGame    maingame of this game
     */
    public GreenRedLightGame(@NonNull MainGame mainGame) {
        super(mainGame, mainGame.getPlugin().getMessages().getString("GreenRedLight.name"), mainGame.getPlugin().getMessages().getString("GreenRedLight.description"));
    }


    /**
     * Getter to check player's movement
     * @return true if is checking players movement
     */
    public boolean isCheckPlayers() {
        return checkPlayers;
    }


    /**
     * Method used to kill player because moved
     * @param player to fire because moved
     */
    public void fire(Player player) {
        Random random = new Random();
        Firework firework = (Firework)player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Builder effect1 = FireworkEffect.builder();
        effect1.withColor(Color.fromBGR(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        int FL = random.nextInt(2);
        if (FL == 1)
            effect1.flicker(true);
        effect1.withFade(new Color[] { Color.fromBGR((new Random()).nextInt(255), (new Random()).nextInt(255), (new Random()).nextInt(255)), Color.fromBGR((new Random()).nextInt(255), (new Random()).nextInt(255), (new Random()).nextInt(255)) });
        int type = random.nextInt(5);
        int FL1 = random.nextInt(2);
        if (FL1 == 1)
            effect1.trail(true);
        switch (type) {
            case 0:
                effect1.with(FireworkEffect.Type.BALL);
                break;
            case 1:
                effect1.with(FireworkEffect.Type.BALL_LARGE);
                break;
            case 2:
                effect1.with(FireworkEffect.Type.BURST);
                break;
            case 3:
                effect1.with(FireworkEffect.Type.CREEPER);
                break;
            case 4:
                effect1.with(FireworkEffect.Type.STAR);
                break;
            default:
        }
        meta.addEffect(effect1.build());
        firework.setFireworkMeta(meta);
        firework.setPassenger(player);

        //eliminate
        mainGame.eliminate(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(specSpawn);
            }
        }
        .runTaskLater(mainGame.getPlugin(), 20L);
    }

    /**
     * Method used to check if player arrived at end
     * @param player to check on movement
     */
    public void checkEnd(Player player) {
        if(player.getGameMode().equals(GameMode.SPECTATOR))
            return;
        GamePlayer gP = mainGame.find(player);
        if(gP.getProperties().containsKey("arrived"))
            return;
        if(endRegion.contains(player.getLocation())) {
            gP.getProperties().put("arrived", true);
            player.setGameMode(GameMode.SPECTATOR);

            //check if all players arrived
            boolean allArrived = true;
            for(GamePlayer a: mainGame.getPlayers()) {
                if(!a.getProperties().containsKey("arrived")) {
                    allArrived = false;
                    break;
                }
            }
            if(allArrived) {
                currentTask.cancel();
                for(GamePlayer a: mainGame.getPlayers()) {
                    a.teleport(mainGame.getLobbyLoc());
                    a.resetPlayer();
                    a.getProperties().remove("arrived");
                }
                reset();
                mainGame.nextGame();
            }
        }

    }


    /**
     * Method called when the time is up
     */
    private void endTime() {
        mainGame.setGameStatus(GameStatus.ENDING);
        currentTask = new BukkitRunnable() {
            int j = 0;
            @Override
            public void run() {
                if(j==mainGame.getPlayers().size()) {
                    cancel();
                    for(GamePlayer gP: mainGame.getPlayers()) {
                        gP.resetPlayer();
                        gP.teleport(mainGame.getLobbyLoc());
                        if(gP.getProperties().containsKey("arrived"))
                            gP.getProperties().remove("arrived");
                    }
                    reset();
                    mainGame.nextGame();
                    return;
                }
                GamePlayer gP = mainGame.getPlayers().get(j);
                //if player is not arrived
                boolean v = (boolean) gP.getProperties().getOrDefault("arrived", false);

                if(!v) {
                    fire(gP.getPlayer());
                }

                j++;
            }
        }.runTaskTimer(mainGame.getPlugin(), 0L, 20L);
    }


    @Override
    public void start() {
        if(endRegion==null)
            endRegion = new Region(end_1, end_2);
        mainGame.setGameStatus(GameStatus.PLAYING);

        for(GamePlayer gamePlayer: mainGame.getPlayers())
            gamePlayer.teleport(getRandomSpawn(getSpawns()));
        setInventory(checkPlayers);
        currentTask = new BukkitRunnable() {
            int secs = 60;
            int temp = 1;
            @Override
            public void run() {
                if(secs==0) {
                    cancel();
                    //end and kill players not safe
                    endTime();
                    return;
                }
                if(temp==4) {
                    checkPlayers = !checkPlayers && Math.random() < 0.5;
                    setInventory(checkPlayers);
                    temp = 0;
                }
                mainGame.sendActionBar("&7"+secs);
                temp++;
                secs--;
            }
        }.runTaskTimer(mainGame.getPlugin(), 0L, 20L);
    }

    @Override
    public void reset() {
        if(!currentTask.isCancelled()) currentTask.cancel();
        endRegion = null;
        checkPlayers = false;
        currentTask = null;
    }

    @Override
    public void teleportSpectators() {
        for(GamePlayer spec: mainGame.getSpectators())
            spec.teleport(specSpawn);
    }

    /**
     * A method that eventually does something specific in the minigame when a player exits
     *
     * @param gamePlayer gamePlayer that quits
     */
    @Override
    public void onQuit(GamePlayer gamePlayer) {

    }

    /**
     * Method that saves the game in the config.yml
     *
     * @param cfg
     * @param prefix
     */
    @Override
    public void save(FileConfiguration cfg, String prefix) {
        cfg.set(prefix+"end_1", end_1);
        cfg.set(prefix+"end_2", end_2);
        cfg.set(prefix+"specSpawn", specSpawn);
    }

    /**
     * Method that loades the game from games.yml
     *
     * @param cfg
     * @param prefix
     */
    @Override
    public void load(FileConfiguration cfg, String prefix) {
        end_1 = cfg.getLocation(prefix+"end_1");
        end_2 = cfg.getLocation(prefix+"end_2");
        specSpawn = cfg.getLocation(prefix+"specSpawn");
    }


    private void setInventory(boolean checkPlayers) {
        ItemStack  itm = new ItemStack(checkPlayers ? Material.RED_WOOL : Material.GREEN_WOOL);
        ItemStack[] inv = new ItemStack[] {itm,itm,itm,itm,itm,itm,itm,itm,itm};
        for(GamePlayer gP: mainGame.getPlayers())
            gP.getPlayer().getInventory().setContents(inv);
    }



}
