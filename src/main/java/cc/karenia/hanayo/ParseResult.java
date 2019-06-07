package cc.karenia.hanayo;

import java.text.ParseException;

/**
 * Represents the result of a parse action.
 * 
 * @param <T> type of the result of this parse
 */
public class ParseResult<T> {
  /** Is this parse successful? */
  @Deprecated
  public boolean parseSuccess;
  /** The offset number after parsing this segment of string */
  public int newPtr;
  /** The object result being parsed */
  public T result;

  /** The exception encountered in parsing */
  @Deprecated
  public Throwable exception;

  /**
   * Initialize an empty result.
   */
  public ParseResult() {
  }

  /**
   * Initialize a new successful result with an offset.
   * 
   * @param newPtr the new offset
   * @param result the result
   */
  public ParseResult(int newPtr, T result) {
    this.newPtr = newPtr;
    this.result = result;
    this.parseSuccess = true;
    this.exception = null;
  }

  /**
   * Initialize a new result with successfulness
   * 
   * @param success
   * @param newPtr
   */
  public ParseResult(boolean success, int newPtr) {
    this.parseSuccess = success;
    this.newPtr = newPtr;
    this.result = null;
    this.exception = null;
  }

  /**
   * Initialize a failed result with an exception
   * 
   * @param newPtr        the pointer to rollback to
   * @param failException the exception that would otherwise been thrown
   */
  @Deprecated
  public ParseResult(int newPtr, Throwable failException) {
    this.parseSuccess = false;
    this.newPtr = newPtr;
    this.result = null;
    this.exception = failException;
  }

  /**
   * Gets the result of this parse and ditches the offset. A runtime exception
   * will be thrown if any exception is avaliable.
   * 
   * @return the result
   */
  public T unwrap() {
    if (this.parseSuccess) {
      return result;
    } else {
      if (this.exception != null) {
        throw new RuntimeException(this.exception);
      } else {
        throw new RuntimeException(new ParseException("Parse failed", newPtr));
      }
    }
  }

  /**
   * Gets the result of this parse and ditches the offset. An exception will be
   * thrown if avaliable.
   * 
   * @throws Throwable the exception
   * @return the result
   */
  public T unwrapThrow() throws Throwable {
    if (this.parseSuccess) {
      return result;
    } else {
      throwIfPossible();
      return null;
    }
  }

  /**
   * Gets the result of this parse and ditches the offset. If the parse is
   * unsuccessful, return null.
   * 
   * @return the result
   */
  public T unwrapSilent() {
    if (this.parseSuccess)
      return result;
    else
      return null;
  }

  /**
   * Throws an exception if avaliable.
   * 
   * @throws Throwable the exception
   * @return itself
   */
  public ParseResult<T> throwIfPossible() throws Throwable {
    if (this.parseSuccess) {
      if (this.exception != null) {
        throw this.exception;
      } else {
        throw new ParseException("Parse failed", newPtr);
      }
    } else {
      return this;
    }
  }

  /**
   * Initialize a failed parse result
   * 
   * @param <T> result type
   * @param ptr the offset to rollback to
   * @return the parse result
   */
  public static <T> ParseResult<T> fail(int ptr) {
    return new ParseResult<T>(false, ptr);
  }

  /**
   * Initialize a failed parse result
   * 
   * @param <T>       result type
   * @param ptr       the offset to rollback to
   * @param exception the exception
   * @return the parse result
   */
  public static <T> ParseResult<T> fail(int ptr, Throwable exception) {
    return new ParseResult<T>(ptr, exception);
  }

  /**
   * Initialize a successful parse result
   * 
   * @param <T> result type
   * @param ptr the offset to advance to
   * @return the parse result
   */
  public static <T> ParseResult<T> success(int ptr) {
    return new ParseResult<T>(true, ptr);
  }

  /**
   * Initialize a successful parse result
   * 
   * @param <T> result type
   * @param ptr the offset to advance to
   * @param T   parse result
   * @return the parse result
   */
  public static <T> ParseResult<T> success(int ptr, T result) {
    return new ParseResult<T>(ptr, result);
  }
}
