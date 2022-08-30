package com.vlad2305m.perashasteleportersfabric;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class TeleporterSettings {
    public static final Vec3i[] tpStructure = new Vec3i[]{
            new Vec3i(-2, 0, -2),                                new Vec3i(-2, 0, 0),                            new Vec3i(-2,0,2),
                                           new Vec3i(-1, 0, -1), new Vec3i(-1,0,0), new Vec3i(-1, 0, 1),
            new Vec3i(0, 0, -2),   new Vec3i(0, 0, -1),                             new Vec3i(0, 0, 1),  new Vec3i(0,0,2),
                                           new Vec3i(1, 0, -1),  new Vec3i(1,0,0),  new Vec3i(1, 0, 1),
            new Vec3i(2, 0, -2),                                 new Vec3i(2, 0, 0),                             new Vec3i(2,0,2),
    };
    public static final Block material = Blocks.REDSTONE_WIRE;
    public static final Item activationMaterial = Items.ENDER_PEARL;
    public static final String portalItemTemp =
            "{id:\"minecraft:ender_pearl\",Count:1b,tag:{display:{Name:'{\"text\":\"Teleporter%s\",\"color\":\"white\",\"italic\":false}'},Enchantments:[{}]}}";
    public static final String heldItemNbtKey = "ptHeldItem";
    public static final String settingsNbtKey = "ptSettings";

    public String name = "";

    public boolean sticky = false;

    public int[] color = intToRGB(DyeColor.MAGENTA.getFireworkColor());
    public int[][] colorCycle = null;
    public int[] secondaryColor = null;

    public enum UPGRADES {
        NULL((ItemStack s) -> false, (TeleporterSettings t, ItemStack s) -> {}),
        NAME((ItemStack s) -> s.isOf(Items.PAPER), (TeleporterSettings t, ItemStack s) -> t.name = s.hasCustomName() ? s.getName().getString() : ""),
        STICKIFY((ItemStack s) -> s.isOf(Items.SLIME_BALL), (TeleporterSettings t, ItemStack s) -> t.sticky = true),
        UNSTICKIFY((ItemStack s) -> s.isOf(Items.SUGAR), (TeleporterSettings t, ItemStack s) -> t.sticky = false),
        COLOR_DYE((ItemStack s) -> s.getItem() instanceof DyeItem, (TeleporterSettings t, ItemStack s) -> {t.color = intToRGB(((DyeItem)s.getItem()).getColor().getFireworkColor());t.colorCycle=null;t.secondaryColor=null;}),
        COLOR_FIREWORK_STAR((ItemStack s) -> s.getItem() instanceof FireworkStarItem, (TeleporterSettings t, ItemStack s) -> {
            NbtCompound nbt = s.getSubNbt("Explosion");if (nbt == null || nbt.isEmpty()) return;
            int[] c = nbt.getIntArray("Colors");
            int[] fc = nbt.getIntArray("FadeColors");
            if (c.length == 0 && fc.length == 0) return;
            if (c.length > 0 && fc.length == 0) {
                t.colorCycle = Arrays.stream(c).mapToObj(TeleporterSettings::intToRGB).toArray(int[][]::new); t.secondaryColor = null; t.color = null;
            } else if (c.length > 0) {
                t.color = Arrays.stream(c).collect(() -> new int[]{0,0,0}, TeleporterSettings::intToRGB, (int[] a, int[] b) -> {a[0]+=b[0];a[1]+=b[1];a[2]+=b[2];});
                for (int i : new int[]{0, 1, 2}) t.color[i] /= c.length;
                t.secondaryColor = Arrays.stream(fc).collect(() -> new int[]{0,0,0}, TeleporterSettings::intToRGB, (int[] a, int[] b) -> {a[0]+=b[0];a[1]+=b[1];a[2]+=b[2];});
                for (int i : new int[]{0, 1, 2}) t.secondaryColor[i] /= fc.length;
                t.colorCycle = null;
            }
        });
        public final Predicate<ItemStack> checkItem;
        public final BiConsumer<TeleporterSettings, ItemStack> upgradeFunction;
        UPGRADES(Predicate<ItemStack> checkItem, BiConsumer<TeleporterSettings, ItemStack> upgradeF){this.checkItem = checkItem; upgradeFunction = upgradeF;}
    }

    public NbtCompound toNbt(){
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putBoolean("sticky", sticky);
        if (color!=null)nbt.putIntArray("color", color);
        if (secondaryColor!=null) nbt.putIntArray("secondaryColor", secondaryColor);
        if (colorCycle!=null) {NbtList l = new NbtList();
            for (int[] i : colorCycle) l.add(new NbtIntArray(i));
            nbt.put("colorCycle", l);}
        return nbt;
    }

    public void readNbt(NbtCompound nbt){
        name = nbt.getString("name");
        sticky = nbt.getBoolean("sticky");
        color = nbt.getIntArray("color");
        if (nbt.contains("secondaryColor")) secondaryColor = nbt.getIntArray("secondaryColor");
        if (nbt.contains("colorCycle") && !((NbtList)nbt.get("colorCycle")).isEmpty()) {
            color = null;
            List<int[]> cc = new ArrayList<>();
            for (NbtElement i : (NbtList) nbt.get("colorCycle")) cc.add(((NbtIntArray)i).getIntArray());
            colorCycle = cc.toArray(new int[][]{});
        }
    }

    public int currCheckI = 0;
    public boolean checkDisintegrity(BlockPos pos, World world, boolean full) {
        if (full){
            return checkDisintegrity(pos, world);
        }
        else {
            if (!world.getBlockState(pos.add(tpStructure[currCheckI++])).isOf(material)) return true;
            currCheckI %= tpStructure.length;
        }
        return false;
    }

    public static boolean checkDisintegrity(BlockPos pos, World world) {
            for (Vec3i i : tpStructure) {
                if (!world.getBlockState(pos.add(i)).isOf(material)) return true;
            }
        return false;
    }

    public static boolean isInStructure(BlockPos pos, BlockPos blockPos) {
        Vec3i d = pos.subtract(blockPos);
        for (Vec3i i : tpStructure) {
            if (i.equals(d)) return true;
        }
        return false;
    }

    public static boolean checkSupport(BlockPos pos, World world) {
        for (Vec3i i : tpStructure) {
            if (!Block.isFaceFullSquare(world.getBlockState(pos.add(i).add(0,-1,0)).getCollisionShape(world, pos.add(i).add(0,-1,0)), Direction.UP)) return false;
        }
        return true;
    }

    public static boolean checkVoid(BlockPos pos, World world) {
        for (Vec3i i : tpStructure) {
            if (!world.isAir(pos.add(i))) return false;
        }
        return true;
    }

    public void destroyStructure(BlockPos pos, World world) {
        if (sticky){
            for (Vec3i i : tpStructure) {
                world.breakBlock(pos.add(i), false);
            }
        }
    }

    public void setupTeleporter(World world, BlockPos pos) {
        if (sticky) {
            for (Vec3i i : tpStructure) {
                world.setBlockState(pos.add(i), Blocks.REDSTONE_WIRE.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }
    public static int[] intToRGB(int color) {
        int j = (color & 16711680) >> 16;
        int k = (color & '\uff00') >> 8;
        int l = (color & 255);
        return new int[]{j, k , l};
    }

    private static void intToRGB(int[] floats, int color) {
        floats[0] += ((color & 16711680) >> 16);
        floats[1] += ((color & '\uff00') >> 8);
        floats[2] += (color & 255);
    }
}
