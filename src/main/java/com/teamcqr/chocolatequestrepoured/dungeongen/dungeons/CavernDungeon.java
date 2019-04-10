package com.teamcqr.chocolatequestrepoured.dungeongen.dungeons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.teamcqr.chocolatequestrepoured.dungeongen.DungeonBase;
import com.teamcqr.chocolatequestrepoured.dungeongen.IDungeonGenerator;
import com.teamcqr.chocolatequestrepoured.dungeongen.Generators.CavernGenerator;
import com.teamcqr.chocolatequestrepoured.dungeongen.Generators.CavernGenerator.EStairDirection;
import com.teamcqr.chocolatequestrepoured.dungeongen.lootchests.ELootTable;
import com.teamcqr.chocolatequestrepoured.util.DungeonGenUtils;
import com.teamcqr.chocolatequestrepoured.util.PropertyFileHelper;
import com.teamcqr.chocolatequestrepoured.util.VectorUtil;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class CavernDungeon extends DungeonBase {
	
	private int minRooms = 1;
	private int maxRooms = 8;
	private int minY = 30;
	private int maxY = 60;
	private int minCaveSize = 5;
	private int maxCaveSize = 15;
	private int minHeight = 4;
	private int maxHeight = 8;
	private int maxRoomDistance = 18;
	private int minRoomDistance = 10;
	private int chestChancePerRoom = 20;
	private boolean buildStaris = true;
	
	private boolean placeSpawners = false;
	private boolean placeBoss = true;
	private boolean lootChests = false;
	private String mobName = "minecraft:zombie";
	private String bossMobName = "minecraft:wither";
	private Block floorMaterial = Blocks.STONE;
	private Block airBlock = Blocks.AIR;

	@Override
	public IDungeonGenerator getGenerator() {
		return new CavernGenerator();
	}
	
	public CavernDungeon(File configFile) {
		super(configFile);
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
			prop.load(fis);
		} catch (FileNotFoundException e) {
			System.out.println("Unable to read config file: " + configFile.getName());
			e.printStackTrace();
			prop = null;
			configFile = null;
		} catch (IOException e) {
			System.out.println("Unable to read config file: " + configFile.getName());
			e.printStackTrace();
			prop = null;
			configFile = null;
		}
		if(prop != null && configFile != null && fis != null) {
			super.chance = PropertyFileHelper.getIntProperty(prop, "chance", 0);
			super.name = configFile.getName().replaceAll(".prop", "");
			super.allowedDims = PropertyFileHelper.getIntArrayProperty(prop, "allowedDims", new int[]{0});
			super.unique = PropertyFileHelper.getBooleanProperty(prop, "unique", false);
			
			this.minRooms = PropertyFileHelper.getIntProperty(prop, "minRooms", 1);
			this.maxRooms = PropertyFileHelper.getIntProperty(prop, "maxRooms", 8);
			
			this.minY = PropertyFileHelper.getIntProperty(prop, "minY", 30);
			this.maxY = PropertyFileHelper.getIntProperty(prop, "maxY", 50);
			
			this.minCaveSize = PropertyFileHelper.getIntProperty(prop, "minCaveSize", 5);
			this.maxCaveSize = PropertyFileHelper.getIntProperty(prop, "maxCaveSize", 15);
			
			this.minHeight = PropertyFileHelper.getIntProperty(prop, "minCaveHeight", 4);
			this.maxHeight = PropertyFileHelper.getIntProperty(prop, "maxCaveHeight", 12);
			
			this.maxRoomDistance = PropertyFileHelper.getIntProperty(prop, "maxRoomDistance", 20);
			this.minRoomDistance = PropertyFileHelper.getIntProperty(prop, "minRoomDistance", 12);
			
			this.buildStaris = PropertyFileHelper.getBooleanProperty(prop, "buildStairs", false);
			
			this.chestChancePerRoom = PropertyFileHelper.getIntProperty(prop, "chestChancePerRoom", 20);
			
			this.placeBoss = PropertyFileHelper.getBooleanProperty(prop, "spawnBoss", true);
			this.placeSpawners = PropertyFileHelper.getBooleanProperty(prop, "placeSpawners", true);
			this.lootChests = PropertyFileHelper.getBooleanProperty(prop, "lootchests", true);
			
			this.mobName = prop.getProperty("mobname", "minecraft:zombie");
			this.bossMobName = prop.getProperty("bossmobname", "minecraft:pig");
			
			this.floorMaterial = Blocks.STONE;
			try {
				Block tmp = Block.getBlockFromName(prop.getProperty("floorblock", "minecraft:stone"));
				if(tmp != null) {
					this.floorMaterial = tmp;
				}
			} catch(Exception ex) {
				System.out.println("couldnt load floor block! using default value (stone block)...");
			}
			
			this.airBlock = Blocks.AIR;
			try {
				Block tmp = Block.getBlockFromName(prop.getProperty("airblock", "minecraft:air"));
				if(tmp != null) {
					this.airBlock = tmp;
				}
			} catch(Exception ex) {
				System.out.println("couldnt load cave block! using default value (air block)...");
			}
			
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//One block below starts y is the floor...
	@Override
	protected void generate(int x, int z, World world, Chunk chunk) {
		super.generate(x, z, world, chunk);
		
		List<CavernGenerator> caves = new ArrayList<CavernGenerator>();
		HashMap<CavernGenerator, Integer> xMap = new HashMap<CavernGenerator, Integer>();
		HashMap<CavernGenerator, Integer> zMap = new HashMap<CavernGenerator, Integer>();
		
		int rooms = maxRooms <= minRooms ? minRooms : DungeonGenUtils.getIntBetweenBorders(minRooms, maxRooms, world.getSeed());
		int y = DungeonGenUtils.getIntBetweenBorders(minY, maxY, world.getSeed());
		System.out.println("Generating structure " + this.name + " at X: " + x + "  Y: " + y + "  Z: " + z + "  ...");
		int roomIndex = 1;
		
		int OrigX = new Integer(x);
		int OrigZ = new Integer(z);
		
		Vec3i distance = new Vec3i(0, 0, 0);
		
		do {
			x += distance.getX();
			z +=distance.getZ();
					
			CavernGenerator cave = new CavernGenerator(this);
			//Let the cave calculate its air blocks...
			cave.preProcess(world, chunk, x + distance.getX(), y, z + distance.getZ());
			
			int vLength = DungeonGenUtils.getIntBetweenBorders(minRoomDistance, maxRoomDistance, world.getSeed());
			distance = new Vec3i(vLength, 0, 0);
			double angle = ((Integer)new Random().nextInt(360)).doubleValue();
			distance = VectorUtil.rotateVectorAroundY(distance, angle);
			
			caves.add(cave);
			xMap.put(cave, x);
			zMap.put(cave, z);
		} while(roomIndex < rooms);
		
		int currX = new Integer(OrigX);
		int currZ = new Integer(OrigZ);
		for(int i = 0; i < caves.size(); i++) {
			
			CavernGenerator cave = caves.get(i);
			
			BlockPos start = new BlockPos(currX, y, currZ);
			BlockPos end = new BlockPos(xMap.get(cave), y, zMap.get(cave));
			
			//Dig out the cave...
			cave.buildStructure(world, chunk, xMap.get(cave), y, zMap.get(cave));
			
			//connect the tunnels
			cave.generateTunnel(start, end, world);
			
			//Place a loot chest....
			if(lootChests && DungeonGenUtils.PercentageRandom(this.chestChancePerRoom, world.getSeed())) {
				cave.fillChests(world, chunk, xMap.get(cave), y, zMap.get(cave));
			}
			
			//Place a spawner...
			if(placeSpawners) {
				cave.placeSpawners(world, chunk, xMap.get(cave), y +1, zMap.get(cave));
				/*world.setBlockState(start.add(0, 1, 0), Blocks.MOB_SPAWNER.getDefaultState());
				TileEntityMobSpawner spawner = (TileEntityMobSpawner) world.getTileEntity(start.add(0, 1, 0));
				//DONE: set spawner mob*/
			}
			
			currX = new Integer(end.getX());
			currZ = new Integer(end.getZ());
		}
		Random rdmCI = new Random();
		int bossCaveIndx = rdmCI.nextInt(caves.size());
		if(placeBoss) {
			
			BlockPos bossPos = new BlockPos(xMap.get(caves.get(bossCaveIndx)), y +1, zMap.get(caves.get(bossCaveIndx)));
			world.setBlockToAir(bossPos.down());
			
			//BOSS CHEST
			world.setBlockState(bossPos.down(), Blocks.CHEST.getDefaultState());
			TileEntityChest bossChest = (TileEntityChest) world.getTileEntity(bossPos.down());
			bossChest.setLootTable(ELootTable.CQ_VANILLA_END_CITY.getLootTable(), world.getSeed());
			
			//BOSS SPAWNER
		}
		if(this.buildStaris) {
			int entryCave = rdmCI.nextInt(caves.size());
			while(entryCave == bossCaveIndx) {
				entryCave = rdmCI.nextInt(caves.size());
			}
			caves.get(entryCave).buildLadder(EStairDirection.WEST, world);
		}
		
	}
	
	int getMinCaveHeight() {
		return this.minHeight;
	}
	int getMaxCaveHeight() {
		return this.maxHeight;
	}
	int getMinCaveSize() {
		return this.minCaveSize;
	}
	int getMaxCaveSize() {
		return this.maxCaveSize;
	}
	public Block getAirBlock() {
		return this.airBlock;
	}
	public Block getFloorBlock() {
		return this.floorMaterial;
	}
	
	public ResourceLocation getBossMob() {
		String[] bossString = this.bossMobName.split(":");
		
		return new ResourceLocation(bossString[0], bossString[1]);
	}
	public ResourceLocation getMob() {
		String[] bossString = this.mobName.split(":");
		
		return new ResourceLocation(bossString[0], bossString[1]);
	}

	public boolean doBuildStaris() {
		return buildStaris;
	}

}