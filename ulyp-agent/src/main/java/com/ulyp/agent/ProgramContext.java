package com.ulyp.agent;

import com.ulyp.agent.transport.UploadingTransport;
import com.ulyp.agent.util.Log;
import com.ulyp.agent.util.NoopLog;
import com.ulyp.agent.util.ProcessUtils;
import com.ulyp.agent.util.SysoutLog;
import com.ulyp.core.ProcessInfo;

import java.util.concurrent.TimeUnit;

public class ProgramContext {

    private final Settings settings;
    private final UploadingTransport transport;
    private final MethodDescriptionDictionary methodCache;
    private final Log log;

    public ProgramContext() {
        this.settings = Settings.fromJavaProperties();
        if (settings.loggingTurnedOn()) {
            this.log = new SysoutLog();
        } else {
            this.log = new NoopLog();
        }
        this.methodCache = new MethodDescriptionDictionary(log);
        this.transport = new UploadingTransport(new ProcessInfo(ProcessUtils.getMainClassName()), settings.getUiAddress());

        Thread shutdown = new Thread(
                () -> {
                    try {
                        transport.shutdownNowAndAwaitForTraceLogsSending(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
        );
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    public UploadingTransport getTransport() {
        return transport;
    }

    public Settings getSettings() {
        return settings;
    }

    public MethodDescriptionDictionary getMethodCache() {
        return methodCache;
    }

    public Log getLog() {
        return log;
    }
}
