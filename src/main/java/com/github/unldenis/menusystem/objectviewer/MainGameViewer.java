package com.github.unldenis.menusystem.objectviewer;

import com.github.unldenis.gamelogic.Game;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.gamelogic.game.GreenRedLightGame;
import com.github.unldenis.gamelogic.game.HoneycombGame;
import com.github.unldenis.helper.gui.Menu;
import com.github.unldenis.helper.util.ReflectionUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class MainGameViewer extends Menu {

    private MainGame obj;

    public MainGameViewer(Player player, MainGame obj ) {
        super(player);
        this.obj = obj;
    }

    @Override
    public String getMenuName() {
        return obj.getName() + " setup";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clicked = e.getCurrentItem();
        String displayName= clicked.getItemMeta().getDisplayName().replace("§f§n", "");

        if(clicked.getType().equals(Material.SEA_LANTERN)) {
            int pos = Integer.parseInt(""+displayName.charAt(0));
            Game gameDef = obj.getGameList().get(pos);
            if(gameDef instanceof GreenRedLightGame) {
                GreenRedLightGame game = (GreenRedLightGame) gameDef;
                new GreenRedLightGameViewer(player, game).open();
            }else if(gameDef instanceof HoneycombGame) {
                HoneycombGame game = (HoneycombGame) gameDef;
                new HoneycombGameViewer(player, game).open();
            }
            return;
        }

        try {
            Field field = obj.getClass().getDeclaredField(displayName);
            Object value = ReflectionUtil.getValue(obj, field);

            if(field.getType().equals(Integer.class)) {
                Integer to = null;
                if(e.getClick().isRightClick()) {
                    //-1
                    to = ((Integer)value)-1;
                    if(to==1) return;
                }else if(e.getClick().isLeftClick()) {
                    //+1
                    to = ((Integer)value)+1;
                }
                field.set(obj, to);
                player.sendMessage("§7"+field.getName()+" set to §a"+to);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                new MainGameViewer(player, obj).open(); //update

            }else if(field.getType().equals(Location.class)) {
                Location loc = player.getLocation();
                field.set(obj, loc);
                player.sendMessage("§7"+field.getName()+" set to §a" + loc.toVector());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

            }
        } catch (NoSuchFieldException | IllegalAccessException noSuchFieldException) {
            noSuchFieldException.printStackTrace();
        }


    }

    @Override
    public void setMenuItems() {
        for (Field field : obj.getClass().getDeclaredFields()) {
            ItemStack itemStack =  getItemStack(field);
            if(itemStack!=null) getInventory().setItem(getInventory().firstEmpty(), itemStack);
        }

        for(int j=0; j<obj.getGameList().size(); j++) {
            Game game = obj.getGameList().get(j);
            getInventory().setItem(getInventory().firstEmpty(), makeItem(Material.SEA_LANTERN, "§f§n"+j + " " +game.getName(), game.getDescription()));
        }

    }


    private ItemStack getItemStack(@NonNull Field field) {
        try {
            if(field.getType().equals(Integer.class)) {
                Integer value = (Integer) ReflectionUtil.getValue(obj, field);
                if(value==null)
                    return null;
                ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET, value);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f§n" + field.getName());
                itemStack.setItemMeta(meta);
                return itemStack;
            }else if(field.getType().equals(Location.class)) {
                ItemStack itemStack = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName("§f§n" + field.getName());
                itemStack.setItemMeta(meta);
                return itemStack;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
