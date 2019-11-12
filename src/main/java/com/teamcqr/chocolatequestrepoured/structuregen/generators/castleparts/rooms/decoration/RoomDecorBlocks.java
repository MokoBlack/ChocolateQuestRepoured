package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.decoration;

import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.CastleDungeon;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class RoomDecorBlocks implements IRoomDecor
{
    protected class DecoBlockOffset
    {
        public Vec3i offset;
        public IBlockState block;

        protected DecoBlockOffset(int x, int y, int z, IBlockState block)
        {
            this.offset = new Vec3i(x, y, z);
            this.block = block;
        }

        protected DecoBlockOffset(Vec3i offset, IBlockState block)
        {
            this.offset = offset;
            this.block = block;
        }
    }

    protected List<DecoBlockOffset> schematic; //Array of blockstates and their offsets

    protected RoomDecorBlocks()
    {
        this.schematic = new ArrayList<>();
        makeSchematic();
    }

    protected abstract void makeSchematic();

    public boolean wouldFit(BlockPos start, EnumFacing side, HashSet<BlockPos> decoArea, HashSet<BlockPos> decoMap)
    {
        ArrayList<DecoBlockOffset> rotated = alignSchematic(side);

        for (DecoBlockOffset placement : rotated)
        {
            BlockPos pos = start.add(placement.offset);
            if (!decoArea.contains(pos) || decoMap.contains(pos))
            {
                return false;
            }
        }

        return true;
    }

    public void build(World world, CastleDungeon dungeon, BlockPos start, EnumFacing side, HashSet<BlockPos> decoMap)
    {
        ArrayList<DecoBlockOffset> rotated = alignSchematic(side);

        for (DecoBlockOffset placement : rotated)
        {
            BlockPos pos = start.add(placement.offset);
            world.setBlockState(pos, placement.block);
            decoMap.add(pos);
        }

    }

    private ArrayList<DecoBlockOffset> alignSchematic(EnumFacing side)
    {
        ArrayList<DecoBlockOffset> result = new ArrayList<>();

        for (DecoBlockOffset p : schematic)
        {
            result.add(new DecoBlockOffset(DungeonGenUtils.rotateVec3i(p.offset, side), getRotatedBlockState(p.block, side)));
        }

        return result;
    }

    /*
    * This can be overridden so individual block properties can be changed
     */
    protected IBlockState getRotatedBlockState(IBlockState state, EnumFacing side)
    {
        return state;
    }
}