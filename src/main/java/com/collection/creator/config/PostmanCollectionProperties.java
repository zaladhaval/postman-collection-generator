package com.collection.creator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Postman Collection Generator.
 *
 * <p>These properties can be configured in application.properties or application.yml:
 * <pre>
 * postman.collection.generator.enabled=true
 * postman.collection.generator.output.directory=./
 * postman.collection.generator.output.filename=collection.json
 * postman.collection.generator.collection.name=API Collection
 * postman.collection.generator.authorization.header-name=Authorization
 * postman.collection.generator.authorization.header-value={{logintoken}}
 * postman.collection.generator.base-url=http://localhost:8080
 * </pre>
 *
 * <p>This library provides only the {@link com.collection.creator.service.PostmanCollectionService}
 * for programmatic access. Inject the service into your own controllers or services to generate
 * Postman collections.
 */
@ConfigurationProperties(prefix = "postman.collection.generator")
public class PostmanCollectionProperties {

    /**
     * Enable or disable the Postman collection generator.
     */
    private boolean enabled = true;

    /**
     * Output file configuration.
     */
    private Output output = new Output();

    /**
     * Postman collection metadata configuration.
     */
    private Collection collection = new Collection();

    /**
     * Authorization header configuration.
     */
    private Authorization authorization = new Authorization();

    /**
     * Base URL to prepend to all API endpoints in the collection.
     * This is used when calling generateCollection() method.
     */
    private String baseUrl = "";

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Output file configuration.
     */
    public static class Output {
        /**
         * Directory where the collection file will be saved.
         */
        private String directory = "./";

        /**
         * Name of the generated collection file.
         */
        private String filename = "collection.json";

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        /**
         * Get the full path to the output file.
         */
        public String getFullPath() {
            String dir = directory.endsWith("/") ? directory : directory + "/";
            return dir + filename;
        }
    }

    /**
     * Postman collection metadata configuration.
     */
    public static class Collection {
        /**
         * Name of the Postman collection.
         */
        private String name = "API Collection";

        /**
         * Postman collection schema version.
         */
        private String schema = "https://schema.getpostman.com/json/collection/v2.0.0/collection.json";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    /**
     * Authorization header configuration.
     */
    public static class Authorization {
        /**
         * Enable or disable adding authorization header to requests.
         */
        private boolean enabled = true;

        /**
         * Name of the authorization header.
         */
        private String headerName = "Authorization";

        /**
         * Value of the authorization header (supports Postman variables like {{token}}).
         */
        private String headerValue = "{{logintoken}}";

        /**
         * Type of the header.
         */
        private String headerType = "text";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        public String getHeaderValue() {
            return headerValue;
        }

        public void setHeaderValue(String headerValue) {
            this.headerValue = headerValue;
        }

        public String getHeaderType() {
            return headerType;
        }

        public void setHeaderType(String headerType) {
            this.headerType = headerType;
        }
    }
}

