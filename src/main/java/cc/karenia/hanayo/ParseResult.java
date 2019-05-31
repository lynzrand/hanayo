package cc.karenia.hanayo;

import java.text.ParseException;

public class ParseResult<T> {
  public boolean parseSuccess;
  public int newPtr;
  public T result;
  public Throwable exception;

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

  public ParseResult(int newPtr, Throwable failException) {
    this.parseSuccess = false;
    this.newPtr = newPtr;
    this.result = null;
    this.exception = failException;
  }

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

  public T unwrapThrow() throws Throwable {
    if (this.parseSuccess) {
      return result;
    } else {
      throwIfPossible();
      return null;
    }
  }

  public T unwrapSilent() {
    if (this.parseSuccess)
      return result;
    else
      return null;
  }

  public ParseResult<T> throwIfPossible() throws Throwable {
    if (this.exception != null) {
      throw this.exception;
    } else {
      throw new ParseException("Parse failed", newPtr);
    }
  }

  public static <T> ParseResult<T> fail(int ptr) {
    return new ParseResult<T>(false, ptr);
  }

  public static <T> ParseResult<T> fail(int ptr, Throwable exception) {
    return new ParseResult<T>(ptr, exception);
  }

  public static <T> ParseResult<T> success(int ptr) {
    return new ParseResult<T>(true, ptr);
  }

  public static <T> ParseResult<T> success(int ptr, T result) {
    return new ParseResult<T>(ptr, result);
  }
}
