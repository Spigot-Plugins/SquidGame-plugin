package com.github.unldenis.menusystem.objectviewer;

import com.github.unldenis.helper.gui.Menu;
import com.github.unldenis.obj.honeycomb.Shape;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class ShapeViewer extends Menu {
    private Shape obj;

    public ShapeViewer(Player player, Shape obj) {
        super(player);
        this.obj = obj;
    }

    @Override
    public String getMenuName() {
        return obj.getName()+" setup";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        String displayName = e.getCurrentItem().getItemMeta().getDisplayName().replace("§f§n", "");

        try {
            Field field = obj.getClass().getDeclaredField(displayName);
            field.setAccessible(true);
            if(field.getType().equals(Location.class)) {
                Location loc = player.getLocation();
                field.set(obj, loc);
                player.sendMessage("§7"+field.getName()+" set to §a" + loc.toVector());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            }else if(field.getType().isArray()) {
                if(obj.getStructure()!=null)
                    obj.setStructure(null);
                else
                    obj.load();

                new ShapeViewer(player, obj).open();
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
    }

    private ItemStack getItemStack(@NonNull Field field) {
        if(field.getType().equals(Location.class)) {
            ItemStack itemStack = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName("§f§n" + field.getName());
            itemStack.setItemMeta(meta);
            return itemStack;
        }else if(field.getType().isArray()) {
            ItemStack itemStack = new ItemStack(obj.getStructure()!=null? Material.LIME_DYE: Material.GRAY_DYE);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName("§f§n" + field.getName());
            itemStack.setItemMeta(meta);
            return itemStack;
        }
        return null;
    }
}
