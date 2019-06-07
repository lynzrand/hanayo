package cc.karenia.hanayo.types;

/**
 * The interface for every HOCON Element.
 */
public interface IHoconElement extends Cloneable {
  /**
   * Gets the HOCON type of this element
   * 
   * @return type
   */
  HoconType getType();

  /**
   * Gets the string representation of this element. Intended for concatenation
   * uses.
   * 
   * @return the string representation
   */
  String asString();

  /**
   * Concat this element with another element
   * 
   * @param newElement the new element to concat with
   * @return concat result
   */
  IHoconElement concat(IHoconElement newElement);

  /**
   * Returns the string representation of this object when displayed as JSON
   * format
   * 
   * @param baseIndent the base indent
   * @param indent     the indent added in each layer
   * @return the string representation
   */
  String toString(int baseIndent, int indent);

  IHoconElement clone();
}
