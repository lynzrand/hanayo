package cc.karenia.hanayo.types;

public class HoconString implements IHoconElement {
  public String value;
  public boolean isQuoted;
  public boolean isMultiline;
  public boolean isConcatResult;

  public HoconString(String value) {
    this.value = value;
    this.isQuoted = true;
    this.isMultiline = false;
    this.isConcatResult = false;
  }

  public HoconString(String value, boolean isQuoted, boolean isMultiline) {
    this.value = value;
    this.isQuoted = isQuoted;
    this.isMultiline = isMultiline;
    this.isConcatResult = false;
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
    default:
      throw new IllegalArgumentException(String.format(
          "Cannot concat %s with %s", this.getType(), newElement.getType()));
    }
  }

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

}
