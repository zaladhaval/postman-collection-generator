package com.collection.creator.service;

import com.collection.creator.config.PostmanCollectionProperties;
import com.collection.creator.model.Body;
import com.collection.creator.model.Header;
import com.collection.creator.model.Info;
import com.collection.creator.model.Item;
import com.collection.creator.model.Options;
import com.collection.creator.model.PostmanRequest;
import com.collection.creator.model.Raw;
import com.collection.creator.model.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for generating Postman collections from Spring REST endpoints.
 * 
 * <p>This service introspects all registered Spring MVC endpoints and generates
 * a Postman Collection v2.0.0 compatible JSON file.
 */
public class PostmanCollectionService {

    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionService.class);

    private final ApplicationContext applicationContext;
    private final PostmanCollectionProperties properties;
    private final ObjectMapper objectMapper;

    public PostmanCollectionService(ApplicationContext applicationContext, 
                                   PostmanCollectionProperties properties,
                                   ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate Postman collection and save to file.
     * 
     * @param apiPostFix Base URL to prepend to all endpoints (optional if configured in properties)
     * @return Path to the generated collection file
     * @throws IOException if file writing fails
     */
    public String generateCollection(String apiPostFix) throws IOException {
        logger.info("Starting Postman collection generation");
        
        String baseUrl = determineBaseUrl(apiPostFix);
        PostmanRequest postmanRequest = buildPostmanCollection(baseUrl);
        String outputPath = writeCollectionToFile(postmanRequest);
        
        logger.info("Postman collection generated successfully at: {}", outputPath);
        return outputPath;
    }

    /**
     * Determine the base URL to use for the collection.
     */
    private String determineBaseUrl(String apiPostFix) {
        if (StringUtils.isNotBlank(properties.getBaseUrl())) {
            return properties.getBaseUrl();
        }
        return StringUtils.defaultString(apiPostFix, "");
    }

    /**
     * Build the complete Postman collection structure.
     */
    private PostmanRequest buildPostmanCollection(String baseUrl) {
        RequestMappingHandlerMapping requestMappingHandlerMapping =
                applicationContext.getBean("requestMappingHandlerMapping",
                        RequestMappingHandlerMapping.class);
        
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        
        PostmanRequest postmanRequest = new PostmanRequest();
        postmanRequest.setInfo(createCollectionInfo());
        postmanRequest.setItem(createCollectionItems(handlerMethods, baseUrl));
        
        return postmanRequest;
    }

    /**
     * Create collection metadata.
     */
    private Info createCollectionInfo() {
        Info info = new Info();
        info.setName(properties.getCollection().getName());
        info.setSchema(properties.getCollection().getSchema());
        return info;
    }

    /**
     * Create collection items from all registered endpoints.
     */
    private List<Item> createCollectionItems(Map<RequestMappingInfo, HandlerMethod> handlerMethods, String baseUrl) {
        List<Item> itemList = new ArrayList<>();
        
        handlerMethods.forEach((requestMappingInfo, handlerMethod) -> {
            try {
                Set<PathPattern> urlPatterns = requestMappingInfo.getPathPatternsCondition().getPatterns();
                Set<RequestMethod> httpMethods = requestMappingInfo.getMethodsCondition().getMethods();
                
                if (CollectionUtils.isEmpty(urlPatterns) || CollectionUtils.isEmpty(httpMethods)) {
                    return;
                }
                
                Map<String, Object> requestBodyDefaults = new HashMap<>();
                Map<String, Object> requestParamDefaults = new HashMap<>();
                
                extractParameterDefaults(handlerMethod, requestBodyDefaults, requestParamDefaults);
                
                for (PathPattern pattern : urlPatterns) {
                    for (RequestMethod method : httpMethods) {
                        Item item = createItem(pattern, method, baseUrl, requestBodyDefaults, requestParamDefaults);
                        itemList.add(item);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to process endpoint: {}", handlerMethod.getMethod().getName(), e);
            }
        });
        
        return itemList;
    }

    /**
     * Extract default values for request parameters.
     */
    private void extractParameterDefaults(HandlerMethod handlerMethod,
                                         Map<String, Object> requestBodyDefaults,
                                         Map<String, Object> requestParamDefaults) {
        Parameter[] parameters = handlerMethod.getMethod().getParameters();
        
        if (parameters == null || parameters.length == 0) {
            return;
        }
        
        for (Parameter parameter : parameters) {
            Annotation[] annotations = parameter.getAnnotations();
            
            if (Arrays.stream(annotations).anyMatch(a -> a instanceof RequestBody)) {
                extractRequestBodyDefaults(parameter, requestBodyDefaults);
            } else if (Arrays.stream(annotations).anyMatch(a -> a instanceof RequestParam)) {
                extractRequestParamDefaults(parameter, annotations, requestParamDefaults);
            }
        }
    }

    /**
     * Extract default values for @RequestBody parameters.
     */
    private void extractRequestBodyDefaults(Parameter parameter, Map<String, Object> requestBodyDefaults) {
        List<Field> fieldList = FieldUtils.getAllFieldsList(parameter.getType());
        for (Field field : fieldList) {
            Class<?> fieldType = field.getType();
            requestBodyDefaults.put(field.getName(), getDefaultValueForType(fieldType));
        }
    }

    /**
     * Extract default values for @RequestParam parameters.
     */
    private void extractRequestParamDefaults(Parameter parameter, Annotation[] annotations,
                                            Map<String, Object> requestParamDefaults) {
        RequestParam annotation = (RequestParam) Arrays.stream(annotations)
                .filter(a -> a instanceof RequestParam)
                .findFirst()
                .orElse(null);
        
        if (annotation == null) {
            return;
        }
        
        Object data = null;
        if (StringUtils.isNotBlank(annotation.defaultValue()
                .replaceAll("\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n", "").trim())) {
            data = annotation.defaultValue();
        } else {
            data = getDefaultValueForType(parameter.getType());
        }
        
        String paramName = StringUtils.isNotBlank(annotation.value().trim()) ?
                annotation.value() : parameter.getName();
        
        if (data == null || StringUtils.isBlank(data.toString())) {
            data = "{" + paramName + "}";
        }
        
        requestParamDefaults.put(paramName, data);
    }

    /**
     * Create a single collection item for an endpoint.
     */
    private Item createItem(PathPattern pattern, RequestMethod method, String baseUrl,
                           Map<String, Object> requestBodyDefaults,
                           Map<String, Object> requestParamDefaults) throws JsonProcessingException {
        Item item = new Item();
        item.setName(pattern.getPatternString() + "_" + method.name().toUpperCase());
        
        Request request = new Request();
        request.setMethod(method.name().toUpperCase());
        request.setHeader(createHeaders());
        
        if (MapUtils.isNotEmpty(requestBodyDefaults)) {
            request.setBody(createBody(requestBodyDefaults));
        }
        
        request.setUrl(buildUrl(baseUrl, pattern.getPatternString(), requestParamDefaults));
        
        item.setRequest(request);
        return item;
    }

    /**
     * Create request headers.
     */
    private List<Header> createHeaders() {
        List<Header> headers = new ArrayList<>();
        
        if (properties.getAuthorization().isEnabled()) {
            Header authHeader = new Header();
            authHeader.setKey(properties.getAuthorization().getHeaderName());
            authHeader.setValue(properties.getAuthorization().getHeaderValue());
            authHeader.setType(properties.getAuthorization().getHeaderType());
            headers.add(authHeader);
        }
        
        return headers;
    }

    /**
     * Create request body.
     */
    private Body createBody(Map<String, Object> requestBodyDefaults) throws JsonProcessingException {
        Body body = new Body();
        body.setMode("raw");
        body.setRaw(objectMapper.writeValueAsString(requestBodyDefaults));
        
        Options options = new Options();
        Raw raw = new Raw();
        raw.setLanguage("json");
        options.setRaw(raw);
        body.setOptions(options);
        
        return body;
    }

    /**
     * Build the complete URL with query parameters.
     */
    private String buildUrl(String baseUrl, String path, Map<String, Object> requestParamDefaults) {
        StringBuilder url = new StringBuilder(baseUrl).append(path);
        
        if (MapUtils.isNotEmpty(requestParamDefaults)) {
            url.append("?");
            List<String> queryParams = new ArrayList<>();
            for (Map.Entry<String, Object> entry : requestParamDefaults.entrySet()) {
                queryParams.add(entry.getKey() + "=" + entry.getValue());
            }
            url.append(String.join("&", queryParams));
        }
        
        return url.toString();
    }

    /**
     * Write the collection to a file.
     */
    private String writeCollectionToFile(PostmanRequest postmanRequest) throws IOException {
        String finalJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(postmanRequest);
        
        Path outputPath = Paths.get(properties.getOutput().getFullPath());
        
        // Create parent directories if they don't exist
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        
        // Delete existing file if present
        FileUtils.deleteQuietly(outputPath.toFile());
        
        // Write new file
        Files.write(outputPath, finalJson.getBytes());
        
        return outputPath.toAbsolutePath().toString();
    }

    /**
     * Get default value for a given type.
     */
    private static Object getDefaultValueForType(Class<?> type) {
        if (type.isPrimitive()) {
            if (type.equals(int.class)) return 0;
            if (type.equals(double.class)) return 0.0;
            if (type.equals(float.class)) return 0.0f;
            if (type.equals(long.class)) return 0L;
            if (type.equals(boolean.class)) return false;
            if (type.equals(char.class)) return '\u0000';
            if (type.equals(byte.class)) return (byte) 0;
            if (type.equals(short.class)) return (short) 0;
        } else {
            if (type.equals(String.class)) return "";
            if (type.equals(Integer.class)) return null;
            if (type.equals(Double.class)) return null;
            if (type.equals(Float.class)) return null;
            if (type.equals(Long.class)) return null;
            if (type.equals(Boolean.class)) return null;
            if (type.equals(Character.class)) return null;
            if (Collection.class.isAssignableFrom(type)) return Collections.emptyList();
            if (Map.class.isAssignableFrom(type)) return Collections.emptyMap();
        }
        return null;
    }
}

