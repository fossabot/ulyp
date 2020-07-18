package com.ulyp.agent;

import com.ulyp.agent.log.AgentLogManager;
import com.ulyp.agent.log.LoggingSettings;
import com.ulyp.agent.transport.NamedThreadFactory;
import com.ulyp.agent.util.EnhancedThreadLocal;
import com.ulyp.core.*;
import com.ulyp.transport.*;
import org.apache.logging.log4j.Logger;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.*;

@SuppressWarnings("unused")
@ThreadSafe
public class CallTracer {

    private static final Logger logger = AgentLogManager.getLogger(BbTransformer.class);

    private final EnhancedThreadLocal<CallTraceLog> threadLocalTraceLog = new EnhancedThreadLocal<>();
    private final AgentContext context;

    private final ScheduledExecutorService settingsUpdatingService = Executors.newScheduledThreadPool(
            1,
            new NamedThreadFactory("Settings-Updater", true)
    );

    private volatile boolean mayStartTracing = true;
    private final TracingParams tracingParams = new TracingParams(false, false, false);

    public CallTracer(AgentContext context) {
        this.context = context;

        try {
            SettingsResponse settings = context.getTransport().getSettingsBlocking(Duration.ofSeconds(3));
            onSettings(settings);
        } catch (Exception e) {
            // NOP
        }

        settingsUpdatingService.scheduleAtFixedRate(() -> {
            try {
                SettingsResponse settings = context.getTransport().getSettingsBlocking(Duration.ofMillis(500));
                onSettings(settings);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // NOP
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void onSettings(SettingsResponse settings) {
        mayStartTracing = settings.getMayStartTracing();
        tracingParams.updateTraceCollections(settings.getTraceCollections());
    }

    public boolean tracingIsActiveInThisThread() {
        return threadLocalTraceLog.get() != null;
    }

    public void startOrContinueTracing(MethodDescription methodDescription, Object[] args) {
        if (!tracingIsActiveInThisThread() && !mayStartTracing) {
            return;
        }

        CallTraceLog traceLog = threadLocalTraceLog.getOrCreate(() -> {
            CallTraceLog log = new CallTraceLog(
                    context.getMethodDescriptionDictionary(),
                    context.getSettings().getMaxTreeDepth(),
                    context.getSettings().getMaxCallsPerMethod());
            if (LoggingSettings.IS_TRACE_TURNED_ON) {
                logger.trace("Create new {}, method {}, args {}", log, methodDescription, args);
            }
            return log;
        });
        onMethodEnter(methodDescription, args);
    }

    public void endTracingIfPossible(MethodDescription methodDescription, Object result, Throwable thrown) {
        CallTraceLog traceLog = threadLocalTraceLog.get();
        onMethodExit(methodDescription, result, thrown);

        if (traceLog != null && traceLog.isComplete()) {
            threadLocalTraceLog.clear();
            if (traceLog.size() >= context.getSettings().getMinTraceCount()) {
                if (LoggingSettings.IS_TRACE_TURNED_ON) {
                    logger.trace("Will send trace log {}", traceLog);
                }
                context.getTransport().uploadAsync(traceLog, context.getMethodDescriptionDictionary(), context.getProcessInfo());
            }
        }
    }

    public void onMethodEnter(MethodDescription method, Object[] args) {
        CallTraceLog callTraces = threadLocalTraceLog.get();
        if (callTraces == null) {
            return;
        }
        if (LoggingSettings.IS_TRACE_TURNED_ON) {
            logger.trace("Method enter on {}, method {}, args {}", callTraces, method, args);
        }
        callTraces.onMethodEnter(method.getId(), tracingParams, method.getParamPrinters(), args);
    }

    public void onMethodExit(MethodDescription method, Object result, Throwable thrown) {
        CallTraceLog callTracesLog = threadLocalTraceLog.get();
        if (callTracesLog == null) return;

        if (LoggingSettings.IS_TRACE_TURNED_ON) {
            logger.trace("Method exit {}, method {}, return value {}, thrown {}", callTracesLog, method, result, thrown);
        }
        callTracesLog.onMethodExit(method.getId(), tracingParams, method.getResultPrinter(), result, thrown);
    }
}