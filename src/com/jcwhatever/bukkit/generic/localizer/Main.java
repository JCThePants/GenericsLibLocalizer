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

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String args[]) {

        if (args.length != 2) {
            printHelp();
            return;
        }

        String inputJarName = args[0];
        String version = args[1];

        File jarFile = new File(inputJarName);
        if (!jarFile.exists()) {
            System.out.println("File not found: " + inputJarName);
            return;
        }

        if (!inputJarName.endsWith(".jar") || !jarFile.isFile()) {
            System.out.println("jar file expected: " + inputJarName);
            return;
        }

        File outFile = new File("lang.keys.txt");

        LanguageGenerator generator = new LanguageGenerator(jarFile, outFile, version);

        try {
            generator.generate();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void printHelp() {

        System.out.println("Format expect:");
        System.out.println("jar -jar GenericsLibLocalizer.jar <jarFileName> <version>");
    }

}
