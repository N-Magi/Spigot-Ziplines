package net.rikkido;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class BukkitContainerSerializer {
    public static <type> byte[] serialize(type obj) {
        try {
            var outstream = new ByteArrayOutputStream();
            var dataOut = new BukkitObjectOutputStream(outstream);
            dataOut.writeObject(obj);
            dataOut.close();
            return outstream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Bukkit Serialization Error.", e);
        }
    }

    public static <Type> Type deserialize(byte[] data) {
        try {
            var inStream = new ByteArrayInputStream(data);
            var dataIn = new BukkitObjectInputStream(inStream);
            var res = (Type) dataIn.readObject();
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Bukkit Deserialize Error.", e);
        }
    }
}