package com.vlad2305m.perashasteleportersfabric.interfaces;

import com.vlad2305m.perashasteleportersfabric.WorldNetworkManager;
import net.minecraft.entity.ItemEntity;

public interface WorldInterface {

    void addPotentialPortal(ItemEntity itemEntity);
    void addPotentialUpgrade(ItemEntity itemEntity);
    WorldNetworkManager getManager();
}
