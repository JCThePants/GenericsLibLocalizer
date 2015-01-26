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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores information about a localizable literal.
 */
public class LiteralInfo {

    private static final Pattern PATTERN_FILEPATH_SLASH = Pattern.compile("[\\/\\\\]");

    private final String _literal;
    private final String _comment;

    /**
     * Constructor.
     *
     * @param literal  The literal text that can be localized.
     * @param comment  Comment to be included int output key file to give context
     *                 information about the text.
     */
    LiteralInfo(String literal, String comment) {
        _literal = literal;

        Matcher matcher = PATTERN_FILEPATH_SLASH.matcher(comment);
        _comment = matcher.replaceAll(".");
    }

    /**
     * Get the localizable literal text.
     */
    public String getLiteral() {
        return _literal;
    }

    /**
     * Get the text comment.
     */
    public String getComment() {
        return _comment;
    }

    @Override
    public int hashCode() {
        return _literal.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LiteralInfo && ((LiteralInfo) obj)._literal.equals(_literal);
    }
}
