package cc.karenia.hanayo.types;

/**
 * A class that can get and set elements according to a {@link HoconKey} path.
 */
public interface IHoconPathResolvable extends IHoconElement {
  /**
   * Gets the corresponding element in this element according to the key.
   * 
   * @param key
   * @return the corresponding element found, or null if not found
   */
  IHoconElement get(String key);

  /**
   * Gets the corresponding element this element and its children according to
   * the path given.
   * 
   * @param path the path of the element
   * @return the corresponding element found, or null if not found
   * @throws HoconParseException thrown when the path does not parse into a
   *                             HoconKey
   */
  IHoconElement getPath(String path) throws HoconParseException;

  /**
   * Gets the corresponding element this element and its children according to
   * the path given.
   * 
   * @param path the path of the element
   * @return the corresponding element found, or null if not found
   */
  IHoconElement getPath(HoconKey path);

  /**
   * Sets or replaces the corresponding element according to the given path.
   * 
   * @param path    the path to the element
   * @param element the new element to set or replace
   */
  void setOrReplace(HoconKey path, IHoconElement element);

}
