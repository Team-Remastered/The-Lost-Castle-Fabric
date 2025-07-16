package net.teamremastered.tlc.registries;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;
import net.teamremastered.tlc.TheLostCastle;
import net.teamremastered.tlc.structures.LostCastle;

public class LCStructure {

    public static StructureType<LostCastle> LOST_CASTLE;
    public static final Identifier CASTLE_ID = new Identifier(TheLostCastle.MODID, "lost_castle");

    /**
     * Registers the structure itself and sets what its path is. In this case, the
     * structure will have the Identifier of structure_tutorial:sky_structures.
     *
     * It is always a good idea to register your Structures so that other mods and datapacks can
     * use them too directly from the registries. It's great for mod/datapacks compatibility.
     */
    public static void init() {
        LOST_CASTLE = Registry.register(Registries.STRUCTURE_TYPE, CASTLE_ID, () -> LostCastle.CODEC);
    }

}
