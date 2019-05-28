package cc.karenia.hanayo.types;

public class HoconNumber implements IHoconElement {

  @Override
  public HoconType getType() {
    return HoconType.Number;
  }

  @Override
  public String getOriginalString() {
    return null;
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    return null;
  }
}
