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

import edu.harvard.iq.dvn.core.study.StudyLock;

public class Util {

    public static String getStudyLockMessage(StudyLock studyLock, String globalId) {
        if (studyLock != null) {
            String detail = studyLock.getDetail() != null ? studyLock.getDetail() : "";
            String startTime = studyLock.getStartTime() != null ? studyLock.getStartTime().toString() : "";
            return "Study lock for " + globalId + " detected with message '" + detail + "' and start time of '" + startTime + "'. Please try again.";
        } else {
            return "Study lock for " + globalId + " was present momentarily. Please try again.";
        }
    }
}
