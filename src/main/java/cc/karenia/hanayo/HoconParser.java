package cc.karenia.hanayo;

import java.text.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

import cc.karenia.hanayo.types.*;

public class HoconParser {
  static BitSet EolChars = new BitSet();
  static {
    EolChars.set('\n');
    EolChars.set('\r');
    EolChars.set('\f');
  }

  static BitSet KeyDelimiters = new BitSet();
  static {
    KeyDelimiters.set('.');
    KeyDelimiters.set(':');
    KeyDelimiters.set('=');
    KeyDelimiters.set('{');
    KeyDelimiters.set('}');
    KeyDelimiters.set('[');
    KeyDelimiters.set(']');
  }

  static BitSet UnquotedStringForbiddenChars = new BitSet();
  static {
    // UnquotedStringForbiddenChars.set('$');
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

  static public ParseResult<HoconMap> parseList(final char[] buf, int ptr) {

    throw new RuntimeException("Method not implemented");
  }

  static public ParseResult<HoconMap> parseMap(final char[] buf, int ptr) {

    throw new RuntimeException("Method not implemented");
  }

  static ParseResult<IHoconElement> parseValue(final char[] buf, int ptr) {

    throw new RuntimeException("Method not implemented");
  }

  static ParseResult<HoconNumber> parseNumber(final char[] buf, int ptr) {
    if (buf[ptr] < '0' || buf[ptr] > '9')
      return ParseResult.fail(ptr);

    var initPtr = ptr;
    var isInteger = true;

    // match integral part
    while (Character.isDigit(buf[ptr]))
      ptr++;

    // match decimal point and fractional part
    if (buf[ptr] == '.' && Character.isDigit(buf[ptr + 1])) {
      ptr += 2;
      isInteger = false;
      while (Character.isDigit(buf[ptr]))
        ptr++;
    }

    // match exponent
    if ((buf[ptr] == 'E' || buf[ptr] == 'e') && Character.isDigit(buf[ptr + 1])) {
      ptr += 2;
      isInteger = false;
      while (Character.isDigit(buf[ptr]))
        ptr++;
    }

    return ParseResult.success(ptr, new HoconNumber(String.copyValueOf(buf, initPtr, ptr), isInteger));
  }

  static ParseResult<?> parseEol(char[] buf, int ptr) {
    if (buf[ptr] == '\n') {
      ptr++;
      return ParseResult.success(ptr);
    } else if (buf[ptr] == '\r') {
      ptr++;
      if (buf[ptr] == '\n')
        ptr++;
      return ParseResult.success(ptr);
    }
    return ParseResult.fail(ptr);
  }

  static void skipWhitespace(char[] buf, int ptr) {
    while (Character.isWhitespace(buf[ptr]))
      ptr++;
  }

  static ParseResult<HoconKey> parseKey(final char[] buf, int ptr) {
    int initPtr = ptr;

    // Parse the first part of the key
    ParseResult<String> parseResult;
    if (buf[ptr] == '"')
      parseResult = parseQuotedString(buf, ptr);
    else
      parseResult = parseUnquotedString(buf, ptr, KeyDelimiters);

    if (!parseResult.parseSuccess)
      return ParseResult.fail(initPtr, new ParseException("Unable to parse key", ptr));

    var key = new HoconKey(parseResult.result.stripLeading());
    ptr = parseResult.newPtr;

    var keyTail = key;
    while (buf[ptr] == '.') {
      ptr++;
      if (buf[ptr] == '"')
        parseResult = parseQuotedString(buf, ptr);
      else
        parseResult = parseUnquotedString(buf, ptr, KeyDelimiters);

      if (!parseResult.parseSuccess)
        return ParseResult.fail(initPtr, parseResult.exception);

      keyTail.next = new HoconKey(parseResult.result);
      keyTail = keyTail.next;
      ptr = parseResult.newPtr;
    }

    keyTail.name = keyTail.name.stripTrailing();

    return ParseResult.success(ptr, key);
  }

  static ParseResult<String> parseMultilineString(final char[] buf, int ptr) {
    if (!(buf[ptr] == '"' && buf[ptr + 1] == '"' && buf[ptr + 2] == '"'))
      return ParseResult.fail(ptr, new ParseException("Expected multiline string", ptr));

    ptr += 3;
    var initPtr = ptr;
    while (true) {
      char c = buf[ptr];

      // Find end delimiter
      if (c == '"' && buf[ptr + 1] == '"' && buf[ptr + 2] == '"') {
        ptr += 3;
        while (buf[ptr] == '"')
          ptr++;
        break;
      }

      ptr++;
    }
    return ParseResult.success(ptr, String.copyValueOf(buf, initPtr, ptr - initPtr - 3));
  }

  static ParseResult<String> parseQuotedString(final char[] buf, int ptr) {
    if (buf[ptr] != '"')
      return ParseResult.fail(ptr, new ParseException("Expected quoted string", ptr));
    var initPtr = ptr;
    ptr++;
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = buf[ptr];

      // Check if the string ends here. Break if yes.
      if (c == '"')
        break;

      // Cannot break line when inside quoted string
      if (EolChars.get(c))
        return ParseResult.fail(initPtr, new ParseException("Unexpected end of line", ptr));

      // Escaped chars. Read them.
      if (c == '\\') {
        boolean shouldReadUnicodeChars = false;
        ptr++;
        char d = buf[ptr];
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
      ptr++;
    }
    ptr++;
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

  static ParseResult<String> parseUnquotedString(final char[] buf, int ptr, BitSet delimiters) {
    var initPtr = ptr;
    while (true) {
      char c = buf[ptr];

      // Check if the string ends here. Break if yes.
      if (delimiters.get(c))
        break;

      // Check if it is a comment. End if it is.
      if (c == '/')
        if (buf[ptr + 1] == '/')
          break;

      // Avoid substitutions
      if (c == '$')
        if (buf[ptr] == '{')
          break;

      ptr++;
    }
    return ParseResult.success(ptr, String.copyValueOf(buf, initPtr, ptr - initPtr));
  }

  static ParseResult<?> skipComments(final char[] buf, int ptr) {
    if (buf[ptr] != '#' || (buf[ptr] != '/' && buf[ptr + 1] != '/'))
      return ParseResult.fail(ptr);
    else {
      while (!EolChars.get(buf[ptr]))
        ptr++;
      return ParseResult.success(ptr);
    }
  }
}
