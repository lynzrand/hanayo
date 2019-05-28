package cc.karenia.hanayo.types;

import java.util.*;

public class HoconMap extends HashMap<String, IHoconElement> implements IHoconElement {
  private static final long serialVersionUID = 1L;

  @Override
  public HoconType getType() {
    return HoconType.Map;
  }

  public void setOrCreatePath(HoconKey key, IHoconElement el) {
    if (key.next != null) {
      var val = this.get(key.name);
      if (val == null || !(val instanceof HoconMap))
        this.put(key.name, val = new HoconMap());
      ((HoconMap) val).setOrCreatePath(key.next, el);
    } else {
      this.put(key.name, el);
    }
  }

  @Override
  public String getOriginalString() {
    return null;
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

}
