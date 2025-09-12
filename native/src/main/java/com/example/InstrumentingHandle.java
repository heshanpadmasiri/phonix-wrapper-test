package com.example;

import org.graalvm.polyglot.Context;

/**
 * InstrumentingHandle
 */
public record InstrumentingHandle(int id) {
  public String fnName() {
    return "__instrumenting_handle_%d__".formatted(id);
  }

  public String handleValue() {
    return "__instrumenting_handle_%d_value__".formatted(id);
  }

  public void exec(Context cx, String inputValue, String outputValue) {
    cx.eval("python",
        """
            %s = "%s"
            %s("%s")
            """.formatted(handleValue(), outputValue, fnName(), inputValue));
  }
}
