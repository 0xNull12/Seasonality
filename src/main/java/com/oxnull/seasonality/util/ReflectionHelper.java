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
package com.oxnull.seasonality.util;

import java.lang.reflect.Method;

public final class ReflectionHelper {
    private ReflectionHelper() {}

    public static Method getMethod(String className, String methodName, Class<?>... params) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, params);
            method.setAccessible(true);
            return method;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + className, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method not found: " + methodName + " in class " + className, e);
        } catch (Exception e) {
            throw new RuntimeException("Error accessing method: " + methodName + " in class " + className, e);
        }
    }
}
