package com.oitsjustjose.geolosys.items;

import com.google.common.collect.Lists;
import com.oitsjustjose.geolosys.Geolosys;
import com.oitsjustjose.geolosys.util.HelperFunctions;
import com.oitsjustjose.geolosys.util.Lib;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class ItemFieldManual extends Item
{
    public ItemFieldManual()
    {
        this.setMaxStackSize(1);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setRegistryName(new ResourceLocation(Lib.MODID, "FIELD_MANUAL"));
        this.setUnlocalizedName(this.getRegistryName().toString().replaceAll(":", "."));
        ForgeRegistries.ITEMS.register(this);
        this.registerModel();
    }

    private void registerModel()
    {
        Geolosys.getInstance().clientRegistry.register(new ItemStack(this), new ResourceLocation(this.getRegistryName().toString()), "inventory");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return stack.getItem().getRegistryName().toString().replaceAll(":", ".");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack)
    {
        return HelperFunctions.getTranslation(getUnlocalizedName(stack));
    }


    @Override
    @SideOnly(Side.CLIENT)
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBook(player, getBook(new Book(21)), false));
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
        {
            tooltip.add(HelperFunctions.getTranslation(this.getUnlocalizedName(stack) + ".tooltip"));
        }
        else
        {
            tooltip.add(HelperFunctions.getTranslation("shift.tooltip"));
        }
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getBook(Book book)
    {
        NBTTagCompound tags = new NBTTagCompound();
        List<NBTTagString> pages = Lists.newArrayList();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        TIntObjectHashMap<String> contents = new TIntObjectHashMap<>();

        for (Page page : book.pages)
        {
            StringBuilder sb = new StringBuilder();
            String title = page.getTitle();
            String text = page.getText();

            if (title == null || text == null || title.isEmpty() || text.isEmpty())
            {
                System.out.println("Well you succ");
                break;
            }

            contents.put(pages.size(), title);
            sb.append(TextFormatting.UNDERLINE).append(title).append(TextFormatting.RESET).append("\n\n").append(text);
            String formattedString = sb.toString();
            List<String> splitStrings = fontRenderer.listFormattedStringToWidth(formattedString, 116);

            StringBuilder sb2 = new StringBuilder();
            int lineNumber = 0;
            for (String s : splitStrings)
            {
                sb2.append(s).append("\n");
                lineNumber++;
                if (lineNumber >= 13)
                {
                    pages.add(new NBTTagString(sb2.toString()));
                    sb2 = new StringBuilder();
                    lineNumber = 0;
                }
            }

            if (lineNumber != 0)
            {
                pages.add(new NBTTagString(sb2.toString()));
            }
        }

        final NBTTagList pageList = new NBTTagList();
        pageList.appendTag((NBTBase) new NBTTagString(TextFormatting.BOLD + "\n\n\n\n      Geolosys\n    Field Manual"));
        final int offset = 3 + contents.size() / 13;
        final int[] keys = contents.keys();
        Arrays.sort(keys);
        StringBuilder builder3 = new StringBuilder("Contents:\n\n");
        int i = 2;
        for (final int key : keys)
        {
            String line;
            int a;
            for (line = contents.get(key), a = key + offset; fontRenderer.listFormattedStringToWidth(line + " " + a, 116).size() > 1; line = line.substring(0, line.length() - 1))
                ;
            for (line += " "; fontRenderer.listFormattedStringToWidth(line + " " + a, 116).size() == 1; line += " ")
                ;
            line += a;
            builder3.append(line).append('\n');
            if (++i >= 13)
            {
                i = 0;
                pageList.appendTag(new NBTTagString(builder3.toString()));
                builder3 = new StringBuilder();
            }
        }

        if (i != 0)
        {
            pageList.appendTag(new NBTTagString(builder3.toString()));
        }
        for (final NBTTagString page2 : pages)
        {
            pageList.appendTag(page2);
        }
        tags.setTag("pages", pageList);
        tags.setString("title", HelperFunctions.getTranslation("geolosys.field_manual.name"));
        tags.setString("author", "oitsjustjose");
        final ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.setTagCompound(tags);
        return stack;
    }

    @SideOnly(Side.CLIENT)
    public class Book
    {
        private List<Page> pages;

        public Book(int pageCount)
        {
            pages = Lists.newArrayList();
            for (int i = 0; i < pageCount; i++)
            {
                pages.add(new Page(i + 1));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public class Page
    {
        private String title;
        private String text;

        public Page(int pageNumber)
        {
            title = translateTitle(pageNumber);
            text = translatePageText(pageNumber);
        }

        public String getTitle()
        {
            return title;
        }

        public String getText()
        {
            return text;
        }
    }

    @SideOnly(Side.CLIENT)
    public static String translatePageText(int pageNumber)
    {
        String langFile = Minecraft.getMinecraft().gameSettings.language;
        langFile = langFile.substring(0, langFile.indexOf("_")) + langFile.substring(langFile.indexOf("_")).toUpperCase();
        InputStream in = Geolosys.class.getResourceAsStream("/assets/geolosys/book/" + langFile + ".lang");
        try
        {
            for (String s : IOUtils.readLines(in, "utf-8"))
            {
                if (s.indexOf("=") == -1)
                {
                    continue;
                }
                if (s.substring(0, s.indexOf("=")).equals("page_" + pageNumber + "_text"))
                {
                    return s.substring(s.indexOf("=") + 1);
                }
            }
        }
        catch (IOException e)
        {
        }
        return "ERROR READING PAGE " + pageNumber;
    }

    @SideOnly(Side.CLIENT)
    public static String translateTitle(int pageNumber)
    {
        String langFile = Minecraft.getMinecraft().gameSettings.language;
        langFile = langFile.substring(0, langFile.indexOf("_")) + langFile.substring(langFile.indexOf("_")).toUpperCase();
        InputStream in = Geolosys.class.getResourceAsStream("/assets/geolosys/book/" + langFile + ".lang");
        try
        {
            for (String s : IOUtils.readLines(in, "utf-8"))
            {
                if (s.indexOf("=") == -1)
                {
                    continue;
                }
                if (s.substring(0, s.indexOf("=")).equals("page_" + pageNumber + "_title"))
                {
                    return s.substring(s.indexOf("=") + 1);
                }
            }
        }
        catch (IOException e)
        {
        }
        return "ERROR READING PAGE " + pageNumber;
    }
}
