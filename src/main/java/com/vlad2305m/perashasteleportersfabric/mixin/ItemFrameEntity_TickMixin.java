package com.vlad2305m.perashasteleportersfabric.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vlad2305m.perashasteleportersfabric.TeleporterSettings;
import com.vlad2305m.perashasteleportersfabric.interfaces.TeleporterEntity;
import com.vlad2305m.perashasteleportersfabric.interfaces.WorldInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntity_TickMixin extends AbstractDecorationEntity implements TeleporterEntity {

    private boolean isAPortal = false;
    private boolean wasAPortal = false;
    private int obstructionCheckCounter = (int) (System.nanoTime() % 100);

    protected ItemFrameEntity_TickMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
    }

    private TeleporterSettings settings = null;

    @Override
    public void tick() {
        if (!isAPortal) {super.tick();return;}
        if (wasAPortal) this.kill();

        if (!this.world.isClient) {
            if (this.obstructionCheckCounter++ == 100) {
                this.obstructionCheckCounter = 0;
                this.attemptTickInVoid();
                if (!this.isRemoved() && settings.checkDisintegrity(this.getBlockPos(), this.world, false)) {
                    this.discard();
                    this.onBreak(null);
                }
            }
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {

    }

    public void breakPortal() throws CommandSyntaxException {
        if(world.isClient())return;
        if (settings == null) throw new NullPointerException("Perasha's teleporter settings are null on death");
        wasAPortal = true;
        ItemStack itemStack = this.getHeldItemStack();
        ItemStack portal = ItemStack.fromNbt(NbtHelper.fromNbtProviderString(String.format(TeleporterSettings.portalItemTemp, Objects.equals(settings.name, "") ? "" : " \\\\\""+settings.name+"\u00A7r\u00A7f\\\\\"")));
        NbtCompound nbt = portal.getOrCreateNbt();
        nbt.put(TeleporterSettings.heldItemNbtKey, itemStack.writeNbt(new NbtCompound()));
        nbt.put(TeleporterSettings.settingsNbtKey, settings.toNbt());
        NbtList attributes = new NbtList();
        if (settings.sticky) attributes.add(NbtString.of(Text.Serializer.toJson(Text.of("\u00A7aSticky"))));
        if (!itemStack.isEmpty()) attributes.add(NbtString.of(Text.Serializer.toJson(Text.of("\u00A77Contains "+itemStack))));
        if (settings.color != null) attributes.add(NbtString.of("[{\"text\":\"Color:\",\"color\":\"gray\",\"italic\":false},{\"text\":\"█\",\"color\":\""+ String.format("#%02X%02X%02X", settings.color[0], settings.color[1], settings.color[2])+"\",\"italic\":false}" + (settings.secondaryColor == null ? "" : ",{\"text\":\"~\",\"color\":\"gray\"},{\"text\":\"█\",\"color\":\""+ String.format("#%02X%02X%02X", settings.secondaryColor[0], settings.secondaryColor[1], settings.secondaryColor[2])+"\",\"italic\":false}")+']'));
        if (settings.colorCycle != null && settings.colorCycle.length > 0) attributes.add(NbtString.of("[{\"text\":\"Color:\",\"color\":\"gray\",\"italic\":false},"+String.join(",", Arrays.stream(settings.colorCycle).map((i)->"{\"text\":\"█\",\"color\":\""+ String.format("#%02X%02X%02X", i[0], i[1], i[2])+"\",\"italic\":false}").toArray(String[]::new))+"]"));
        if (!attributes.isEmpty()) portal.getNbt().getCompound("display").put("Lore", attributes);
        this.setHeldItemStack(portal);
        this.dropHeldStack(null,true);
        isAPortal = false;
        ((WorldInterface)world).getManager().removeTeleporter(settings, getUuidAsString());
        settings.destroyStructure(getBlockPos(), world);
        this.kill();
    }

    @Inject(method = "onBreak(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"),cancellable = true)
    public void dropPortal(Entity entity, CallbackInfo ci) throws CommandSyntaxException {
        if(isAPortal) {
            breakPortal();
            ci.cancel();
        }
    }

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
    public void shiftBreak(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (isAPortal){
            Entity entity = source.getAttacker();
            if (entity instanceof PlayerEntity player && player.isSneaking()) {
                this.discard();
                this.onBreak(entity);
                cir.setReturnValue(true);
            }
        }
    }

    @ModifyVariable(method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;dropHeldStack(Lnet/minecraft/entity/Entity;Z)V", at = @At("HEAD"), argsOnly = true)
    public boolean dontDrop(boolean value){
        if (isAPortal) return false;
        return value;
    }

    @Shadow
    public abstract ItemStack getHeldItemStack();

    @Shadow public abstract void kill();

    @Shadow protected abstract void dropHeldStack(@Nullable Entity entity, boolean alwaysDrop);

    @Shadow public abstract void setHeldItemStack(ItemStack stack);

    public boolean isAPortal(){return isAPortal;}
    public void setPortal(boolean b){isAPortal = b;}
    public TeleporterSettings getSettings(){return settings;}
    public void setSettings(TeleporterSettings s){settings = s; isAPortal = true;}
}
