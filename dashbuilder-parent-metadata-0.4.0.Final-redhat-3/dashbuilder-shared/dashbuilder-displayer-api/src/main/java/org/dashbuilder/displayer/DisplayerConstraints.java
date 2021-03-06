/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.displayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dashbuilder.dataset.DataSetLookupConstraints;
import org.dashbuilder.dataset.ValidationError;

/**
 * Every Displayer implementation use this class to specify what are the supported DisplayerSettings attributes
 * as well as the supported data sets in order to fit the displayer needs.
 */
public class DisplayerConstraints {

    protected DataSetLookupConstraints dataSetLookupConstraints;
    protected Set<DisplayerAttributeDef> supportedEditorAttributes;
    protected Set<String> supportedEditorAttrStrings;

    public DisplayerConstraints(DataSetLookupConstraints dataSetLookupConstraints) {
        this.dataSetLookupConstraints = dataSetLookupConstraints;
        supportedEditorAttributes = new HashSet<DisplayerAttributeDef>();
        supportedEditorAttrStrings = new HashSet<String>();
    }

    public DisplayerConstraints supportsAttribute( DisplayerAttributeDef attributeDef ) {

        // Support the attribute and all its ancestors.
        DisplayerAttributeDef _attr = attributeDef;
        while (_attr != null) {
            supportedEditorAttributes.add(_attr);
            supportedEditorAttrStrings.add(_attr.getFullId());
            _attr = _attr.getParent();
        }
        // ... and all its descendants as well.
        if (attributeDef instanceof DisplayerAttributeGroupDef) {
            for (DisplayerAttributeDef member : ((DisplayerAttributeGroupDef) attributeDef).getChildren()) {
                supportsAttribute(member);
            }
        }
        return this;
    }

    public Set<DisplayerAttributeDef> getSupportedAttributes() {
        return supportedEditorAttributes;
    }

    public DataSetLookupConstraints getDataSetLookupConstraints() {
        return dataSetLookupConstraints;
    }

    public DisplayerConstraints setDataSetLookupConstraints(DataSetLookupConstraints dataSetLookupConstraints) {
        this.dataSetLookupConstraints = dataSetLookupConstraints;
        return this;
    }

    public void removeUnsupportedAttributes(DisplayerSettings displayerSettings) {
        Map<String,String> settingsMap = displayerSettings.getSettingsFlatMap();
        for (String setting : new HashSet<String>(settingsMap.keySet())) {
            if (!supportedEditorAttrStrings.contains(setting)) {
                displayerSettings.removeDisplayerSetting(setting);
            }
        }
    }

    public ValidationError check(DisplayerSettings settings) {
        if (dataSetLookupConstraints == null) {
            return createValidationError(ERROR_DATASET_LOOKUP_CONSTRAINTS_NOT_FOUND);
        }
        if (settings.getDataSet() != null) {
            ValidationError error = dataSetLookupConstraints.check(settings.getDataSet());
            if (error != null) return error;
        }
        else if (settings.getDataSetLookup() != null) {
            ValidationError error = dataSetLookupConstraints.check(settings.getDataSetLookup());
            if (error != null) return error;
        }
        return null;
    }

    public static final int ERROR_DATASET_LOOKUP_CONSTRAINTS_NOT_FOUND = 301;

    protected ValidationError createValidationError(int error) {
        switch (error) {
            case ERROR_DATASET_LOOKUP_CONSTRAINTS_NOT_FOUND:
                return new ValidationError(error, "Missing DataSetLookupContraints instance");
        }
        return new ValidationError(error);
    }
}