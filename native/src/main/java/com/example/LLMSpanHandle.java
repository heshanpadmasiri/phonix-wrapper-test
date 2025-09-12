package com.example;

import org.graalvm.polyglot.Context;

public class LLMSpanHandle extends SpanHandle {
  private final String modelName;
  private final String providerName;

  public LLMSpanHandle(int id, String name, String modelName, String providerName) {
    super(id, name);
    this.modelName = modelName;
    this.providerName = providerName;
  }

  public LLMSpanHandle(String name, String modelName, String providerName) {
    super(name);
    this.modelName = modelName;
    this.providerName = providerName;
  }

  @Override
  public void init(Context cx, String traceVar) {
    String code = """
        from opentelemetry import trace
        from openinference.semconv.trace import SpanAttributes
        %s = %s.start_as_current_span(
                "%s",
            attributes={
                SpanAttributes.OPENINFERENCE_SPAN_KIND: "LLM",
                SpanAttributes.LLM_MODEL_NAME: "%s",
                SpanAttributes.LLM_PROVIDER: "%s"
            }
        )
        """.formatted(spanContextVar(), traceVar, name, modelName, providerName);
    cx.eval("python", code);
  }

  @Override
  public void setInput(Context cx, String input) {
    super.setInput(cx, input);
    setAttribute(cx, "llm.input_messages.0.message.role", "user");
    setAttribute(cx, "llm.input_messages.0.message.content", input);
  }

  @Override
  public void setOutput(Context cx, String output) {
    super.setOutput(cx, output);
    setAttribute(cx, "llm.output_messages.0.message.role", "assistant");
    setAttribute(cx, "llm.output_messages.0.message.content", output);
  }
}
