package cc.karenia.hanayo;

import java.text.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

import cc.karenia.hanayo.types.*;

public class HoconParser {
  /**
   * End delimiters of HOCON keys
   */
  static final char[] KEY_END_DELIMITERS = new char[] { '.', ':', '=', '{' };

  static BitSet UnquotedStringForbiddenChars = new BitSet();
  static {
    UnquotedStringForbiddenChars.set('$');
    UnquotedStringForbiddenChars.set('"');
    UnquotedStringForbiddenChars.set('{');
    UnquotedStringForbiddenChars.set('}');
    UnquotedStringForbiddenChars.set('[');
    UnquotedStringForbiddenChars.set(']');
    UnquotedStringForbiddenChars.set(':');
    UnquotedStringForbiddenChars.set('=');
    UnquotedStringForbiddenChars.set(',');
    UnquotedStringForbiddenChars.set('+');
    UnquotedStringForbiddenChars.set('#');
    UnquotedStringForbiddenChars.set('\\');
    UnquotedStringForbiddenChars.set('?');
    UnquotedStringForbiddenChars.set('!');
    UnquotedStringForbiddenChars.set('@');
    UnquotedStringForbiddenChars.set('&');
    UnquotedStringForbiddenChars.set('^');
    UnquotedStringForbiddenChars.set('\n');
    UnquotedStringForbiddenChars.set('\r');
    UnquotedStringForbiddenChars.set('\f');
  }

  static public IHoconElement parse(String src) {
    // throw new RuntimeException("Method not implemented");
    return parseDocument(src.toCharArray());
  }

  static public IHoconElement parseDocument(char[] buffer) {
    throw new RuntimeException("Method not implemented");
  }

  static public HoconMap parseMap(final PushbackReader reader) throws IOException {
    int c;
    var map = new HoconMap();
    while ((c = reader.read()) != '}' && c != -1) {
      reader.unread(c);
      var key = readKey(reader);
      var val = readValue(reader);
    }
    return map;
  }

  static IHoconElement readValue(final PushbackReader reader) throws IOException {

    throw new RuntimeException("Method not implemented");
  }

  static ParseResult<?> parseEol(char[] buffer, int pointer) {
    if (buffer[pointer] == '\n') {
      pointer++;
      return ParseResult.success(pointer);
    } else if (buffer[pointer] == '\r') {
      pointer++;
      if (buffer[pointer] == '\n')
        pointer++;
      return ParseResult.success(pointer);
    }
    return ParseResult.fail(pointer);
  }

  static void skipWhitespace(char[] buffer, int pointer) {
    while (Character.isWhitespace(buffer[pointer]))
      pointer++;
  }

  /**
   * Reads the key in a Hocon Object(Map).
   * 
   * @param reader the stream to read from
   * @return the key being read
   * @throws IOException
   */
  @Deprecated
  static HoconKey readKey(final PushbackReader reader) throws IOException {
    String keyString;
    int c;
    if ((c = reader.read()) == '"')
      keyString = readRawQuotedString(reader);
    else {
      reader.unread(c);
      keyString = readRawUnquotedString(reader, KEY_END_DELIMITERS);
    }

    var key = new HoconKey(keyString.trim());
    var keyTail = key;
    while ((c = reader.read()) == '.') {
      keyTail.next = readKey(reader);
      keyTail = keyTail.next;
    }
    reader.unread(c);
    return key;
  }

  /**
   * Read a quoted string. The delimiter (double quote) are <b>NOT</b> pushed back
   * to the stream.
   * 
   * @param reader the stream to read from
   * @return The string read
   * @throws IOException
   */
  @Deprecated
  static String readRawQuotedString(final PushbackReader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = (char) reader.read();

      // Check if the string ends here. Break if yes.
      if (c == '"')
        break;

      // Escaped chars. Read them.
      if (c == '\\') {
        boolean shouldReadUnicodeChars = false;
        char d = (char) reader.read();
        switch (d) {
        case '\\':
          c = '\\';
          break;
        case '/':
          c = '/';
          break;
        case 'n':
          c = '\n';
          break;
        case 'r':
          c = '\r';
          break;
        case 'b':
          c = '\b';
          break;
        case 'f':
          c = '\f';
          break;
        case 't':
          c = '\t';
          break;
        case 'u':
          shouldReadUnicodeChars = true;
          break;
        default:
          c = d;
        }
        if (shouldReadUnicodeChars) {
          var unicode = CharBuffer.allocate(4);
          reader.read(unicode);
          var codePoint = Integer.parseUnsignedInt(unicode, 0, 4, 16);
          sb.appendCodePoint(codePoint);
          continue;
        }
      }

      // Raise error when meeting EOF
      if (c == -1)
        throw new EOFException("Early EOF when parsing file.");

      sb.append(c);
    }
    return sb.toString();
  }

  @Deprecated
  static boolean isCharIn(char c, char[] delimiter) {
    for (var d : delimiter) {
      if (c == d)
        return true;
    }
    return false;
  }

  /**
   * Reads an unquoted string. The delimiters are pushed back into the stream when
   * met.
   * 
   * @param reader        the stream to read from
   * @param endDelimiters the end delimiters. Use an empty array when none.
   * @return The string read
   * @throws IOException
   */

  @Deprecated
  static String readRawUnquotedString(final PushbackReader reader, final char[] endDelimiters) throws IOException {
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = (char) reader.read();

      // Check if the string ends here. Break if yes.
      if (isCharIn(c, endDelimiters)) {
        reader.unread(c);
        break;
      }

      // Raise error when meeting EOF
      if (c == -1)
        throw new EOFException("Early EOF when parsing file.");

      sb.append(c);
    }
    return sb.toString().trim();
  }
}
