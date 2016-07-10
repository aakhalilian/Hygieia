package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.IcartApplication;
import com.capitalone.dashboard.model.IcartEnvResCompData;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DefaultIcartClient implements IcartClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIcartClient.class);

    private final IcartSettings icartSettings;
    private final RestOperations restOperations;

    @Autowired
    public DefaultIcartClient(IcartSettings uDeploySettings,
                                Supplier<RestOperations> restOperationsSupplier) {
        this.icartSettings = uDeploySettings;
        this.restOperations = restOperationsSupplier.get();
    }

    @Override
    public List<IcartApplication> getApplications(String instanceUrl) {
        List<IcartApplication> applications = new ArrayList<>();

//        for (Object item : paresAsArray(makeRestCall(instanceUrl,
//                "deploy/application"))) {
//            JSONObject jsonObject = (JSONObject) item;
//            IcartApplication application = new IcartApplication();
//            application.setInstanceUrl(instanceUrl);
//            application.setApplicationName(str(jsonObject, "name"));
//            application.setApplicationId(str(jsonObject, "id"));
//            applications.add(application);
//        }
        ///////fake Codes
		IcartApplication application = new IcartApplication();
		application.setInstanceUrl("fakeUrl");
		application.setApplicationName("fakeAppName");
		application.setApplicationId("0001");
		applications.add(application);
		return applications;
	}

    @Override
    public List<Environment> getEnvironments(IcartApplication application) {
        List<Environment> environments = new ArrayList<>();
//        String url = "deploy/application/" + application.getApplicationId()
//                + "/environments/false";
//
//        for (Object item : paresAsArray(makeRestCall(
//                application.getInstanceUrl(), url))) {
//            JSONObject jsonObject = (JSONObject) item;
//            environments.add(new Environment(str(jsonObject, "id"), str(
//                    jsonObject, "name")));
//        }
        //////////////////////////fake
        environments.add(new Environment("002", "fakeEnvironment"));
        return environments;
    }

    @SuppressWarnings("PMD.AvoidCatchingNPE")
    @Override
    public List<EnvironmentComponent> getEnvironmentComponents(
            IcartApplication application, Environment environment) {
        List<EnvironmentComponent> components = new ArrayList<>();
//        String url = "deploy/environment/" + environment.getId()
//                + "/latestDesiredInventory";
//        try {
//            for (Object item : paresAsArray(makeRestCall(
//                    application.getInstanceUrl(), url))) {
//                JSONObject jsonObject = (JSONObject) item;
//
//                JSONObject versionObject = (JSONObject) jsonObject
//                        .get("version");
//                JSONObject componentObject = (JSONObject) jsonObject
//                        .get("component");
//                JSONObject complianceObject = (JSONObject) jsonObject
//                        .get("compliancy");
//
//                EnvironmentComponent component = new EnvironmentComponent();
//                component.setEnvironmentID(environment.getId());
//                component.setEnvironmentName(environment.getName());
//                component.setEnvironmentUrl(normalizeUrl(
//                        application.getInstanceUrl(), "/#environment/"
//                                + environment.getId()));
//                component.setComponentID(str(componentObject, "id"));
//                component.setComponentName(str(componentObject, "name"));
//                component.setComponentVersion(str(versionObject, "name"));
//                component.setDeployed(complianceObject.get("correctCount")
//                        .equals(complianceObject.get("desiredCount")));
//                component.setAsOfDate(date(jsonObject, "date"));
//                components.add(component);
//            }
//        } catch (NullPointerException npe) {
//            LOGGER.info("No Environment data found, No components deployed");
//        }
        /////////////////////////////fake codes
		EnvironmentComponent component = new EnvironmentComponent();
		component.setEnvironmentID(environment.getId());
		component.setEnvironmentName(environment.getName());
		component.setEnvironmentUrl(normalizeUrl(application.getInstanceUrl(), "/#environment/" + environment.getId()));
		component.setComponentID("003");
		component.setComponentName("fakeComponent");
		component.setComponentVersion("fakeVersion");
		component.setDeployed(true);
		component.setAsOfDate(0);
		components.add(component);

		return components;
	}

    // Called by DefaultEnvironmentStatusUpdater
//    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts") // agreed, this method needs refactoring.
    @Override
    public List<IcartEnvResCompData> getEnvironmentResourceStatusData(
            IcartApplication application, Environment environment) {

        List<IcartEnvResCompData> environmentStatuses = new ArrayList<>();
//        String urlNonCompliantResources = "deploy/environment/"
//                + environment.getId() + "/noncompliantResources";
//        String urlAllResources = "deploy/environment/" + environment.getId()
//                + "/resources";
//
//        ResponseEntity<String> nonCompliantResourceResponse = makeRestCall(
//                application.getInstanceUrl(), urlNonCompliantResources);
//        JSONArray nonCompliantResourceJSON = paresAsArray(nonCompliantResourceResponse);
//        ResponseEntity<String> allResourceResponse = makeRestCall(
//                application.getInstanceUrl(), urlAllResources);
//        JSONArray allResourceJSON = paresAsArray(allResourceResponse);
/**
 * New logic - Dec16/2015
 * json has generic parent->children relationship that can be N deep where N can be anything.
 * Logic should be to get each parentobject, get to the the lowest leaf children and process each child.
 * How to get to the lowest leaf? Leaf child has '"hasChildren": false'
 *
 * For resource json, the structure is this: top->agent (agent) -> domain (subresource) -> component
 * For nonCompliance resources, the path is: top -> children -> version -> component
 * 1. Write a method to return the lowest leaf children as jsonArray and then process.
 * 2. From resources json, if version is empty, it is not a component to deploy. if version if non-empty, those are the actual compnent
 * 3. From nonCompliance resource, it will have entries that were failed in depolyment.
 */

        // Failed to deploy list:
//        Set<String> failedComponents = getFailedComponents(nonCompliantResourceJSON);
//        Map<String, List<String>> versionFileMap = new HashMap<>();
//        for (Object item : allResourceJSON) {
//            JSONObject jsonObject = (JSONObject) item;
//            if (jsonObject == null) continue;
//            JSONArray childArray = getLowestLevelChildren(jsonObject, new JSONArray());
//            if (childArray.isEmpty()) continue;
//            for (Object child : childArray) {
//                JSONObject childObject = (JSONObject) child;
//                JSONArray jsonVersions = (JSONArray) childObject.get("versions");
//                if (jsonVersions == null || jsonVersions.size() == 0) continue;
//                JSONObject versionObject = (JSONObject) jsonVersions.get(0);
//                // get version fileTree and build data.
//                List<String> physicalFileNames = versionFileMap.get(str(versionObject, "id"));
//                if (CollectionUtils.isEmpty(physicalFileNames)) {
//                    physicalFileNames = getPhysicalFileList(application, versionObject);
//                    versionFileMap.put(str(versionObject, "id"), physicalFileNames);
//                }
//                for (String fileName : physicalFileNames) {
//                    environmentStatuses.add(buildUdeployEnvResCompData(environment, application, versionObject, fileName, childObject, failedComponents));
//                }
//            }
//        }
        return environmentStatuses;
    }

    private List<String> getPhysicalFileList(IcartApplication application, JSONObject versionObject) {
        List<String> list = new ArrayList<>();
//        String fileTreeUrl = "deploy/version/" + str(versionObject, "id") + "/fileTree";
//        ResponseEntity<String> fileTreeResponse = makeRestCall(
//                application.getInstanceUrl(), fileTreeUrl);
//        JSONArray fileTreeJson = paresAsArray(fileTreeResponse);
//        for (Object f : fileTreeJson) {
//            JSONObject fileJson = (JSONObject) f;
//            list.add(cleanFileName(str(fileJson, "name"), str(versionObject, "name")));
//        }
        //fake codes
        list.add("fkeFileName");
        return list;
    }

    private Set<String> getFailedComponents(JSONArray nonCompliantResourceJSON) {
        HashSet<String> failedComponents = new HashSet<>();
//        for (Object nonCompItem : nonCompliantResourceJSON) {
//            JSONArray nonCompChildrenArray = (JSONArray) ((JSONObject) nonCompItem)
//                    .get("children");
//            for (Object nonCompChildItem : nonCompChildrenArray) {
//                JSONObject nonCompChildObject = (JSONObject) nonCompChildItem;
//                JSONObject nonCompVersionObject = (JSONObject) nonCompChildObject
//                        .get("version");
//                if (nonCompVersionObject == null) continue;
//                JSONObject nonCompComponentObject =
//                        (JSONObject) nonCompVersionObject.get("component");
//                if (nonCompComponentObject != null) {
//                    failedComponents.add(str(nonCompComponentObject, "name"));
//                }
//            }
//        }
        //fake codes
        failedComponents.add("failedComponentsname");
        return failedComponents;
    }

    private String cleanFileName(String fileName, String version) {
        if (fileName.contains("-" + version))
            return fileName.replace("-" + version, "");
        if (fileName.contains(version))
            return fileName.replace(version, "");
        return fileName;
    }

    private IcartEnvResCompData buildUdeployEnvResCompData(Environment environment, IcartApplication application, JSONObject versionObject, String fileName, JSONObject childObject, Set<String> failedComponents) {
        IcartEnvResCompData data = new IcartEnvResCompData();
//        data.setEnvironmentName(environment.getName());
//        data.setCollectorItemId(application.getId());
//        data.setComponentVersion(str(versionObject, "name"));
//        data.setAsOfDate(date(versionObject, "created"));
//        data.setDeployed(!failedComponents.contains(str(childObject, "name")));
//        data.setComponentName(fileName);
//        data.setOnline("ONLINE".equalsIgnoreCase(str(
//                childObject, "status")));
//        JSONObject resource = (JSONObject) childObject.get("parent");
//        if (resource != null) {
//            data.setResourceName(str(resource, "name"));
//        }
        //fake codes
		data.setEnvironmentName(environment.getName());
		data.setCollectorItemId(application.getId());
		data.setComponentVersion("compVersion");
		data.setAsOfDate(0);
		data.setDeployed(true);
		data.setComponentName(fileName);
		data.setOnline(true);
		return data;
	}

    private JSONArray getLowestLevelChildren(JSONObject topParent, JSONArray returnArray) {
        JSONArray jsonChildren = (JSONArray) topParent.get("children");
//
//        if (jsonChildren != null && jsonChildren.size() > 0) {
//            for (Object child : jsonChildren) {
//                if (!hasChildren((JSONObject) child)) {
//                    returnArray.add(child);
//                } else {
//                    getLowestLevelChildren((JSONObject) child, returnArray);
//                }
//            }
//        }
        return returnArray;
    }

    private boolean hasChildren(JSONObject object) {
        return (boolean) object.get("hasChildren");
    }
    // ////// Helpers

    private ResponseEntity<String> makeRestCall(String instanceUrl,
                                                String endpoint) {
        String url = normalizeUrl(instanceUrl, "/rest/" + endpoint);
        ResponseEntity<String> response = null;
        try {
            response = restOperations.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(createHeaders()), String.class);

        } catch (RestClientException re) {
            LOGGER.error("Error with REST url: " + url);
            LOGGER.error(re.getMessage());
        }
        return response;
    }

    private String normalizeUrl(String instanceUrl, String remainder) {
        return StringUtils.removeEnd(instanceUrl, "/") + remainder;
    }

    protected HttpHeaders createHeaders() {
        String auth = icartSettings.getUsername() + ":"
                + icartSettings.getPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(
                StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }

    private JSONArray paresAsArray(ResponseEntity<String> response) {
        if (response == null)
            return new JSONArray();
        try {
            return (JSONArray) new JSONParser().parse(response.getBody());
        } catch (ParseException pe) {
            LOGGER.debug(response.getBody());
            LOGGER.error(pe.getMessage());
        }
        return new JSONArray();
    }

    private String str(JSONObject json, String key) {
        Object value = json.get(key);
        return value == null ? null : value.toString();
    }

    private long date(JSONObject jsonObject, String key) {
        Object value = jsonObject.get(key);
        return value == null ? 0 : (long) value;
    }
}
