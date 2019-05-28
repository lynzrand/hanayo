package cc.karenia.hanayo.types;

/**
 * The key of a HOCON object's field
 */
public class HoconKey {
  /**
   * The name of this key
   */
  public final String name;

  /**
   * The next part in key path
   */
  public HoconKey next;

  public HoconKey(String key) {
    this.name = key;
    this.next = null;
  }

  public HoconKey(String key, HoconKey next) {
    this.name = key;
    this.next = next;
  }
}
