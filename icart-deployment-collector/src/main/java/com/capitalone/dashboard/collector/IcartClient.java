package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.EnvironmentStatus;
import com.capitalone.dashboard.model.IcartApplication;
import com.capitalone.dashboard.model.IcartEnvResCompData;

import java.util.List;

/**
 * Client for fetching information from UDeploy.
 */
public interface IcartClient {

    /**
     * Fetches all {@link IcartApplication}s for a given instance URL.
     *
     * @param instanceUrl instance URL
     * @return list of {@link IcartApplication}s
     */
    List<IcartApplication> getApplications(String instanceUrl);

    /**
     * Fetches all {@link Environment}s for a given {@link IcartApplication}.
     *
     * @param application a {@link IcartApplication}
     * @return list of {@link Environment}s
     */
    List<Environment> getEnvironments(IcartApplication application);

    /**
     * Fetches all {@link EnvironmentComponent}s for a given {@link IcartApplication} and {@link Environment}.
     *
     * @param application a {@link IcartApplication}
     * @param environment an {@link Environment}
     * @return list of {@link EnvironmentComponent}s
     */
    List<EnvironmentComponent> getEnvironmentComponents(IcartApplication application, Environment environment);

    /**
     * Fetches all {@link EnvironmentStatus}es for a given {@link IcartApplication} and {@link Environment}.
     *
     * @param application a {@link IcartApplication}
     * @param environment an {@link Environment}
     * @return list of {@link EnvironmentStatus}es
     */
    List<IcartEnvResCompData> getEnvironmentResourceStatusData(IcartApplication application, Environment environment);
}
