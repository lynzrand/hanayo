package cc.karenia.hanayo.types;

public class HoconSubstitution implements IHoconElement {
  public HoconKey path;
  public boolean isDetermined;
  public IHoconPathResolvable rootElement;

  public HoconSubstitution(HoconKey path, boolean isDetermined) {
    this.path = path;
    this.isDetermined = isDetermined;
    this.rootElement = null;
  }

  public HoconSubstitution(HoconKey path, boolean isDetermined, IHoconPathResolvable rootElement) {
    this.path = path;
    this.isDetermined = isDetermined;
    this.rootElement = rootElement;
  }

  public IHoconElement resolve() {
    if (rootElement == null)
      throw new NullPointerException("Cannot resolve substitution: root element is null.");
    if (isDetermined)
      return rootElement.getPath(path);
    else
      try {
        return rootElement.getPath(path);
      } catch (Exception e) {
        return null;
      }
  }

  @Override
  public HoconType getType() {
    return HoconType.Substitution;
  }

  @Override
  public String asString() {
    return null;
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    return this.resolve();
  }

}
