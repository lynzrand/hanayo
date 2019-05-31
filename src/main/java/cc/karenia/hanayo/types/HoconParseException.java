package cc.karenia.hanayo.types;

public class HoconParseException extends Exception {

  private static final long serialVersionUID = -4460218072445886387L;
  public HoconKey path;
  public int ptr;
  public String message;

  public HoconParseException(String message, int ptr, HoconKey path) {
    this.message = message;
    this.ptr = ptr;
    this.path = path;
  }

  public HoconParseException(String message, int ptr) {
    this.message = message;
    this.ptr = ptr;
  }

  public HoconParseException(int ptr, HoconKey path) {
    this.message = null;
    this.ptr = ptr;
    this.path = path;
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
