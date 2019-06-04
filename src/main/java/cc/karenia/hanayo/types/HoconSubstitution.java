package cc.karenia.hanayo.types;

/**
 * Represents a substitution element in a Hocon Document.
 * 
 * <p>
 * This class should not appear in the final parse result. All substitutions
 * must be either replaced or thrown before handling out the result.
 * </p>
 */
public class HoconSubstitution implements IHoconElement {
  /** The path this substitution is targeting at */
  public HoconKey path;
  /**
   * Whether this substitution is a determined substitution. Non-determined
   * substitutions are replaced with null substitutions when resolved, while
   * determined ones throw an error.
   */
  public boolean isDetermined;

  /**
   * Create a new substitution with given path and determination
   * 
   * @param path         the path this substitution targets at
   * @param isDetermined is this substitution determined?
   */
  public HoconSubstitution(HoconKey path, boolean isDetermined) {
    this.path = path;
    this.isDetermined = isDetermined;
  }

  @Deprecated
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

  public static class NullSubstitution implements IHoconElement {

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
