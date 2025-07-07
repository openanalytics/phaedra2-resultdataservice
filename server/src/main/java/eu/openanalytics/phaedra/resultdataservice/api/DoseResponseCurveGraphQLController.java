/**
 * Phaedra II
 * <p>
 * Copyright (C) 2016-2024 Open Analytics
 * <p>
 * ===========================================================================
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * Apache License as published by The Apache Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Apache
 * License for more details.
 * <p>
 * You should have received a copy of the Apache License along with this program.  If not, see
 * <http://www.apache.org/licenses/>
 */
package eu.openanalytics.phaedra.resultdataservice.api;

import eu.openanalytics.phaedra.resultdataservice.dto.CurveDTO;
import eu.openanalytics.phaedra.resultdataservice.service.CurveService;
import java.util.List;
import java.util.Optional;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DoseResponseCurveGraphQLController {

  private final CurveService curveService;

  public DoseResponseCurveGraphQLController(CurveService curveService) {
    this.curveService = curveService;
  }

  @QueryMapping
  public CurveDTO getCurveById(@Argument long curveId) {
    return curveService.getCurveById(curveId);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesByPlateId(@Argument long plateId) {
    return curveService.getLatestCurveByPlateId(plateId);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesByPlateIds(@Argument List<Long> plateIds) {
    return curveService.getLatestCurveByPlateIds(plateIds);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesBySubstanceName(@Argument String substanceName) {
    return curveService.getCurvesBySubstanceName(substanceName);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesBySubstanceType(@Argument String substanceType) {
    return curveService.getCurvesBySubstanceType(substanceType);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesByFeatureId(@Argument long featureId) {
    return curveService.getCurvesByFeatureId(featureId);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesThatIncludesWellId(@Argument long wellId) {
    return curveService.getCurvesThatIncludesWellId(wellId);
  }

  @QueryMapping
  public List<CurveDTO> getCurvesByWellIds(@Argument List<Long> wellIds, @Argument Optional<Long> resultSetId) {
    return curveService.getCurvesByWellIds(wellIds, resultSetId);
  }
}
