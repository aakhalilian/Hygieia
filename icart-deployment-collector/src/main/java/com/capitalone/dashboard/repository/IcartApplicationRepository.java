package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.IcartApplication;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Repository for {@link IcartApplication}s.
 */
public interface IcartApplicationRepository extends BaseCollectorItemRepository<IcartApplication> {

    /**
     * Find a {@link IcartApplication} by UDeploy instance URL and UDeploy application id.
     *
     * @param collectorId ID of the {@link com.capitalone.dashboard.model.IcartCollector}
     * @param instanceUrl UDeploy instance URL
     * @param applicationId UDeploy application ID
     * @return a {@link IcartApplication} instance
     */
    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.applicationId : ?2}")
    IcartApplication findUDeployApplication(ObjectId collectorId, String instanceUrl, String applicationId);

    /**
     * Finds all {@link IcartApplication}s for the given instance URL.
     *
     * @param collectorId ID of the {@link com.capitalone.dashboard.model.IcartCollector}
     * @param instanceUrl UDeploy instance URl
     * @return list of {@link IcartApplication}s
     */
    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<IcartApplication> findEnabledApplications(ObjectId collectorId, String instanceUrl);
}
