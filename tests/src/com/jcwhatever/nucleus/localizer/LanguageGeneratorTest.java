package com.jcwhatever.nucleus.localizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class LanguageGeneratorTest {

    private static File _testJarFile;
    private static Map<String, String> _expected = new HashMap<>(10);


    @ClassRule
    public static TemporaryFolder _folder = new TemporaryFolder();

    /**
     * Unpack test jar
     */
    @BeforeClass
    public static void init() {

        InputStream stream = LanguageGeneratorTest.class.getResourceAsStream("/NucleusLocalizerTest.jar");

        try {
            _testJarFile = _folder.newFile("NucleusLocalizerTest.jar");
        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return;
        }

        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(_testJarFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
            return;
        }

        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        finally {
            try {
                stream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            try {
                outputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add expected output file values.
     *
     * <p>Map is keyed to comment, value is text</p>
     */
    @BeforeClass
    public static void initExpected() {
        _expected.put(
                "# ANNOTATION METHOD: test1() com.jcwhatever.nucleus.language.test.LocalizedFields Lcom.jcwhatever.nucleus.language.test.LocalizableAnnotation;",
                "Annotation test 1");

        _expected.put(
                "# ANNOTATION METHOD: test2[1|2] com.jcwhatever.nucleus.language.test.LocalizedFields Lcom.jcwhatever.nucleus.language.test.LocalizableAnnotation;",
                "Annotation test 2");

        _expected.put(
                "# ANNOTATION METHOD: test2[2|2] com.jcwhatever.nucleus.language.test.LocalizedFields Lcom.jcwhatever.nucleus.language.test.LocalizableAnnotation;",
                "Annotation test3");

        _expected.put(
                "# FIELD: TEST1 com.jcwhatever.nucleus.language.test.LocalizedFields",
                "Test Localization Text Field 1");

        _expected.put(
                "# FIELD: TEST2 com.jcwhatever.nucleus.language.test.LocalizedFields",
                "Test Localization Text Field 2");
    }

    @Test
    public void test() throws IOException {

        File output = _folder.newFile("testoutput.txt");

        LanguageGenerator generator = new LanguageGenerator(_testJarFile, output, "1.0");

        generator.generate();

        FileInputStream stream = null;

        try {

            stream = new FileInputStream(output);

            Scanner scanner = new Scanner(stream, "UTF-16");

            System.out.println();
            System.out.println("Output:");
            System.out.println();


            Set<String> commentLines = new HashSet<>(5); // store the parsed comment lines for further validation

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);

                // skip version and empty lines
                if (line.startsWith("version>") ||
                        line.isEmpty()) {
                    continue;
                }

                // get the value expected after the current comment.
                String expected = _expected.get(line);
                assertNotNull(expected);

                commentLines.add(line);

                // should have a next line with the value on it.
                assertEquals(true, scanner.hasNext());

                String value = scanner.nextLine();
                System.out.println(value);

                // 3 is the space taken by the index that precedes the value (order is not important)
                assertEquals(expected, value.substring(3));
            }

            // all expected values should have been found
            assertEquals(_expected.keySet().size(), commentLines.size());

        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        finally {
            if (stream != null)
                stream.close();
        }
    }

}