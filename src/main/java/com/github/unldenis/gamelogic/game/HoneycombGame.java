package com.github.unldenis.gamelogic.game;

import com.github.unldenis.gamelogic.Game;
import com.github.unldenis.gamelogic.GamePlayer;
import com.github.unldenis.gamelogic.GameStatus;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.obj.honeycomb.Shape;
import com.github.unldenis.util.StuffUtils;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;


public class HoneycombGame extends Game {

    private Location specSpawn;
    @Getter private ArrayList<Shape> shapes= new ArrayList<>();

    @Getter private ArrayList<Location> blocksPlaced = new ArrayList<>();


    /**
     * Constructor of Honeycomb
     *
     * @param mainGame    maingame of this game
     */
    public HoneycombGame(@NonNull MainGame mainGame) {
        super(mainGame, "Honeycomb", "L'obbiettivo è ricreare le figure");
        shapes.add( new Shape("Circle", Material.GREEN_WOOL));
        shapes.add(new Shape("Triangle", Material.RED_WOOL));
        shapes.add(new Shape("Star", Material.YELLOW_WOOL));
        shapes.add(new Shape("Umbrella", Material.BLUE_WOOL));

    }

    /**
     * Method of the end of the game.
     * It can end in 2 ways:
     * - In case there are 2 last teams and the only player of a team should go out: the team of the exiting player is eliminated and the other team goes on.
     * - In case a team remains without having copied correctly.
     * @param lastShape last Shape object that loses
     */
    private void endGame(Shape lastShape) {
        mainGame.setGameStatus(GameStatus.ENDING);
        mainGame.sendActionBar("&6"+lastShape.getName()+" &7loses.");
        for(GamePlayer gP: lastShape.getGamePlayers()) {
            mainGame.eliminate(gP);
            gP.getPlayer().setGameMode(GameMode.SPECTATOR);
            gP.teleport(specSpawn);
        }

        for(GamePlayer gP: mainGame.getPlayers()) {
            gP.resetPlayer();
            gP.teleport(mainGame.getLobbyLoc());
            gP.getProperties().remove("shape");
        }
        reset();
        mainGame.nextGame();
    }

    /**
     * Method called after lobby countdown in maingame
     */
    @Override
    public void start() {
        mainGame.setGameStatus(GameStatus.PLAYING);
        for(GamePlayer gP: mainGame.getPlayers()) {
            //player join in shape team
            Shape shape = findShape();
            shape.getGamePlayers().add(gP);
            gP.getProperties().put("shape", shape.getName());
            gP.teleport(shape.getSpawn());
            gP.getPlayer().setGameMode(GameMode.SURVIVAL);
        }

        //chek eventually shape without player
        for(Shape shape: shapes)
            if(shape.getGamePlayers().size()==0) shape.setRight(true);
    }



    /**
     * Method called to reset a game
     */
    @Override
    public void reset() {
        for(Shape shape: shapes) {
            shape.getGamePlayers().clear();
            shape.setRight(false);
        }
        for(Location location: blocksPlaced)
            location.getBlock().setType(Material.AIR);
    }


    /**
     * Method to find the shape with less gamePlayers
     * @return the shape
     */
    private @NonNull Shape findShape() {
        shapes.sort(Comparator.comparingInt(s -> s.getGamePlayers().size()));
        return shapes.get(0);
    }




    /**
     * Method called when a shape is correct
     * @param shape shape completed
     */
    public void completed(@NonNull Shape shape) {
        shape.setRight(true);

        ArrayList<Shape> last = new ArrayList<>();
        for(Shape s: shapes)
            if(!s.isRight()) last.add(s);

        mainGame.sendMessage("&6" + shape.getName()+" &7made it! &c"+last.size()+" &7remaining teams");

        //end game
        if(last.size()==1)
            endGame(last.get(0));
    }


    /**
     * Method to find the shape
     * @param name name of the shape
     * @return the shape
     */
    public Shape findShape(String name) {
        for(Shape s: shapes)
            if(s.getName().equals(name))
                return s;
        return null;
    }



    /**
     * Method that teleports spectators in game
     */
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
        if(mainGame.getGameStatus().equals(GameStatus.PLAYING)) {
            Shape shape = findShape((String) gamePlayer.getProperties().get("shape"));
            shape.getGamePlayers().remove(gamePlayer);

            if(shape.getGamePlayers().size()==0){
                shape.setRight(true);
                ArrayList<Shape> last = new ArrayList<>();
                for(Shape s: shapes)
                    if(!s.isRight()) last.add(s);

                mainGame.sendMessage("&c" + shape.getName()+" &7team left! &c"+(last.size())+" &7remaining teams");

                //end game
                if(last.size()==1)
                    endGame(shape);
            }
        }


    }

    /**
     * Method that saves the game in the config.yml
     *
     * @param cfg
     * @param prefix
     */
    @Override
    public void save(FileConfiguration cfg, String prefix) {
        cfg.set(prefix+"specSpawn", specSpawn);
        prefix+="shapes.";
        for(Shape shape: shapes) {
            //cfg.set(prefix+shape.getName()+".material", new ItemStack(shape.getMaterial()));
            cfg.set(prefix+shape.getName()+".spawn", shape.getSpawn());
            cfg.set(prefix+shape.getName()+".pos1", shape.getPos1());
            cfg.set(prefix+shape.getName()+".pos2", shape.getPos2());
            cfg.set(prefix+shape.getName()+".structure", shape.getStructure());
        }
    }

    /**
     * Method that loades the game from games.yml
     *
     * @param cfg
     * @param prefix
     */
    @Override
    public void load(FileConfiguration cfg, String prefix) {
        specSpawn = cfg.getLocation(prefix+"specSpawn");
        prefix+="shapes";
        var list = cfg.getConfigurationSection(prefix);
        if(list!=null) {
            for(String s: list.getKeys(false)) {
                Shape shape = findShape(s);
                if(shape!=null) {
                    shape.setSpawn(cfg.getLocation(prefix+"."+s+".spawn"));
                    shape.setPos1(cfg.getLocation(prefix+"."+s+".pos1"));
                    shape.setPos2(cfg.getLocation(prefix+"."+s+".pos2"));
                    var struct = StuffUtils.listTo2DArray((List<List<Boolean>>) cfg.getList(prefix + "." + s + ".structure"));
                    shape.setStructure(struct);
                }
            }
        }
    }


}
