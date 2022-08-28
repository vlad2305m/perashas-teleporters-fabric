package com.vlad2305m.perashasteleportersfabric.interfaces;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;

public interface TeleporterEntity {
    boolean isAPortal();
    void setPortal(boolean isAPortal);

    TeleporterSettings getSettings();
    void setSettings(TeleporterSettings settings);

    void breakPortal() throws CommandSyntaxException;

}
