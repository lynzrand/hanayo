package cc.karenia.hanayo.types;

/**
 * Represents a boolean value in a Hocon document.
 */
public class HoconBoolean implements IHoconElement {
  /**
   * Initialize a boolean value.
   * 
   * @param value
   */
  public HoconBoolean(boolean value) {
    this.value = value;
  }

  /** The boolean value this object holds. */
  public boolean value;

  @Override
  public HoconType getType() {
    return HoconType.Boolean;
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
  public String asString() {
    if (this.value)
      return "true";
    else
      return "false";
  }

  @Override
  public String toString(int baseIndent, int indent) {
    return String.valueOf(this.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HoconBoolean))
      return false;
    return ((HoconBoolean) obj).value == this.value;
  }

}
