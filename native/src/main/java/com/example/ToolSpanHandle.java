package com.example;

import org.graalvm.polyglot.Context;

public class ToolSpanHandle extends SpanHandle {

  public ToolSpanHandle(int id, String name) {
    super(id, name);
  }

  public ToolSpanHandle(String name) {
    super(name);
  }

  @Override
  public void init(Context cx, String traceVar) {
    String code = """
        from opentelemetry import trace
        from openinference.semconv.trace import SpanAttributes
        %s = %s.start_as_current_span(
                "%s",
            attributes={SpanAttributes.OPENINFERENCE_SPAN_KIND: "TOOL"}
        )
        """.formatted(spanContextVar(), traceVar, name);
    cx.eval("python", code);
  }
}