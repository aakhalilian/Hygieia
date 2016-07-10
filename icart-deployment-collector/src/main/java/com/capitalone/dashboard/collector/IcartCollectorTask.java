package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.EnvironmentStatus;
import com.capitalone.dashboard.model.IcartApplication;
import com.capitalone.dashboard.model.IcartCollector;
import com.capitalone.dashboard.model.IcartEnvResCompData;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.EnvironmentComponentRepository;
import com.capitalone.dashboard.repository.EnvironmentStatusRepository;
import com.capitalone.dashboard.repository.IcartApplicationRepository;
import com.capitalone.dashboard.repository.IcartCollectorRepository;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects {@link EnvironmentComponent} and {@link EnvironmentStatus} data from
 * {@link IcartApplication}s.
 */
@Component
public class IcartCollectorTask extends CollectorTask<IcartCollector> {
    @SuppressWarnings({"unused", "PMD.UnusedPrivateField"})
    private static final Logger LOGGER = LoggerFactory.getLogger(IcartCollectorTask.class);

    private final IcartCollectorRepository iCartCollectorRepository;
    private final IcartApplicationRepository iCartApplicationRepository;
    private final IcartClient iCartClient;
    private final IcartSettings iCartSettings;

    private final EnvironmentComponentRepository envComponentRepository;
    private final EnvironmentStatusRepository environmentStatusRepository;

    private final ComponentRepository dbComponentRepository;

    @Autowired
    public IcartCollectorTask(TaskScheduler taskScheduler,
                                IcartCollectorRepository uDeployCollectorRepository,
                                IcartApplicationRepository uDeployApplicationRepository,
                                EnvironmentComponentRepository envComponentRepository,
                                EnvironmentStatusRepository environmentStatusRepository,
                                IcartSettings uDeploySettings, IcartClient uDeployClient,
                                ComponentRepository dbComponentRepository) {
        super(taskScheduler, "UDeploy");
        this.iCartCollectorRepository = uDeployCollectorRepository;
        this.iCartApplicationRepository = uDeployApplicationRepository;
        this.iCartSettings = uDeploySettings;
        this.iCartClient = uDeployClient;
        this.envComponentRepository = envComponentRepository;
        this.environmentStatusRepository = environmentStatusRepository;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public IcartCollector getCollector() {
        return IcartCollector.prototype(iCartSettings.getServers());
    }

    @Override
    public BaseCollectorRepository<IcartCollector> getCollectorRepository() {
        return iCartCollectorRepository;
    }

    @Override
    public String getCron() {
        return iCartSettings.getCron();
    }

    @Override
    public void collect(IcartCollector collector) {
        for (String instanceUrl : collector.getIcartServers()) {

            logBanner(instanceUrl);

            long start = System.currentTimeMillis();

            clean(collector);

            addNewApplications(iCartClient.getApplications(instanceUrl),
                    collector);
            updateData(enabledApplications(collector, instanceUrl));

            log("Finished", start);
        }
    }

    /**
     * Clean up unused deployment collector items
     *
     * @param collector the {@link IcartCollector}
     */
    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private void clean(IcartCollector collector) {
        deleteUnwantedJobs(collector);
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
                .findAll()) {
            if (comp.getCollectorItems() == null || comp.getCollectorItems().isEmpty()) continue;
            List<CollectorItem> itemList = comp.getCollectorItems().get(
                    CollectorType.Deployment);
            if (itemList == null) continue;
            for (CollectorItem ci : itemList) {
                if (ci == null) continue;
                uniqueIDs.add(ci.getId());
            }
        }
        List<IcartApplication> appList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet< >();
        udId.add(collector.getId());
        for (IcartApplication app : iCartApplicationRepository.findByCollectorIdIn(udId)) {
            if (app != null) {
                app.setEnabled(uniqueIDs.contains(app.getId()));
                appList.add(app);
            }
        }
        iCartApplicationRepository.save(appList);
    }

    private void deleteUnwantedJobs(IcartCollector collector) {

        List<IcartApplication> deleteAppList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (IcartApplication app : iCartApplicationRepository.findByCollectorIdIn(udId)) {
            if (!collector.getIcartServers().contains(app.getInstanceUrl()) ||
                    (!app.getCollectorId().equals(collector.getId()))) {
                deleteAppList.add(app);
            }
        }

        iCartApplicationRepository.delete(deleteAppList);

    }

    private List<EnvironmentComponent> getEnvironmentComponent(List<IcartEnvResCompData> dataList, Environment environment, IcartApplication application) {
        List<EnvironmentComponent> returnList = new ArrayList<>();
        for (IcartEnvResCompData data : dataList) {
            EnvironmentComponent component = new EnvironmentComponent();
            component.setComponentName(data.getComponentName());
            component.setCollectorItemId(data.getCollectorItemId());
            component.setComponentVersion(data
                    .getComponentVersion());
            component.setDeployed(data.isDeployed());
            component.setEnvironmentName(data
                    .getEnvironmentName());

            component.setEnvironmentName(environment.getName());
            component.setAsOfDate(data.getAsOfDate());
            String environmentURL = StringUtils.removeEnd(
                    application.getInstanceUrl(), "/")
                    + "/#environment/" + environment.getId();
            component.setEnvironmentUrl(environmentURL);

            returnList.add(component);
        }
        return returnList;
    }


    private List<EnvironmentStatus> getEnvironmentStatus(List<IcartEnvResCompData> dataList) {
        List<EnvironmentStatus> returnList = new ArrayList<>();
        for (IcartEnvResCompData data : dataList) {
            EnvironmentStatus status = new EnvironmentStatus();
            status.setCollectorItemId(data.getCollectorItemId());
            status.setComponentID(data.getComponentID());
            status.setComponentName(data.getComponentName());
            status.setEnvironmentName(data.getEnvironmentName());
            status.setOnline(data.isOnline());
            status.setResourceName(data.getResourceName());

            returnList.add(status);
        }
        return returnList;
    }


    /**
     * For each {@link IcartApplication}, update the current
     * {@link EnvironmentComponent}s and {@link EnvironmentStatus}.
     *
     * @param uDeployApplications list of {@link IcartApplication}s
     */
    private void updateData(List<IcartApplication> uDeployApplications) {
        for (IcartApplication application : uDeployApplications) {
            List<EnvironmentComponent> compList = new ArrayList<>();
            List<EnvironmentStatus> statusList = new ArrayList<>();
            long startApp = System.currentTimeMillis();

            for (Environment environment : iCartClient
                    .getEnvironments(application)) {

                List<IcartEnvResCompData> combinedDataList = iCartClient
                        .getEnvironmentResourceStatusData(application,
                                environment);

                compList.addAll(getEnvironmentComponent(combinedDataList, environment, application));
                statusList.addAll(getEnvironmentStatus(combinedDataList));
            }
            if (!compList.isEmpty()) {
                List<EnvironmentComponent> existingComponents = envComponentRepository
                        .findByCollectorItemId(application.getId());
                envComponentRepository.delete(existingComponents);
                envComponentRepository.save(compList);
            }
            if (!statusList.isEmpty()) {
                List<EnvironmentStatus> existingStatuses = environmentStatusRepository
                        .findByCollectorItemId(application.getId());
                environmentStatusRepository.delete(existingStatuses);
                environmentStatusRepository.save(statusList);
            }

            log(" " + application.getApplicationName(), startApp);
        }
    }

    private List<IcartApplication> enabledApplications(
            IcartCollector collector, String instanceUrl) {
        return iCartApplicationRepository.findEnabledApplications(
                collector.getId(), instanceUrl);
    }

    /**
     * Add any new {@link IcartApplication}s.
     *
     * @param applications list of {@link IcartApplication}s
     * @param collector    the {@link IcartCollector}
     */
    private void addNewApplications(List<IcartApplication> applications,
                                    IcartCollector collector) {
        long start = System.currentTimeMillis();
        int count = 0;

        log("All apps", start, applications.size());
        for (IcartApplication application : applications) {

            if (isNewApplication(collector, application)) {
                application.setCollectorId(collector.getId());
                application.setEnabled(false);
                application.setDescription(application.getApplicationName());
                try {
                    iCartApplicationRepository.save(application);
                } catch (org.springframework.dao.DuplicateKeyException ce) {
                    log("Duplicates items not allowed", 0);

                }
                count++;
            }

        }
        log("New apps", start, count);
    }

    private boolean isNewApplication(IcartCollector collector,
                                     IcartApplication application) {
        return iCartApplicationRepository.findUDeployApplication(
                collector.getId(), application.getInstanceUrl(),
                application.getApplicationId()) == null;
    }

    @SuppressWarnings("unused")
	private boolean changed(EnvironmentStatus status, EnvironmentStatus existing) {
        return existing.isOnline() != status.isOnline();
    }

    @SuppressWarnings("unused")
	private EnvironmentStatus findExistingStatus(
            final EnvironmentStatus proposed,
            List<EnvironmentStatus> existingStatuses) {

        return Iterables.tryFind(existingStatuses,
                new Predicate<EnvironmentStatus>() {
                    @Override
                    public boolean apply(EnvironmentStatus existing) {
                        return existing.getEnvironmentName().equals(
                                proposed.getEnvironmentName())
                                && existing.getComponentName().equals(
                                proposed.getComponentName())
                                && existing.getResourceName().equals(
                                proposed.getResourceName());
                    }
                }).orNull();
    }

    @SuppressWarnings("unused")
	private boolean changed(EnvironmentComponent component,
                            EnvironmentComponent existing) {
        return existing.isDeployed() != component.isDeployed()
                || existing.getAsOfDate() != component.getAsOfDate() || !existing.getComponentVersion().equalsIgnoreCase(component.getComponentVersion());
    }

    @SuppressWarnings("unused")
	private EnvironmentComponent findExistingComponent(
            final EnvironmentComponent proposed,
            List<EnvironmentComponent> existingComponents) {

        return Iterables.tryFind(existingComponents,
                new Predicate<EnvironmentComponent>() {
                    @Override
                    public boolean apply(EnvironmentComponent existing) {
                        return existing.getEnvironmentName().equals(
                                proposed.getEnvironmentName())
                                && existing.getComponentName().equals(
                                proposed.getComponentName());

                    }
                }).orNull();
    }
}
