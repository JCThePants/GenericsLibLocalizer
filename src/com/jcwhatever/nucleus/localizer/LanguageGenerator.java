/* This file is part of NucleusLocalizer, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jcwhatever.nucleus.localizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates language key file from jar file.
 */
public class LanguageGenerator {

    private static Pattern NEW_LINE = Pattern.compile("\n");

    private final File _jarFile;
    private final File _outputFile;
    private final String _version;

    /**
     * Constructor.
     *
     * @param jarFile     The jar file to parse for Localizable fields and annotations.
     * @param outputFile  The output key file.
     * @param version     The output file version.
     */
    public LanguageGenerator(File jarFile, File outputFile, String version) {

        _jarFile = jarFile;
        _outputFile = outputFile;
        _version = version;
    }

    /**
     * Generate key file.
     */
    public void generate() throws IOException {

        // check if file exists
        if (_outputFile.exists()) {

            Console console = System.console();
            if (console != null) {

                // find out if user wants to overwrite existing file.
                while (true) {
                    System.out.print("Language key file already exists. Overwrite? (Y or N)");

                    String input = console.readLine();

                    if (input.equalsIgnoreCase("Y")) {
                        break;
                    }
                    else if (input.equalsIgnoreCase("N")) {
                        return;
                    }
                }
            }
        }

        System.out.println("Generating...");


        List<LiteralInfo> literals = getStringLiterals(_jarFile);
        Map<LiteralInfo, LiteralInfo> added = new HashMap<>(literals.size());

        if (literals.size() == 0) {
            System.out.println("No localizable string literals found. exiting.");
            return;
        }

        System.out.println(literals.size() + " literals found.");
        System.out.println("Opening file: " + _outputFile.getName());

        PrintWriter writer = new PrintWriter(_outputFile, "UTF-16");

        writer.write("version> ");
        writer.write(_version);
        writer.write("\n\n");

        for (int i=0, j=0; i < literals.size(); i++) {

            LiteralInfo info = literals.get(i);

            if (added.containsKey(info)) {

                LiteralInfo current = added.get(info);

                System.out.println("[DUPLICATE DETECTED] [SKIPPED]");
                System.out.println("    added: " + current.getComment());
                System.out.println("    duplicate: " + info.getComment());
                continue;
            }

            added.put(info, info);

            // write comment
            writer.write("# ");
            writer.write(info.getComment());
            writer.write('\n');

            // write string literal key
            Matcher matcher = NEW_LINE.matcher(info.getLiteral());
            writer.write(String.valueOf(j));
            writer.write("> ");
            writer.write(matcher.replaceAll("\\n"));
            writer.write('\n');
            writer.write('\n');
            j++;
        }

        writer.close();

        System.out.print("Finished.");
    }

    private boolean isLocalizableAnnotation(String annotationName) {
        return annotationName.equals("Lcom/jcwhatever/nucleus/utils/language/Localizable;");
    }

    // get localizable string literals from class files
    private List<LiteralInfo> getStringLiterals(File file) throws IOException {

        System.out.println("Opening jar file: " + file.getAbsolutePath());

        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        List<LiteralInfo> results = new LinkedList<>();

        LinkedList<ClassNode> parseQueue = new LinkedList<>();
        Map<String, AnnotationInfo> annotations = new HashMap<>(10);

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.isDirectory() || !entry.getName().endsWith(".class"))
                continue;

            InputStream stream = jarFile.getInputStream(entry);
            ClassReader reader = new ClassReader(stream);

            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // check if class is an annotation
            if (!classNode.interfaces.isEmpty() &&
                    classNode.interfaces.contains("java/lang/annotation/Annotation")) {

                // get annotation first so they can be applied in classes
                AnnotationInfo info = parseAnnotation(classNode);
                if (info == null)
                    continue;

                annotations.put(info.className, info);
            }
            else {
                // set aside non-annotation classes for now
                parseQueue.addLast(classNode);
            }

            stream.close();
        }

        // parse classes
        while (!parseQueue.isEmpty()) {
            ClassNode node = parseQueue.removeFirst();
            results.addAll(parseClass(node, annotations));
        }

        jarFile.close();

        return results;
    }

    // parse an annotation for localizable methods
    private AnnotationInfo parseAnnotation(ClassNode classNode) {

        @SuppressWarnings("unchecked")
        List<MethodNode> methods = (List<MethodNode>)classNode.methods;
        if (methods == null)
            return null;

        List<String> methodNames = new ArrayList<>(5);

        for (MethodNode node : methods) {

            @SuppressWarnings("unchecked")
            List<AnnotationNode> annotationNodes = node.visibleAnnotations;
            if (annotationNodes == null)
                continue;

            for (AnnotationNode annotation : annotationNodes) {
                if (!isLocalizableAnnotation(annotation.desc))
                    continue;

                System.out.println("@Localizable Annotation method found: " + node.name + ' ' + classNode.name);

                methodNames.add(node.name);
            }
        }

        return new AnnotationInfo(classNode.name, methodNames);
    }

    // parse a class for localizable fields and localizable annotation usages.
    private List<LiteralInfo> parseClass(ClassNode classNode, Map<String, AnnotationInfo> annotations) {

        List<LiteralInfo> result = new ArrayList<>(10);

        // parse class annotations
        @SuppressWarnings("unchecked")
        List<AnnotationNode> classAnnotations = classNode.visibleAnnotations;
        if (classAnnotations != null) {

            for (AnnotationNode node : classAnnotations) {

                String nodeName = node.desc.substring(1, node.desc.length() - 1);

                AnnotationInfo info = annotations.get(nodeName);
                if (info == null)
                    continue;

                result.addAll(parseAnnotationUsage(classNode, node));
            }
        }

        // parse fields
        @SuppressWarnings("unchecked")
        List<FieldNode> fields = (List<FieldNode>)classNode.fields;
        if (fields == null)
            return new ArrayList<>(0);

        for (FieldNode node : fields) {

            @SuppressWarnings("unchecked")
            List<AnnotationNode> annotationNodes = node.visibleAnnotations;
            if (annotationNodes == null)
                continue;

            for (AnnotationNode annotation : annotationNodes) {
                if (!isLocalizableAnnotation(annotation.desc))
                    continue;

                boolean isStatic = (node.access & Opcodes.ACC_STATIC) != 0;
                boolean isFinal = (node.access & Opcodes.ACC_FINAL) != 0;

                String desc = "FIELD: " + node.name + ' ' + classNode.name;

                if (!isStatic) {
                    System.out.println("[IGNORED] @Localizable field found but isn't static: " + desc);
                }

                if (!isFinal) {
                    System.out.println("[IGNORED] @Localizable field found but isn't final: " + desc);
                }

                if (!isStatic || !isFinal)
                    continue;

                if (node.value instanceof String) {
                    System.out.println("@Localizable field found: " + desc);

                    result.add(new LiteralInfo((String) node.value, desc));
                }
                else {
                    System.out.println("[IGNORED] @Localizable field found but did not contain a String value: " + desc);
                }
            }
        }

        return result;
    }

    // parse the usage of an annotation for the localizable text
    private List<LiteralInfo> parseAnnotationUsage(ClassNode classNode, AnnotationNode annotation) {

        if (annotation.values == null)
            return new ArrayList<>(0);

        List<LiteralInfo> result = new ArrayList<>(10);

        for (int i=0; i < annotation.values.size(); i+=2) {

            String methodName = (String)annotation.values.get(i);
            Object valueObject = annotation.values.get(i + 1);

            List<String> values;
            boolean isArray;

            if (valueObject instanceof String) {
                values = new ArrayList<>(1);
                values.add((String)valueObject);
                isArray = false;
            }
            else if (valueObject instanceof List) {
                values = (List<String>)valueObject;
                isArray = true;
            }
            else {
                continue;
            }

            int count = 1;
            for (String value : values) {

                String nameSuffix = isArray
                        ? "[" + count + '|' + values.size() + ']'
                        : "()";

                String desc = "ANNOTATION METHOD: " +
                        methodName + nameSuffix + ' ' + classNode.name + ' ' + annotation.desc;

                System.out.println("Annotation usage found: " + desc);
                result.add(new LiteralInfo(value, desc));
                count++;
            }
        }

        return result;
    }

    private static class AnnotationInfo {
        final String className;
        final List<String> methodNames;

        AnnotationInfo(String className, List<String> methodNames) {
            this.className = className;
            this.methodNames = methodNames;
        }
    }
}
