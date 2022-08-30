package com.vlad2305m.perashasteleportersfabric;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class TeleporterAnimations {
    public static void tpCreate(ServerWorld world, Vec3d pos) {
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1f, 0.91f, 20);
        world.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(TeleporterSettings.activationMaterial)), pos.getX(), pos.getY(), pos.getZ(),10, 0, 1,0,0.5);
    }

    public static void tpCreateFail(ServerWorld world, Vec3d pos) {
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, SoundCategory.PLAYERS, 1f, 0.5f, 20);
    }
}
