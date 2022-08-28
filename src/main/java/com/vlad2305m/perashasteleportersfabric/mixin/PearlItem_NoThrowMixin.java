package com.vlad2305m.perashasteleportersfabric.mixin;

import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderPearlItem.class)
public class PearlItem_NoThrowMixin {

    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at =@At("HEAD"), cancellable = true)
    public void cancelThrow(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
        if (user.getStackInHand(hand).hasNbt() && user.getStackInHand(hand).getNbt().contains(TeleporterSettings.settingsNbtKey)) cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
    }
}
