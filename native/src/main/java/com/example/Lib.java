package com.example;

import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.polyglot.Context;

public class Lib {

  public static int nextInstrumentingHandleId = 0;
  public static final String TRACER = "tracer";

  public static Context getContext() {
    return GraalPyResources.createContext();
  }

  public static void closeContext(Context cx) {
    cx.close();
  }

  public static InstrumentingHandle createInstrumentingHandle(Context cx) {
    int id = nextInstrumentingHandleId++;
    var handle = new InstrumentingHandle(id);
    String fnName = handle.fnName();
    String handleValue = handle.handleValue();
    cx.eval("python",
        """
            %s = ""
            @%s.chain
            def %s(input: str) -> str:
              return %s
            """.formatted(handleValue, TRACER, fnName, handleValue));
    return handle;
  }

  public static SpanHandle createSpanHandle(Context cx, String name, String kind) {
    SpanHandle.Kind spanKind = switch (kind.toLowerCase()) {
      case "llm" -> SpanHandle.Kind.LLM;
      case "tool" -> SpanHandle.Kind.TOOL;
      case "agent" -> SpanHandle.Kind.AGENT;
      default -> throw new IllegalArgumentException("Unknown kind: " + kind);
    };
    SpanHandle handle = new SpanHandle(name, spanKind);
    handle.init(cx, TRACER);
    return handle;
  }

  public static void setSpanAttribute(Context cx, SpanHandle handle, String key, String value) {
    handle.setAttribute(cx, key, value);
  }

  public static void enterSpanHandle(Context cx, SpanHandle handle) {
    handle.enter(cx);
  }

  public static void setSpanHandleInput(Context cx, SpanHandle handle, String input) {
    handle.setInput(cx, input);
  }

  public static void setSpanHandleOutput(Context cx, SpanHandle handle, String output) {
    handle.setOutput(cx, output);
  }

  public static void setSpanHandleStatus(Context cx, SpanHandle handle, String status) {
    handle.setStatus(cx, SpanHandle.Status.from(status));
  }

  public static void exitSpanHandle(Context cx, SpanHandle handle) {
    handle.exit(cx);
  }

  public static void execInstrumentingHandle(Context cx, InstrumentingHandle handle, String input, String output) {
    handle.exec(cx, input, output);
  }

  public static Object setupTracer(Context cx, String projectName, String endpoint) {
    cx.eval("python",
        """
            import os
            os.environ["PHOENIX_COLLECTOR_ENDPOINT"] = "http://localhost:6006/v1/traces"
            """);
    return cx.eval("python",
        """
            from phoenix.otel import register
            tracer_provider = register(
              project_name="%s", # Default is 'default'
              auto_instrument=True, # See 'Trace all calls made to a library' below
              endpoint="%s",
            )
            %s = tracer_provider.get_tracer(__name__)
            """.formatted(projectName, endpoint, TRACER));
  }

  public static void execLLMCall(Context cx) {
    cx.eval("python",
        """
            import os
            os.environ["OPENAI_API_KEY"] = "INVALID_KEY"
            """);
    cx.eval("python",
        """
            import openai
            client = openai.OpenAI()
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=[{"role": "user", "content": "Hi how are you?"}],
            )
            print(response.choices[0].message.content)
            """);
  }

  public static void execTraceAgent(Context cx) {
    String code = """
        import os
        from opentelemetry import trace
        from opentelemetry.sdk.trace import TracerProvider
        from opentelemetry.sdk.trace.export import BatchSpanProcessor, ConsoleSpanExporter
        from opentelemetry.semconv._incubating.attributes.gen_ai_attributes import (
            GEN_AI_PROVIDER_NAME,
            GEN_AI_REQUEST_MODEL,
            GEN_AI_USAGE_INPUT_TOKENS,
            GEN_AI_USAGE_OUTPUT_TOKENS,
            GEN_AI_TOOL_NAME,
            GEN_AI_AGENT_NAME
        )


        def chat(prompt: str) -> str:
            span_context = tracer.start_as_current_span(
                "llm",
                attributes={
                    GEN_AI_PROVIDER_NAME: "openai",  # e.g., the provider
                    GEN_AI_REQUEST_MODEL: "gpt-4",   # specify your model
                    GEN_AI_USAGE_INPUT_TOKENS: 100, # optional: token count
                    GEN_AI_USAGE_OUTPUT_TOKENS: 50, # optional: token count
                },
                kind=trace.SpanKind.CLIENT,  # Use CLIENT span kind for LLM calls
            )
            span = span_context.__enter__()
            try:
                # Set input value (required for OpenInference LLM span).
                span.set_attribute("input.value", prompt)

                # Simulate the actual LLM call here (replace with your real API call).
                # For example: response = openai.ChatCompletion.create(...)
                response = f"LLM response to: {prompt}"

                # Set output value (required for OpenInference LLM span).
                span.set_attribute("output.value", response)

                # Optional: Add events for intermediate steps if needed.
                # span.add_event("generation.completed", attributes={"reason": "done"})

                return response
            finally:
                span_context.__exit__(None, None, None)

        def tool(input_str: str) -> str:
            span_context = tracer.start_as_current_span(
                "tool",
                attributes={
                    GEN_AI_TOOL_NAME: "example_tool",  # name of the tool
                },
                kind=trace.SpanKind.INTERNAL,  # Tools are typically INTERNAL
            )
            span = span_context.__enter__()
            try:
                # Set input value (required for OpenInference TOOL span).
                span.set_attribute("input.value", input_str)

                # Simulate the actual tool execution here (replace with your logic).
                # For example: result = some_function(input_str)
                result = f"Tool result for: {input_str}"

                # Set output value (required for OpenInference TOOL span).
                span.set_attribute("output.value", result)

                return result
            finally:
                span_context.__exit__(None, None, None)

        def agent_flow(initial_prompt: str) -> str:
            span_context = tracer.start_as_current_span(
                "agent",
                attributes={
                    GEN_AI_AGENT_NAME: "simple_agent",
                },
                kind=trace.SpanKind.INTERNAL,
            )
            span = span_context.__enter__()
            try:
                # Set initial input for the agent.
                span.set_attribute("input.value", initial_prompt)

                # Simulate agent loop: chat -> decide tool -> tool -> chat again.
                current_state = initial_prompt

                # First chat call.
                current_state = chat(current_state)

                # Simulate agent decides to call a tool based on chat output.
                tool_input = "extract info from: " + current_state  # In real agent, parse from chat response.
                current_state = tool(tool_input)

                # Second chat call with tool result.
                final_response = chat(current_state)

                # Set overall output for the agent span.
                span.set_attribute("output.value", final_response)

                return final_response
            finally:
                span_context.__exit__(None, None, None)

        agent_flow("Hi how are you?")
              """;
    cx.eval("python", code);
  }
}
