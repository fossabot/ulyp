package com.ulyp.agent;

import com.ulyp.agent.log.LoggingSettings;
import com.ulyp.agent.settings.SystemPropertiesSettings;
import com.ulyp.agent.settings.TracingStartMethodList;
import com.ulyp.agent.settings.UiSettings;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.List;

public class Agent {

    public static void start(String args, Instrumentation instrumentation) {

        String logLevel = LoggingSettings.LOG_LEVEL.name();
        AgentContext instance = AgentContext.getInstance();
        SystemPropertiesSettings systemPropSettings = SystemPropertiesSettings.loadFromSystemProperties();
        UiSettings uiSettings = instance.getUiSettings();
        List<String> tracePackages = uiSettings.getTracePackages().getValue();
        TracingStartMethodList tracingStartMethodList = uiSettings.getTracingStartMethod().getValue();

        // TODO show that connected to UI (if connected)
        System.out.println("Starting ULYP agent, logging level = " + logLevel +
                ", packages = " + uiSettings.getTracePackages() +
                ", tracing start methods = " + uiSettings.getTracingStartMethod());
//
        ElementMatcher.Junction<TypeDescription> tracingMatcher = null;

        for (String tracePackage : tracePackages) {
            if (tracingMatcher == null) {
                tracingMatcher = ElementMatchers.nameStartsWith(tracePackage);
            } else {
                tracingMatcher = tracingMatcher.or(ElementMatchers.nameStartsWith(tracePackage));
            }
        }

        for (int i = 0; i < systemPropSettings.getExcludePackages().size(); i++) {
            if (tracingMatcher == null) {
                tracingMatcher = ElementMatchers.not(ElementMatchers.nameStartsWith(systemPropSettings.getPackages().get(i)));
            } else {
                tracingMatcher = tracingMatcher.and(ElementMatchers.not(ElementMatchers.nameStartsWith(systemPropSettings.getPackages().get(i))));
            }
        }

        ElementMatcher.Junction<TypeDescription> finalMatcher = ElementMatchers
                .not(ElementMatchers.nameStartsWith("com.ulyp"))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("shadowed")));

        if (tracingMatcher != null) {
            finalMatcher = finalMatcher.and(tracingMatcher);
        }

//        AgentLogManager.getLogger(Agent.class).trace("Matcher for scanning is {}", finalMatcher);

        new AgentBuilder.Default()
                .type(finalMatcher)
                .transform(new BbTransformer(StartTracingMethodAdvice.class, MethodAdvice.class, tracingStartMethodList))
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
                .installOn(instrumentation);
    }
}
