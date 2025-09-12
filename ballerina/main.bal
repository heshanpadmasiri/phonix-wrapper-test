import ballerina/jballerina.java;

public function main() returns error? {
    handle cx = getContext();
    _ = setupTracer(cx, java:fromString("my-bal-llm"), java:fromString("http://localhost:6006/v1/traces"));
    execAgent(cx);
    closeContext(cx);
}

function execAgent(handle cx) {
    handle spanHandle = createSpanHandle(cx, java:fromString("agent"));
    enterSpanHandle(cx, spanHandle);
    setSpanHandleInput(cx, spanHandle, java:fromString("hi"));
    execLLM(cx);

    setSpanHandleOutput(cx, spanHandle, java:fromString("hello"));
    exitSpanHandle(cx, spanHandle);
}

function execLLM(handle cx) {
    handle spanHandle = createSpanHandle(cx, java:fromString("llm"));
    enterSpanHandle(cx, spanHandle);
    setSpanHandleInput(cx, spanHandle, java:fromString("hi llm"));
    execTool(cx);
    setSpanHandleOutput(cx, spanHandle, java:fromString("hello llm"));
    exitSpanHandle(cx, spanHandle);
}

function execTool(handle cx) {
    handle spanHandle = createSpanHandle(cx, java:fromString("tool"));
    enterSpanHandle(cx, spanHandle);
    setSpanHandleInput(cx, spanHandle, java:fromString("hi tool"));
    setSpanHandleOutput(cx, spanHandle, java:fromString("hello tool"));
    exitSpanHandle(cx, spanHandle);
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

isolated function createSpanHandle(handle cx, handle kind) returns handle = @java:Method {
    'class: "com.example.Lib",
    name: "createSpanHandle"
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
