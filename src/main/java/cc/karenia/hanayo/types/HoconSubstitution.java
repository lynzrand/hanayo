package cc.karenia.hanayo.types;

public class HoconSubstitution implements IHoconElement {
  public HoconKey path;
  public boolean isDetermined;

  public HoconSubstitution(HoconKey path, boolean isDetermined) {
    this.path = path;
    this.isDetermined = isDetermined;
  }

  public HoconSubstitution(HoconKey path, boolean isDetermined,
      IHoconPathResolvable rootElement) {
    this.path = path;
    this.isDetermined = isDetermined;
  }

  public IHoconElement resolve(IHoconPathResolvable rootElement) {
    if (rootElement == null)
      throw new NullPointerException(
          "Cannot resolve substitution: root element is null.");
    if (isDetermined)
      return rootElement.getPath(path);
    else
      try {
        return rootElement.getPath(path);
      } catch (Exception e) {
        return new NullSubstitution();
      }
  }

  @Override
  public HoconType getType() {
    return HoconType.Substitution;
  }

  @Override
  public String toString() {
    return String.format("[Hocon Substitution to %s]", this.path.path());
  }

  @Override
  public String asString() {
    return null;
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    throw new RuntimeException(String.format(
        "Resolve this substitution (%s) before concatenation.", path.path()));
  }

  @Override
  public String toString(int baseIndent, int indent) {
    return null;
  }

  public class NullSubstitution implements IHoconElement {

    @Override
    public HoconType getType() {
      return HoconType.NullSubstitution;
    }

    @Override
    public String asString() {
      return null;
    }

    @Override
    public IHoconElement concat(IHoconElement newElement) {
      return newElement;
    }

    @Override
    public String toString(int baseIndent, int indent) {
      return "[Null Substitution]";
    }

  }

}
