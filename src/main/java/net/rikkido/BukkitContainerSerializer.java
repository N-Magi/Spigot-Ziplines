package net.rikkido;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.AccessFlag.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class BukkitContainerSerializer {
    // public static <type> byte[] serialize(type obj) {
    //     try {
    //         var outstream = new ByteArrayOutputStream();
    //         //var a = new BukkitObjectOutputStream();
    //         //var dataOut = new ObjectOutputStream(outstream);
    //         var dataOut = new BukkitObjectOutputStream(outstream);
    //         dataOut.writeObject(obj);
    //         dataOut.close();
    //         return outstream.toByteArray();
    //     } catch (Exception e) {
    //         Bukkit.getLogger().info(e.toString());
    //         //_plugin.getLogger().info("container wrote");
    //         return null; 
    //     }
    // }

    public static byte[] serialize(org.bukkit.Location obj) {
        try {
            var outstream = new ByteArrayOutputStream();
            //var a = new BukkitObjectOutputStream();
            var dataOut = new ObjectOutputStream(outstream);
            //var dataOut = new BukkitObjectOutputStream(outstream);
            dataOut.writeObject(obj.serialize());
            dataOut.close();
            return outstream.toByteArray();
        } catch (Exception e) {
            Bukkit.getLogger().info(e.toString());
            //_plugin.getLogger().info("container wrote");
            return null; 
        }
    }

    public static byte[] serialize(List<org.bukkit.Location> obj) {
        try {
            var outstream = new ByteArrayOutputStream();
            //var a = new BukkitObjectOutputStream();
            var dataOut = new ObjectOutputStream(outstream);
            //var dataOut = new BukkitObjectOutputStream(outstream);
            var serializedLocs = new ArrayList<Map<String, Object>>();
            for(var loc : obj){
                serializedLocs.add(loc.serialize());
            }
            dataOut.writeObject(serializedLocs);
            dataOut.close();
            return outstream.toByteArray();
        } catch (Exception e) {
            Bukkit.getLogger().info(e.toString());
            //_plugin.getLogger().info("container wrote");
            return null; 
        }
    }

    public static byte[] serialize(MovePlayer obj) {
        try {
            var outstream = new ByteArrayOutputStream();
            //var a = new BukkitObjectOutputStream();
            var dataOut = new ObjectOutputStream(outstream);
            //var dataOut = new BukkitObjectOutputStream(outstream);
            dataOut.writeObject(obj.serialize());
            dataOut.close();
            return outstream.toByteArray();
        } catch (Exception e) {
            Bukkit.getLogger().info(e.toString());
            //_plugin.getLogger().info("container wrote");
            return null; 
        }
    }
///////////////////////////////////////////////////////////////////////////////////
    public static org.bukkit.Location deserializeLoc(byte[] data){
        try {
            var inStream = new ByteArrayInputStream(data);
            var dataIn = new ObjectInputStream(inStream);
            //var dataIn = new BukkitObjectInputStream(inStream);
            var res = (Map<String, Object>) dataIn.readObject();
            return org.bukkit.Location.deserialize(res);
        } catch (Exception e) {
            //throw new Exception(e.getMessage());
            return null;
        }
    }

    public static MovePlayer deserializeMP(byte[] data){
        try {
            var inStream = new ByteArrayInputStream(data);
            var dataIn = new ObjectInputStream(inStream);
            //var dataIn = new BukkitObjectInputStream(inStream);
            var res = (Map<String, Object>) dataIn.readObject();
            return MovePlayer.deserialize(res);
        } catch (Exception e) {
            //throw new Exception(e.getMessage());
            return null;
        }
    }

    public static List<org.bukkit.Location> deserializeListLoc(byte[] data){
        try {
            var inStream = new ByteArrayInputStream(data);
            var dataIn = new ObjectInputStream(inStream);
            //var dataIn = new BukkitObjectInputStream(inStream);
            var res = (List<Map<String, Object>>) dataIn.readObject();
            var locs = new ArrayList<org.bukkit.Location>();
            for(var serializedLoc:res){
                locs.add(org.bukkit.Location.deserialize(serializedLoc));
            }
            return locs;
        } catch (Exception e) {
            //throw new Exception(e.getMessage());
            return null;
        }
    }




    // public static <Type> Type deserialize(byte[] data){
    //     try {
    //         var inStream = new ByteArrayInputStream(data);
    //         var dataIn = new ObjectInputStream(inStream);
    //         //var dataIn = new BukkitObjectInputStream(inStream);
    //         var res = (Type) dataIn.readObject();
    //         return res;
    //     } catch (Exception e) {
    //         //throw new Exception(e.getMessage());
    //         return null;
    //     }
    // }
}