package com.ulyp.agent.settings;

import com.ulyp.agent.transport.UiAddress;
import net.bytebuddy.description.method.MethodDescription;
import java.util.List;

public interface AgentSettings {

    UiAddress getUiAddress();

    boolean shouldStartTracing(MethodDescription description);

    int getMaxTreeDepth();

    int getMinTraceCount();

    int getMaxCallsPerMethod();

    List<String> getPackages();

    List<String> getExcludePackages();
}
