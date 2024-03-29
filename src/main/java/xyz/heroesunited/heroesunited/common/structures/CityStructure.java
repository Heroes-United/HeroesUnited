//package xyz.heroesunited.heroesunited.common.structures;
//
//import com.mojang.serialization.Codec;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Registry;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.core.Vec3i;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.LevelHeightAccessor;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.chunk.ChunkGenerator;
//import net.minecraft.world.level.levelgen.GenerationStep;
//import net.minecraft.world.level.levelgen.feature.StructureFeature;
//import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
//import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
//import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
//import net.minecraft.world.level.levelgen.structure.StructurePiece;
//import net.minecraft.world.level.levelgen.structure.StructureStart;
//import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
//import org.apache.logging.log4j.Level;
//import xyz.heroesunited.heroesunited.HeroesUnited;
//
//public class CityStructure extends StructureFeature<NoneFeatureConfiguration> {
//
//    public CityStructure(Codec<NoneFeatureConfiguration> p_197165_, PieceGeneratorSupplier<NoneFeatureConfiguration> p_197166_) {
//        super(p_197165_, p_197166_);
//    }
//
//    @Override
//    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
//        return CityStructure.Start::new;
//    }
//
//    @Override
//    public GenerationStep.Decoration step() {
//        return GenerationStep.Decoration.TOP_LAYER_MODIFICATION;
//    }
//
//    public static class Start extends StructureStart<NoneFeatureConfiguration> {
//        public Start(StructureFeature<NoneFeatureConfiguration> structureIn, ChunkPos chunkPos, int referenceIn, long seedIn) {
//            super(structureIn, chunkPos, referenceIn, seedIn);
//        }
//
//        @Override
//        public void generatePieces(RegistryAccess dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager templateManagerIn, ChunkPos chunkPos, Biome biomeIn, NoneFeatureConfiguration config, LevelHeightAccessor p_163621_) {
//
//            // Turns the chunk coordinates into actual coordinates we can use
//            int x = chunkPos.x * 16;
//            int z = chunkPos.z * 16;
//
//            /*
//             * We pass this into addPieces to tell it where to generate the structure.
//             * If addPieces's last parameter is true, blockpos's Y value is ignored and the
//             * structure will spawn at terrain height instead. Set that parameter to false to
//             * force the structure to spawn at blockpos's Y value instead. You got options here!
//             */
//            BlockPos centerPos = new BlockPos(x, 0, z);
//
//            /*
//             * If you are doing Nether structures, you'll probably want to spawn your structure on top of ledges.
//             * Best way to do that is to use getBaseColumn to grab a column of blocks at the structure's x/z position.
//             * Then loop through it and look for land with air above it and set blockpos's Y value to it.
//             * Make sure to set the final boolean in JigsawManager.addPieces to false so
//             * that the structure spawns at blockpos's y value instead of placing the structure on the Bedrock roof!
//             */
//            //IBlockReader blockReader = chunkGenerator.getBaseColumn(blockpos.getX(), blockpos.getZ());
//
//            // All a structure has to do is call this method to turn it into a jigsaw based structure!
//            JigsawPlacement.addPieces(
//                    dynamicRegistryManager,
//                    new JigsawConfiguration(() -> dynamicRegistryManager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
//                            // The path to the starting Template Pool JSON file to read.
//                            //
//                            // Note, this is "structure_tutorial:run_down_house/start_pool" which means
//                            // the game will automatically look into the following path for the template pool:
//                            // "resources/data/structure_tutorial/worldgen/template_pool/run_down_house/street.json"
//                            // This is why your pool files must be in "data/<modid>/worldgen/template_pool/<the path to the pool here>"
//                            // because the game automatically will check in worldgen/template_pool for the pools.
////                            .get(new ResourceLocation(HeroesUnited.MODID, "city/build_1/bottom")),
//                            .get(new ResourceLocation(HeroesUnited.MODID, "city/structure")),
//
//                            // How many pieces outward from center can a recursive jigsaw structure spawn.
//                            // Our structure is only 1 piece outward and isn't recursive so any value of 1 or more doesn't change anything.
//                            // However, I recommend you keep this a decent value like 10 so people can use datapacks to add additional pieces to your structure easily.
//                            // But don't make it too large for recursive structures like villages or you'll crash server due to hundreds of pieces attempting to generate!
//                            50),
//                    PoolElementStructurePiece::new,
//                    chunkGenerator,
//                    templateManagerIn,
//                    centerPos, // Position of the structure. Y value is ignored if last parameter is set to true.
//                    this, // The list that will be populated with the jigsaw pieces after this method.
//                    this.random,
//                    false, // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
//                    // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
//                    true, p_163621_);  // Place at heightmap (top land). Set this to false for structure to be place at the passed in blockpos's Y value instead.
//            // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
//
//
//            // **THE FOLLOWING TWO LINES ARE OPTIONAL**
//            //
//            // Right here, you can do interesting stuff with the pieces in this.pieces such as offset the
//            // center piece by 50 blocks up for no reason, remove repeats of a piece or add a new piece so
//            // only 1 of that piece exists, etc. But you do not have access to the piece's blocks as this list
//            // holds just the piece's size and positions. Blocks will be placed later in JigsawManager.
//            //
//            // In this case, we do `piece.offset` to raise pieces up by 1 block so that the house is not right on
//            // the surface of water or sunken into land a bit.
//            //
//            // Then we extend the bounding box down by 1 by doing `piece.getBoundingBox().minY` which will cause the
//            // land formed around the structure to be lowered and not cover the doorstep. You can raise the bounding
//            // box to force the structure to be buried as well. This bounding box stuff with land is only for structures
//            // that you added to Structure.NOISE_AFFECTING_FEATURES field handles adding land around the base of structures.
//
//            // Since by default, the start piece of a structure spawns with it's corner at centerPos
//            // and will randomly rotate around that corner, we will center the piece on centerPos instead.
//            // This is so that our structure's start piece is now centered on the water check done in isFeatureChunk.
//            // Whatever the offset done to center the start piece, that offset is applied to all other pieces
//            // so the entire structure is shifted properly to the new spot.
//            Vec3i structureCenter = this.pieces.get(0).getBoundingBox().getCenter();
//            int xOffset = centerPos.getX() - structureCenter.getX();
//            int zOffset = centerPos.getZ() - structureCenter.getZ();
//            for(StructurePiece structurePiece : this.pieces){
//                structurePiece.move(xOffset, 0, zOffset);
//            }
//
//            // I use to debug and quickly find out if the structure is spawning or not and where it is.
//            // This is returning the coordinates of the center starting piece.
//            HeroesUnited.LOGGER.log(Level.DEBUG, "City at " +
//                    this.pieces.get(0).getBoundingBox().minX() + " " +
//                    this.pieces.get(0).getBoundingBox().minY() + " " +
//                    this.pieces.get(0).getBoundingBox().minZ());
//        }
//    }
//}
