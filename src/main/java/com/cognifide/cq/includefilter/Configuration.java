package com.cognifide.cq.includefilter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Include filter configuration.
 * 
 * @author tomasz.rekawek
 * 
 */
@Component(metatype = true, configurationFactory = true, 
	label = "Cognifide : SDI : Configuration", immediate = true, policy = ConfigurationPolicy.REQUIRE)
@Service(Configuration.class)
@Properties({
	@Property(name = Constants.SERVICE_VENDOR, value = "Cognifide"),
	@Property(name = Configuration.PROPERTY_FILTER_ENABLED, boolValue = Configuration.DEFAULT_FILTER_ENABLED, label = "Enabled", description = "Check to enable the filter"),
	@Property(name = Configuration.PROPERTY_FILTER_PATH, value = Configuration.DEFAULT_FILTER_PATH, label = "Base path", description = "This SDI configuration will work only for this path"),
	@Property(name = Configuration.PROPERTY_FILTER_RESOURCE_TYPES, cardinality = Integer.MAX_VALUE, label = "Resource types", description = "Filter will replace components with selected resource types"),
	@Property(name = Configuration.PROPERTY_INCLUDE_TYPE, value = Configuration.DEFAULT_INCLUDE_TYPE, label = "Include type", description = "Type of generated include tags", options = {
			@PropertyOption(name = "SSI", value = "Apache SSI"),
			@PropertyOption(name = "ESI", value = "ESI"),
			@PropertyOption(name = "JSI", value = "Javascript") }),
	@Property(name = Configuration.PROPERTY_ADD_COMMENT, boolValue = Configuration.DEFAULT_ADD_COMMENT, label = "Add comment", description = "Add comment to included components"),
	@Property(name = Configuration.PROPERTY_FILTER_SELECTOR, value = Configuration.DEFAULT_FILTER_SELECTOR, label = "Filter selector", description = "Selector used to mark included resources"),
	@Property(name = Configuration.PROPERTY_COMPONENT_TTL, label = "Component TTL", description = "\"Time to live\" cache header for rendered component (in seconds)"),
	@Property(name = Configuration.PROPERTY_REQUIRED_HEADER, value = Configuration.DEFAULT_REQUIRED_HEADER, label = "Required header", description = "SDI will work only for requests with given header"),
	@Property(name = Configuration.PROPERTY_IGNORE_URL_PARAMS, cardinality = Integer.MAX_VALUE, label = "Ignore URL params", description = "SDI will process the request even if it contains configured GET parameters"),
	@Property(name = Configuration.PROPERTY_REWRITE_PATH, boolValue = Configuration.DEFAULT_REWRITE_DISABLED, label = "Include path rewriting", description = "Check to enable include path rewriting"),
    @Property(name = Configuration.PROPERTY_SESSIONID_SELECTOR_ENABLED, boolValue = Configuration.DEFAULT_SESSIONID_SELECTOR_ENABLED, label = "Session id selector enabled", description = "Check to enable adding of the session id selector"),
})
public class Configuration {

	static final String PROPERTY_FILTER_PATH = "include-filter.config.path";

	static final String DEFAULT_FILTER_PATH = "/content";

	static final String PROPERTY_FILTER_ENABLED = "include-filter.config.enabled";

	static final boolean DEFAULT_FILTER_ENABLED = false;
    
    static final String PROPERTY_SESSIONID_SELECTOR_ENABLED = "include-filter.sessionid-selector.enabled";

    static final boolean DEFAULT_SESSIONID_SELECTOR_ENABLED = true;
    
	static final String PROPERTY_FILTER_RESOURCE_TYPES = "include-filter.config.resource-types";

	static final String PROPERTY_FILTER_SELECTOR = "include-filter.config.selector";

	static final String DEFAULT_FILTER_SELECTOR = "nocache";

	static final String PROPERTY_COMPONENT_TTL = "include-filter.config.ttl";
	
	static final String PROPERTY_INCLUDE_TYPE = "include-filter.config.include-type";

	static final String DEFAULT_INCLUDE_TYPE = "SSI";

	static final String PROPERTY_ADD_COMMENT = "include-filter.config.add_comment";

	static final boolean DEFAULT_ADD_COMMENT = false;

	static final String PROPERTY_REQUIRED_HEADER = "include-filter.config.required_header";

	static final String DEFAULT_REQUIRED_HEADER = "Server-Agent=Communique-Dispatcher";

	static final String PROPERTY_IGNORE_URL_PARAMS = "include-filter.config.ignoreUrlParams";

	static final String PROPERTY_REWRITE_PATH = "include-filter.config.rewrite";
	
	static final boolean DEFAULT_REWRITE_DISABLED = false;
	
	private boolean isEnabled;

	private String path;

	private String includeSelector;
	
	private int ttl;

	private List<String> resourceTypes;

	private boolean addComment;

	private String includeTypeName;

	private String requiredHeader;

	private List<String> ignoreUrlParams;
	
	private boolean rewritePath;
    
    private boolean isSessionIdSelectorEnabled;

	@Activate
	public void activate(ComponentContext context, Map<String, ?> properties) {
		isEnabled = PropertiesUtil.toBoolean(properties.get(PROPERTY_FILTER_ENABLED), DEFAULT_FILTER_ENABLED);
		path = PropertiesUtil.toString(properties.get(PROPERTY_FILTER_PATH), DEFAULT_FILTER_PATH);
		String[] resourceTypeList;
		resourceTypeList = PropertiesUtil.toStringArray(properties.get(PROPERTY_FILTER_RESOURCE_TYPES), new String[0]);
		for (int i = 0; i < resourceTypeList.length; i++) {
			String[] s = resourceTypeList[i].split(";");
			String name = s[0].trim();
			resourceTypeList[i] = name;
		}
		this.resourceTypes = Arrays.asList(resourceTypeList);

		includeSelector = PropertiesUtil.toString(properties.get(PROPERTY_FILTER_SELECTOR),
				DEFAULT_FILTER_SELECTOR);
		ttl = PropertiesUtil.toInteger(properties.get(PROPERTY_COMPONENT_TTL), -1);
		addComment = PropertiesUtil.toBoolean(properties.get(PROPERTY_ADD_COMMENT), DEFAULT_ADD_COMMENT);
		includeTypeName = PropertiesUtil
				.toString(properties.get(PROPERTY_INCLUDE_TYPE), DEFAULT_INCLUDE_TYPE);
		requiredHeader = PropertiesUtil.toString(properties.get(PROPERTY_REQUIRED_HEADER),
				DEFAULT_REQUIRED_HEADER);
		ignoreUrlParams = Arrays.asList(PropertiesUtil.toStringArray(properties.get(PROPERTY_IGNORE_URL_PARAMS), new String[0]));
		rewritePath = PropertiesUtil.toBoolean(properties.get(PROPERTY_REWRITE_PATH), DEFAULT_REWRITE_DISABLED);
        isSessionIdSelectorEnabled = PropertiesUtil.toBoolean(properties.get(PROPERTY_SESSIONID_SELECTOR_ENABLED), DEFAULT_SESSIONID_SELECTOR_ENABLED);
	}

	public String getBasePath() {
		return path;
	}

	public boolean hasIncludeSelector(SlingHttpServletRequest request) {
		return ArrayUtils.contains(request.getRequestPathInfo().getSelectors(), includeSelector);
	}

	public String getIncludeSelector() {
		return includeSelector;
	}
	
	public boolean hasTtlSet() {
		return ttl >= 0;
	}
	
	public int getTtl() {
		return ttl;
	}

	public boolean isSupportedResourceType(String resourceType) {
		return StringUtils.isNotBlank(resourceType) && resourceTypes.contains(resourceType);
	}

	public boolean getAddComment() {
		return addComment;
	}

	public String getIncludeTypeName() {
		return includeTypeName;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public String getRequiredHeader() {
		return requiredHeader;
	}

	public List<String> getIgnoreUrlParams() {
		return ignoreUrlParams;
	}

	public boolean isRewritePath() {
		return rewritePath;
	}

    public boolean isSessionIdSelectorEnabled() {
        return isSessionIdSelectorEnabled;
    }

    public void setSessionIdSelectorEnabled(boolean isSessionIdSelectorEnabled) {
        this.isSessionIdSelectorEnabled = isSessionIdSelectorEnabled;
    }
}
