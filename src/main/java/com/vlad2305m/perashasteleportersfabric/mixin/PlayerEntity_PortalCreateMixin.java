package com.vlad2305m.perashasteleportersfabric.mixin;

import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;
import com.vlad2305m.perashasteleportersfabric.interfaces.WorldInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntity_PortalCreateMixin extends LivingEntity {
    protected PlayerEntity_PortalCreateMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setPickupDelay(I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void addPearlTracker(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir, double d, ItemEntity itemEntity){
        if(world.isClient())return;
        if(stack.getCount() == 1 && stack.getItem() == Items.ENDER_PEARL) {
            ((WorldInterface)world).addPotentialPortal(itemEntity);
        }
        if(stack.getCount() == 1 && Arrays.stream(TeleporterSettings.UPGRADES.values()).anyMatch((TeleporterSettings.UPGRADES u) -> u.checkItem.test(stack))) {
            ((WorldInterface)world).addPotentialUpgrade(itemEntity);
        }
    }
}
