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
package com.oxnull.seasonality.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap; 
import java.util.Map;

/**
 * ClassTransformer that dynamically generates Serene Seasons API classes
 * without the need to distribute its source code
 */
public class SeasonalityClassTransformer implements IClassTransformer {
	private static final Map<String, byte[]> BYTECODE_CACHE = new HashMap<>();
    private static class SafeClassWriter extends ClassWriter {
        SafeClassWriter(int flags) { super(flags); }
        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            if (type1.equals(type2)) return type1;
            return "java/lang/Object";
        }
    }

    private static final Set<String> COMPAT_CLASSES = new HashSet<>();
    static {
        // Enums
        COMPAT_CLASSES.add("sereneseasons.api.season.Season");
        COMPAT_CLASSES.add("sereneseasons.api.season.Season$SubSeason");
        COMPAT_CLASSES.add("sereneseasons.api.season.Season$TropicalSeason");
        COMPAT_CLASSES.add("sereneseasons.api.config.SeasonsOption");
        
        // Interfaces
        COMPAT_CLASSES.add("sereneseasons.api.season.ISeasonState");
        COMPAT_CLASSES.add("sereneseasons.api.season.ISeasonColorProvider");
        COMPAT_CLASSES.add("sereneseasons.api.config.ISyncedOption");
        
        // Classes
        COMPAT_CLASSES.add("sereneseasons.api.season.SeasonHelper");
        COMPAT_CLASSES.add("sereneseasons.api.config.SyncedConfig");
        COMPAT_CLASSES.add("sereneseasons.season.SeasonASMHelper");
        COMPAT_CLASSES.add("sereneseasons.season.SeasonTime");
        COMPAT_CLASSES.add("sereneseasons.season.SeasonSavedData");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // TODO: Eliminar messajes
        if (transformedName.startsWith("sereneseasons.")) {
            System.out.println("[SEASONALITY-compat] Transformer sees: " + transformedName +
                             " (already loaded: " + (basicClass != null) + ")");
        }
        if (basicClass != null) return basicClass;
        if (COMPAT_CLASSES.contains(transformedName)) {
            System.out.println("[SEASONALITY-compat] Generating: " + transformedName);
            return generateCompatClass(transformedName);
        }
        return basicClass;
    }
    
    private void emitColorMethod(ClassWriter cw, String methodName, String desc) {
        boolean isFloat = desc.endsWith(")F");
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            isFloat ? "colorFloat" : "colorInt",
            isFloat ? "(Ljava/lang/Object;Ljava/lang/String;)F"
                    : "(Ljava/lang/Object;Ljava/lang/String;)I",
            false);
        mv.visitInsn(isFloat ? Opcodes.FRETURN : Opcodes.IRETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }

    private byte[] generateCompatClass(String className) {
		byte[] cached = BYTECODE_CACHE.get(className);
        if (cached != null) return cached;
        ClassWriter cw = new SafeClassWriter(ClassWriter.COMPUTE_FRAMES);
        switch (className) {
            case "sereneseasons.api.season.Season":
                generateSeasonEnum(cw);
                break;
            case "sereneseasons.api.season.Season$SubSeason":
                generateSubSeasonEnum(cw);
                break;
            case "sereneseasons.api.season.Season$TropicalSeason":
                generateTropicalSeasonEnum(cw);
                break;
            case "sereneseasons.api.season.ISeasonState":
                generateISeasonState(cw);
                break;
            case "sereneseasons.api.season.ISeasonColorProvider":
                generateISeasonColorProvider(cw);
                break;
            case "sereneseasons.api.season.SeasonHelper":
                generateSeasonHelper(cw);
                break;
            case "sereneseasons.api.config.SeasonsOption":
                generateSeasonsOptionEnum(cw);
                break;
            case "sereneseasons.api.config.ISyncedOption":
                generateISyncedOption(cw);
                break;
            case "sereneseasons.api.config.SyncedConfig":
                generateSyncedConfig(cw);
                break;
            case "sereneseasons.season.SeasonASMHelper":
                generateSeasonASMHelper(cw);
                break;
            case "sereneseasons.season.SeasonTime":
                generateSeasonTime(cw);
                break;
            case "sereneseasons.season.SeasonSavedData":
                generateSeasonSavedData(cw);
                break;
            default:
                return null;
        }
        
        byte[] out = cw.toByteArray();
        BYTECODE_CACHE.put(className, out);
        return out;
    }

    // --------------
    // Enum: Season
    // --------------
    private void generateSeasonEnum(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
            "sereneseasons/api/season/Season",
            "Ljava/lang/Enum<Lsereneseasons/api/season/Season;>;",
            "java/lang/Enum",
            null
        );

        // fields enum
        String[] values = {"SPRING", "SUMMER", "AUTUMN", "WINTER"};
        for (String v : values) {
            cw.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                v,
                "Lsereneseasons/api/season/Season;",
                null,
                null
            ).visitEnd();
        }

        // VALUES array
        cw.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            "VALUES",
            "[Lsereneseasons/api/season/Season;",
            null,
            null
        ).visitEnd();

        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE,
            "<init>",
            "(Ljava/lang/String;I)V",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        // Static initializer
        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        for (int i = 0; i < values.length; i++) {
            mv.visitTypeInsn(Opcodes.NEW, "sereneseasons/api/season/Season");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(values[i]);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "sereneseasons/api/season/Season", "<init>", "(Ljava/lang/String;I)V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/season/Season", values[i], "Lsereneseasons/api/season/Season;");
        }

        // VALUES array
        mv.visitIntInsn(Opcodes.BIPUSH, values.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "sereneseasons/api/season/Season");
        for (int i = 0; i < values.length; i++) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "sereneseasons/api/season/Season", values[i], "Lsereneseasons/api/season/Season;");
            mv.visitInsn(Opcodes.AASTORE);
        }
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/season/Season", "VALUES", "[Lsereneseasons/api/season/Season;");
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(4, 0);
        mv.visitEnd();

        // values() method
        mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "values",
            "()[Lsereneseasons/api/season/Season;",
            null,
            null
        );
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "sereneseasons/api/season/Season", "VALUES", "[Lsereneseasons/api/season/Season;");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();

        cw.visitEnd();
    }

    // --------------
    // Enum: Season$SubSeason
    // --------------
    private void generateSubSeasonEnum(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
            "sereneseasons/api/season/Season$SubSeason",
            "Ljava/lang/Enum<Lsereneseasons/api/season/Season$SubSeason;>;Lsereneseasons/api/season/ISeasonColorProvider;",
            "java/lang/Enum",
            new String[]{"sereneseasons/api/season/ISeasonColorProvider"}
        );

        String[] values = {
            "EARLY_SPRING", "MID_SPRING", "LATE_SPRING",
            "EARLY_SUMMER", "MID_SUMMER", "LATE_SUMMER",
            "EARLY_AUTUMN", "MID_AUTUMN", "LATE_AUTUMN",
            "EARLY_WINTER", "MID_WINTER", "LATE_WINTER"
        };

        for (String v : values) {
            cw.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                v,
                "Lsereneseasons/api/season/Season$SubSeason;",
                null,
                null
            ).visitEnd();
        }

        cw.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            "VALUES",
            "[Lsereneseasons/api/season/Season$SubSeason;",
            null,
            null
        ).visitEnd();

        // Constructor
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE,
            "<init>",
            "(Ljava/lang/String;I)V",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        // Static init
        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        for (int i = 0; i < values.length; i++) {
            mv.visitTypeInsn(Opcodes.NEW, "sereneseasons/api/season/Season$SubSeason");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(values[i]);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "sereneseasons/api/season/Season$SubSeason", "<init>", "(Ljava/lang/String;I)V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/season/Season$SubSeason", values[i], "Lsereneseasons/api/season/Season$SubSeason;");
        }

        mv.visitIntInsn(Opcodes.BIPUSH, values.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "sereneseasons/api/season/Season$SubSeason");
        for (int i = 0; i < values.length; i++) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "sereneseasons/api/season/Season$SubSeason", values[i], "Lsereneseasons/api/season/Season$SubSeason;");
            mv.visitInsn(Opcodes.AASTORE);
        }
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/season/Season$SubSeason", "VALUES", "[Lsereneseasons/api/season/Season$SubSeason;");
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(4, 0);
        mv.visitEnd();

        // ISeasonColorProvider methods (delegan al adaptador)
        String[] colorMethods = {
            "getGrassOverlay", "()I",
            "getGrassSaturationMultiplier", "()F",
            "getFoliageOverlay", "()I",
            "getFoliageSaturationMultiplier", "()F",
            "getBirchColor", "()I"
        };

        for (int i = 0; i < colorMethods.length; i += 2) {
            emitColorMethod(cw, colorMethods[i], colorMethods[i + 1]);
        }

        // getSeason() method
        mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC,
            "getSeason",
            "()Lsereneseasons/api/season/Season;",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            "getSeasonFromSubSeason",
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            false
        );
        mv.visitTypeInsn(Opcodes.CHECKCAST, "sereneseasons/api/season/Season");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        cw.visitEnd();
    }

    // --------------
    // Enum: Season$TropicalSeason
    // --------------
    private void generateTropicalSeasonEnum(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
            "sereneseasons/api/season/Season$TropicalSeason",
            "Ljava/lang/Enum<Lsereneseasons/api/season/Season$TropicalSeason;>;Lsereneseasons/api/season/ISeasonColorProvider;",
            "java/lang/Enum",
            new String[]{"sereneseasons/api/season/ISeasonColorProvider"}
        );

        String[] values = {
            "EARLY_DRY", "MID_DRY", "LATE_DRY",
            "EARLY_WET", "MID_WET", "LATE_WET"
        };

        for (String v : values) {
            cw.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                v,
                "Lsereneseasons/api/season/Season$TropicalSeason;",
                null,
                null
            ).visitEnd();
        }

        cw.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            "VALUES",
            "[Lsereneseasons/api/season/Season$TropicalSeason;",
            null,
            null
        ).visitEnd();

        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE,
            "<init>",
            "(Ljava/lang/String;I)V",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        // Static init
        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        for (int i = 0; i < values.length; i++) {
            mv.visitTypeInsn(Opcodes.NEW, "sereneseasons/api/season/Season$TropicalSeason");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(values[i]);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "sereneseasons/api/season/Season$TropicalSeason", "<init>", "(Ljava/lang/String;I)V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/season/Season$TropicalSeason", values[i], "Lsereneseasons/api/season/Season$TropicalSeason;");
        }

        mv.visitIntInsn(Opcodes.BIPUSH, values.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "sereneseasons/api/season/Season$TropicalSeason");
        for (int i = 0; i < values.length; i++) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitFieldInsn(Opcodes.GETSTATIC, "sereneseasons/api/season/Season$TropicalSeason", values[i], "Lsereneseasons/api/season/Season$TropicalSeason;");
            mv.visitInsn(Opcodes.AASTORE);
        }
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/season/Season$TropicalSeason", "VALUES", "[Lsereneseasons/api/season/Season$TropicalSeason;");
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(4, 0);
        mv.visitEnd();

        // ISeasonColorProvider methods
        String[] colorMethods = {
            "getGrassOverlay", "()I",
            "getGrassSaturationMultiplier", "()F",
            "getFoliageOverlay", "()I",
            "getFoliageSaturationMultiplier", "()F",
            "getBirchColor", "()I"
        };

        for (int i = 0; i < colorMethods.length; i += 2) {
            mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                colorMethods[i],
                colorMethods[i + 1],
                null,
                null
            );
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
                "delegateColorMethod",
                "(Ljava/lang/Object;Ljava/lang/String;)" + colorMethods[i + 1].replace("()", ""),
                false
            );
            mv.visitInsn(getReturnOpcode(colorMethods[i + 1]));
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }

        cw.visitEnd();
    }

    // --------------
    // Interface: ISeasonState
    // --------------
    private void generateISeasonState(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
            "sereneseasons/api/season/ISeasonState",
            null,
            "java/lang/Object",
            null
        );

        String[][] methods = {
            {"getDayDuration", "()I"},
            {"getSubSeasonDuration", "()I"},
            {"getSeasonDuration", "()I"},
            {"getCycleDuration", "()I"},
            {"getSeasonCycleTicks", "()I"},
            {"getDay", "()I"},
            {"getSubSeason", "()Lsereneseasons/api/season/Season$SubSeason;"},
            {"getSeason", "()Lsereneseasons/api/season/Season;"},
            {"getTropicalSeason", "()Lsereneseasons/api/season/Season$TropicalSeason;"}
        };

        for (String[] method : methods) {
            cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                method[0],
                method[1],
                null,
                null
            ).visitEnd();
        }

        cw.visitEnd();
    }

    // --------------
    // Interface: ISeasonColorProvider
    // --------------
    private void generateISeasonColorProvider(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
            "sereneseasons/api/season/ISeasonColorProvider",
            null,
            "java/lang/Object",
            null
        );

        String[][] methods = {
            {"getGrassOverlay", "()I"},
            {"getGrassSaturationMultiplier", "()F"},
            {"getFoliageOverlay", "()I"},
            {"getFoliageSaturationMultiplier", "()F"},
            {"getBirchColor", "()I"}
        };

        for (String[] method : methods) {
            cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                method[0],
                method[1],
                null,
                null
            ).visitEnd();
        }

        cw.visitEnd();
    }

    // --------------
    // Class: SeasonHelper
    // --------------
    private void generateSeasonHelper(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            "sereneseasons/api/season/SeasonHelper",
            null,
            "java/lang/Object",
            null
        );

        // dataProvider field
        cw.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "dataProvider",
            "Lsereneseasons/api/season/SeasonHelper$ISeasonDataProvider;",
            null,
            null
        ).visitEnd();

        // getSeasonState(World) method
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getSeasonState",
            "(Lnet/minecraft/world/World;)Lsereneseasons/api/season/ISeasonState;",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            "getSeasonState",
            "(Lnet/minecraft/world/World;)Ljava/lang/Object;",
            false
        );
        mv.visitTypeInsn(Opcodes.CHECKCAST, "sereneseasons/api/season/ISeasonState");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();
    }

    // --------------
    // Enum: SeasonsOption
    // --------------
    private void generateSeasonsOptionEnum(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
            "sereneseasons/api/config/SeasonsOption",
            "Ljava/lang/Enum<Lsereneseasons/api/config/SeasonsOption;>;Lsereneseasons/api/config/ISyncedOption;",
            "java/lang/Enum",
            new String[]{"sereneseasons/api/config/ISyncedOption"}
        );

        String[] values = {
            "DAY_DURATION", "SUB_SEASON_DURATION", "STARTING_SUB_SEASON",
            "PROGRESS_SEASON_WHILE_OFFLINE", "ADVANCE_SEASON_WHILE_SLEEPING",
            "CHANGE_DAYLIGHT_WITH_SEASONS"
        };

        for (String v : values) {
            cw.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                v,
                "Lsereneseasons/api/config/SeasonsOption;",
                null,
                null
            ).visitEnd();
        }

        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PRIVATE,
            "<init>",
            "(Ljava/lang/String;ILjava/lang/String;)V",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 4);
        mv.visitEnd();

        // Static init
        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        
        for (int i = 0; i < values.length; i++) {
            mv.visitTypeInsn(Opcodes.NEW, "sereneseasons/api/config/SeasonsOption");
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(values[i]);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            mv.visitLdcInsn(values[i]); // optionName
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "sereneseasons/api/config/SeasonsOption", "<init>", "(Ljava/lang/String;ILjava/lang/String;)V", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/api/config/SeasonsOption", values[i], "Lsereneseasons/api/config/SeasonsOption;");
        }
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(5, 0);
        mv.visitEnd();

        // getOptionName() method
        mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC,
            "getOptionName",
            "()Ljava/lang/String;",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Enum",
            "name",
            "()Ljava/lang/String;",
            false
        );
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();
    }

    // --------------
    // Interface: ISyncedOption
    // --------------
    private void generateISyncedOption(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
            "sereneseasons/api/config/ISyncedOption",
            null,
            "java/lang/Object",
            null
        );

        cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
            "getOptionName",
            "()Ljava/lang/String;",
            null,
            null
        ).visitEnd();

        cw.visitEnd();
    }

    // --------------
    // Class: SyncedConfig
    // --------------
    private void generateSyncedConfig(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            "sereneseasons/api/config/SyncedConfig",
            null,
            "java/lang/Object",
            null
        );

        // getBooleanValue(ISyncedOption)
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getBooleanValue",
            "(Lsereneseasons/api/config/ISyncedOption;)Z",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            "getBooleanValue",
            "(Lsereneseasons/api/config/ISyncedOption;)Z",
            false
        );
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // getIntValue(ISyncedOption)
        mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getIntValue",
            "(Lsereneseasons/api/config/ISyncedOption;)I",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            "getIntValue",
            "(Lsereneseasons/api/config/ISyncedOption;)I",
            false
        );
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();
    }

    private int getReturnOpcode(String desc) {
        if (desc.endsWith(")I")) return Opcodes.IRETURN;
        if (desc.endsWith(")F")) return Opcodes.FRETURN;
        if (desc.endsWith(")Z")) return Opcodes.IRETURN;
        return Opcodes.ARETURN;
    }
    
    // --------------
    // Class: SeasonASMHelper
    // --------------
    private void generateSeasonASMHelper(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            "sereneseasons/season/SeasonASMHelper",
            null,
            "java/lang/Object",
            null
        );

        // getFloatTemperature(World, Biome, BlockPos) -> float
        MethodVisitor mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getFloatTemperature",
            "(Lnet/minecraft/world/World;Lnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/BlockPos;)F",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); // world
        mv.visitVarInsn(Opcodes.ALOAD, 1); // biome
        mv.visitVarInsn(Opcodes.ALOAD, 2); // pos
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            "getFloatTemperature",
            "(Lnet/minecraft/world/World;Lnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/BlockPos;)F",
            false
        );
        mv.visitInsn(Opcodes.FRETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        // getFloatTemperature(SubSeason, Biome, BlockPos) -> float
        mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "getFloatTemperature",
            "(Lsereneseasons/api/season/Season$SubSeason;Lnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/BlockPos;)F",
            null,
            null
        );
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); // subSeason
        mv.visitVarInsn(Opcodes.ALOAD, 1); // biome
        mv.visitVarInsn(Opcodes.ALOAD, 2); // pos
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/oxnull/seasonality/asm/SeasonalityCompatAdapter",
            "getFloatTemperatureFromSubSeason",
            "(Ljava/lang/Object;Lnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/BlockPos;)F",
            false
        );
        mv.visitInsn(Opcodes.FRETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
        cw.visitEnd();
    }
    
    // --------------
    // Class: SeasonTime
    // --------------
    private void generateSeasonTime(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            "sereneseasons/season/SeasonTime",
            null,
            "java/lang/Object",
            new String[]{"sereneseasons/api/season/ISeasonState"}
        );

        /* public constructor */
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(I)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        // public static field ZERO
        cw.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
            "ZERO",
            "Lsereneseasons/season/SeasonTime;",
            null,
            null
        ).visitEnd();

        // Static initializer for ZERO
        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, "sereneseasons/season/SeasonTime");
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "sereneseasons/season/SeasonTime", "<init>", "(I)V", false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "sereneseasons/season/SeasonTime", "ZERO", "Lsereneseasons/season/SeasonTime;");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();

        // ISeasonState stub methods
        String[] intMethods = {
            "getDayDuration",
            "getSubSeasonDuration",
            "getSeasonDuration",
            "getCycleDuration",
            "getSeasonCycleTicks",
            "getDay"
        };

        for (String methodName : intMethods) {
            MethodVisitor stubMv = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                methodName,
                "()I",
                null,
                null
            );
            stubMv.visitCode();
            stubMv.visitInsn(Opcodes.ICONST_0);
            stubMv.visitInsn(Opcodes.IRETURN);
            stubMv.visitMaxs(1, 1);
            stubMv.visitEnd();
        }

        // getSubSeason() -> null
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getSubSeason", "()Lsereneseasons/api/season/Season$SubSeason;", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // getSeason() -> null
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getSeason", "()Lsereneseasons/api/season/Season;", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // getTropicalSeason() -> null
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getTropicalSeason", "()Lsereneseasons/api/season/Season$TropicalSeason;", null, null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();
    }

    // --------------
    // Class: SeasonSavedData
    // --------------
    private void generateSeasonSavedData(ClassWriter cw) {
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            "sereneseasons/season/SeasonSavedData",
            null,
            "net/minecraft/world/storage/WorldSavedData",
            null
        );

        // public field seasonCycleTicks
        cw.visitField(
            Opcodes.ACC_PUBLIC,
            "seasonCycleTicks",
            "I",
            null,
            null
        ).visitEnd();

        // Builder
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/world/storage/WorldSavedData", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        cw.visitEnd();
    }
}