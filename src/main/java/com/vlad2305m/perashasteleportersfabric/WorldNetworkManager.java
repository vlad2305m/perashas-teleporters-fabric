package com.vlad2305m.perashasteleportersfabric;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class WorldNetworkManager {

    public Map<String, TeleporterSettings> getTeleporters() {
        return teleporters;
    }

    final Map<String, TeleporterSettings> teleporters = new HashMap<>(8);
    public WorldNetworkManager() {
    }

    public void addTeleporter(TeleporterSettings settings, String tpId) {
        teleporters.put(tpId, settings);
    }

    public void removeTeleporter(TeleporterSettings settings, String tpId) {
        teleporters.remove(tpId);
    }

    public NbtCompound writeNm(NbtCompound nbt){
        NbtList map = new NbtList();
        for (Map.Entry<String, TeleporterSettings> i : teleporters.entrySet())  {
            NbtCompound t = new NbtCompound();
            t.putString("id", i.getKey());
            NbtCompound st = i.getValue().toNbt();
            t.put("settings", st);
            map.add(t);
        }
        nbt.put("teleporters", map);
        return nbt;
    }

    public void readNm(NbtCompound nbt){
        if (!nbt.contains("teleporters")) return;
        NbtList map = (NbtList) nbt.get("teleporters");
        //noinspection ConstantConditions ,SuspiciousToArrayCall
        for (NbtCompound t : map.toArray(NbtCompound[]::new))  {
            String id = t.getString("id");
            TeleporterSettings st = new TeleporterSettings();
            st.readNbt(t.getCompound("settings"));
            teleporters.put(id, st);
        }
    }
}
