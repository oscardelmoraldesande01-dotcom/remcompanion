package com.remmod.remcompanion;

import com.remmod.remcompanion.entity.RemEntity;
import com.remmod.remcompanion.registry.ModEntities;
import com.remmod.remcompanion.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RemCompanion.MOD_ID)
public class RemCompanion {
    public static final String MOD_ID = "remcompanion";

    public RemCompanion() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        bus.register(this);
    }

    @SubscribeEvent
    public void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.REM.get(), RemEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.REM_SPAWN_EGG);
        }
    }
}
