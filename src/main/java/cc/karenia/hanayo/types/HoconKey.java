package cc.karenia.hanayo.types;

/**
 * The key of a HOCON object's field
 */
public class HoconKey {
  /**
   * The name of this key
   */
  public String name;

  /**
   * The next part in key path
   */
  public HoconKey next;
  /**
   * The root of this key
   */
  public HoconKey root;

  public HoconKey(String key) {
    this.name = key;
    this.next = null;
    this.root = this;
  }

  public HoconKey(String key, HoconKey root) {
    this.name = key;
    this.next = null;
    this.root = root;
  }

  public void setNext(HoconKey next) {
    this.next = next;
    next.root = this.root;
  }

  public String path() {
    if (this.next == null)
      return this.name;
    else
      return this.name + '.' + this.next.path();
  }

  public HoconKey tail() {
    var tail = this;
    while (tail.next != null)
      tail = tail.next;
    return tail;
  }

  @Override
  public HoconKey clone() {
    var newObj = new HoconKey(name, root);
    if (this.next != null)
      newObj.next = this.next.clone();
    return newObj;
  }
}
