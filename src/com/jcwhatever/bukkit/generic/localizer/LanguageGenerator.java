/* This file is part of GenericsLibLocalizer, licensed under the MIT License (MIT).
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

package com.jcwhatever.bukkit.generic.localizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageGenerator {

    private static Pattern NEW_LINE = Pattern.compile("\n");

    private final File _jarFile;
    private final File _outputFile;
    private final String _version;

    public LanguageGenerator(File jarFile, File outputFile, String version) {

        _jarFile = jarFile;
        _outputFile = outputFile;
        _version = version;
    }

    private boolean isLocalizableAnnotation(String annotationName) {
        return annotationName.startsWith("Lcom/jcwhatever/") &&
               annotationName.endsWith("/generic/language/Localizable;");
    }

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

        LinkedList<LiteralInfo> literals = getStringLiterals(_jarFile);

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

        for (int i=0; i < literals.size(); i++) {

            LiteralInfo info = literals.get(i);

            // write comment
            writer.write("# ");
            writer.write(info.getFieldName());
            writer.write('\n');

            // write string literal key
            Matcher matcher = NEW_LINE.matcher(info.getLiteral());
            writer.write(String.valueOf(i));
            writer.write("> ");
            writer.write(matcher.replaceAll("\\n"));
            writer.write('\n');
            writer.write('\n');
        }

        writer.close();

        System.out.print("Finished.");
    }

    // get localizable string literals from class files
    private LinkedList<LiteralInfo> getStringLiterals(File file) throws IOException {
        System.out.println("Opening jar file: " + file.getAbsolutePath());

        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        LinkedList<LiteralInfo> results = new LinkedList<>();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.isDirectory() || !entry.getName().endsWith(".class"))
                continue;

            InputStream stream = jarFile.getInputStream(entry);
            ClassReader reader = new ClassReader(stream);

            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            @SuppressWarnings("unchecked")
            List<FieldNode> fields = (List<FieldNode>)classNode.fields;
            if (fields == null)
                continue;

            for (FieldNode node : fields) {

                @SuppressWarnings("unchecked")
                List<AnnotationNode> annotationNodes = node.visibleAnnotations;
                if (annotationNodes == null)
                    continue;

                for (AnnotationNode annotation : annotationNodes) {
                    if (!isLocalizableAnnotation(annotation.desc))
                        continue;

                    System.out.println("@Localizable found: " + node.name + ' ' + classNode.name);

                    if (node.value instanceof String) {
                        String fieldDescriptor = node.name + ' ' + classNode.name;

                        results.add(new LiteralInfo((String)node.value, fieldDescriptor));
                    }
                }
            }

            stream.close();
        }

        jarFile.close();

        return results;
    }


}
