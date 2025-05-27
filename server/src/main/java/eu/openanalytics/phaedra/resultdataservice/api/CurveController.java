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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/curves")
public class CurveController {

  private final CurveService curveService;

  public CurveController(CurveService curveService) {
    this.curveService = curveService;
  }

  @PostMapping
  public ResponseEntity<CurveDTO> createCurve(@RequestBody CurveDTO curveDTO) {
    CurveDTO result = curveService.createCurve(curveDTO);
    return new ResponseEntity<>(result, HttpStatus.CREATED);
  }

  @PutMapping()
  public ResponseEntity<CurveDTO> updateCurve(@RequestBody CurveDTO curveDTO) {
    CurveDTO result = curveService.updateCurve(curveDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<List<CurveDTO>> getCurves(@RequestParam Optional<Long> plateId) {
    if (plateId.isPresent()) {
      return new ResponseEntity<>(curveService.getCurveByPlateId(plateId.get()), HttpStatus.OK);
    }

    return new ResponseEntity<>(curveService.getAllCurves(), HttpStatus.OK);
  }

  @GetMapping("/{plateId}/latest")
  public ResponseEntity<List<CurveDTO>> getCurves(@PathVariable Long plateId) {
    List<CurveDTO> results = curveService.getLatestCurveByPlateId(plateId);
    if (CollectionUtils.isNotEmpty(results)) {
      return new ResponseEntity<>(results, HttpStatus.OK);
    }

    return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
  }
}
