package com.ulyp.agent;

import com.ulyp.agent.util.EnhancedThreadLocal;
import com.ulyp.agent.util.Log;
import com.ulyp.core.*;
import com.ulyp.transport.TClassDescriptionList;
import com.ulyp.transport.TMethodDescriptionList;
import com.ulyp.transport.TMethodTraceLog;
import com.ulyp.transport.TMethodTraceLogUploadRequest;

import javax.annotation.concurrent.ThreadSafe;

@SuppressWarnings("unused")
@ThreadSafe
public class Tracer {

    private final EnhancedThreadLocal<MethodTraceLog> threadLocalTraceLog = new EnhancedThreadLocal<>();
    private final AgentContext context;
    private final Log log;

    public Tracer(AgentContext context) {
        this.context = context;
        this.log = context.getLog();
    }

    public void startOrContinueTracing(MethodDescription methodDescription, Object[] args) {
        MethodTraceLog traceLog = threadLocalTraceLog.getOrCreate(() -> new MethodTraceLog(
                context.getMethodDescriptionDictionary(),
                context.getSettings().getMaxTreeDepth())
        );
        onMethodEnter(methodDescription, args);
    }

    public void endTracingIfPossible(MethodDescription methodDescription, Object result, Throwable thrown) {
        MethodTraceLog traceLog = threadLocalTraceLog.get();

        log.log(() -> "May end tracing, trace log id = " + methodDescription.getId());
        onMethodExit(methodDescription, result, thrown);

        if (traceLog.isComplete()) {
            enqueueToPrinter(threadLocalTraceLog.pop());
        }
    }

    public void onMethodEnter(MethodDescription method, Object[] args) {
        MethodTraceLog methodTracesLog = threadLocalTraceLog.get();
        if (methodTracesLog == null) {
            return;
        }
        methodTracesLog.onMethodEnter(method.getId(), method.getParamPrinters(), args);
    }

    public void onMethodExit(MethodDescription method, Object result, Throwable thrown) {
        MethodTraceLog methodTracesLog = threadLocalTraceLog.get();
        if (methodTracesLog == null) return;

        methodTracesLog.onMethodExit(method.getId(), method.getResultPrinter(), result, thrown);
    }

    private void enqueueToPrinter(MethodTraceLog traceLog) {
        TMethodTraceLog log = TMethodTraceLog.newBuilder()
                .setEnterTraces(traceLog.getEnterTraces().toByteString())
                .setExitTraces(traceLog.getExitTraces().toByteString())
                .build();

        MethodDescriptionList methodDescriptionList = new MethodDescriptionList();
        for (MethodDescription description : context.getMethodDescriptionDictionary().getMethodDescriptions()) {
            methodDescriptionList.add(description);
        }
        ClassDescriptionList classDescriptionList = new ClassDescriptionList();
        for (ClassDescription classDescription : context.getMethodDescriptionDictionary().getClassDescriptions()) {
            classDescriptionList.add(classDescription);
        }

        TMethodTraceLogUploadRequest.Builder requestBuilder = TMethodTraceLogUploadRequest.newBuilder();

        requestBuilder
                .setTraceLogId(traceLog.getId())
                .setTraceLog(log)
                .setMethodDescriptionList(TMethodDescriptionList.newBuilder().setData(methodDescriptionList.toByteString()).build())
                .setClassDescriptionList(TClassDescriptionList.newBuilder().setData(classDescriptionList.toByteString()).build())
                .setMainClassName(context.getProcessInfo().getMainClassName())
                .setCreateEpochMillis(traceLog.getEpochMillisCreatedTime())
                .setLifetimeMillis(System.currentTimeMillis() - traceLog.getEpochMillisCreatedTime());

        context.getTransport().upload(requestBuilder.build());
    }
}
