package ru.prizraq.SpringWeb.bin;

// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** JSON Parser and Generator.
 * 
 * <p>This class provides some static methods to convert POJOs to and from JSON
 * notation.  The mapping from JSON to java is:<pre>
 *   object ==> Map
 *   array  ==> Object[]
 *   number ==> Double or Long
 *   string ==> String
 *   null   ==> null
 *   bool   ==> Boolean
 * </pre>
 * </p><p>
 * The java to JSON mapping is:<pre>
 *   String --> string
 *   Number --> number
 *   Map    --> object
 *   List   --> array
 *   Array  --> array
 *   null   --> null
 *   Boolean--> boolean
 *   Object --> string (dubious!)
 * </pre>
 * </p><p>
 * The interface {@link JSON.Generator} may be implemented by classes that know how to render themselves as JSON and
 * the {@link #toString(Object)} method will use {@link JSON.Generator#addJSON(StringBuffer)} to generate the JSON.
 * The class {@link JSON.Literal} may be used to hold pre-gnerated JSON object. 
 * </p>
 * @author gregw
 *
 */
public class JSON
{
    private JSON()
    {
    }

    public static String toString(Object object)
    {
        StringBuffer buffer = new StringBuffer();
        append(buffer, object);
        return buffer.toString();
    }

    public static String toString(Map object)
    {
        StringBuffer buffer = new StringBuffer();
        appendMap(buffer, object);
        return buffer.toString();
    }

    public static String toString(Object [] array)
    {
        StringBuffer buffer = new StringBuffer();
        appendArray(buffer, array);
        return buffer.toString();
    }
    
    public static Object parse(String s, boolean stripOuterComment, int initialMapCapacity)
    {
        return parse(new Source(s), stripOuterComment, initialMapCapacity);
    }

    /**
     * @param s String containing JSON object or array.
     * @param stripOuterComment If true, an outer comment around the JSON is ignored.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(String s, boolean stripOuterComment)
    {
        return parse(s, stripOuterComment, 16);
    }

    /**
     * @param s String containing JSON object or array.
     * @return A Map, Object array or primitive array parsed from the JSON.
     */
    public static Object parse(String s)
    {
        return parse(s, false);
    }

    /**
     * Append object as JSON to string buffer.
     * @param buffer
     * @param object
     */
    public static void append(StringBuffer buffer, Object object)
    {
        if (object == null)
            buffer.append("null");
        else if (object instanceof JSONObject)
            appendMap(buffer, ((JSONObject)object).toJSONMap());
        else if (object instanceof Generator)
            appendJSON(buffer, (Generator)object);
        else if (object instanceof Map)
            appendMap(buffer, (Map)object);
        else if (object instanceof Collection)
            appendArray(buffer, toArray(object, Object.class));
        else if (object.getClass().isArray())
            appendArray(buffer, object);
        else if (object instanceof Number)
            appendNumber(buffer, (Number)object);
        else if (object instanceof Boolean)
            appendBoolean(buffer, (Boolean)object);
        else if (object instanceof String)
            appendString(buffer, (String)object);
        else
            // TODO - maybe some bean stuff?
            appendString(buffer, object.toString());
    }

    public static Object toArray(Object list, Class aClass)
    {
        if (list == null)
            return (Object [])Array.newInstance(aClass, 0);

        if (list instanceof Collection)
        {
            Collection l = (Collection)list;
            if (aClass.isPrimitive())
            {
                Object a = Array.newInstance(aClass, l.size());
                int index = 0;
                for (Object o : l)
                {
                    Array.set(a, index++, index);
                }
                return a;
            }
            return l.toArray((Object [])Array.newInstance(aClass, l.size()));
        }

        Object a = Array.newInstance(aClass, 1);
        Array.set(a, 0, list);
        return a;
    }

    private static void appendNull(StringBuffer buffer)
    {
        buffer.append("null");
    }

    private static void appendJSON(StringBuffer buffer, Generator generator)
    {
        generator.addJSON(buffer);
    }

    private static void appendMap(StringBuffer buffer, Map object)
    {
        if (object == null)
        {
            appendNull(buffer);
            return;
        }

        buffer.append('{');
        Iterator iter = object.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();
            quote(buffer, String.valueOf(entry.getKey()));
            buffer.append(':');
            append(buffer, entry.getValue());
            if (iter.hasNext())
                buffer.append(',');
        }

        buffer.append('}');
    }

    public static void quote(StringBuffer buf, String s)
    {
        synchronized (buf)
        {
            buf.append('"');
            for (int i = 0; i < s.length(); i++)
            {
                char c = s.charAt(i);
                switch (c)
                {
                    case '"':
                    buf.append("\\\"");
                    continue;
                    case '\\':
                    buf.append("\\\\");
                    continue;
                    case '\n':
                    buf.append("\\n");
                    continue;
                    case '\r':
                    buf.append("\\r");
                    continue;
                    case '\t':
                    buf.append("\\t");
                    continue;
                    case '\f':
                    buf.append("\\f");
                    continue;
                    case '\b':
                    buf.append("\\b");
                    continue;

                    default:
                    buf.append(c);
                    continue;
                }
            }
            buf.append('"');
        }
    }

    private static void appendArray(StringBuffer buffer, Object array)
    {
        if (array == null)
        {
            appendNull(buffer);
            return;
        }

        buffer.append('[');
        int length = Array.getLength(array);

        for (int i = 0; i < length; i++)
        {
            if (i != 0)
                buffer.append(',');
            append(buffer, Array.get(array, i));
        }

        buffer.append(']');
    }

    private static void appendBoolean(StringBuffer buffer, Boolean b)
    {
        if (b == null)
        {
            appendNull(buffer);
            return;
        }
        buffer.append(b.booleanValue() ? "true" : "false");
    }

    private static void appendNumber(StringBuffer buffer, Number number)
    {
        if (number == null)
        {
            appendNull(buffer);
            return;
        }
        buffer.append(number);
    }

    private static void appendString(StringBuffer buffer, String string)
    {
        if (string == null)
        {
            appendNull(buffer);
            return;
        }

        quote(buffer, string);
    }

    private static Object parse(Source source, boolean stripOuterComment, int initialMapCapacity)
    {
        int comment_state = 0; // 0=no comment, 1="/", 2="/*", 3="/* *" -1="//"
        int strip_state = stripOuterComment ? 1 : 0; // 0=no strip, 1=wait for /*, 2= wait for */

        while (source.hasNext())
        {
            char c = source.peek();

            // handle // or /* comment
            if (comment_state == 1)
            {
                switch (c)
                {
                    case '/':
                    comment_state = -1;
                    break;
                    case '*':
                    comment_state = 2;
                    if (strip_state == 1)
                    {
                        comment_state = 0;
                        strip_state = 2;
                    }
                }
            }
            // handle /* */ comment
            else if (comment_state > 1)
            {
                switch (c)
                {
                    case '*':
                    comment_state = 3;
                    break;
                    case '/':
                    if (comment_state == 3)
                        comment_state = 0;
                    else
                        comment_state = 2;
                    break;
                    default:
                    comment_state = 2;
                }
            }
            // handle // comment
            else if (comment_state < 0)
            {
                switch (c)
                {
                    case '\r':
                    case '\n':
                    comment_state = 0;
                    break;
                    default:
                    break;
                }
            }
            // handle unknown
            else
            {
                switch (c)
                {
                    case '{':
                    return parseObject(source, initialMapCapacity);
                    case '[':
                    return parseArray(source, initialMapCapacity);
                    case '"':
                    return parseString(source);
                    case '-':
                    return parseNumber(source);

                    case 'n':
                    complete("null", source);
                    return null;
                    case 't':
                    complete("true", source);
                    return Boolean.TRUE;
                    case 'f':
                    complete("false", source);
                    return Boolean.FALSE;
                    case 'u':
                    complete("undefined", source);
                    return null;

                    case '/':
                    comment_state = 1;
                    break;

                    case '*':
                    if (strip_state == 2)
                    {
                        complete("*/", source);
                        strip_state = 0;
                    }
                    return null;

                    default:
                    if (Character.isDigit(c))
                        return parseNumber(source);
                    else if (Character.isWhitespace(c))
                        break;

                    throw new IllegalStateException("unknown char '" + c + "' " + (int)c + source.info());
                }
            }
            source.next();
        }

        return null;
    }

    private static Map parseObject(Source source, int initialMapCapacity)
    {
        if (source.next() != '{')
            throw new IllegalStateException();
        LinkedHashMap map = new LinkedHashMap(initialMapCapacity);

        char next = seekTo("\"}", source);

        while (source.hasNext())
        {
            if (next == '}')
            {
                source.next();
                break;
            }

            String name = parseString(source);
            seekTo(':', source);
            source.next();

            Object value = parse(source, false, initialMapCapacity);
            map.put(name, value);

            seekTo(",}", source);
            next = source.next();
            if (next == '}')
                break;
            else
                next = seekTo("\"}", source);
        }

        return map;
    }

    private static Object parseArray(Source source, int initialMapCapacity)
    {
        if (source.next() != '[')
            throw new IllegalStateException();

        ArrayList list = new ArrayList();
        boolean coma = true;

        while (source.hasNext())
        {
            char c = source.peek();
            switch (c)
            {
                case ']':
                source.next();
                return list.toArray(new Object[list.size()]);

                case ',':
                if (coma)
                    throw new IllegalStateException();
                coma = true;
                source.next();

                default:
                if (Character.isWhitespace(c))
                    source.next();
                else
                {
                    coma = false;
                    list.add(parse(source, false, initialMapCapacity));
                }
            }

        }

        throw new IllegalStateException("unexpected end of array" + source.info());
    }

    private static String parseString(Source source)
    {
        if (source.next() != '"')
            throw new IllegalStateException();

        boolean escape = false;
        StringBuffer b = new StringBuffer();
        while (source.hasNext())
        {
            char c = source.next();

            if (escape)
            {
                escape = false;
                switch (c)
                {
                    case 'n':
                    b.append('\n');
                    break;
                    case 'r':
                    b.append('\r');
                    break;
                    case 't':
                    b.append('\t');
                    break;
                    case 'f':
                    b.append('\f');
                    break;
                    case 'b':
                    b.append('\b');
                    break;
                    case 'u':
                    int code = Integer.valueOf(source.next(4), 16);
                    b.append((char)code);
                    break;
                    default:
                    b.append(c);
                }
            }
            else if (c == '\\')
            {
                escape = true;
                continue;
            }
            else if (c == '\"')
                break;
            else
                b.append(c);
        }

        return b.toString();
    }

    /** 
     * @param b An ASCII encoded character 0-9 a-f A-F
     * @return The byte value of the character 0-16.
     */
    public static byte convertHexDigit(byte b)
    {
        if ((b >= '0') && (b <= '9'))
            return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f'))
            return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F'))
            return (byte)(b - 'A' + 10);
        return 0;
    }

    private static Number parseNumber(Source source)
    {
        int start = source.index();
        int end = -1;
        boolean is_double = false;
        while (source.hasNext() && end < 0)
        {
            char c = source.peek();
            switch (c)
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                source.next();
                break;

                case '.':
                case 'e':
                case 'E':
                is_double = true;
                source.next();
                break;

                default:
                end = source.index();
            }
        }
        String s = end >= 0 ? source.from(start, end) : source.from(start);
        if (is_double)
            return new Double(s);
        else
            return new Long(s);
    }

    private static void seekTo(char seek, Source source)
    {
        while (source.hasNext())
        {
            char c = source.peek();
            if (c == seek)
                return;

            if (!Character.isWhitespace(c))
                throw new IllegalStateException("Unexpected '" + c + " while seeking '" + seek + "'" + source.info());
            source.next();
        }

        throw new IllegalStateException("Expected '" + seek + "'");
    }

    private static char seekTo(String seek, Source source)
    {
        while (source.hasNext())
        {
            char c = source.peek();
            if (seek.indexOf(c) >= 0)
            {
                return c;
            }

            if (!Character.isWhitespace(c))
                throw new IllegalStateException("Unexpected '" + c + "' while seeking one of '" + seek + "'" + source.info());
            source.next();
        }

        throw new IllegalStateException("Expected one of '" + seek + "'");
    }

    private static void complete(String seek, Source source)
    {
        int i = 0;
        while (source.hasNext() && i < seek.length())
        {
            char c = source.next();
            if (c != seek.charAt(i++))
                throw new IllegalStateException("Unexpected '" + c + " while seeking '" + seek + "'" + source.info());
        }

        if (i < seek.length())
            throw new IllegalStateException("Expected '" + seek + "'" + source.info());
    }
    
    private static class Source
    {
        private final String string;
        private int index;

        Source(String s)
        {
            string = s;
        }

        public String info()
        {
			return " (" + string.length() + ": " + substring(50) + ")";
		}

		public String substring(int len)
        {
        	String bs = "";
        	String es = "";
        	int bi = 0;
        	int ei = string.length();
        	if (index - len > bi)
        	{
        		bi = index - len;
        		bs = "...";
        	}
        	if (index + len < ei)
        	{
        		ei = index + len;
        		es = "...";
        	}
			return bs + string.substring(bi, ei) + es;
		}

		boolean hasNext()
        {
            return (index < string.length());
        }

        char next()
        {
            return string.charAt(index++);
        }

        char peek()
        {
            return string.charAt(index);
        }

        int index()
        {
            return index;
        }

        String from(int mark)
        {
            return string.substring(mark, index);
        }

        String from(int mark, int end)
        {
            return string.substring(mark, end);
        }

        public String next(int count)
        {
            return from(index, index += count);
        }
    }

    public interface Generator
    {
        public void addJSON(StringBuffer buffer);
    }

    /* ------------------------------------------------------------ */
    /** A Literal JSON generator
     * A utility instance of {@link JSON.Generator} that holds a pre-generated string on JSON text.
     */
    public static class Literal implements Generator
    {
        private String _json;

        /* ------------------------------------------------------------ */
        /** Construct a literal JSON instance for use by {@link JSON#toString(Object)}.
         * @param json A literal JSON string that will be parsed to check validity.
         */
        public Literal(String json)
        {
            parse(json);
            _json = json;
        }

        public String toString()
        {
            return _json;
        }

        public void addJSON(StringBuffer buffer)
        {
            buffer.append(_json);
        }
    }
}