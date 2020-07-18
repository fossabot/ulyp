package com.ulyp.agent;

import com.ulyp.agent.log.AgentLogManager;
import com.ulyp.agent.log.LoggingSettings;
import com.ulyp.agent.settings.AgentSettings;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Executable;

public class BbTransformer implements Transformer {

    public static final AgentContext context = AgentContext.getInstance();
    public static final AgentSettings settings = context.getSettings();
    @SuppressWarnings("unused")
    public static final CallTracer callTracer = new CallTracer(context);

    private static final Logger logger = AgentLogManager.getLogger(BbTransformer.class);

    @Override
    public DynamicType.Builder<?> transform(
            final DynamicType.Builder<?> builder,
            final TypeDescription typeDescription,
            final ClassLoader classLoader,
            final JavaModule module)
    {
        if (typeDescription.isInterface()) {
            return builder;
        }

        if (LoggingSettings.IS_TRACE_TURNED_ON) {
            logger.trace("Scanning type {}", typeDescription);
        }

        final AsmVisitorWrapper methodsStartVisitor =
                new AsmVisitorWrapper.ForDeclaredMethods()
                        .method(
                                ElementMatchers
                                        .isMethod()
                                        .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                        .and(ElementMatchers.not(ElementMatchers.isTypeInitializer()))
                                        .and(ElementMatchers.not(ElementMatchers.isToString()))
                                        .and(desc -> {
                                            boolean shouldStartTracing = settings.shouldStartTracing(desc);
                                            if (shouldStartTracing) {
                                                logger.debug("Should start tracing at {}.{}", typeDescription.getName(), desc.getActualName());
                                            }
                                            return shouldStartTracing;
                                        }),
                                Advice.to(StartTracingMethodAdvice.class));

        final AsmVisitorWrapper methodsVisitor =
                new AsmVisitorWrapper.ForDeclaredMethods()
                        .method(ElementMatchers
                                        .isMethod()
                                        .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                                        .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                        .and(ElementMatchers.not(ElementMatchers.isTypeInitializer()))
                                        .and(ElementMatchers.not(ElementMatchers.isToString()))
                                        .and(desc -> {
                                            boolean shouldTrace = !settings.shouldStartTracing(desc);
                                            if (shouldTrace) {
                                                logger.debug("Should trace at {}.{}", typeDescription.getName(), desc.getActualName());
                                            }
                                            return shouldTrace;
                                        }),
                                Advice.to(MethodAdvice.class));

        return builder
                .visit(methodsStartVisitor)
                .visit(methodsVisitor);
    }

    public static class StartTracingMethodAdvice {

        @Advice.OnMethodEnter
        static void enter(
                @Advice.Origin Executable executable,
                @Advice.AllArguments Object[] arguments) {
            callTracer.startOrContinueTracing(context.getMethodDescriptionDictionary().get(executable), arguments);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        static void exit(
                @Advice.Origin Executable executable,
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue,
                @Advice.Thrown Throwable throwable) {
            callTracer.endTracingIfPossible(context.getMethodDescriptionDictionary().get(executable), returnValue, throwable);
        }
    }

    public static class MethodAdvice {

        @Advice.OnMethodEnter
        static void enter(
                @Advice.Origin Executable executable,
                @Advice.AllArguments Object[] arguments) {
            if (callTracer.tracingIsActiveInThisThread()) {
                callTracer.onMethodEnter(context.getMethodDescriptionDictionary().get(executable), arguments);
            }
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        static void exit(
                @Advice.Origin Executable executable,
                @Advice.Thrown Throwable throwable,
                @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
            if (callTracer.tracingIsActiveInThisThread()) {
                callTracer.onMethodExit(context.getMethodDescriptionDictionary().get(executable), returnValue, throwable);
            }
        }
    }
}
