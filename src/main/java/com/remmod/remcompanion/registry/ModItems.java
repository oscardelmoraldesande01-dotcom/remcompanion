package com.remmod.remcompanion.registry;

import com.remmod.remcompanion.RemCompanion;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RemCompanion.MOD_ID);

    public static final RegistryObject<Item> REM_SPAWN_EGG = ITEMS.register("rem_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.REM, 0x5B8FD9, 0xF2F6FF, new Item.Properties()));
}
