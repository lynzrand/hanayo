package cc.karenia.hanayo;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import org.junit.*;

public class HoconParserTest {
  @Test
  public void testParseUnquoted() throws IOException {
    var reader = new PushbackReader(new StringReader("    this is a test string:"));
    var str = HoconParser.readRawUnquotedString(reader, HoconParser.KEY_END_DELIMITERS);
    assertEquals("Unquoted string should be parsed and trimmed", "this is a test string", str);
  }

  @Test
  public void testParseKey() throws IOException {
    var reader = new PushbackReader(new StringReader("key1.space separated path.\"segments\": value"), 16);
    var path = HoconParser.readKey(reader);
    assertEquals("Key parse part 1, unquoted string", path.name, "key1");
    assertEquals("Key parse part 2, path segmenter", path.next.name, "space separated path");
    assertEquals("Key parse part 3, quoted string", path.next.next.name, "segments");
  }
}
