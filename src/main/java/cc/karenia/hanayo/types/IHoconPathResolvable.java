package cc.karenia.hanayo.types;

public interface IHoconPathResolvable extends IHoconElement {
  IHoconElement get(String key);

  IHoconElement getPath(String path);

  IHoconElement getPath(HoconKey path);

  void setOrReplace(HoconKey path, IHoconElement element);

}
