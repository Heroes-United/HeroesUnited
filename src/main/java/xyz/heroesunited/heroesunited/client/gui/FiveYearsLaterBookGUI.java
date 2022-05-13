package xyz.heroesunited.heroesunited.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import xyz.heroesunited.heroesunited.HeroesUnited;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class FiveYearsLaterBookGUI extends Screen {

    private final String[] modPages = new String[]{"cover", "p1", "p2", "p3", "p4", "p5", "p6", "p7"};
    private final String[] pages = new String[]{"fa9fdd40b15b4723880d6b75c6e519e5~mv2_d_2550_3300_s_4_2.png", "ed9e4398b40a438aa8a604726ac87cd9~mv2_d_2544_3272_s_4_2.png",
            "65af417085ec4fbc8361cdd2f88318e3~mv2_d_2544_3280_s_4_2.png", "64666dbbf09a49a3b542e7338ac5c92d~mv2_d_2536_3288_s_4_2.png",
            "3062f60bc4384f40aca9bb382f74f3bd~mv2_d_2528_3248_s_4_2.png", "bfab32cb0af2420585668b9986e472e4~mv2_d_2528_3288_s_4_2.png",
            "b41936cea1554e3faad4c004c8428cf7~mv2_d_2536_3288_s_4_2.png", "acf30ce177db4bcfb908aabb39428de3~mv2_d_2550_3300_s_4_2.png"};

    private int xSize, ySize;
    private int pageNum = 0;
    private double value = 0.0D;

    public FiveYearsLaterBookGUI() {
        super(new TranslatableComponent("screen.heroesunited.fiveyearslater"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        this.xSize = (width - 200) / 2;
        this.ySize = (height - 260) / 2;
        this.addRenderableWidget(new Button(xSize - 20, height / 2, 20, 20, new TranslatableComponent("<"), b -> backPage()));
        this.addRenderableWidget(new Button(xSize + 200, height / 2, 20, 20, new TranslatableComponent(">"), b -> nextPage()));
        //this.addRenderableWidget(new Button(xSize + 70, height / 2 + 130, 60, 20, new TranslationTextComponent(isHighRes ? "Low res" : "High res"), b -> isHighRes = !isHighRes));
        if (pageNum == pages.length - 1) {
            this.addRenderableWidget(new Button(xSize + 25, ySize + (260 / 2) + 50, 150, 20, new TranslatableComponent("Check Out The 5YL Comic!"),
                    b -> minecraft.setScreen(new ConfirmLinkScreen(this::confirmCallback, "https://www.theinktank.co/5yearslater", true))));
        }
        ForgeSlider slider = new ForgeSlider(xSize + 50, height / 2 + 130, 100, 20, TextComponent.EMPTY, TextComponent.EMPTY, 0, 1275, 0, false) {
            @Override
            protected void applyValue() {
                super.applyValue();
                FiveYearsLaterBookGUI.this.value = this.value;
            }
        };
        this.addRenderableWidget(slider);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        SnowWidget.drawSnowOnScreen(matrixStack, this.width, this.height);
        this.renderBackground(matrixStack);
        String page, modPage;
        try {
            page = pages[pageNum];
            modPage = modPages[pageNum];
        } catch (ArrayIndexOutOfBoundsException e) {
            page = pages[0];
            modPage = modPages[0];
        }
        matrixStack.pushPose();
        RenderSystem.setShaderTexture(0, getTexture(page, modPage));
        blit(matrixStack, xSize, ySize, 0, 0, 200, 260, 200, 260);
        matrixStack.popPose();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private ResourceLocation getTexture(String fileName, String modPage) {
        String url = "https://static.wixstatic.com/media/b7520e_" + fileName + "/v1/fill/w_200,h_260,al_c,q_85,usm_0.66_1.00_0.01/page.png";
        double value = this.value == 0.0D ? 10D : this.value;
        url = url.replace("200,h_260", String.format("%s,h_%s", (int) value * 2, (int) (value * 2.6)));

        String s = String.valueOf(url.hashCode());
        ResourceLocation resourcelocation = new ResourceLocation("5yl_comic/" + s);
        AbstractTexture texture = minecraft.textureManager.getTexture(resourcelocation);
        if (texture == null) {
            File file1 = new File(new File(minecraft.gameDirectory.toPath().resolve("assets").toFile(), "5yl_comic"), s.length() > 2 ? s.substring(0, 2) : "xx");
            File file2 = new File(file1, s);
            HttpTexture downloadingtexture = new HttpTexture(file2, url, new ResourceLocation(HeroesUnited.MODID, String.format("textures/gui/comic/5yl%s.png", modPage)), false, () -> {
            });
            minecraft.textureManager.register(resourcelocation, downloadingtexture);
        }
        return resourcelocation;
    }

    private void confirmCallback(boolean will) {
        this.getMinecraft().setScreen(null);
        if (will) {
            try {
                Util.getPlatform().openUri(new URI("https://www.theinktank.co/5yearslater"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private void nextPage() {
        pageNum++;
        if (pageNum > pages.length - 1) {
            pageNum = 0;
        }
    }

    private void backPage() {
        pageNum -= 1;
        if (pageNum < 0) {
            pageNum = pages.length - 1;
        }
    }
}
