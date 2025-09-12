package com.example;

import org.graalvm.polyglot.Context;

public record SpanHandle(int id) {
    private static int nextSpanHandleId = 0;
    public SpanHandle() {
        this(nextSpanHandleId++);
    }
    public String spanContextVar() {
        return "span_context_" + id;
    }

    public String spanVar() {
        return "span_" + id;
    }

    public void init(Context cx, String traceVar, Kind kind) {
        String code = """
            from opentelemetry import trace
            %s = %s.start_as_current_span(
                "%s",
                kind=%s,
            )
            """.formatted(spanContextVar(), traceVar, kind.toString(), kind.kind());
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
    }

    public void setOutput(Context cx, String output) {
        setAttribute(cx, "output.value", output);
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
                case LLM -> "trace.SpanKind.CLIENT";
                case TOOL -> "trace.SpanKind.INTERNAL";
                case AGENT -> "trace.SpanKind.INTERNAL";
            };
        }
    }

}
