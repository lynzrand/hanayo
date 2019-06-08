package cc.karenia.hanayo.types;

/**
 * Represents an error the parser meets when parsing a file.
 */
public class HoconParseException extends Exception {

  private static final long serialVersionUID = -4460218072445886387L;

  /** The path when the error occurred */
  public HoconKey path;
  /** The offset in string when the error orccurred */
  public int ptr;
  /** The message of this error */
  private String message;

  private Object[] params;

  private boolean isFormatted = false;

  /**
   * Should we gather stacktraces when throwing this exception? Defaults to
   * false to improve performance.
   */
  public static boolean shouldGatherStacktrace = false;

  /**
   * Initialize a new exception.
   * 
   * @param message The message to leaveoccurred
   * @param ptr     the offset when the error occurred
   * @param path    the path in document when the error occurred
   */
  public HoconParseException(String message, int ptr, HoconKey path) {
    this.message = message;
    this.ptr = ptr;
    this.path = path;
    this.isFormatted = true;
  }

  /**
   * Initialize a new exception.
   * 
   * @param message The message to leaveoccurred
   * @param ptr     the offset when the error occurred
   */
  public HoconParseException(String message, int ptr) {
    this.message = message;
    this.ptr = ptr;
    this.isFormatted = true;
  }

  /**
   * Initialize a new exception with lazy formatting.
   * 
   * @param message The message to leaveoccurred
   * @param ptr     the offset when the error occurred
   * @param path    the path in document when the error occurred
   * @param params  string format arguments
   */
  public HoconParseException(String message, int ptr, HoconKey path,
      Object... params) {
    this.message = message;
    this.ptr = ptr;
    this.path = path;
    this.params = params;
    this.isFormatted = false;
  }

  /**
   * Initialize a new exception.
   * 
   * @param message The message to leaveoccurred
   * @param ptr     the offset when the error occurred
   * @param params  string format arguments
   */
  public HoconParseException(String message, int ptr, Object... params) {
    this.message = message;
    this.ptr = ptr;
    this.params = params;
    this.isFormatted = false;
  }

  /**
   * Initialize a new exception.
   * 
   * @param ptr  the offset when the error occurred
   * @param path the path in document when the error occurred
   */
  public HoconParseException(int ptr, HoconKey path) {
    this.message = null;
    this.ptr = ptr;
    this.path = path.clone();
    this.isFormatted = true;
  }

  void lazyFormat() {
    if (!this.isFormatted) {
      this.message = String.format(message, params);
      this.isFormatted = true;
    }
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    if (shouldGatherStacktrace)
      super.fillInStackTrace();
    return this;
  }

  @Override
  public String getMessage() {
    lazyFormat();
    if (path == null)
      return String.format("HoconParseException: %s; Occurred at pointer %d",
          message, ptr);
    else
      return String.format(
          "HoconParseException: %s; Occurred at pointer %d, path %s", message,
          ptr, path.path());

  }

  @Override
  public String toString() {
    var sb = new StringBuilder(this.getMessage());
    sb.append('\n');
    if (shouldGatherStacktrace) {
      for (var st : this.getStackTrace()) {
        sb.append(st.toString());
        sb.append('\n');
      }
    }
    return sb.toString();
  }

  public static class BlankString extends HoconParseException {
    private static final long serialVersionUID = 1L;

    public BlankString(int ptr) {
      super("Blank string", ptr);
    }
  }

  public static class FoundComment extends HoconParseException {
    private static final long serialVersionUID = 1L;

    public FoundComment(int ptr) {
      super("Found comment", ptr);
    }
  }
}
