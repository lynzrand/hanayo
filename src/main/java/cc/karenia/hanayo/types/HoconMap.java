package cc.karenia.hanayo.types;

import java.util.*;

import cc.karenia.hanayo.HoconParser;

/**
 * Represents a Map in a Hocon document.
 */
public class HoconMap extends HashMap<String, IHoconElement>
    implements IHoconElement, IHoconPathResolvable {
  private static final long serialVersionUID = 1L;

  public HoconMap() {
    super();
  }

  public HoconMap(Map<String, ? extends IHoconElement> elements) {
    super(elements);
  }

  @Override
  public HoconType getType() {
    return HoconType.Map;
  }

  @Override
  public void setOrReplace(HoconKey key, IHoconElement el) {
    if (key.next != null) {
      var val = this.get(key.name);
      if (key.name.matches("\\d+")
          && (val == null || !(val instanceof IHoconPathResolvable))) {
        this.put(key.name, val = new HoconList());
      } else if (val == null) {
        this.put(key.name, val = new HoconMap());
      }
      ((IHoconPathResolvable) val).setOrReplace(key.next, el);
    } else {
      this.put(key.name, el);
    }
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    if (newElement instanceof HoconSubstitution.NullSubstitution) {
      return this;
    } else if (newElement instanceof HoconMap) {
      var map = (HoconMap) newElement;
      for (var kvp : map.entrySet()) {
        String key = kvp.getKey();
        if (this.containsKey(key)) {
          IHoconElement value = kvp.getValue();
          // Concatenating nested maps is an undefined behavior in the
          // official document. Merging them seems to be the best choice for
          // now.
          if (value instanceof HoconMap)
            this.put(key, this.get(key).concat(value));
          else
            this.put(key, value);
        } else {
          this.put(key, kvp.getValue());
        }
      }
      return this;
    } else {
      return newElement;
    }
  }

  @Override
  public IHoconElement get(String key) {
    return this.get((Object) key);
  }

  @Override
  public IHoconElement getPath(String path) throws HoconParseException {
    var key = HoconParser.of(path).parseKey(0).unwrap();
    return this.getPath(key);
  }

  @Override
  public IHoconElement getPath(HoconKey path) {
    if (path.next == null)
      return this.get(path.name);
    else {
      var val = this.get(path.name);
      if (val instanceof IHoconPathResolvable) {
        return ((IHoconPathResolvable) val).getPath(path.next);
      } else
        throw new NullPointerException(
            "Path \"" + path.root.path() + "\" does not exist.");
    }
  }

  @Override
  public String asString() {
    throw new NoSuchMethodError("HoconMap cannot be represented as String");
  }

  @Override
  public String toString() {
    return this.toString(0, 2);
  }

  @Override
  public String toString(int baseIndent, int indent) {
    var sb = new StringBuilder();
    sb.append('{');
    sb.append('\n');
    forEach((key, val) -> {
      sb.append(String.join("", Collections.nCopies(baseIndent + indent, " ")));
      sb.append('"');
      sb.append(key);
      sb.append("\": ");
      sb.append(val.toString(baseIndent + indent, indent));
      sb.append(",\n");
    });
    sb.append(String.join("", Collections.nCopies(baseIndent, " ")));
    sb.append('}');

    return sb.toString();
  }

  @Override
  public HoconMap clone() {
    return new HoconMap(this);
  }

}
