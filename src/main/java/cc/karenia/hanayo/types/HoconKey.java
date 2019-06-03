package cc.karenia.hanayo.types;

import java.util.Collection;
import java.util.List;

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

  /**
   * Initialize a new key with only path segment
   * 
   * @param key
   */
  public HoconKey(String key) {
    this.name = key;
    this.next = null;
    this.root = this;
  }

  /**
   * Initialize a new key with a path segment and root being another key.
   * 
   * @param key
   * @param root
   */
  public HoconKey(String key, HoconKey root) {
    this.name = key;
    this.next = null;
    this.root = root;
  }

  /**
   * Set the next path segment of this key to another key
   * 
   * @param next the next path segment
   */
  public void setNext(HoconKey next) {
    this.next = next;
    if (next != null)
      next.root = this.root;
  }

  /**
   * Return the string representation of this key and its decendants' path.
   * 
   * @return the path
   */
  public String path() {
    if (this.next == null)
      return this.name;
    else
      return this.name + '.' + this.next.path();
  }

  /**
   * Finds the tail segment in a key
   * 
   * @return the tail segment
   */
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

  /**
   * Initialize a key from a list of path segments.
   * 
   * @param keys the path segments
   */
  public HoconKey(List<String> keys) {
    this(keys, 0, null);
  }

  /**
   * Initialize a key from a list of path segments.
   * 
   * @param keys   the path segments
   * @param offset the offset this key should start from
   * @param root   the root key segment
   */
  HoconKey(List<String> keys, int offset, HoconKey root) {
    this.name = keys.get(offset);
    if (offset == 0) {
      this.root = this;
    } else {
      this.root = root;
    }
    if (offset >= keys.size() - 1) {
      this.next = null;
    } else {
      this.next = new HoconKey(keys, offset + 1, this.root);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HoconKey))
      return false;

    var other = (HoconKey) obj;
    return this.name.equals(other.name)
        && ((this.next == null && other.next == null)
            || this.next.equals(other.next));
  }
}
