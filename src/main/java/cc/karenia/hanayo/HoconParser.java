package cc.karenia.hanayo;

import java.text.*;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import cc.karenia.hanayo.types.*;

/**
 * A parser for Hocon documents.
 * 
 * @author Rynco Maekawa
 */
public final class HoconParser {
  /**
   * Characters that indicate the end of a line.
   */
  static BitSet EolChars = new BitSet();
  static {
    EolChars.set('\n');
    EolChars.set('\r');
    EolChars.set('\f');
  }

  /**
   * Characters that indicates the separation between elements.
   */
  static BitSet ElementSeparator = new BitSet();
  static {
    ElementSeparator.or(EolChars);
    ElementSeparator.set(',');
  }

  /**
   * Characters that indicates the end of an element.
   */
  static BitSet ElementEndings = new BitSet();
  static {
    ElementEndings.or(ElementSeparator);
    ElementEndings.set('}');
    ElementEndings.set(']');
  }

  /**
   * Characters that indicates the end of a key in a key-value pair.
   */
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

  /**
   * Characters that directly separates the key and the value in a key-value
   * pair.
   */
  static BitSet KeyValueSeparator = new BitSet();
  static {
    KeyValueSeparator.set(':');
    KeyValueSeparator.set('=');
  }

  /**
   * Characters that indicates the end of an unquoted string.
   * 
   * <p>
   * These characters are written as specified in the HOCON Spec.
   * </p>
   */
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

  /**
   * Initiate a new HoconParser instance and parse the given string of text.
   * 
   * @param src the string of text to be parsed
   * @return the resulting IHoconElement
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  static public IHoconPathResolvable parse(String src)
      throws HoconParseException {
    // throw new RuntimeException("Method not implemented");
    return new HoconParser(src).parseDocument();
  }

  /** The character buffer used by the parser */
  char[] buf;
  /** The start position for the parsing process */
  int startOffset;

  /** The current path this parser is parsing at */
  Stack<String> currentPath = new Stack<String>();
  /** The stack of path elements this parser has gone through */
  Stack<IHoconPathResolvable> pathStack = new Stack<IHoconPathResolvable>();

  /**
   * Initiate an empty parser.
   * <p>
   * Specify the content needing to be parsed with
   * {@see HoconParser#with(String)}.
   * </p>
   */
  public HoconParser() {

  }

  /**
   * Initiate a parser with the specified string to parse.
   * 
   * @param str the string to parse.
   */
  public HoconParser(String str) {
    this.with(str);
  }

  /**
   * Initiate a parser with specified string and initial offset.
   * 
   * @param str    the string to parse
   * @param offset initial offset
   */
  public HoconParser(String str, int offset) {
    this.with(str, offset);
  }

  /**
   * Specify the string to parse.
   * 
   * @param str the string to parse
   * @return the parser itself
   */
  public HoconParser with(String str) {
    buf = str.toCharArray();
    startOffset = 0;
    currentPath.clear();
    pathStack.clear();
    return this;
  }

  /**
   * Specify the string to parse and an initial offset.
   * 
   * @param str    the string to parse
   * @param offset initial offset
   * @return the parser itself
   */
  public HoconParser with(String str, int offset) {
    buf = str.toCharArray();
    offset = 0;
    currentPath.clear();
    pathStack.clear();
    return this;
  }

  /**
   * Initiate a parser with a string to parse.
   * 
   * @param str the string to parse
   * @return
   */
  public static HoconParser of(String str) {
    return new HoconParser(str);
  }

  /**
   * Initiate a parser with a string to parse and an initial offset.
   * 
   * @param str    the string to parse
   * @param offset initial offset
   * @return
   */
  public static HoconParser of(String str, int offset) {
    return new HoconParser(str, offset);
  }

  /**
   * Parse the string as a HOCON document.
   * 
   * @return the parsed document
   * @throws HoconParseException thrown when the parser cannot parse the string
   *                             a document
   */
  public IHoconPathResolvable parseDocument() throws HoconParseException {
    int ptr = startOffset;
    ptr = skipWhitespaceAndComments(ptr);

    // Try parsing map
    try {
      return parseMap(ptr, null).unwrap();
    } catch (HoconParseException e) {
      if (e.ptr != 0)
        throw e;
    }

    // If this document is not a map, try parsing as a list
    try {
      return parseList(ptr, null).unwrap();
    } catch (HoconParseException e) {
      if (e.ptr != 0)
        throw e;
    }

    // If none of these work, try parsing as bracket-less map
    try {
      var map = new HoconMap();
      ParseResult<Entry<HoconKey, IHoconElement>> result;

      var root = map;
      pathStack.push(map);

      while (true) {
        if (ptr >= buf.length)
          break;

        result = parseKeyValuePair(ptr, root);
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

  /**
   * Parse a list from the buffer
   * 
   * @param ptr  the offset to start with
   * @param root the root element of the parse tree
   * @return the parsed list
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  public ParseResult<HoconList> parseList(int ptr, IHoconPathResolvable root)
      throws HoconParseException {

    if (buf[ptr] != '[')
      throw new HoconParseException("Expected '[' at the start of a list", ptr,
          new HoconKey(currentPath));

    ptr++;
    ptr = skipWhitespaceAndComments(ptr);

    var list = new HoconList();
    pathStack.push(list);
    int index = 0;
    ParseResult<? extends IHoconElement> result;

    if (root == null) {
      root = list;
    }

    while (true) {
      if (buf[ptr] == ']') {
        ptr++;
        break;
      }

      currentPath.push(Integer.toString(index));

      result = parseValue(ptr, root);
      ptr = result.newPtr;
      var el = result.result;

      list.add(el);

      // Skip element separator
      if (buf[ptr] == ',')
        ptr++;

      currentPath.pop();

      // Skip whitespace and comments
      ptr = skipWhitespaceAndComments(ptr);
    }
    pathStack.pop();

    return ParseResult.success(ptr, list);
  }

  /**
   * Parse a map from the buffer.
   * 
   * @param ptr  the offset to start with
   * @param root the root object of the parse tree
   * @return the parsed map
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  public ParseResult<HoconMap> parseMap(int ptr, IHoconPathResolvable root)
      throws HoconParseException {

    if (buf[ptr] != '{')
      throw new HoconParseException("Expected '{' at the start of a Map", ptr,
          new HoconKey(currentPath));

    ptr++;
    ptr = skipWhitespaceAndComments(ptr);

    var map = new HoconMap();
    pathStack.push(map);
    ParseResult<Entry<HoconKey, IHoconElement>> result;

    if (root == null) {
      root = map;
    }

    while (true) {
      if (buf[ptr] == '}') {
        ptr++;
        break;
      }

      result = parseKeyValuePair(ptr, root);
      ptr = result.newPtr;
      var val = result.result;
      map.setOrReplace(val.getKey(), val.getValue());

      // Skip element separator
      if (buf[ptr] == ',')
        ptr++;

      // Skip whitespace and comments
      ptr = skipWhitespaceAndComments(ptr);
    }
    pathStack.pop();
    return ParseResult.success(ptr, map);
  }

  /**
   * Parse a key-value pair from the buffer.
   * 
   * @param ptr  the offset to start with
   * @param root the root element of the parse tree
   * @return the parsed key-value pair
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  ParseResult<Entry<HoconKey, IHoconElement>> parseKeyValuePair(int ptr,
      IHoconPathResolvable root) throws HoconParseException {
    ptr = skipWhitespace(ptr);

    var keyResult = parseKey(ptr);
    var key = keyResult.result;
    ptr = keyResult.newPtr;

    ptr = skipWhitespace(ptr);
    currentPath.push(key.name);

    IHoconElement value;
    if (KeyValueSeparator.get(buf[ptr])) {
      ptr++;
      var valueResult = parseValue(ptr, root);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else if (buf[ptr] == '{') {
      var valueResult = parseMap(ptr, root);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else if (buf[ptr] == '[') {
      var valueResult = parseList(ptr, root);
      value = valueResult.result;
      ptr = valueResult.newPtr;
    } else {
      throw new HoconParseException("Expected a value, map or list.", ptr);
    }

    currentPath.pop();
    var entry = new SimpleEntry<HoconKey, IHoconElement>(key, value);
    return ParseResult.success(ptr, entry);
  }

  /**
   * Skips whitespace and comments.
   * 
   * @param ptr the offset to start with
   * @return the new offset after whitespaces and comments
   */
  int skipWhitespaceAndComments(int ptr) {
    int oldPtr;
    do {
      oldPtr = ptr;
      ptr = skipWhitespace(ptr);
      ptr = skipComments(ptr);
    } while (ptr != oldPtr);
    return ptr;
  }

  /**
   * Parse a value from buffer
   * 
   * @param ptr  the offset to start with
   * @param root the root of the parse tree
   * @return the parsed value element
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  ParseResult<? extends IHoconElement> parseValue(int ptr,
      IHoconPathResolvable root) throws HoconParseException {
    IHoconElement value = null;
    ptr = skipWhitespace(ptr);

    var parseResult = parseValueSegment(ptr, root);

    ptr = parseResult.newPtr;
    value = parseResult.result;

    while (ptr < buf.length && !ElementEndings.get(buf[ptr])) {
      try {
        parseResult = parseValueSegment(ptr, root);
      } catch (HoconParseException.BlankString e) {
        ptr = e.ptr;
        continue;
      }

      ptr = parseResult.newPtr;
      value = value.concat(parseResult.result);
    }

    if (value.getType() == HoconType.String) {
      var strVal = (HoconString) value;
      strVal.value = strVal.value.stripTrailing();
      value = strVal.transformIfPossible();
    }
    return ParseResult.success(ptr, value);
  }

  /**
   * Parse a value segment from buffer
   * 
   * @param ptr  the offset to start with
   * @param root the root of the parse tree
   * @return the parsed value segment
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  ParseResult<? extends IHoconElement> parseValueSegment(int ptr,
      IHoconPathResolvable root) throws HoconParseException {
    if (Character.isWhitespace(buf[ptr])) {
      ParseResult<HoconString> result = null;
      try {
        result = parseHoconString(ptr, false, false);
        return result;
      } catch (HoconParseException.BlankString e) {
        throw e;
      } catch (HoconParseException e) {
        // Silently swallow error
      }
      ptr = skipWhitespaceAndComments(ptr);
    }
    if (Character.isDigit(buf[ptr]) || buf[ptr] == '+' || buf[ptr] == '-') {
      return parseNumber(ptr);
    } else if (buf[ptr] == '[') {
      return parseList(ptr, root);
    } else if (buf[ptr] == '{') {
      return parseMap(ptr, root);
    } else if (buf[ptr] == '$') {
      var subResult = parseSubstitution(ptr);
      return ParseResult.success(subResult.newPtr,
          resolveSubstitutionOnCurrentPath(subResult.result));
    } else if (buf[ptr] == '"') {
      if (ptr + 3 > buf.length && buf[ptr + 1] == '"' && buf[ptr + 2] == '"')
        return parseHoconString(ptr, false, true);
      else
        return parseHoconString(ptr, true, false);
    } else {
      return parseHoconString(ptr, false, false);

    }
  }

  /**
   * Resolves a substitution when parsing.
   * 
   * @param sub the substitution to resolve
   * @return the resolved target element
   * @throws HoconParseException thrown when the substitution is invalid
   */
  public IHoconElement resolveSubstitutionOnCurrentPath(HoconSubstitution sub)
      throws HoconParseException {
    // Find common path
    var targetPath = sub.path;
    int ptr = 0;
    while (targetPath != null && ptr < currentPath.size() - 1) {
      if (!targetPath.name.equals(currentPath.get(ptr)))
        break;
      targetPath = targetPath.next;
      ptr++;
    }
    if (targetPath == null)
      throw new HoconParseException("Invalid self referential", ptr);

    var latestCommonAncestor = pathStack.get(ptr);

    if (sub.isDetermined)
      return latestCommonAncestor.getPath(targetPath).clone();
    else
      try {
        return latestCommonAncestor.getPath(targetPath).clone();
      } catch (Exception e) {
        return new HoconSubstitution.NullSubstitution();
      }
  }

  /**
   * Parse a substitution from buffer
   * 
   * @param ptr the offset to start with
   * @return the parsed substitution
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
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

  /**
   * Parse a number from buffer
   * 
   * @param ptr the offset to start with
   * @return the parsed number
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
  ParseResult<HoconNumber> parseNumber(int ptr) throws HoconParseException {
    var initPtr = ptr;

    if (ptr >= buf.length)
      throw new HoconParseException("Expected digit, got early EOF", ptr);

    if (buf[ptr] == '+' || buf[ptr] == '-')
      ptr++;
    if (!Character.isDigit(buf[ptr]))
      throw new HoconParseException("Expected digit", ptr);

    var isInteger = true;

    // match integral part
    while (ptr < buf.length && Character.isDigit(buf[ptr]))
      ptr++;

    // match decimal point and fractional part
    if (ptr + 1 < buf.length
        && (buf[ptr] == '.' && Character.isDigit(buf[ptr + 1]))) {
      ptr += 2;
      isInteger = false;
      while (ptr < buf.length && Character.isDigit(buf[ptr]))
        ptr++;
    }

    // match exponent
    if (ptr + 1 < buf.length && ((buf[ptr] == 'E' || buf[ptr] == 'e')
        && Character.isDigit(buf[ptr + 1]))) {
      ptr += 2;
      isInteger = false;
      while (ptr < buf.length && Character.isDigit(buf[ptr]))
        ptr++;
    }

    return ParseResult.success(ptr, new HoconNumber(
        String.copyValueOf(buf, initPtr, ptr - initPtr), isInteger));
  }

  /**
   * Parse an end-of-line character from buffer
   * 
   * @param ptr the offset to start with
   * @return the new offset. Fail if no EOL was found
   *         <p>
   *         (TODO: change the representation method)
   *         </p>
   */
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

  /**
   * Skips whitespace in buffer
   * 
   * @param ptr the offset to start with
   * @return the new offset
   */
  int skipWhitespace(int ptr) {
    while (ptr < buf.length && Character.isWhitespace(buf[ptr]))
      ptr++;
    return ptr;
  }

  /**
   * Parses a key from buffer
   * 
   * @param ptr the offset to start with
   * @return the parsed key
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
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

  /**
   * Parses a string from buffer. This method wraps the parsed string with a
   * {@link HoconString} wrapper.
   * 
   * @param ptr         the offset to start with
   * @param isQuoted    is this string quoted? ({@code "like this"})
   * @param isMultiline is this a multiline (triple-quoted) string?
   * @return the parsed string
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
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

  /**
   * Parses a multiline string from buffer.
   * <p>
   * A multiline string is surrounded by triple quotes
   * ({@code """a multiline string"""})
   * </p>
   * 
   * @param ptr the pointer to start with
   * @return the parsed string
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
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

  /**
   * Parses a quoted string.
   * <p>
   * A quoted string is surrounded by double-quotes ({@code "like this"}).
   * </p>
   * 
   * @param ptr the pointer to start with
   * @return the parsed string
   * @throws HoconParseException
   */
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

  /**
   * Parse an unquoted string from buffer. This method is also used to parse the
   * keys in maps.
   * 
   * @param ptr        the offset to start with
   * @param delimiters the delimiter for this specific kind of unquoted string
   * @return the parsed string
   * @throws HoconParseException thrown when the parser cannot parse the text.
   *                             No stacktrace is given due to performance
   *                             consideration
   */
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
        if (buf[ptr + 1] == '{')
          break;

      ptr++;
    }
    if (ptr == initPtr)
      throw new HoconParseException(
          "Unexpected '%c' at the start of an unquoted string", ptr, buf[ptr]);

    String stringValue = String.copyValueOf(buf, initPtr, ptr - initPtr);

    if (stringValue.isBlank())
      throw new HoconParseException.BlankString(ptr);

    return ParseResult.success(ptr, stringValue);
  }

  /**
   * Skips the comments in buffer and stops just before the end-of-line
   * character.
   * 
   * @param ptr the pointer to start with
   * @return the new offset
   */
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
