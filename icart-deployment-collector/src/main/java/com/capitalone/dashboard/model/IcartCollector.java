package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Collector implementation for UDeploy that stores UDeploy server URLs.
 */
public class IcartCollector extends Collector {
    private List<String> iCcartServers = new ArrayList<>();

    public List<String> getIcartServers() {
        return iCcartServers;
    }

    public static IcartCollector prototype(List<String> servers) {
        IcartCollector protoType = new IcartCollector();
        protoType.setName("iCart");
        protoType.setCollectorType(CollectorType.Deployment);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getIcartServers().addAll(servers);
        return protoType;
    }
}
