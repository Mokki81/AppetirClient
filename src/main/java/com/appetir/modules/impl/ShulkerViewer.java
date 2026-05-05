package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class ShulkerViewer extends Module {

    public ShulkerViewer() {
        super("ShulkerViewer", "Просмотр содержимого шалкеров", Category.RENDER);
    }

    // Возвращает список предметов из шалкера в руке
    public static List<ItemStack> getShulkerContents(ItemStack stack) {
        List<ItemStack> items = new ArrayList<>();
        if (!(stack.getItem() instanceof BlockItem)) return items;
        if (!(((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)) return items;

        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return items;
        NbtCompound blockEntity = nbt.getCompound("BlockEntityTag");
        if (blockEntity == null) return items;
        NbtList itemList = blockEntity.getList("Items", 10);
        for (int i = 0; i < itemList.size(); i++) {
            items.add(ItemStack.fromNbt(itemList.getCompound(i)));
        }
        return items;
    }
}
