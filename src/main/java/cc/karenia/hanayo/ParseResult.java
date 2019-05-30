package cc.karenia.hanayo;

import cc.karenia.hanayo.types.IHoconElement;

class ParseResult<T> {
  boolean parseSuccess;
  int newPtr;
  T result;

  public ParseResult() {
  }

  public ParseResult(int newPtr, T result) {
    this.newPtr = newPtr;
    this.result = result;
    this.parseSuccess = true;
  }

  public ParseResult(boolean success, int newPtr) {
    this.parseSuccess = success;
    this.newPtr = newPtr;
  }

  public static <T> ParseResult<T> fail(int ptr) {
    return new ParseResult<T>(false, ptr);
  }

  public static <T> ParseResult<T> success(int ptr) {
    return new ParseResult<T>(true, ptr);
  }

  public static <T> ParseResult<T> Success(int ptr, T result) {
    return new ParseResult<T>(ptr, result);
  }
}
