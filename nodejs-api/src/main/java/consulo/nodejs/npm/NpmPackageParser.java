/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.nodejs.npm;

import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a {@code package.json} into a {@link NpmPackage}, without PSI: it works for the modules without the Node.js
 * extension too (an import, a Maven module) and outside of a read action.
 * <p>
 * A small JSON reader of its own: the users of the API (plugins, their tests) need no JSON library on the classpath.
 *
 * @since 2026-10-31
 */
public final class NpmPackageParser {
    private NpmPackageParser() {
    }

    /**
     * @return {@code null} if the file can't be read or is not a JSON object
     */
    public static @Nullable NpmPackage parse(@Nullable VirtualFile packageJson) {
        if (packageJson == null || !packageJson.isValid() || packageJson.isDirectory()) {
            return null;
        }
        try {
            return parse(new String(packageJson.contentsToByteArray(), StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            return null;
        }
    }

    public static @Nullable NpmPackage parse(Path packageJson) {
        try {
            return Files.isRegularFile(packageJson) ? parse(Files.readString(packageJson, StandardCharsets.UTF_8)) : null;
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * @return the JSON value of the text: {@link Map}, {@link List}, {@link String}, {@link Double}, {@link Boolean} or
     * {@code null} - also for a syntax error
     */
    public static @Nullable Object readJson(String text) {
        try {
            return new Reader(text).readDocument();
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static @Nullable NpmPackage parse(String text) {
        Object root = readJson(text);
        if (!(root instanceof Map<?, ?> object)) {
            return null;
        }
        return new NpmPackage(
            string(object.get("name")),
            string(object.get("version")),
            strings(object.get("dependencies")),
            strings(object.get("devDependencies")),
            strings(object.get("peerDependencies")),
            strings(object.get("optionalDependencies")),
            strings(object.get("scripts")),
            workspaces(object.get("workspaces"))
        );
    }

    /**
     * {@code "workspaces": ["packages/*"]} or {@code "workspaces": {"packages": ["packages/*"]}}
     */
    public static List<String> workspaces(@Nullable Object value) {
        Object patterns = value instanceof Map<?, ?> object ? object.get("packages") : value;
        if (!(patterns instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object pattern : list) {
            if (pattern instanceof String string && !string.isBlank()) {
                result.add(string.trim());
            }
        }
        return result;
    }

    private static @Nullable String string(@Nullable Object value) {
        return value instanceof String string ? string : null;
    }

    private static Map<String, String> strings(@Nullable Object value) {
        if (!(value instanceof Map<?, ?> object)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : object.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * JSON (RFC 8259) to {@link Map}, {@link List}, {@link String}, {@link Double}, {@link Boolean} and {@code null};
     * an {@link IllegalArgumentException} on a syntax error.
     */
    private static final class Reader {
        private final String myText;
        private int myOffset;

        private Reader(String text) {
            myText = text;
        }

        private @Nullable Object readDocument() {
            // a byte order mark may start the file
            if (myText.startsWith("﻿")) {
                myOffset = 1;
            }
            Object value = readValue();
            skipWhitespace();
            if (myOffset != myText.length()) {
                throw error();
            }
            return value;
        }

        private @Nullable Object readValue() {
            skipWhitespace();
            if (myOffset >= myText.length()) {
                throw error();
            }
            char c = myText.charAt(myOffset);
            switch (c) {
                case '{':
                    return readObject();
                case '[':
                    return readArray();
                case '"':
                    return readString();
                case 't':
                    expect("true");
                    return Boolean.TRUE;
                case 'f':
                    expect("false");
                    return Boolean.FALSE;
                case 'n':
                    expect("null");
                    return null;
                default:
                    return readNumber();
            }
        }

        private Map<String, @Nullable Object> readObject() {
            Map<String, @Nullable Object> result = new LinkedHashMap<>();
            myOffset++;
            skipWhitespace();
            if (peek() == '}') {
                myOffset++;
                return result;
            }
            while (true) {
                skipWhitespace();
                if (peek() != '"') {
                    throw error();
                }
                String key = readString();
                skipWhitespace();
                if (peek() != ':') {
                    throw error();
                }
                myOffset++;
                result.put(key, readValue());
                skipWhitespace();
                char c = peek();
                myOffset++;
                if (c == '}') {
                    return result;
                }
                if (c != ',') {
                    throw error();
                }
            }
        }

        private List<@Nullable Object> readArray() {
            List<@Nullable Object> result = new ArrayList<>();
            myOffset++;
            skipWhitespace();
            if (peek() == ']') {
                myOffset++;
                return result;
            }
            while (true) {
                result.add(readValue());
                skipWhitespace();
                char c = peek();
                myOffset++;
                if (c == ']') {
                    return result;
                }
                if (c != ',') {
                    throw error();
                }
            }
        }

        private String readString() {
            StringBuilder builder = new StringBuilder();
            myOffset++;
            while (true) {
                char c = peek();
                myOffset++;
                if (c == '"') {
                    return builder.toString();
                }
                if (c != '\\') {
                    builder.append(c);
                    continue;
                }
                char escaped = peek();
                myOffset++;
                switch (escaped) {
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> {
                        if (myOffset + 4 > myText.length()) {
                            throw error();
                        }
                        try {
                            builder.append((char) Integer.parseInt(myText.substring(myOffset, myOffset + 4), 16));
                        }
                        catch (NumberFormatException e) {
                            throw error();
                        }
                        myOffset += 4;
                    }
                    default -> builder.append(escaped);
                }
            }
        }

        private Double readNumber() {
            int start = myOffset;
            while (myOffset < myText.length() && "+-0123456789.eE".indexOf(myText.charAt(myOffset)) >= 0) {
                myOffset++;
            }
            if (start == myOffset) {
                throw error();
            }
            try {
                return Double.valueOf(myText.substring(start, myOffset));
            }
            catch (NumberFormatException e) {
                throw error();
            }
        }

        private void expect(String literal) {
            if (!myText.startsWith(literal, myOffset)) {
                throw error();
            }
            myOffset += literal.length();
        }

        private char peek() {
            if (myOffset >= myText.length()) {
                throw error();
            }
            return myText.charAt(myOffset);
        }

        private void skipWhitespace() {
            while (myOffset < myText.length() && Character.isWhitespace(myText.charAt(myOffset))) {
                myOffset++;
            }
        }

        private IllegalArgumentException error() {
            return new IllegalArgumentException("Invalid JSON at " + myOffset);
        }
    }
}
