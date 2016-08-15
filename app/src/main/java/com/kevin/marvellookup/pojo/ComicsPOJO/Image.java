
package com.kevin.marvellookup.pojo.ComicsPOJO;

import java.util.HashMap;
import java.util.Map;

public class Image {

    private String path;
    private String extension;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The path
     */
    public String getPath() {
        return path;
    }

    /**
     * 
     * @param path
     *     The path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * @return
     *     The extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * 
     * @param extension
     *     The extension
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
