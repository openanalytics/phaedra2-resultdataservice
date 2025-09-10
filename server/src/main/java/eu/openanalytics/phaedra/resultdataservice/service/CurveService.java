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
package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.dto.CurveDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.CurveInputParamDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.CurveOutputParamDTO;
import eu.openanalytics.phaedra.resultdataservice.model.Curve;
import eu.openanalytics.phaedra.resultdataservice.model.CurveInputParameter;
import eu.openanalytics.phaedra.resultdataservice.model.CurveOutputParameter;
import eu.openanalytics.phaedra.resultdataservice.repository.CurveInputParameterRepository;
import eu.openanalytics.phaedra.resultdataservice.repository.CurveOutputParameterRepository;
import eu.openanalytics.phaedra.resultdataservice.repository.CurveRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurveService {

  private final ModelMapper modelMapper;
  private final CurveRepository curveRepository;
  private final CurveOutputParameterRepository curveOutputParameterRepository;
  private final CurveInputParameterRepository curveInputParameterRepository;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public CurveDTO createCurve(CurveDTO curveDTO) {
    Curve curve = modelMapper.map(curveDTO);
    Curve created = curveRepository.save(curve);

    if (CollectionUtils.isNotEmpty(curveDTO.getInputParameters())) {
      curveDTO.getInputParameters().forEach(curveInputParamDTO -> {
        logger.info("Saving curve input parameter {} for curve {}", curveInputParamDTO.getName(), created.getId());
        CurveInputParameter curveInputParameter = new CurveInputParameter();
        curveInputParameter.setCurveId(created.getId());
        curveInputParameter.setName(curveInputParamDTO.getName());
        curveInputParameter.setStringValue(curveInputParamDTO.getStringValue());
        curveInputParameter.setNumericValue(curveInputParamDTO.getNumericValue());
        curveInputParameterRepository.save(curveInputParameter);
      });
    }

    if (CollectionUtils.isNotEmpty(curveDTO.getCurveOutputParameters())) {
      curveDTO.getCurveOutputParameters().forEach(curveOutputParamDTO -> {
        logger.info("Saving curve output parameter {} for curve {}", curveOutputParamDTO.getName(), created.getId());
        CurveOutputParameter curveOutputParameter = new CurveOutputParameter();
        curveOutputParameter.setCurveId(created.getId());
        curveOutputParameter.setName(curveOutputParamDTO.getName());
        curveOutputParameter.setNumericValue(curveOutputParamDTO.getNumericValue());
        curveOutputParameter.setStringValue(curveOutputParamDTO.getStringValue());
        curveOutputParameterRepository.save(curveOutputParameter);
      });
    }

    List<CurveOutputParamDTO> curveOutputParameters = curveOutputParameterRepository.findCurveOutputParametersByCurveId(created.getId())
        .stream()
        .map(modelMapper::map)
        .toList();
    List<CurveInputParamDTO> curveInputParameters = curveInputParameterRepository.findCurveOutputParametersByCurveId(created.getId())
        .stream()
        .map(modelMapper::map)
        .toList();
    logger.info("A new curve for {} and featureId {} has been created!", curveDTO.getSubstanceName(), curveDTO.getFeatureId());
    return curveDTO.withId(created.getId())
        .withCurveOutputParameters(curveOutputParameters)
        .withInputParameters(curveInputParameters);
  }

  public CurveDTO getCurveById(Long curveId) {
    return curveRepository.findById(curveId)
        .map(this::toCurveDTOWithCurveOutputParameters)
        .orElse(null);
  }

  public List<CurveDTO> getCurveByPlateId(Long plateId) {
    return curveRepository.findCurveByPlateId(plateId).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getLatestCurveByPlateId(Long plateId) {
    return curveRepository.findLatestCurvesByPlateId(plateId).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getLatestCurveByPlateIds(List<Long> plateIds) {
    return curveRepository.findLatestCurvesByPlateIdIn(plateIds).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getAllCurves() {
    return ((List<Curve>) curveRepository.findAll()).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getCurvesBySubstanceName(String substanceName) {
    return curveRepository.findCurvesBySubstanceName(substanceName).stream()
        .map(this::toCurveDTOWithCurveOutputParameters).
        toList();
  }

  public List<CurveDTO> getCurvesBySubstanceType(String substanceType) {
    return curveRepository.findCurvesBySubstanceType(substanceType).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getCurvesByFeatureId(long featureId) {
    return curveRepository.findCurvesByFeatureId(featureId).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getCurvesThatIncludesWellId(long wellId) {
    return curveRepository.findCurvesThatIncludesWellId(wellId).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  public List<CurveDTO> getCurvesByWellIds(List<Long> wellIds, Optional<Long> resultSetId) {
    if (resultSetId.isPresent()) {
      return curveRepository.findCurvesByWellIdsAndResultSetId(wellIds, resultSetId.get()).stream()
          .map(this::toCurveDTOWithCurveOutputParameters)
          .toList();
    }
    return curveRepository.findLatestCurvesByWellIds(wellIds).stream()
        .map(this::toCurveDTOWithCurveOutputParameters)
        .toList();
  }

  private CurveDTO toCurveDTOWithCurveOutputParameters(Curve curve) {
    List<CurveOutputParameter> curveProperties = curveOutputParameterRepository.findCurveOutputParametersByCurveId(curve.getId());
    return modelMapper.map(curve)
        .withCurveOutputParameters(curveProperties.stream()
            .map(modelMapper::map)
            .toList());
  }

  public CurveDTO updateCurve(CurveDTO curveDTO) {
    return null;
  }
}
