package net.teamremastered.tlc.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.teamremastered.tlc.registries.LCStructure;

import java.util.Optional;

public class LostCastle extends Structure {

    // A custom codec that changes the size limit for our code_structure_sky_fan.json's config to not be capped at 7.
    // With this, we can have a structure with a size limit up to 30 if we want to have extremely long branches of pieces in the structure.
    public static final Codec<LostCastle> CODEC = RecordCodecBuilder.<LostCastle>mapCodec(instance ->
            instance.group(LostCastle.configCodecBuilder(instance),
                    StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, LostCastle::new)).codec();

    private final RegistryEntry<StructurePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public LostCastle(Structure.Config  config,
                      RegistryEntry<StructurePool> startPool,
                      Optional<Identifier> startJigsawName,
                      int size,
                      HeightProvider startHeight,
                      Optional<Heightmap.Type> projectStartToHeightmap,
                      int maxDistanceFromCenter)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    private static int DistanceFromSpawn(ChunkPos structurePos) {
        //Spawn point is always around 0 ~ 0
        ChunkPos spawnPointPos = new ChunkPos(0, 0);

        //Convert the structure position into blocks and get the structure distance from spawn
        int structurePosX = structurePos.x << 4;
        int structurePosZ = structurePos.z << 4;
        int distanceFromSpawn = (int) Math.sqrt(Math.pow((structurePosX-spawnPointPos.x), 2) + Math.pow((structurePosZ-spawnPointPos.z), 2));

        System.out.println("Distance from spawn is: " + distanceFromSpawn);
        return distanceFromSpawn;
    }

    private static boolean extraSpawningChecks(Structure.Context  context) {

        // Grabs the chunk position we are at
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getStartX();
        int z = chunkPos.getStartZ();
        int startHeight = context.chunkGenerator().getHeightInGround(x, z, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, context.world(), context.noiseConfig());

        //Get Height at 78 blocks from castle spawn point (around the castle)
        int height1 = context.chunkGenerator().getHeightInGround(x + 78, z, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, context.world(), context.noiseConfig());
        int height2 = context.chunkGenerator().getHeightInGround(x -78, z, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, context.world(), context.noiseConfig());
        int height3 = context.chunkGenerator().getHeightInGround(x, z + 78, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, context.world(), context.noiseConfig());
        int height4 = context.chunkGenerator().getHeightInGround(x, z - 78, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, context.world(), context.noiseConfig());

        //Check if the height difference around the castle is bigger than 10 and castle if the structure is within 5000 blocks from spawn. If not spawn the castle.
        return (Math.abs(startHeight - height1) < 10 && Math.abs(startHeight - height2) < 10 && Math.abs(startHeight - height3) < 10 && Math.abs(startHeight - height4) < 10 && DistanceFromSpawn(chunkPos) > 5000);

    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context  context) {

        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
        if (!LostCastle.extraSpawningChecks(context)) {
            return Optional.empty();
        }

        // Set's our spawning blockpos's y
        // Since we are going to have heightmap/terrain height spawning set to true further down, this will make it so we spawn at terrain height.
        // If we wanted to spawn on ocean floor, we would set heightmap/terrain height spawning to false and the grab the y value of the terrain with OCEAN_FLOOR_WG heightmap.
        int startY = this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));

        // Turns the chunk coordinates into actual coordinates we can use. (Gets corner of that chunk)
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getStartX();
        int z = chunkPos.getStartZ();

        BlockPos blockPos = new BlockPos(x, startY, z);

        Optional<StructurePosition> structurePiecesGenerator =
                StructurePoolBasedGenerator.generate(
                        context, // Used for JigsawPlacement to get all the proper behaviors done.
                        this.startPool, // The starting pool to use to create the structure layout from
                        this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                        this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                        blockPos, // Where to spawn the structure.
                        false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                        this.projectStartToHeightmap, // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                        // Here, blockpos's y value is 0 which means the structure spawn 0 blocks above terrain height.
                        // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                        // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                        this.maxDistanceFromCenter); // Maximum limit for how far pieces can spawn from center. You cannot set this bigger than 128 or else pieces gets cutoff.

        /*
         * Note, you are always free to make your own JigsawPlacement class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         */

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }

    @Override
    public StructureType<?> getType() {
        return LCStructure.LOST_CASTLE; // Helps the game know how to turn this structure back to json to save to chunks
    }
}

