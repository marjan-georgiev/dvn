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
package edu.harvard.iq.dvn.api.resources;

import edu.harvard.iq.dvn.api.exceptions.*;
import edu.harvard.iq.dvn.api.entities.*;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author leonidandreev
 */
/*
 * This app provides manual, user-controlled registration for the 
 * entities and services of the data-sharing API.
 */
public class DvnApiApp extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register root resources/providers:
        classes.add(DvnApiRootResource.class);
        // register service entities:
        classes.add(MetadataInstance.class);
        classes.add(MetadataFormats.class);
        classes.add(MetadataSearchFields.class);
        classes.add(MetadataSearchResults.class);
        classes.add(DownloadInfo.class);
        classes.add(DownloadInstance.class);
        // register writers for the entities above:
        classes.add(MetadataWriter.class);
        classes.add(MetadataFormatsWriter.class);
        classes.add(MetadataSearchFieldsWriter.class);
        classes.add(MetadataSearchResultsWriter.class);
        classes.add(DownloadInfoWriter.class);
        classes.add(DownloadInstanceWriter.class);
        // register custom exceptions and mappers:
        classes.add(NotFoundException.class);
        classes.add(AuthorizationRequiredException.class);
        classes.add(ServiceUnavailableException.class);
        classes.add(PermissionDeniedException.class);
        classes.add(NotFoundExceptionMapper.class);
        classes.add(AuthorizationRequiredExceptionMapper.class);
        classes.add(ServiceUnavailableExceptionMapper.class);
        classes.add(PermissionDeniedExceptionMapper.class);
        // register resource beans that will supply the resources above:
        classes.add(MetadataResourceBean.class);
	classes.add(MetadataFormatsResourceBean.class);
        classes.add(MetadataSearchFieldsResourceBean.class);
        classes.add(MetadataSearchResultsResourceBean.class);
        classes.add(DownloadInfoResourceBean.class);
        classes.add(DownloadResourceBean.class);
        // and the main EJB singletons, the workhorses of the API:
        classes.add(MetadataSingletonBean.class);
        classes.add(FileAccessSingletonBean.class);
        
        return classes;
    }
}