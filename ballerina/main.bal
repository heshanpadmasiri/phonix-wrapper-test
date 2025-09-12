import ballerina/jballerina.java;

public function main() returns error? {
    handle cx = getContext();
    _ = setupTracer(cx, java:fromString("my-bal-llm"), java:fromString("http://localhost:6006/v1/traces"));
    execAgent(cx);
    closeContext(cx);
}

class Span {
    private final handle cx;
    private final handle spanHandle;
    private final string name;

    public isolated function init(handle cx, handle spanHandle, string name) {
        self.cx = cx;
        self.spanHandle = spanHandle;
        self.name = name;
    }

    public function enter() {
        enterSpanHandle(self.cx, self.spanHandle);
    }

    public function setInput(string input) {
        setSpanHandleInput(self.cx, self.spanHandle, java:fromString(input));
    }

    public function setOutput(string output) {
        setSpanHandleOutput(self.cx, self.spanHandle, java:fromString(output));
    }

    public function setStatus(string status) {
        setSpanHandleStatus(self.cx, self.spanHandle, java:fromString(status));
    }

    public function exit() {
        exitSpanHandle(self.cx, self.spanHandle);
    }

    public function setAttribute(string key, string value) {
        setSpanAttribute(self.cx, self.spanHandle, java:fromString(key), java:fromString(value));
    }
}

isolated function createAgentSpan(handle cx, string name) returns Span {
    handle spanHandle = createAgentSpanHandle(cx, java:fromString(name));
    return new (cx, spanHandle, name);
}

isolated function createLLMSpan(handle cx, string name, string modelName, string providerName) returns Span {
    handle spanHandle = createLLMSpanHandle(cx, java:fromString(name), java:fromString(modelName), java:fromString(providerName));
    return new (cx, spanHandle, name);
}

isolated function createToolSpan(handle cx, string name) returns Span {
    handle spanHandle = createToolSpanHandle(cx, java:fromString(name));
    return new (cx, spanHandle, name);
}

function execAgent(handle cx) {
    Span span = createAgentSpan(cx, "my-agent");
    span.enter();
    span.setInput("hi");
    execLLM(cx);
    execLLM(cx);
    span.setOutput("hello");
    span.setStatus("OK");
    span.exit();
}

function execLLM(handle cx) {
    Span span = createLLMSpan(cx, "my-llm", "gpt-4o", "openai");
    span.enter();
    span.setInput("hi llm");
    execTool(cx);
    span.setOutput("hello llm");
    span.setStatus("OK");
    span.exit();
}

function execTool(handle cx) {
    Span span = createToolSpan(cx, "my-tool");
    span.enter();
    span.setInput("hi tool");
    span.setOutput("hello tool");
    span.setStatus("OK");
    span.exit();
}

function execLLMCallInPython(handle cx) {
    execLLMCall(cx);
}

function execOurTracingCall(handle cx) {
    handle instrumentingHandle = createInstrumentingHandle(cx);
    execInstrumentingHandle(cx, instrumentingHandle, java:fromString("hi"), java:fromString("hello"));
    execInstrumentingHandle(cx, instrumentingHandle, java:fromString("test"), java:fromString("test2"));
}

isolated function getContext() returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "getContext"
} external;

isolated function closeContext(handle cx) = @java:Method {
    'class: "com.example.Lib",
    name: "closeContext"
} external;

isolated function execLLMCall(handle cx) = @java:Method {
    'class: "com.example.Lib",
    name: "execLLMCall"
} external;

isolated function setupTracer(handle cx, handle projectName, handle endpoint) returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "setupTracer"
} external;

isolated function createInstrumentingHandle(handle cx) returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "createInstrumentingHandle"
} external;

isolated function execInstrumentingHandle(handle cx, handle 'handle, handle input, handle output) = @java:Method {
    'class: "com.example.Lib",
    name: "execInstrumentingHandle"
} external;

isolated function execTraceAgent(handle cx) = @java:Method {
    'class: "com.example.Lib",
    name: "execTraceAgent"
} external;

isolated function createLLMSpanHandle(handle cx, handle name, handle modelName, handle providerName) returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "createLLMSpanHandle"
} external;

isolated function createToolSpanHandle(handle cx, handle name) returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "createToolSpanHandle"
} external;

isolated function createAgentSpanHandle(handle cx, handle name) returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "createAgentSpanHandle"
} external;

isolated function enterSpanHandle(handle cx, handle 'handle) = @java:Method {
    'class: "com.example.Lib",
    name: "enterSpanHandle"
} external;

isolated function setSpanHandleInput(handle cx, handle 'handle, handle input) = @java:Method {
    'class: "com.example.Lib",
    name: "setSpanHandleInput"
} external;

isolated function setSpanHandleOutput(handle cx, handle 'handle, handle output) = @java:Method {
    'class: "com.example.Lib",
    name: "setSpanHandleOutput"
} external;

isolated function exitSpanHandle(handle cx, handle 'handle) = @java:Method {
    'class: "com.example.Lib",
    name: "exitSpanHandle"
} external;

isolated function setSpanAttribute(handle cx, handle 'handle, handle key, handle value) = @java:Method {
    'class: "com.example.Lib",
    name: "setSpanAttribute"
} external;

isolated function setSpanHandleStatus(handle cx, handle 'handle, handle status) = @java:Method {
    'class: "com.example.Lib",
    name: "setSpanHandleStatus"
} external;
