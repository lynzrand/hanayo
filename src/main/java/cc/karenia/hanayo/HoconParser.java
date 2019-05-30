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

  static BitSet EolChars = new BitSet();
  static {
    EolChars.set('\n');
    EolChars.set('\r');
    EolChars.set('\f');
  }

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

  @Deprecated
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

  @Deprecated
  static IHoconElement readValue(final PushbackReader reader) throws IOException {

    throw new RuntimeException("Method not implemented");
  }

  static ParseResult<Boolean> parseBoolean(char[] buffer, int pointer) {
    var ptr = pointer;
    if (buffer[ptr++] == 't' && buffer[ptr++] == 'r' && buffer[ptr++] == 'u' && buffer[ptr++] == 'e')
      return ParseResult.success(ptr, true);
    ptr = pointer;
    if (buffer[ptr++] == 'f' && buffer[ptr++] == 'a' && buffer[ptr++] == 'l' && buffer[ptr++] == 's'
        && buffer[ptr++] == 'e')
      return ParseResult.success(ptr, false);
    return ParseResult.fail(pointer);
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

  @Deprecated
  static ParseResult<HoconKey> parseKey(final char[] buf, int ptr) {

  }

  static ParseResult<String> parseQuotedString(final char[] buf, int ptr) {
    if (buf[ptr] != '"')
      return ParseResult.fail(ptr);
    var origPtr = ptr;
    ptr++;
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = buf[ptr];

      // Check if the string ends here. Break if yes.
      if (c == '"')
        break;

      if (!EolChars.get(c))
        return ParseResult.fail(origPtr);

      // Escaped chars. Read them.
      if (c == '\\') {
        boolean shouldReadUnicodeChars = false;
        char d = buf[++ptr];
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
          var unicode = CharBuffer.wrap(buf, ptr, 4);
          ptr += 4;
          var codePoint = Integer.parseUnsignedInt(unicode, 0, 4, 16);
          sb.appendCodePoint(codePoint);
          continue;
        }
      }

      sb.append(c);
    }
    return ParseResult.success(ptr, sb.toString());
  }

  @Deprecated
  static boolean isCharIn(char c, char[] delimiter) {
    for (var d : delimiter) {
      if (c == d)
        return true;
    }
    return false;
  }

  @Deprecated
  static ParseResult<String> parseUnquotedString(final char[] buf, int ptr, BitSet delimiters) {
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = buf[ptr];

      // Check if the string ends here. Break if yes.
      if (delimiters.get(c)) {
        ptr--;
        break;
      }

      // Check if it is a comment. End if it is.
      if (c == '/')
        if (buf[ptr + 1] == '/') {
          ptr--;
          break;
        }

      sb.append(c);
      ptr++;
    }
    return ParseResult.success(ptr, sb.toString().trim());
  }
}
