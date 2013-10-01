/*
 Copyright (C) 2005-2012, by the President and Fellows of Harvard College.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 Dataverse Network - A web application to share, preserve and analyze research data.
 Developed at the Institute for Quantitative Social Science, Harvard University.
 Version 3.0.
 */
package edu.harvard.iq.dvn.api.datadeposit;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.swordapp.server.SwordError;
import org.swordapp.server.UriRegistry;

public class UrlManager {

    private static final Logger logger = Logger.getLogger(UrlManager.class.getCanonicalName());
    String originalUrl;
    SwordConfigurationImpl swordConfiguration = new SwordConfigurationImpl();
    String servlet;
    String targetType;
    String targetIdentifier;
    int port;

    void processUrl(String url) throws SwordError {
        logger.fine("URL was: " + url);
        this.originalUrl = url;
        URI javaNetUri;
        try {
            javaNetUri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Invalid URL syntax: " + url);
        }
        if (!"https".equals(javaNetUri.getScheme())) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "https is required but protocol was " + javaNetUri.getScheme());
        }
        this.port = javaNetUri.getPort();
        String[] urlPartsArray = javaNetUri.getPath().split("/");
        List<String> urlParts = Arrays.asList(urlPartsArray);
        String dataDepositApiBasePath;
        try {
            List<String> dataDepositApiBasePathParts;
            //             1 2   3   4            5  6       7          8         9
            // for example: /dvn/api/data-deposit/v1/swordv2/collection/dataverse/sword
            dataDepositApiBasePathParts = urlParts.subList(0, 6);
            dataDepositApiBasePath = StringUtils.join(dataDepositApiBasePathParts, "/");
        } catch (IndexOutOfBoundsException ex) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Error processing URL: " + url);
        }
        if (!dataDepositApiBasePath.equals(swordConfiguration.getBaseUrlPath())) {
            throw new SwordError(dataDepositApiBasePath + " found but " + swordConfiguration.getBaseUrlPath() + " expected");
        }
        try {
            this.servlet = urlParts.get(6);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Unable to determine servlet path from URL: " + url);
        }
        if (!servlet.equals("service-document")) {
            List<String> targetTypeAndIdentifier;
            try {
                //               6          7         8
                // for example: /collection/dataverse/sword
                targetTypeAndIdentifier = urlParts.subList(7, urlParts.size());
            } catch (IndexOutOfBoundsException ex) {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "No target components specified in URL: " + url);
            }
            this.targetType = targetTypeAndIdentifier.get(0);
            if (targetType != null) {
                if (targetType.equals("dataverse")) {
                    String dvAlias;
                    try {
                        dvAlias = targetTypeAndIdentifier.get(1);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "No dataverse alias provided in URL: " + url);
                    }
                    this.targetIdentifier = dvAlias;
                } else if (targetType.equals("study")) {
                    String globalId;
                    try {
                        List<String> globalIdParts = targetTypeAndIdentifier.subList(1, targetTypeAndIdentifier.size());
                        globalId = StringUtils.join(globalIdParts, "/");
                    } catch (IndexOutOfBoundsException ex) {
                        throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Invalid study global id provided in URL: " + url);
                    }
                    this.targetIdentifier = globalId;
                } else if (targetType.equals("file")) {
                    String fileIdString;
                    try {
                        // a user might reasonably pass in a filename as well [.get(2)] since
                        // we expose it in the statement of a study but we ignore it here
                        fileIdString = targetTypeAndIdentifier.get(1);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "No file id provided in URL: " + url);
                    }
                    this.targetIdentifier = fileIdString;
                } else {
                    throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "unsupported target type: " + targetType);
                }
            } else {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "Unable to determine target type from URL: " + url);
            }
            logger.fine("target type: " + targetType);
            logger.fine("target identifier: " + targetIdentifier);
        }
    }

    String getHostnamePlusBaseUrlPath(String url) throws SwordError {
        String optionalPort = "";
        URI u;
        try {
            u = new URI(url);
            int port = u.getPort();
            if (port != -1) {
                // https often runs on port 8181 in dev
                optionalPort = ":" + port;
            }
        } catch (URISyntaxException ex) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, "unable to part URL");
        }
        String hostName = System.getProperty("dvn.inetAddress");
        return "https://" + hostName + optionalPort + swordConfiguration.getBaseUrlPath();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getServlet() {
        return servlet;
    }

    public void setServlet(String servlet) {
        this.servlet = servlet;
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public void setTargetIdentifier(String targetIdentifier) {
        this.targetIdentifier = targetIdentifier;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
