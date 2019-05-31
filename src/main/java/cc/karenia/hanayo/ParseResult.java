package cc.karenia.hanayo;

import java.text.ParseException;

public class ParseResult<T> {
  public boolean parseSuccess;
  public int newPtr;
  public T result;
  public Exception exception;

  public ParseResult() {
  }

  public ParseResult(int newPtr, T result) {
    this.newPtr = newPtr;
    this.result = result;
    this.parseSuccess = true;
    this.exception = null;
  }

  public ParseResult(boolean success, int newPtr) {
    this.parseSuccess = success;
    this.newPtr = newPtr;
    this.result = null;
    this.exception = null;
  }

  public ParseResult(int newPtr, Exception failException) {
    this.parseSuccess = false;
    this.newPtr = newPtr;
    this.result = null;
    this.exception = failException;
  }

  public T unwrap() {
    if (this.parseSuccess) {
      return result;
    } else {
      throwIfPossible();
      return null;
    }
  }

  public T unwrapThrow() throws Exception {
    if (this.parseSuccess) {
      return result;
    } else {
      if (this.exception != null) {
        throw new Exception(this.exception);
      } else {
        throw new ParseException("Parse failed", newPtr);
      }
    }
  }

  public T unwrapSilent() {
    if (this.parseSuccess)
      return result;
    else
      return null;
  }

  public ParseResult<T> throwIfPossible() {
    if (this.exception != null) {
      throw new RuntimeException(this.exception);
    } else {
      throw new RuntimeException(new ParseException("Parse failed", newPtr));
    }
  }

  public static <T> ParseResult<T> fail(int ptr) {
    return new ParseResult<T>(false, ptr);
  }

  public static <T> ParseResult<T> fail(int ptr, Exception exception) {
    return new ParseResult<T>(ptr, exception);
  }

  public static <T> ParseResult<T> success(int ptr) {
    return new ParseResult<T>(true, ptr);
  }

  public static <T> ParseResult<T> success(int ptr, T result) {
    return new ParseResult<T>(ptr, result);
  }
}
