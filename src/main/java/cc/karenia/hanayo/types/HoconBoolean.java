package cc.karenia.hanayo.types;

public class HoconBoolean implements IHoconElement {
  public HoconBoolean(boolean value) {
    this.value = value;
  }

  public boolean value;

  @Override
  public HoconType getType() {
    return HoconType.Boolean;
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    return null;
  }

  @Override
  public String asString() {
    if (this.value)
      return "true";
    else
      return "false";
  }

}
