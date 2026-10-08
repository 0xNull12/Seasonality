/*
 * Copyright (c) 2025 strubium
 * Copyright (C) 2026 0xNull
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Note: Based on OpenEngineering by strubium, modified and adapted for Seasonality
 * Distributed as part of Seasonality under the GNU General Public License v3.0
 */

package com.oxnull.seasonality.assets;

import com.google.gson.*;
import com.oxnull.seasonality.core.Seasonality;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * Manages the downloading and injection of third-party assets (Serene Seasons)
 * that cannot be legally distributed inside the mod JAR due to CC BY-NC-ND 4.0 licensing
 */
public class RuntimeAssets {

    private static final String ASSETS_DIR = "config/seasonality/assets/";
    private static GeneratedResourcePack runtimePack;

    // Load assets.json from the JAR
    private static JsonObject loadAssetsJson() throws IOException {
        try (InputStream in = RuntimeAssets.class.getResourceAsStream("/assets/" + Seasonality.MOD_ID + "/assets.json")) {
            if (in == null) throw new FileNotFoundException("assets.json was not found in the .jar!");
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return new JsonParser().parse(reader).getAsJsonObject();
            }
        }
    }

    // Checks for missing textures
    public static void checkAssets() {
        try {
            JsonObject assetsJson = loadAssetsJson();
            JsonArray textures = assetsJson.getAsJsonArray("textures");

            StringBuilder missingFiles = new StringBuilder();
            boolean missing = false;
            Set<String> names = new HashSet<>();

            for (JsonElement elem : textures) {
                JsonObject tex = elem.getAsJsonObject();
                String name = tex.get("name").getAsString();
                String type = tex.get("type").getAsString();

                if (!names.add(name)) {
                    throw new RuntimeException("Duplicate texture name found: " + name);
                }

                if (tex.has("split")) continue; // Split textures handled differently

                String path = "textures/" + type + "s/" + name + ".png";
                File file = new File(ASSETS_DIR + Seasonality.MOD_ID + "/" + path);
                
                if (!file.exists()) {
                    missing = true;
                    missingFiles.append(path).append("\n");
                }
            }

            if (missing) {
                showMissingAssetsDialog(missingFiles.toString(), textures);
            }

        } catch (IOException e) {
            Seasonality.LOGGER.error("Failed to check runtime assets!", e);
            showMissingAssetsDialog("Could not read assets.json!", null);
        }
    }

    private static void showMissingAssetsDialog(String message, JsonArray textures) {
        EventQueue.invokeLater(() -> {
            String title = "Missing Seasonality Assets";
            String[] options = {"Download Missing Assets", "Ignore"};

            JTextArea textArea = new JTextArea(
                    "Some texture assets required by Seasonality are missing:\n\n"
                            + message
                            + "\nThese assets are copyrighted by Glitchfiend / Biomes O' Plenty Team "
                            + "and cannot be distributed directly by this mod due to CC BY-NC-ND 4.0 licensing.\n"
                            + "You can choose to download them directly from the official sources "
                            + "by clicking 'Download', or continue without them (visual issues may occur).\n"
                            + "You may need to reload resources (F3+T) or restart the game after downloading."
            );
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            int response = JOptionPane.showOptionDialog(
                    getPopupFrame(),
                    scrollPane,
                    title,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (response == 0 && textures != null) {
                downloadMissingTextures(textures);
            }
        });
    }

    private static void downloadMissingTextures(JsonArray textures) {
        for (JsonElement elem : textures) {
            JsonObject tex = elem.getAsJsonObject();
            String name = tex.get("name").getAsString();
            String url = tex.get("url").getAsString();
            String type = tex.get("type").getAsString();
            String split = tex.has("split") ? tex.get("split").getAsString() : null;

            String basePath = "textures/" + type + "s/";

            if (split == null) {
                File file = new File(ASSETS_DIR + Seasonality.MOD_ID + "/" + basePath + name + ".png");
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    try (InputStream in = new URL(url).openStream();
                         FileOutputStream out = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        Seasonality.LOGGER.info("Downloaded asset: {}", name);
                    } catch (IOException ex) {
                        Seasonality.LOGGER.error("Failed to download {}", name, ex);
                    }
                }
                continue;
            }

            // Handle split textures (half/quarter)
            try (InputStream in = new URL(url).openStream()) {
                BufferedImage image = ImageIO.read(in);
                int width = image.getWidth();
                int height = image.getHeight();

                if ("half".equalsIgnoreCase(split)) {
                    int halfWidth = width / 2;
                    saveTexture(image.getSubimage(0, 0, halfWidth, height), basePath + name + "_0.png");
                    saveTexture(image.getSubimage(halfWidth, 0, halfWidth, height), basePath + name + "_1.png");
                } else if ("quarter".equalsIgnoreCase(split)) {
                    int halfWidth = width / 2;
                    int halfHeight = height / 2;
                    int index = 0;
                    for (int y = 0; y < 2; y++) {
                        for (int x = 0; x < 2; x++) {
                            saveTexture(image.getSubimage(x * halfWidth, y * halfHeight, halfWidth, halfHeight), 
                                       basePath + name + "_" + index++ + ".png");
                        }
                    }
                }
            } catch (IOException ex) {
                Seasonality.LOGGER.error("Failed to process split asset {}", name, ex);
            }
        }
    }

    private static void saveTexture(BufferedImage image, String relativePath) throws IOException {
        File file = new File(ASSETS_DIR + Seasonality.MOD_ID + "/" + relativePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            ImageIO.write(image, "png", file);
        }
    }

    private static JFrame getPopupFrame() {
        JFrame parent = new JFrame();
        parent.setAlwaysOnTop(true);
        return parent;
    }

    // Inject the generated resource pack into Minecraft
    public static void registerGeneratedResourcePack() {
        if (runtimePack == null) {
            runtimePack = new GeneratedResourcePack(new File(ASSETS_DIR));
        }
        Minecraft mc = Minecraft.getMinecraft();
        addToDefaultResourcePacks(mc, runtimePack);
        ((SimpleReloadableResourceManager) mc.getResourceManager()).reloadResourcePack(runtimePack);
        Seasonality.LOGGER.info("Injected runtime resource pack from {}", new File(ASSETS_DIR).getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    private static void addToDefaultResourcePacks(Minecraft mc, IResourcePack pack) {
        for (Field field : Minecraft.class.getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType())) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(mc);
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof IResourcePack) {
                        if (!list.contains(pack)) {
                            ((List<IResourcePack>) list).add(pack);
                        }
                        return;
                    }
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        Seasonality.LOGGER.warn("Could not locate defaultResourcePacks");
    }
}
