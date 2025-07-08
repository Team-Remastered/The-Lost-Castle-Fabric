package net.teamremastered.tlc.mixin;

import net.minecraft.advancement.Advancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Advancement.Builder.class)
public interface AdvBuilderAccessor {
    @Accessor
    String[][] getRequirements();
}
