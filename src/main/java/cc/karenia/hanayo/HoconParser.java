package cc.karenia.hanayo;

import java.text.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import cc.karenia.hanayo.types.*;

public class HoconParser {
  static BitSet EolChars = new BitSet();
  static {
    EolChars.set('\n');
    EolChars.set('\r');
    EolChars.set('\f');
  }

  static BitSet ElementSeparator = new BitSet();
  static {
    ElementSeparator.or(EolChars);
    ElementSeparator.set(',');
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

  static BitSet KeyValueSeparator = new BitSet();
  static {
    KeyValueSeparator.set(':');
    KeyValueSeparator.set('=');
  }

  static BitSet UnquotedStringDelimiters = new BitSet();
  static {
    // UnquotedStringForbiddenChars.set('$');
    UnquotedStringDelimiters.set('"');
    UnquotedStringDelimiters.set('{');
    UnquotedStringDelimiters.set('}');
    UnquotedStringDelimiters.set('[');
    UnquotedStringDelimiters.set(']');
    UnquotedStringDelimiters.set(':');
    UnquotedStringDelimiters.set('=');
    UnquotedStringDelimiters.set(',');
    UnquotedStringDelimiters.set('+');
    UnquotedStringDelimiters.set('#');
    UnquotedStringDelimiters.set('\\');
    UnquotedStringDelimiters.set('?');
    UnquotedStringDelimiters.set('!');
    UnquotedStringDelimiters.set('@');
    UnquotedStringDelimiters.set('&');
    UnquotedStringDelimiters.set('^');
    UnquotedStringDelimiters.set('\n');
    UnquotedStringDelimiters.set('\r');
    UnquotedStringDelimiters.set('\f');
  }

  static public IHoconElement parse(String src) {
    // throw new RuntimeException("Method not implemented");
    return parseDocument(src.toCharArray());
  }

  static public IHoconElement parseDocument(char[] buffer) {
    throw new NoSuchMethodError();
  }

  static public ParseResult<HoconMap> parseList(final char[] buf, int ptr, IHoconPathResolvable root,
      HoconKey currentPath) {

    throw new NoSuchMethodError();
  }

  static public ParseResult<HoconMap> parseMap(final char[] buf, int ptr, IHoconPathResolvable root,
      HoconKey currentPath) {

    throw new NoSuchMethodError();
  }

  static Entry<HoconKey, IHoconElement> parseKeyValuePair(final char[] buf, int ptr, IHoconPathResolvable root,
      HoconKey currentPath) throws HoconParseException {
    var keyResult = parseKey(buf, ptr);
    var key = keyResult.result;
    ptr = keyResult.newPtr;

    ptr = skipWhitespace(buf, ptr);
    currentPath.next = key;

    IHoconElement value;
    if (KeyValueSeparator.get(buf[ptr])) {
      var valueResult = parseValue(buf, ptr, root, currentPath);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else if (buf[ptr] == '{') {
      var valueResult = parseMap(buf, ptr, root, currentPath);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else if (buf[ptr] == '[') {
      var valueResult = parseList(buf, ptr, root, currentPath);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else {
      throw new HoconParseException("Expected a value, map or list.", ptr);
    }
    currentPath.next = null;
    var entry = new SimpleEntry<HoconKey, IHoconElement>(key, value);
    return entry;
  }

  static ParseResult<? extends IHoconElement> parseValue(final char[] buf, int ptr, IHoconPathResolvable root,
      HoconKey currentPath) throws HoconParseException {
    IHoconElement value = null;
    ptr = skipWhitespace(buf, ptr);

    var parseResult = parseValueSegment(buf, ptr, root, currentPath);

    ptr = parseResult.newPtr;
    value = parseResult.result;

    while (!ElementSeparator.get(buf[ptr])) {
      parseResult = parseValueSegment(buf, ptr, root, currentPath);

      ptr = parseResult.newPtr;
      value = value.concat(parseResult.result);
    }

    if (value.getType() == HoconType.String) {
      var strVal = (HoconString) value;
      strVal.value = strVal.value.stripTrailing();
      strVal.transformIfPossible();
    }
    return ParseResult.success(ptr, value);
  }

  static ParseResult<? extends IHoconElement> parseValueSegment(final char[] buf, int ptr, IHoconPathResolvable root,
      HoconKey currentPath) throws HoconParseException {
    var startChar = buf[ptr];
    if (Character.isWhitespace(startChar)) {
      try {
        var result = parseHoconString(buf, ptr, false, false);
        return result;
      } catch (HoconParseException e) {
        // Silently swallow error; Continue on next ones
      }
    }
    if (Character.isDigit(startChar) || startChar == '+' || startChar == '-') {
      return parseNumber(buf, ptr);
    } else if (buf[ptr] == '[') {
      return parseList(buf, ptr, root, currentPath);
    } else if (buf[ptr] == '{') {
      return parseMap(buf, ptr, root, currentPath);
    } else if (buf[ptr] == '$') {
      var subResult = parseSubstitution(buf, ptr);
      if (subResult.parseSuccess) {
        return ParseResult.success(subResult.newPtr, subResult.result.resolve(root));
      } else {
        return subResult;
      }
    } else if (buf[ptr] == '"') {
      if (ptr + 3 > buf.length && buf[ptr + 1] == '"' && buf[ptr + 2] == '"')
        return parseHoconString(buf, ptr, false, true);
      else
        return parseHoconString(buf, ptr, true, false);
    } else {
      return parseHoconString(buf, ptr, false, false);
    }
  }

  static ParseResult<HoconSubstitution> parseSubstitution(final char[] buf, int ptr) throws HoconParseException {
    if (buf.length < ptr + 4 || !(buf[ptr] == '$' && buf[ptr + 1] == '{'))
      throw new HoconParseException("Expected substitution", ptr);
    var initPtr = ptr;
    ptr += 2;
    var isDetermined = true;
    if (buf[ptr] == '?') {
      isDetermined = false;
      ptr++;
    }
    var key = parseKey(buf, ptr);

    ptr = key.newPtr;
    if (buf[ptr] != '}')
      throw new HoconParseException("Expected '}' at the end of a substitution", initPtr);
    ptr++;
    return ParseResult.success(ptr, new HoconSubstitution(key.unwrap(), isDetermined));
  }

  static ParseResult<HoconNumber> parseNumber(final char[] buf, int ptr) throws HoconParseException {
    if (!Character.isDigit(buf[ptr]))
      throw new HoconParseException("Expected digit", ptr);

    var initPtr = ptr;
    var isInteger = true;

    // match integral part
    while (ptr < buf.length && Character.isDigit(buf[ptr]))
      ptr++;

    // match decimal point and fractional part
    if (buf[ptr] == '.' && Character.isDigit(buf[ptr + 1])) {
      ptr += 2;
      isInteger = false;
      while (ptr < buf.length && Character.isDigit(buf[ptr]))
        ptr++;
    }

    // match exponent
    if ((buf[ptr] == 'E' || buf[ptr] == 'e') && (ptr + 1 < buf.length && Character.isDigit(buf[ptr + 1]))) {
      ptr += 2;
      isInteger = false;
      while (ptr < buf.length && Character.isDigit(buf[ptr]))
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

  static int skipWhitespace(char[] buf, int ptr) {
    while (ptr < buf.length && Character.isWhitespace(buf[ptr]))
      ptr++;
    return ptr;
  }

  public static ParseResult<HoconKey> parseKey(final char[] buf, int ptr) throws HoconParseException {
    // Parse the first part of the key
    ParseResult<String> parseResult;
    if (buf[ptr] == '"')
      parseResult = parseQuotedString(buf, ptr);
    else
      parseResult = parseUnquotedString(buf, ptr, KeyDelimiters);

    var key = new HoconKey(parseResult.result.stripLeading());
    ptr = parseResult.newPtr;

    // parse the rest parts if present
    var keyTail = key;
    while (ptr < buf.length && buf[ptr] == '.') {
      ptr++;
      if (buf[ptr] == '"')
        parseResult = parseQuotedString(buf, ptr);
      else
        parseResult = parseUnquotedString(buf, ptr, KeyDelimiters);

      keyTail.next = new HoconKey(parseResult.result, key);
      keyTail = keyTail.next;
      ptr = parseResult.newPtr;
    }

    keyTail.name = keyTail.name.stripTrailing();

    return ParseResult.success(ptr, key);
  }

  static ParseResult<HoconString> parseHoconString(final char[] buf, int ptr, boolean isQuoted, boolean isMultiline)
      throws HoconParseException {
    ParseResult<String> result;
    if (isQuoted)
      result = parseQuotedString(buf, ptr);
    else if (isMultiline)
      result = parseMultilineString(buf, ptr);
    else
      result = parseUnquotedString(buf, ptr, UnquotedStringDelimiters);

    var str = new HoconString(result.result, isQuoted, isMultiline);
    return ParseResult.success(result.newPtr, str);

  }

  static ParseResult<String> parseMultilineString(final char[] buf, int ptr) throws HoconParseException {
    if (!(buf[ptr] == '"' && buf[ptr + 1] == '"' && buf[ptr + 2] == '"'))
      throw new HoconParseException("Expected multiline string to start with '\"\"\"'", ptr);

    ptr += 3;
    var initPtr = ptr;
    while (ptr < buf.length) {
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

  static ParseResult<String> parseQuotedString(final char[] buf, int ptr) throws HoconParseException {
    if (buf[ptr] != '"')
      throw new HoconParseException("Expected quoted string to start with '\"'", ptr);
    ptr++;
    StringBuilder sb = new StringBuilder();
    while (ptr < buf.length) {
      char c = buf[ptr];

      // Check if the string ends here. Break if yes.
      if (c == '"')
        break;

      // Cannot break line when inside quoted string
      if (EolChars.get(c))
        throw new HoconParseException("Unexpected end of line", ptr);

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

  static ParseResult<String> parseUnquotedString(final char[] buf, int ptr, BitSet delimiters)
      throws HoconParseException {
    var initPtr = ptr;
    while (ptr < buf.length) {
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
    if (ptr == initPtr)
      throw new HoconParseException("Empty string!", ptr);
    return ParseResult.success(ptr, String.copyValueOf(buf, initPtr, ptr - initPtr));
  }

  static ParseResult<?> skipComments(final char[] buf, int ptr) {
    if (buf[ptr] != '#' || (buf[ptr] != '/' && buf[ptr + 1] != '/'))
      return ParseResult.fail(ptr);
    else {
      while (!EolChars.get(buf[ptr]) && ptr < buf.length)
        ptr++;
      return ParseResult.success(ptr);
    }
  }
}
