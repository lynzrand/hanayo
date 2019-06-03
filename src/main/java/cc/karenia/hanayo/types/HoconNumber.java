package cc.karenia.hanayo.types;

public class HoconNumber implements IHoconElement {
  public String value;
  public boolean isInteger;

  public HoconNumber(String originalString, boolean isInteger) {
    this.value = originalString;
    this.isInteger = isInteger;
  }

  public long asLong() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Long.parseLong(value);
  }

  public int asInt() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Integer.parseInt(value);
  }

  public short asShort() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Short.parseShort(value);
  }

  public byte asByte() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Byte.parseByte(value);
  }

  public double asDouble() {
    return Double.parseDouble(value);
  }

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
}
