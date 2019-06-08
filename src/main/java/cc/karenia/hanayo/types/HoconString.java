package cc.karenia.hanayo.types;

/**
 * Represents a string in a Hocon document.
 */
public class HoconString implements IHoconElement {
  /** The underlying string value. */
  public String value;
  /** Is this string quoted? */
  public boolean isQuoted;
  /** Is this string triple quoted (multiline)? */
  public boolean isMultiline;
  /** Is this string the result of a concatenation? */
  public boolean isConcatResult;

  /**
   * Initialize a quoted string
   * 
   * @param value the string value
   */
  public HoconString(String value) {
    this.value = value;
    this.isQuoted = true;
    this.isMultiline = false;
    this.isConcatResult = false;
  }

  /**
   * Initialize a string.
   * 
   * @param value       the underlying string value
   * @param isQuoted    is this string quoted?
   * @param isMultiline is this string multiline?
   */
  public HoconString(String value, boolean isQuoted, boolean isMultiline) {
    this.value = value;
    this.isQuoted = isQuoted;
    this.isMultiline = isMultiline;
    this.isConcatResult = false;
  }

  /**
   * Initialize a string.
   * 
   * @param value          the underlying string value
   * @param isQuoted       is this string quoted?
   * @param isMultiline    is this string multiline?
   * @param isConcatResult is this string the result of a concatenation?
   */
  public HoconString(String value, boolean isQuoted, boolean isMultiline,
      boolean isConcatResult) {
    this.value = value;
    this.isQuoted = isQuoted;
    this.isMultiline = isMultiline;
    this.isConcatResult = isConcatResult;
  }

  @Override
  public HoconType getType() {
    return HoconType.String;
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
      this.value = this.value + newElement.asString();
      this.isConcatResult = true;
      return this;

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

  /**
   * Transform this string into other formats if possible.
   * <p>
   * Other formats include: booleans.
   * </p>
   * 
   * @return the transformed object, or the string unchanged if not applicable
   */
  public IHoconElement transformIfPossible() {
    if (!this.isQuoted && !this.isMultiline) {
      if (value.equals("true"))
        return new HoconBoolean(true);
      else if (value.equals("false"))
        return new HoconBoolean(false);
    }

    return this;
  }

  @Override
  public String asString() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.toString(0, 2);
  }

  @Override
  public String toString(int baseIndent, int indent) {
    if (this.isMultiline)
      return new StringBuilder().append("\"\"\"").append(this.value)
          .append("\"\"\"").toString();
    else if (this.isQuoted)
      return new StringBuilder().append("\"").append(this.value).append("\"")
          .toString();
    else
      return new StringBuilder().append("\"").append(this.value)
          .append("\" <unquoted>").toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof HoconString))
      return false;
    var other = (HoconString) obj;
    return ((this.isMultiline == other.isMultiline)
        && (this.isQuoted == other.isQuoted)) && this.value.equals(other.value);
  }

  @Override
  public HoconString clone() {
    return new HoconString(value, isQuoted, isMultiline);
  }
}
