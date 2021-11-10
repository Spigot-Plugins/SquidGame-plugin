package com.github.unldenis.listener;

import com.github.unldenis.SquidGame;
import com.github.unldenis.gamelogic.GamePlayer;
import com.github.unldenis.gamelogic.GameStatus;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.gamelogic.game.HoneycombGame;
import com.github.unldenis.helper.Events;
import com.github.unldenis.obj.honeycomb.Shape;
import lombok.NonNull;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

/**
 * Class that records the events of the HoneycombGames.
 * My library is used to record and manage events.
 * @see <a href="https://github.com/unldenis/UnldenisHelper">https://github.com/unldenis/UnldenisHelper</a>
 */
public class HoneycombEvents {

    private final SquidGame plugin;

    public HoneycombEvents(@NonNull SquidGame plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        /*
            BlockBreakEvent
         */
        Events.subscribe(BlockBreakEvent.class)
        .handler(event -> {
            Player player = event.getPlayer();
            Optional<MainGame> mainGameOptional = plugin.getGameManager().find(player);
            if(mainGameOptional.isEmpty()) return;
            MainGame mainGame = mainGameOptional.get();
            if(mainGame.getGameStatus().equals(GameStatus.PLAYING)) {
                if(mainGame.getCurrentGame() instanceof HoneycombGame) {
                    HoneycombGame honeycombGame = (HoneycombGame) mainGame.getCurrentGame();
                    GamePlayer gamePlayer = mainGame.find(player);
                    Shape shape = honeycombGame.findShape((String) gamePlayer.getProperties().get("shape"));
                    Block block = event.getBlock();
                    if(honeycombGame.getBlocksPlaced().contains(block.getLocation())) {
                        //is a placed block
                        if(!event.getBlock().getType().equals(shape.getMaterial())) {
                            //not player team shape
                            event.setCancelled(true);
                        }else {
                            honeycombGame.getBlocksPlaced().remove(block.getLocation());
                            if(shape.check()) {
                                honeycombGame.completed(shape);
                            }
                        }
                    }else{
                        //is a world block
                        event.setCancelled(true);
                    }

                }
            }
        }).bindWith(plugin);
        /*
            BlockPlaceEvent
         */
        Events.subscribe(BlockPlaceEvent.class)
        .handler(event -> {
            Player player = event.getPlayer();
            Optional<MainGame> mainGameOptional = plugin.getGameManager().find(player);
            if(mainGameOptional.isEmpty()) return;
            MainGame mainGame = mainGameOptional.get();
            if(mainGame.getGameStatus().equals(GameStatus.PLAYING)) {
                if(mainGame.getCurrentGame() instanceof HoneycombGame) {
                    HoneycombGame honeycombGame = (HoneycombGame) mainGame.getCurrentGame();
                    GamePlayer gamePlayer = mainGame.find(player);
                    Shape shape = honeycombGame.findShape((String) gamePlayer.getProperties().get("shape"));
                    Block block = event.getBlock();
                    if(!event.getBlock().getType().equals(shape.getMaterial())) {
                        //not player team shape
                        event.setCancelled(true);
                    }else {
                        honeycombGame.getBlocksPlaced().add(block.getLocation());
                        if(shape.check()) {
                            honeycombGame.completed(shape);
                        }
                    }
                }
            }
        }).bindWith(plugin);
    }

}
