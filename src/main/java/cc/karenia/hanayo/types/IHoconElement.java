package cc.karenia.hanayo.types;

/**
 * The interface for every HOCON Element.
 */
public interface IHoconElement {
  /**
   * Gets the HOCON type of this element
   * 
   * @return type
   */
  HoconType getType();

  /**
   * Gets the original string of this element
   * 
   * @return the underlying string
   */
  String getOriginalString();

  /**
   * Concat this element with another element
   * 
   * @param newElement the new element to concat with
   * @return concat result
   */
  IHoconElement concat(IHoconElement newElement);
}
