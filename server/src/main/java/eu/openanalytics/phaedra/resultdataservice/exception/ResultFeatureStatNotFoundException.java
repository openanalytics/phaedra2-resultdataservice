/**
 * Phaedra II
 *
 * Copyright (C) 2016-2024 Open Analytics
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.exception;

import eu.openanalytics.phaedra.util.exceptionhandling.EntityNotFoundException;

public class ResultFeatureStatNotFoundException extends EntityNotFoundException {

    public ResultFeatureStatNotFoundException(long resultDataId) {
        super(String.format("No ResultFeatureStat found with id %s!", resultDataId));
    }

    public ResultFeatureStatNotFoundException(String statName) {
        super(String.format("No ResultFeatureStat found with name %s!", statName));
    }

    public ResultFeatureStatNotFoundException(String statName, String wellType) {
        super(String.format("No ResultFeatureStat found with name %s and well type %s!", statName, wellType));
    }
}
