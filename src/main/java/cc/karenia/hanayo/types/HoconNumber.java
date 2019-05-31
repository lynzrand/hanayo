package cc.karenia.hanayo.types;

public class HoconNumber implements IHoconElement {
  public String originalString;
  public boolean isInteger;

  public HoconNumber(String originalString, boolean isInteger) {
    this.originalString = originalString;
    this.isInteger = isInteger;
  }

  public long asLong() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Long.parseLong(originalString);
  }

  public int asInt() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Integer.parseInt(originalString);
  }

  public short asShort() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Short.parseShort(originalString);
  }

  public byte asByte() {
    if (!isInteger)
      throw new NumberFormatException("This number is not an integer");
    return Byte.parseByte(originalString);
  }

  public double asDouble() {
    return Double.parseDouble(originalString);
  }

  public float asFloat() {
    return Float.parseFloat(originalString);
  }

  @Override
  public HoconType getType() {
    return HoconType.Number;
  }

  @Override
  public String asString() {
    return originalString;
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    return null;
  }
}
