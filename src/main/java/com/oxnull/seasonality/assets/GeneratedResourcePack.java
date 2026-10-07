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
 * Note: Based on OpenEngineering by strubium (CleanroomMC), modified and adapted for Seasonality
 * Distributed as part of Seasonality under the GNU General Public License v3.0
 */

package com.oxnull.seasonality.assets;

import com.google.common.collect.ImmutableSet;
import com.oxnull.seasonality.core.Seasonality;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Set;

/**
 * Custom ResourcePack implementation that reads assets downloaded to the config folder
 */
public class GeneratedResourcePack implements IResourcePack {
    private final File baseDir;

    public GeneratedResourcePack(File baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        File file = new File(baseDir, location.getNamespace() + "/" + location.getPath());
        if (file.isFile()) {
            return Files.newInputStream(file.toPath());
        }
        throw new FileNotFoundException(file.toString());
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        File file = new File(baseDir, location.getNamespace() + "/" + location.getPath());
        return file.isFile();
    }

    @Override
    public Set<String> getResourceDomains() {
        return ImmutableSet.of(Seasonality.MOD_ID);
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String sectionName) {
        return null;
    }

    @Override
    public BufferedImage getPackImage() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        return img;
    }

    @Override
    public String getPackName() {
        return Seasonality.MOD_NAME + ": RuntimeAssets";
    }
}
