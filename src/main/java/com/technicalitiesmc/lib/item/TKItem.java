package com.technicalitiesmc.lib.item;

import com.technicalitiesmc.lib.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class TKItem extends Item {

    public TKItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        String translationKey = getTranslationKey(stack) + ".tooltip";
        TranslationTextComponent component = new TranslationTextComponent(translationKey);
        if (component.getFormattedText().equals(translationKey)) return;
        tooltip.addAll(TextUtils.breakUp(component));
    }

}
