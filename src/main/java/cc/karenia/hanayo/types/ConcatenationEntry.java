package cc.karenia.hanayo.types;

public class ConcatenationEntry {
  public HoconKey tgt;
  public IHoconElement element;

  public void resolve(IHoconPathResolvable root) {
    var orig = root.getPath(tgt);
    if (element instanceof HoconSubstitution)
      element = ((HoconSubstitution) element).resolve(root);
    var aft = orig.concat(element);
    root.setOrReplace(tgt, aft);
  }
}
