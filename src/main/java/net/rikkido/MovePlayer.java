package net.rikkido;

import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.System.Logger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import javax.naming.directory.InvalidAttributesException;
import javax.xml.stream.events.Namespace;

import org.bukkit.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.Length;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.t;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.intellij.lang.annotations.JdkConstants.VerticalScrollBarPolicy;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.events.EventTarget;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.hover.content.Item;

import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import org.bukkit.util.*;



public class MovePlayer implements ConfigurationSerializable {
    UUID player;
    //Player player;
    Location src;
    Location dst;
    float progress;
    List<Location> path;
    org.bukkit.util.Vector length;

    @Override
    public @NotNull Map<String, Object> serialize() {
        var res = new HashMap<String, Object>();
        res.put("player", player);
        res.put("srclocation", src);
        res.put("dstlocation", dst);
        res.put("progress", progress);
        res.put("length", length);
        res.put("path", path);
        return res;
    }

    public static MovePlayer deserialize(Map<String, Object> args) {
        MovePlayer res = new MovePlayer();
        res.player = (UUID)args.get("player");
        res.src = (Location)args.get("srclocation");
        res.dst = (Location)args.get("dstlocation");
        res.progress = (float)args.get("progress");
        res.length = (org.bukkit.util.Vector)args.get("length");
        res.path = (List<Location>)args.get("path");
        return res;
    }
}