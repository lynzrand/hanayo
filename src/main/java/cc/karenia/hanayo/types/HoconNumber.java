package cc.karenia.hanayo.types;

/**
 * Represents a number in a Hocon document.
 * <p>
 * The number is parsed only on demand.
 * </p>
 */
public class HoconNumber implements IHoconElement {
  /** The string representation of this number */
  public String value;
  /** Can this number be parsed as an integer? */
  public boolean isInteger;

  /**
   * Initialize a new number.
   * 
   * @param originalString the string representation
   * @param isInteger      true if this number can be parsed as an integer
   */
  public HoconNumber(String originalString, boolean isInteger) {
    this.value = originalString;
    this.isInteger = isInteger;
  }

  /**
   * Parse this number as Long
   * 
   * @return the parsed number
   * @throws NumberFormatException thrown when this number cannot be parsed.
   */
  public long asLong() throws NumberFormatException {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Long.parseLong(value);
  }

  /**
   * Parse this number as Int
   * 
   * @return the parsed number
   * @throws NumberFormatException thrown when this number cannot be parsed.
   */
  public int asInt() throws NumberFormatException {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Integer.parseInt(value);
  }

  /**
   * Parse this number as Short
   * 
   * @return the parsed number
   * @throws NumberFormatException thrown when this number cannot be parsed.
   */
  public short asShort() throws NumberFormatException {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Short.parseShort(value);
  }

  /**
   * Parse this number as Byte
   * 
   * @return the parsed number
   * @throws NumberFormatException thrown when this number cannot be parsed.
   */
  public byte asByte() throws NumberFormatException {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Byte.parseByte(value);
  }

  /**
   * Parse this number as Double
   * 
   * @return the parsed number
   * @throws NumberFormatException thrown when this number cannot be parsed.
   */
  public double asDouble() {
    return Double.parseDouble(value);
  }

  /**
   * Parse this number as float
   * 
   * @return the parsed number
   * @throws NumberFormatException thrown when this number cannot be parsed.
   */
  public float asFloat() {
    return Float.parseFloat(value);
  }

  @Override
  public HoconType getType() {
    return HoconType.Number;
  }

  @Override
  public String asString() {
    return value;
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    switch (newElement.getType()) {
    case String:
    case Number:
    case Boolean:
    case Duration:
    case Size:
    case Period:
      return new HoconString(this.value + newElement.asString(), false, false);

    case Map:
    case List:
      return newElement;

    case NullSubstitution:
      return this;

    default:
      throw new IllegalArgumentException(String.format(
          "Cannot concat %s with %s", this.getType(), newElement.getType()));
    }
  }

  @Override
  public String toString() {
    return this.toString(0, 2);
  }

  @Override
  public String toString(int baseIndent, int indent) {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HoconNumber))
      return false;
    return this.value.equals(((HoconNumber) obj).value);
  }

  @Override
  public HoconNumber clone() {
    return new HoconNumber(value, isInteger);
  }
}
