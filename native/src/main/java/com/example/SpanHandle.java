package com.example;

import org.graalvm.polyglot.Context;

public record SpanHandle(int id, String name, Kind kind) {
  private static int nextSpanHandleId = 0;

  public SpanHandle(String name, Kind kind) {
    this(nextSpanHandleId++, name, kind);
  }

  public String spanContextVar() {
    return "span_context_" + id;
  }

  public String spanVar() {
    return "span_" + id;
  }

  public void init(Context cx, String traceVar) {
    String code = """
        from opentelemetry import trace
        from openinference.semconv.trace import SpanAttributes
        %s = %s.start_as_current_span(
                "%s",
            attributes={SpanAttributes.OPENINFERENCE_SPAN_KIND: "%s"}
        )
        """.formatted(spanContextVar(), traceVar, name, kind.kind(), kind.kind());
    cx.eval("python", code);
  }

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

  public enum Kind {
    LLM,
    TOOL,
    AGENT;

    public String toString() {
      return switch (this) {
        case LLM -> "llm";
        case TOOL -> "tool";
        case AGENT -> "agent";
      };
    }

    public String kind() {
      return switch (this) {
        case LLM -> "LLM";
        case TOOL -> "TOOL";
        case AGENT -> "AGENT";
      };
    }
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
