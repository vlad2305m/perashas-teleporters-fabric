package com.vlad2305m.perashasteleportersfabric;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

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



    public NbtCompound toNbt(){
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putBoolean("sticky", sticky);
        return nbt;
    }

    public void readNbt(NbtCompound nbt){
        name = nbt.getString("name");
        sticky = nbt.getBoolean("sticky");
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

}
