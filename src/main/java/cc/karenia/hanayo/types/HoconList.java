package cc.karenia.hanayo.types;

import java.util.*;

import cc.karenia.hanayo.HoconParser;

public class HoconList extends ArrayList<IHoconElement>
    implements IHoconPathResolvable {

  @Override
  public HoconType getType() {
    return HoconType.List;
  }

  @Override
  public String asString() {
    throw new NoSuchMethodError("HoconList cannot be represented as String");
  }

  @Override
  public IHoconElement concat(IHoconElement newElement) {
    if (newElement instanceof HoconList) {
      var list = (HoconList) newElement;
      this.addAll(list);
      return this;
    } else {
      return newElement;
    }
  }

  @Override
  public IHoconElement get(String key) {
    return this.get(Integer.parseInt(key));
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
  public void setOrReplace(HoconKey key, IHoconElement el) {
    var index = Integer.parseInt(key.name);
    if (key.next != null) {
      var val = this.get(index);
      if (val == null || !(val instanceof HoconMap))
        this.set(index, el);
      ((HoconMap) val).setOrReplace(key.next, el);
    } else {
      this.set(index, el);
    }
  }

  private static final long serialVersionUID = 6163610075084687893L;

  @Override
  public String toString(int baseIndent, int indent) {
    var sb = new StringBuilder();
    sb.append('[');
    sb.append('\n');

    forEach((val) -> {
      sb.append(String.join("", Collections.nCopies(baseIndent + indent, " ")));
      sb.append(val.toString(baseIndent + indent, indent));
      sb.append(",\n");
    });

    sb.append(String.join("", Collections.nCopies(baseIndent, " ")));
    sb.append(']');

    return sb.toString();
  }

}
