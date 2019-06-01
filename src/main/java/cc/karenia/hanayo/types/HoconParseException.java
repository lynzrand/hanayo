package cc.karenia.hanayo.types;

public class HoconParseException extends Exception {

  private static final long serialVersionUID = -4460218072445886387L;
  public HoconKey path;
  public int ptr;
  public String message;
  public static boolean shouldGatherStacktrace = false;

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
    this.path = path.clone();
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    if (shouldGatherStacktrace)
      super.fillInStackTrace();
    return this;
  }

  @Override
  public String toString() {
    if (!this.shouldGatherStacktrace) {
      if (path == null)
        return String.format("HoconParseException: %s; Occurred at pointer %d",
            message, ptr);
      else
        return String.format(
            "HoconParseException: %s; Occurred at pointer %d, path %s", message,
            ptr, path.path());
    } else {
      StringBuilder sb = new StringBuilder();
      if (path == null)
        sb.append(String.format(
            "HoconParseException: %s; Occurred at pointer %d.\n\nStacktrace:\n",
            message, ptr));
      else
        sb.append(String.format(
            "HoconParseException: %s; Occurred at pointer %d, path %s.\n\nStacktract:\n",
            message, ptr, path.path()));
      for (var st : this.getStackTrace()) {
        sb.append(st.toString());
        sb.append('\n');
      }
      return sb.toString();
    }
  }
}
