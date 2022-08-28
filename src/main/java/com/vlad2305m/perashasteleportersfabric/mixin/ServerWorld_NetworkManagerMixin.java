package com.vlad2305m.perashasteleportersfabric.mixin;

import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;
import com.vlad2305m.perashasteleportersfabric.WorldNetworkManager;
import com.vlad2305m.perashasteleportersfabric.interfaces.TeleporterEntity;
import com.vlad2305m.perashasteleportersfabric.interfaces.WorldInterface;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorld_NetworkManagerMixin implements WorldInterface {
    @Shadow public abstract ServerWorld toServerWorld();

    @Shadow public abstract void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed);

    @Shadow public abstract <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed);

    @Shadow public abstract boolean spawnEntity(Entity entity);

    @Shadow public abstract void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed);

    private final WorldNetworkManager manager = new WorldNetworkManager();

    private final List<ItemEntity> potentials = new ArrayList<>();

    public void addPotentialPortal(ItemEntity e){
        potentials.add(e);
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;tick()V"))
    public void tickPT(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        List<ItemEntity> toRemove = new ArrayList<>();
        for (ItemEntity i : potentials) {
            if (!i.isAlive() || i.getStack().getCount() != 1) {toRemove.add(i);continue;}
            if (i.isOnGround()) {
                if (toServerWorld().getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class), new Box(i.getPos().add(4.01, -.5, 4.01), i.getPos().add(-4.01, .5, -4.01)),
                            (Entity e) -> e.isAlive() && ((TeleporterEntity) e).isAPortal()).isEmpty()
                        && TeleporterSettings.checkSupport(i.getBlockPos(), toServerWorld()) && (!TeleporterSettings.checkDisintegrity(i.getBlockPos(), toServerWorld()) || i.getStack().hasNbt() && i.getStack().getNbt().contains(TeleporterSettings.settingsNbtKey) && TeleporterSettings.checkVoid(i.getBlockPos(), toServerWorld()))) {
                    ItemFrameEntity portal = new ItemFrameEntity(toServerWorld(), i.getBlockPos(), Direction.UP);
                    portal.setInvisible(true);
                    ((TeleporterEntity)portal).setPortal(true);
                    TeleporterSettings settings = new TeleporterSettings();
                    if (i.getStack().hasNbt() && i.getStack().getNbt().contains(TeleporterSettings.settingsNbtKey)) {
                        NbtCompound settingsNbt = i.getStack().getNbt().getCompound(TeleporterSettings.settingsNbtKey);
                        settings.readNbt(settingsNbt);
                        if(settings.sticky ? !TeleporterSettings.checkVoid(i.getBlockPos(), toServerWorld()) : TeleporterSettings.checkDisintegrity(i.getBlockPos(), toServerWorld())) {toRemove.add(i); playSound(null, i.getX(), i.getY(), i.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, SoundCategory.PLAYERS, 1f, 0.5f, 20); continue;}
                        settings.setupTeleporter(toServerWorld(), i.getBlockPos());
                        portal.setHeldItemStack(ItemStack.fromNbt(i.getStack().getNbt().getCompound(TeleporterSettings.heldItemNbtKey)));
                    }
                    ((TeleporterEntity)portal).setSettings(settings);
                    playSound(null, i.getX(), i.getY(), i.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1f, 0.91f, 20);
                    spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(TeleporterSettings.activationMaterial)), i.getX(), i.getY(), i.getZ(),10, 0, 1,0,0.5);
                    i.getStack().decrement(1);
                    spawnEntity(portal);
                } else {
                    playSound(null, i.getX(), i.getY(), i.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_STEP, SoundCategory.PLAYERS, 1f, 0.5f, 20);
                }
                toRemove.add(i);
            }
        }
        potentials.removeAll(toRemove);
    }
}
