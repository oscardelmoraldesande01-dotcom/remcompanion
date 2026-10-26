package com.remmod.remcompanion.registry;

import com.remmod.remcompanion.RemCompanion;
import com.remmod.remcompanion.entity.RemEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RemCompanion.MOD_ID);

    public static final RegistryObject<EntityType<RemEntity>> REM = ENTITIES.register("rem",
            () -> EntityType.Builder.of(RemEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build("rem"));
}
