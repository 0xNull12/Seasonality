/*
 * Copyright (C) 2026 0xNull
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://gnu.org>.
 */
package com.oxnull.seasonality.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oxnull.seasonality.core.Seasonality;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A utility for reading and writing JSON configuration files using Java's native NIO
 */
public final class JsonUtil {
    public static final Gson SERIALIZER = new GsonBuilder().setPrettyPrinting().create();

    private JsonUtil() {}

    public static <T> T getOrCreateConfigFile(File configDir, String configName, T defaults, Type type) {
        Path path = new File(configDir, configName).toPath();
        if (!Files.exists(path)) {
            writeFile(path, defaults);
        }

        try {
            String jsonContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return SERIALIZER.fromJson(jsonContent, type);
        } catch (Exception e) {
            Seasonality.LOGGER.error("Error parsing config from json: {}", path.toString(), e);
        }
        return null;
    }

    protected static boolean writeFile(Path outputPath, Object obj) {
        try {
            String jsonContent = SERIALIZER.toJson(obj);
            Files.write(outputPath, jsonContent.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            Seasonality.LOGGER.error("Error writing config file {}: {}", outputPath.toAbsolutePath(), e.getMessage());
            return false;
        }
    }
}