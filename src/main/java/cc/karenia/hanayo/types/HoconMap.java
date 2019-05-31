package cc.karenia.hanayo.types;

import java.util.*;

import cc.karenia.hanayo.HoconParser;

public class HoconMap extends HashMap<String, IHoconElement> implements IHoconElement, IHoconPathResolvable {
  private static final long serialVersionUID = 1L;

  @Override
  public HoconType getType() {
    return HoconType.Map;
  }

  @Override
  public void setOrReplace(HoconKey key, IHoconElement el) {
    if (key.next != null) {
      var val = this.get(key.name);
      if (val == null || !(val instanceof HoconMap))
        this.put(key.name, val = new HoconMap());
      ((HoconMap) val).setOrReplace(key.next, el);
    } else {
      this.put(key.name, el);
    }
  }

  public IHoconElement concat(IHoconElement newElement) {
    if (newElement instanceof HoconMap) {
      var map = (HoconMap) newElement;
      for (var kvp : map.entrySet()) {
        String key = kvp.getKey();
        if (this.containsKey(key)) {
          IHoconElement value = kvp.getValue();
          this.put(key, this.get(key).concat(value));
        } else {
          this.put(kvp.getKey(), kvp.getValue());
        }
      }
      return this;
    } else {
      return newElement;
    }
  }

  @Override
  public IHoconElement get(String key) {
    return this.get(key);
  }

  @Override
  public IHoconElement getPath(String path) {
    var key = HoconParser.parseKey(path.toCharArray(), 0).unwrap();
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
        throw new NullPointerException("Path \"" + path.root.path() + "\" does not exist.");
    }
  }

  @Override
  public String asString() {
    throw new NoSuchMethodError("HoconMap cannot be represented as String");
  }

}
