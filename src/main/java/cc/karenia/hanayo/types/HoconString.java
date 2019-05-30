package cc.karenia.hanayo.types;

public class HoconString implements IHoconElement {

  public String value;
  public boolean isQuoted;
  public boolean isMultiline;

  @Override
  public HoconType getType() {
    return HoconType.String;
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
