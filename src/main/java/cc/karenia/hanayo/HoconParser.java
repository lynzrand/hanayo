package cc.karenia.hanayo;

import java.text.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import cc.karenia.hanayo.types.*;

public final class HoconParser {
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

  static public IHoconElement parse(String src)
      throws HoconParseException, ArrayIndexOutOfBoundsException {
    // throw new RuntimeException("Method not implemented");
    return new HoconParser(src).parseDocument();
  }

  char[] buf;
  int startOffset;

  Stack<String> currentPath = new Stack<String>();

  public HoconParser() {

  }

  public HoconParser(String str) {
    this.with(str);
  }

  public HoconParser(String str, int offset) {
    this.with(str, offset);
  }

  public HoconParser with(String str) {
    buf = str.toCharArray();
    startOffset = 0;
    currentPath.clear();
    return this;
  }

  public HoconParser with(String str, int offset) {
    buf = str.toCharArray();
    offset = 0;
    currentPath.clear();
    return this;
  }

  public static HoconParser of(String str) {
    return new HoconParser(str);
  }

  public static HoconParser of(String str, int offset) {
    return new HoconParser(str, offset);
  }

  public IHoconElement parseDocument() throws HoconParseException {
    int ptr = startOffset;
    ptr = skipWhitespaceAndComments(ptr);

    // Try parsing map
    try {
      return parseMap(ptr, null, null).unwrap();
    } catch (HoconParseException e) {
      if (e.ptr != 0)
        throw e;
    }

    // If this document is not a map, try parsing as a list
    try {
      return parseList(ptr, null, null).unwrap();
    } catch (HoconParseException e) {
      if (e.ptr != 0)
        throw e;
    }

    // If none of these work, try parsing as bracket-less map
    try {
      var map = new HoconMap();
      ParseResult<Entry<HoconKey, IHoconElement>> result;

      var root = map;
      var currentPath = new HoconKey("{root}");

      while (true) {
        if (ptr >= buf.length)
          break;

        result = parseKeyValuePair(ptr, root, currentPath);
        ptr = result.newPtr;
        var val = result.result;
        map.setOrReplace(val.getKey(), val.getValue());

        // Skip element separator
        if (buf[ptr] == ',')
          ptr++;

        // Skip whitespace and comments
        ptr = skipWhitespaceAndComments(ptr);
      }

      return map;
    } catch (HoconParseException e) {
      if (e.ptr != ptr)
        throw e;
    }

    throw new HoconParseException("Unable to parse this file as a HOCON config",
        ptr);
  }

  public ParseResult<HoconList> parseList(int ptr, IHoconPathResolvable root,
      HoconKey currentPath) throws HoconParseException {

    if (buf[ptr] != '[')
      throw new HoconParseException("Expected '[' at the start of a list", ptr,
          currentPath.clone());

    ptr++;
    ptr = skipWhitespaceAndComments(ptr);

    var list = new HoconList();
    int index = 0;
    ParseResult<? extends IHoconElement> result;

    if (root == null) {
      root = list;
      currentPath = new HoconKey("[root]");
    }

    while (true) {
      if (buf[ptr] == '}')
        break;

      currentPath.setNext(new HoconKey(Integer.toString(index)));

      result = parseValue(ptr, root, currentPath.next);
      ptr = result.newPtr;
      var el = result.result;

      list.add(el);

      // Skip element separator
      if (buf[ptr] == ',')
        ptr++;

      // Skip whitespace and comments
      ptr = skipWhitespaceAndComments(ptr);
    }

    return ParseResult.success(ptr, list);
  }

  public ParseResult<HoconMap> parseMap(int ptr, IHoconPathResolvable root,
      HoconKey currentPath) throws HoconParseException {

    if (buf[ptr] != '{')
      throw new HoconParseException("Expected '{' at the start of a Map", ptr,
          currentPath.clone());

    ptr++;
    ptr = skipWhitespaceAndComments(ptr);

    var map = new HoconMap();
    ParseResult<Entry<HoconKey, IHoconElement>> result;

    if (root == null) {
      root = map;
      currentPath = new HoconKey("{root}");
    }

    while (true) {
      if (buf[ptr] == '}')
        break;

      result = parseKeyValuePair(ptr, root, currentPath);
      ptr = result.newPtr;
      var val = result.result;
      map.setOrReplace(val.getKey(), val.getValue());

      // Skip element separator
      if (buf[ptr] == ',')
        ptr++;

      // Skip whitespace and comments
      ptr = skipWhitespaceAndComments(ptr);
    }

    return ParseResult.success(ptr, map);
  }

  ParseResult<Entry<HoconKey, IHoconElement>> parseKeyValuePair(int ptr,
      IHoconPathResolvable root, HoconKey currentPath)
      throws HoconParseException {
    var keyResult = parseKey(ptr);
    var key = keyResult.result;
    ptr = keyResult.newPtr;

    ptr = skipWhitespace(ptr);
    currentPath.setNext(key);

    IHoconElement value;
    if (KeyValueSeparator.get(buf[ptr])) {
      var valueResult = parseValue(ptr, root, currentPath.next);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else if (buf[ptr] == '{') {
      var valueResult = parseMap(ptr, root, currentPath.next);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else if (buf[ptr] == '[') {
      var valueResult = parseList(ptr, root, currentPath.next);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else {
      throw new HoconParseException("Expected a value, map or list.", ptr);
    }

    currentPath.next = null;
    var entry = new SimpleEntry<HoconKey, IHoconElement>(key, value);
    return ParseResult.success(ptr, entry);
  }

  int skipWhitespaceAndComments(int ptr) {
    int oldPtr;
    do {
      oldPtr = ptr;
      ptr = skipWhitespace(ptr);
      ptr = skipComments(ptr);
    } while (ptr != oldPtr);
    return ptr;
  }

  ParseResult<? extends IHoconElement> parseValue(int ptr,
      IHoconPathResolvable root, HoconKey currentPath)
      throws HoconParseException {
    IHoconElement value = null;
    ptr = skipWhitespace(ptr);

    var parseResult = parseValueSegment(ptr, root, currentPath);

    ptr = parseResult.newPtr;
    value = parseResult.result;

    while (ptr < buf.length && !ElementSeparator.get(buf[ptr])) {
      parseResult = parseValueSegment(ptr, root, currentPath);

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

  ParseResult<? extends IHoconElement> parseValueSegment(int ptr,
      IHoconPathResolvable root, HoconKey currentPath)
      throws HoconParseException {
    var startChar = buf[ptr];
    if (Character.isWhitespace(startChar)) {
      try {
        var result = parseHoconString(ptr, false, false);
        return result;
      } catch (HoconParseException e) {
        // Silently swallow error; Continue on next ones
      }

    }
    if (Character.isDigit(startChar) || startChar == '+' || startChar == '-') {
      return parseNumber(ptr);
    } else if (buf[ptr] == '[') {
      return parseList(ptr, root, currentPath);
    } else if (buf[ptr] == '{') {
      return parseMap(ptr, root, currentPath);
    } else if (buf[ptr] == '$') {
      var subResult = parseSubstitution(ptr);
      return ParseResult.success(subResult.newPtr,
          subResult.result.resolve(root));
    } else if (buf[ptr] == '"') {
      if (ptr + 3 > buf.length && buf[ptr + 1] == '"' && buf[ptr + 2] == '"')
        return parseHoconString(ptr, false, true);
      else
        return parseHoconString(ptr, true, false);
    } else {
      return parseHoconString(ptr, false, false);
    }
  }

  ParseResult<HoconSubstitution> parseSubstitution(int ptr)
      throws HoconParseException {
    if (buf.length < ptr + 4 || !(buf[ptr] == '$' && buf[ptr + 1] == '{'))
      throw new HoconParseException("Expected substitution", ptr);
    var initPtr = ptr;
    ptr += 2;
    var isDetermined = true;
    if (buf[ptr] == '?') {
      isDetermined = false;
      ptr++;
    }
    var key = parseKey(ptr);

    ptr = key.newPtr;
    if (buf[ptr] != '}')
      throw new HoconParseException("Expected '}' at the end of a substitution",
          initPtr);
    ptr++;
    return ParseResult.success(ptr,
        new HoconSubstitution(key.unwrap(), isDetermined));
  }

  ParseResult<HoconNumber> parseNumber(int ptr) throws HoconParseException {
    var initPtr = ptr;
    if (buf[ptr] == '+' || buf[ptr] == '-')
      ptr++;
    if (!Character.isDigit(buf[ptr]))
      throw new HoconParseException("Expected digit", ptr);

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
    if ((buf[ptr] == 'E' || buf[ptr] == 'e')
        && (ptr + 1 < buf.length && Character.isDigit(buf[ptr + 1]))) {
      ptr += 2;
      isInteger = false;
      while (ptr < buf.length && Character.isDigit(buf[ptr]))
        ptr++;
    }

    return ParseResult.success(ptr,
        new HoconNumber(String.copyValueOf(buf, initPtr, ptr), isInteger));
  }

  ParseResult<?> parseEol(int ptr) {
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

  int skipWhitespace(int ptr) {
    while (ptr < buf.length && Character.isWhitespace(buf[ptr]))
      ptr++;
    return ptr;
  }

  public ParseResult<HoconKey> parseKey(int ptr) throws HoconParseException {
    // Parse the first part of the key
    ParseResult<String> parseResult;
    if (buf[ptr] == '"')
      parseResult = parseQuotedString(ptr);
    else
      parseResult = parseUnquotedString(ptr, KeyDelimiters);

    var key = new HoconKey(parseResult.result.stripLeading());
    ptr = parseResult.newPtr;

    // parse the rest parts if present
    var keyTail = key;
    while (ptr < buf.length && buf[ptr] == '.') {
      ptr++;
      if (buf[ptr] == '"')
        parseResult = parseQuotedString(ptr);
      else
        parseResult = parseUnquotedString(ptr, KeyDelimiters);

      keyTail.next = new HoconKey(parseResult.result, key);
      keyTail = keyTail.next;
      ptr = parseResult.newPtr;
    }

    keyTail.name = keyTail.name.stripTrailing();

    return ParseResult.success(ptr, key);
  }

  ParseResult<HoconString> parseHoconString(int ptr, boolean isQuoted,
      boolean isMultiline) throws HoconParseException {
    ParseResult<String> result;
    if (isQuoted)
      result = parseQuotedString(ptr);
    else if (isMultiline)
      result = parseMultilineString(ptr);
    else
      result = parseUnquotedString(ptr, UnquotedStringDelimiters);

    var str = new HoconString(result.result, isQuoted, isMultiline);
    return ParseResult.success(result.newPtr, str);

  }

  ParseResult<String> parseMultilineString(int ptr) throws HoconParseException {
    if (!(buf[ptr] == '"' && buf[ptr + 1] == '"' && buf[ptr + 2] == '"'))
      throw new HoconParseException(
          "Expected multiline string to start with '\"\"\"'", ptr);

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
    return ParseResult.success(ptr,
        String.copyValueOf(buf, initPtr, ptr - initPtr - 3));
  }

  ParseResult<String> parseQuotedString(int ptr) throws HoconParseException {
    if (buf[ptr] != '"')
      throw new HoconParseException("Expected quoted string to start with '\"'",
          ptr);
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

  ParseResult<String> parseUnquotedString(int ptr, BitSet delimiters)
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
      throw new HoconParseException(String.format("Unexpected '%c'", buf[ptr]),
          ptr);
    return ParseResult.success(ptr,
        String.copyValueOf(buf, initPtr, ptr - initPtr));
  }

  int skipComments(int ptr) {
    if (ptr + 1 >= buf.length || buf[ptr] != '#'
        || (buf[ptr] != '/' && buf[ptr + 1] != '/'))
      return ptr;
    else {
      while (!EolChars.get(buf[ptr]) && ptr < buf.length)
        ptr++;
      return ptr;
    }
  }
}
