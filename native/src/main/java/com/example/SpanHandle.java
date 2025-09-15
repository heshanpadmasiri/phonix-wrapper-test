package com.example;

import org.graalvm.polyglot.Context;

public abstract class SpanHandle {
  private static int nextSpanHandleId = 0;

  protected final int id;
  protected final String name;

  public SpanHandle(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public SpanHandle(String name) {
    this(nextSpanHandleId++, name);
  }

  public String spanContextVar() {
    return "span_context_" + id;
  }

  public String spanVar() {
    return "span_" + id;
  }

  public abstract void init(Context cx, String traceVar);

  public void enter(Context cx) {
    String code = """
        %s = %s.__enter__()
        """.formatted(spanVar(), spanContextVar());
    cx.eval("python", code);
  }

  public void setAttribute(Context cx, String key, String value) {
    String code = """
        %s.set_attribute("%s", "%s")
        """.formatted(spanVar(), key, value);
    cx.eval("python", code);
  }

  public void setAttribute(Context cx, String key, int value) {
    String code = """
        %s.set_attribute("%s", %s)
        """.formatted(spanVar(), key, value);
    cx.eval("python", code);
  }

  public void setInput(Context cx, String input) {
    setAttribute(cx, "input.value", input);
    setAttribute(cx, "input.mime_type", "text/plain");
  }

  public void setOutput(Context cx, String output) {
    setAttribute(cx, "output.value", output);
    setAttribute(cx, "output.mime_type", "text/plain");
  }

  public void setStatus(Context cx, Status status) {
    String code = """
        from opentelemetry.trace import Status, StatusCode
        %s.set_status(Status(StatusCode.%s))
        """.formatted(spanVar(), status.toString());
    cx.eval("python", code);
  }

  public void exit(Context cx) {
    String code = """
        %s.__exit__(None, None, None)
        """.formatted(spanContextVar());
    cx.eval("python", code);
  }

  public enum Status {
    OK,
    ERROR;

    public String toString() {
      return switch (this) {
        case OK -> "OK";
        case ERROR -> "ERROR";
      };
    }

    public static Status from(String status) {
      return switch (status.toUpperCase()) {
        case "OK" -> OK;
        case "ERROR" -> ERROR;
        default -> throw new IllegalArgumentException("Unknown status: " + status);
      };
    }
  }
}
