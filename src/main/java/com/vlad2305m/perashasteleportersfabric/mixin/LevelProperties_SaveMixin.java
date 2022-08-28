package com.vlad2305m.perashasteleportersfabric.mixin;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(LevelProperties.class)
public class LevelProperties_SaveMixin {
    private ConcurrentHashMap<Integer, ?> ptDataPerWorld = null;

    @Inject(method = "Lnet/minecraft/world/level/LevelProperties;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/NbtCompound;ZIIIFJJIIIZIZZZLnet/minecraft/world/border/WorldBorder$Properties;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/timer/Timer;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)V",at=@At("TAIL"))
    public void loader(DataFixer dataFixer, int dataVersion, NbtCompound playerData, boolean modded, int spawnX, int spawnY, int spawnZ, float spawnAngle, long time, long timeOfDay, int version, int clearWeatherTime, int rainTime, boolean raining, int thunderTime, boolean thundering, boolean initialized, boolean difficultyLocked, WorldBorder.Properties worldBorder, int wanderingTraderSpawnDelay, int wanderingTraderSpawnChance, UUID wanderingTraderId, Set serverBrands, Timer scheduledEvents, NbtCompound customBossEvents, NbtCompound dragonFight, LevelInfo levelInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfo ci){
        NbtCompound ptData = (NbtCompound) dragonFight.get(PTDATAKEY);
        dragonFight.remove(PTDATAKEY);
        if (ptData == null) {
            defaultPTData();
        } else {
            readPTData(ptData);
        }
    }

    private static final String PTDATAKEY = "perashasteleportersfabric_data";
    @Inject(method = "Lnet/minecraft/world/level/LevelProperties;updateProperties(Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/nbt/NbtCompound;Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "TAIL"))
    public void saver(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci){
        NbtCompound DF = (NbtCompound) levelNbt.get("DragonFight");
        if (DF == null) {System.out.println("Could not save PT data!!!");return;}
        NbtCompound ptData = new NbtCompound();
        writePTData(ptData);
        DF.put(PTDATAKEY, ptData);
    }

    private void defaultPTData() {
        ptDataPerWorld = new ConcurrentHashMap<>();
    }

    private void readPTData(NbtCompound compound) {
        if (ptDataPerWorld == null) defaultPTData();

    }

    private void writePTData(NbtCompound compound) {
        if (ptDataPerWorld == null) return;

    }

}