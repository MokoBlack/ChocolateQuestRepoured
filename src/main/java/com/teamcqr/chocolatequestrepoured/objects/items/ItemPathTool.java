package com.teamcqr.chocolatequestrepoured.objects.items;

import java.util.List;

import com.teamcqr.chocolatequestrepoured.capability.pathtool.CapabilityPathTool;
import com.teamcqr.chocolatequestrepoured.capability.pathtool.CapabilityPathToolProvider;
import com.teamcqr.chocolatequestrepoured.objects.entity.bases.AbstractEntityCQR;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemPathTool extends Item {

	public ItemPathTool() {
		this.setMaxStackSize(1);
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return CapabilityPathToolProvider.createProvider();
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		/*
		 * shift Left click -> apply path points and overwrite the existing ones
		 * left click -> get existing path points
		 */
		if(!player.world.isRemote) {
			CapabilityPathTool capa = stack.getCapability(CapabilityPathToolProvider.PATH_TOOL, null);
			if(entity instanceof AbstractEntityCQR) {
				if(player.isSneaking() && capa.getPathPoints().length > 0) {
					((AbstractEntityCQR)entity).setPath(capa.getPathPoints());
					((WorldServer) player.world).spawnParticle((EntityPlayerMP) player, EnumParticleTypes.VILLAGER_HAPPY, false, entity.posX, entity.posY + 0.5D, entity.posZ, 8, 0.5D, 0.5D, 0.5D, 0.1D);
				} else {
					BlockPos[] oldPath = ((AbstractEntityCQR)entity).getGuardPathPoints();
					final BlockPos home = ((AbstractEntityCQR)entity).getHomePositionCQR();
					if(oldPath.length > 0) {
						final BlockPos[] path = new BlockPos[oldPath.length];
						for(int i = 0; i < path.length; i++) {
							path[i] = oldPath[i];
							path[i] = path[i].add(home);
						}
						capa.setPathPoints(path);
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		/*
		 * Shift right click with no target block -> erase positions
		 * right click -> add position
		 */
		ItemStack stack = player.getHeldItem(hand);
		BlockPos position = pos.offset(side);
		if(player.isSneaking()) {
			resetPathPoints(stack, player);
		} else {
			addPathPoint(stack, position, player);
		}
		
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		CapabilityPathTool capability = stack.getCapability(CapabilityPathToolProvider.PATH_TOOL, null);
		if(capability.getPathPoints().length > 0) {
			tooltip.add("Path points: ");
			for(int i = 0; i < capability.getPathPoints().length; i++) {
				tooltip.add("  - " + capability.getPathPoints()[i].toString());
			}
		} else {
			tooltip.add("No Path stored");
		}
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		if(isSelected && entityIn instanceof EntityPlayer && entityIn.world.isRemote) {
			CapabilityPathTool capa = stack.getCapability(CapabilityPathToolProvider.PATH_TOOL, null);
			BlockPos[] path = capa.getPathPoints();
			//Draw path
			if(path.length > 0) {
				for(int i = 0; i < path.length; i++) {
					EnumParticleTypes particle = EnumParticleTypes.VILLAGER_HAPPY;
					if(i == 0) {
						particle = EnumParticleTypes.TOTEM;
					} else if(i == path.length -1) {
						particle = EnumParticleTypes.FLAME;
					}
					Vec3d pos = new Vec3d(path[i]);
					pos = pos.addVector(0.5, 0.5, 0.5);
					if(i > 0) {
						Vec3d v = new Vec3d(path[i]).subtract(new Vec3d(path[i-1]));
						double dist = v.lengthVector();
						v = v.normalize();
						//Draw connection lines
						for(double j = 0.25; j < dist; j += 0.5) {
							worldIn.spawnParticle(EnumParticleTypes.CRIT_MAGIC, true, pos.x - j * v.x, pos.y - j * v.y, pos.z - j * v.z, v.x * 0.1, v.y * 0.1, v.z * 0.1, 3);
						}
					}
						//Draw start point
					worldIn.spawnParticle(particle, true, pos.x, pos.y, pos.z, 0.0, 0.025, 0.0, 10);
				}
			}
		}
	}
	
	/*
	 * Methods to edit capability
	 */
	public static void resetPathPoints(ItemStack stack, EntityPlayer player) {
		stack.getCapability(CapabilityPathToolProvider.PATH_TOOL, null).clearPathPoints();
		if(player.world.isRemote) {
			player.sendMessage(new TextComponentString("Cleared Path!"));
		}
	}
	
	public static void addPathPoint(ItemStack stack, BlockPos pos, EntityPlayer player) {
		stack.getCapability(CapabilityPathToolProvider.PATH_TOOL, null).addPathPoint(pos);
		if(player.world.isRemote) {
			player.sendMessage(new TextComponentString("Added " + pos.toString() + " to the path!"));
		}
	}

}
