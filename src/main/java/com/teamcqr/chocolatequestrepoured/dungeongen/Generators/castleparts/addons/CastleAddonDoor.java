package com.teamcqr.chocolatequestrepoured.dungeongen.Generators.castleparts.addons;

import com.teamcqr.chocolatequestrepoured.util.BlockPlacement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.ArrayList;

public class CastleAddonDoor implements ICastleAddon
{
    public enum Orientation
    {
        HORIZONTAL,
        VERTICAL
    }

    public enum DoorType
    {
        EMPTY,
        FENCE_BORDER
    }

    private int startX;
    private int startY;
    private int startZ;
    private int width;
    private int height;
    private Orientation orientation;
    private DoorType type;

    public CastleAddonDoor(int x, int y, int z, int width, int height, DoorType type, boolean horizontal)
    {
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.width = width;
        this.height = height;
        this.type = type;
        this.orientation = (horizontal) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    }

    @Override
    public void generate(ArrayList<BlockPlacement> blocks)
    {
        switch (type)
        {
            case FENCE_BORDER:
            {
                generateFenceBorder(blocks);
                break;
            }
            case EMPTY:
            default:
            {
                generateEmpty(blocks);
                break;
            }
        }
    }

    private void generateEmpty(ArrayList<BlockPlacement> blocks)
    {
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                if (orientation == Orientation.HORIZONTAL)
                {
                    blocks.add(new BlockPlacement(startX + i, startY + j, startZ, Blocks.AIR.getDefaultState()));
                }
                else //vertical
                {
                    blocks.add(new BlockPlacement(startX, startY + j, startZ + i, Blocks.AIR.getDefaultState()));
                }
            }
        }
    }

    private void generateFenceBorder(ArrayList<BlockPlacement> blocks)
    {
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                IBlockState blockToBuild;
                if (i == 0 || i == width - 1 || j == height - 1)
                {
                    blockToBuild = Blocks.DARK_OAK_FENCE.getDefaultState();
                }
                else
                {
                    blockToBuild = Blocks.AIR.getDefaultState();
                }
                if (orientation == Orientation.HORIZONTAL)
                {
                    blocks.add(new BlockPlacement(startX + i, startY + j, startZ, blockToBuild));
                }
                else //vertical
                {
                    blocks.add(new BlockPlacement(startX, startY + j, startZ + i, blockToBuild));
                }
            }
        }
    }
}