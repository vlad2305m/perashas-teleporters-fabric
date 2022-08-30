package com.vlad2305m.perashasteleportersfabric.mixin;

import com.vlad2305m.perashasteleportersfabric.TeleporterAnimations;
import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;
import com.vlad2305m.perashasteleportersfabric.WorldNetworkManager;
import com.vlad2305m.perashasteleportersfabric.interfaces.LvlPropertiesI;
import com.vlad2305m.perashasteleportersfabric.interfaces.TeleporterEntity;
import com.vlad2305m.perashasteleportersfabric.interfaces.WorldInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorld_NetworkManagerMixin extends World implements WorldInterface {
    protected ServerWorld_NetworkManagerMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Shadow public abstract ServerWorld toServerWorld();
    @Shadow public abstract boolean spawnEntity(Entity entity);

    @Shadow @Final private MinecraftServer server;
    private WorldNetworkManager manager = null;

    @Override
    public RegistryEntry<DimensionType> getDimensionEntry(){
        if(manager == null) manager = ((LvlPropertiesI)server.getSaveProperties()).getPtWorldMap().computeIfAbsent(super.getRegistryKey().getValue().toString(),(a)->new WorldNetworkManager());
        return super.getDimensionEntry();
    }
    private final List<ItemEntity> potentials = new ArrayList<>();
    private final List<ItemEntity> potentialUps = new ArrayList<>();

    public void addPotentialPortal(ItemEntity e){
        potentials.add(e);
    }

    public void addPotentialUpgrade(ItemEntity e){
        potentialUps.add(e);
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
                        if(settings.sticky ? !TeleporterSettings.checkVoid(i.getBlockPos(), toServerWorld()) : TeleporterSettings.checkDisintegrity(i.getBlockPos(), toServerWorld())) {toRemove.add(i); TeleporterAnimations.tpCreateFail(toServerWorld(), i.getPos()); continue;}
                        settings.setupTeleporter(toServerWorld(), i.getBlockPos());
                        portal.setHeldItemStack(ItemStack.fromNbt(i.getStack().getNbt().getCompound(TeleporterSettings.heldItemNbtKey)));
                    }
                    ((TeleporterEntity)portal).setSettings(settings);
                    getManager().addTeleporter(settings, portal.getUuidAsString());
                    TeleporterAnimations.tpCreate(toServerWorld(), i.getPos());
                    i.getStack().decrement(1);
                    spawnEntity(portal);
                } else {
                    TeleporterAnimations.tpCreateFail(toServerWorld(), i.getPos());
                }
                toRemove.add(i);
            }
        }
        potentials.removeAll(toRemove);
        toRemove = new ArrayList<>();
        for (ItemEntity i : potentialUps) {
            if (!i.isAlive() || i.getStack().getCount() != 1) {toRemove.add(i);continue;}
            if (i.isOnGround()) {
                List<ItemFrameEntity> portals = toServerWorld().getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class), new Box(i.getPos().add(4.01, -.5, 4.01), i.getPos().add(-4.01, .5, -4.01)),
                        (Entity e) -> e.isAlive() && ((TeleporterEntity) e).isAPortal());
                if (portals.size() == 1) {
                    ItemFrameEntity portal = portals.get(0);
                    TeleporterSettings settings = ((TeleporterEntity)portal).getSettings();
                    Arrays.stream(TeleporterSettings.UPGRADES.values()).filter((TeleporterSettings.UPGRADES u) -> u.checkItem.test(i.getStack())).findAny().orElse(TeleporterSettings.UPGRADES.NULL).upgradeFunction.accept(settings, i.getStack());
                    i.getStack().decrement(1);
                }
                toRemove.add(i);
            }
        }
        potentialUps.removeAll(toRemove);
    }

    @Mixin(ServerEntityManager.class)
    private static class entityManagerMixin {
        @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"))
        public <T extends EntityLike> void injectTpSettings(T entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
            if (existing && entity instanceof ItemFrameEntity i && ((WorldInterface)i.world).getManager().getTeleporters().containsKey(i.getUuidAsString())) {
                ((TeleporterEntity) i).setSettings(((WorldInterface)i.world).getManager().getTeleporters().get(i.getUuidAsString()));
            }
        }
    }

    public WorldNetworkManager getManager() {
        return manager;
    }
}
