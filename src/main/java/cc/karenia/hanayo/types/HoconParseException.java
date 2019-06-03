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
  public String message;

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
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    if (shouldGatherStacktrace)
      super.fillInStackTrace();
    return this;
  }

  @Override
  public String getMessage() {
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
}
