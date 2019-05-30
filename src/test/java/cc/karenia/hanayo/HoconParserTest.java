package cc.karenia.hanayo;

import static org.junit.Assert.*;

import org.junit.*;

public class HoconParserTest {
  @Test
  public void TestKeyParser() throws Exception {
    var charArray = "test.\"quoted\".spaced key: someKey".toCharArray();
    var parseResult = HoconParser.parseKey(charArray, 0);
    if (parseResult.exception != null)
      throw parseResult.exception;
    assertEquals(parseResult.parseSuccess, true);
    assertEquals(parseResult.result.name, "test");
    assertEquals(parseResult.result.next.name, "quoted");
    assertEquals(parseResult.result.next.next.name, "spaced key");
  }
}
