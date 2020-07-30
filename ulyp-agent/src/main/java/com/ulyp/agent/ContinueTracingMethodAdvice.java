package com.ulyp.agent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.lang.reflect.Executable;

public class ContinueTracingMethodAdvice {

    @Advice.OnMethodEnter
    static void enter(
            @Advice.Origin Executable executable,
            @Advice.Local(value = "instrumentationIdStore") long instrumentationIdStore,
            @InstrumentationId long instrumentationId,
            @Advice.AllArguments Object[] arguments) {
        if (CallTracer.getInstance().tracingIsActiveInThisThread()) {
            instrumentationIdStore = instrumentationId;
            CallTracer.getInstance().onMethodEnter(AgentContext.getInstance().getMethodDescriptionDictionary().get(executable), arguments);
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void exit(
            @Advice.Origin Executable executable,
            @Advice.Local(value = "instrumentationIdStore") long instrumentationIdStore,
            @Advice.Thrown Throwable throwable,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
        if (CallTracer.getInstance().tracingIsActiveInThisThread()) {
            CallTracer.getInstance().onMethodExit(AgentContext.getInstance().getMethodDescriptionDictionary().get(executable), returnValue, throwable);
        }
    }
}
