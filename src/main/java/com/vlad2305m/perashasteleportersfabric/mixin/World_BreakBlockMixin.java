package com.vlad2305m.perashasteleportersfabric.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;
import com.vlad2305m.perashasteleportersfabric.interfaces.TeleporterEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(World.class)
public abstract class World_BreakBlockMixin {
    @Shadow public abstract <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate);

    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    //@Inject(method = "removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", at = @At("HEAD"))
    //public void cB1(BlockPos pos, boolean move, CallbackInfoReturnable<Boolean> cir){ checkBreak(pos); }

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"))
    public void cB2(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir){ if (!getBlockState(pos).isOf(state.getBlock())) checkBreak(pos); }

    private void checkBreak(BlockPos pos) {
        if (getBlockState(pos).isOf(TeleporterSettings.material))
            getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class), new Box(new Vec3d(pos.getX()+5, pos.getY()-.5, pos.getZ()+5), new Vec3d(pos.getX()-4, pos.getY()+.5, pos.getZ()-4)),
                    (Entity e)-> e.isAlive() && ((TeleporterEntity)e).isAPortal())
                .forEach((Entity tp) -> {if (TeleporterSettings.isInStructure(tp.getBlockPos(), pos)) {
                    try {
                        ((TeleporterEntity)tp).breakPortal();
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
                });
    }


}
