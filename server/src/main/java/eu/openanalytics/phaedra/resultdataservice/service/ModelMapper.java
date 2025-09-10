/**
 * Phaedra II
 *
 * Copyright (C) 2016-2025 Open Analytics
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
package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.dto.CurveDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.CurveInputParamDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.CurveOutputParamDTO;
import eu.openanalytics.phaedra.resultdataservice.model.Curve;
import eu.openanalytics.phaedra.resultdataservice.model.CurveInputParameter;
import eu.openanalytics.phaedra.resultdataservice.model.CurveOutputParameter;
import java.util.List;

import org.modelmapper.Conditions;
import org.modelmapper.Converter;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.stereotype.Service;

import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.model.ResultFeatureStat;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;

@Service
public class ModelMapper {

    private final org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();

    public ModelMapper() {
        modelMapper.getConfiguration().setDestinationNamingConvention(NamingConventions.builder());
        modelMapper.getConfiguration().setDestinationNameTransformer(NameTransformers.builder());

        modelMapper.createTypeMap(ResultDataDTO.class, ResultData.ResultDataBuilder.class)
            .setPropertyCondition(Conditions.isNotNull());

        modelMapper.createTypeMap(ResultData.class, ResultDataDTO.ResultDataDTOBuilder.class)
            .setPropertyCondition(Conditions.isNotNull())
            .addMappings(mapper -> mapper.skip(ResultDataDTO.ResultDataDTOBuilder::resultFeatureStats));

        modelMapper.emptyTypeMap(ResultSetDTO.class, ResultSet.ResultSetBuilder.class)
            .setPropertyCondition(Conditions.isNotNull())
            .addMappings(mapper -> mapper
            		.using((Converter<List<ErrorDTO>, ResultSet.ErrorHolder>) context -> { return (context.getSource() == null) ? null : new ResultSet.ErrorHolder(context.getSource()); })
            		.map(ResultSetDTO::getErrors, ResultSet.ResultSetBuilder::errors))
            .implicitMappings();

        modelMapper.createTypeMap(ResultSet.class, ResultSetDTO.ResultSetDTOBuilder.class)
            .setPropertyCondition(Conditions.isNotNull());

        modelMapper.createTypeMap(ResultFeatureStat.class, ResultFeatureStatDTO.ResultFeatureStatDTOBuilder.class)
            .setPropertyCondition(Conditions.isNotNull());

        modelMapper.createTypeMap(ResultFeatureStatDTO.class, ResultFeatureStat.ResultFeatureStatBuilder.class)
            .setPropertyCondition(Conditions.isNotNull());

        modelMapper.validate(); // ensure that objects can be mapped
    }

    /**
     * Maps a {@link ResultDataDTO} to a {@link ResultData.ResultDataBuilder}.
     * The return value can be further customized by calling the builder methods.
     */
    public ResultData.ResultDataBuilder map(ResultDataDTO resultDataDTO) {
        ResultData.ResultDataBuilder builder = ResultData.builder();
        modelMapper.map(resultDataDTO, builder);
        return builder;
    }

    /**
     * Maps a {@link ResultData} to a {@link ResultDataDTO.ResultDataDTOBuilder}.
     * The return value can be further customized by calling the builder methods.
     */
    public ResultDataDTO.ResultDataDTOBuilder map(ResultData resultData) {
        ResultDataDTO.ResultDataDTOBuilder builder = ResultDataDTO.builder();
        modelMapper.map(resultData, builder);
        return builder;
    }

    /**
     * Maps a {@link ResultSetDTO} to a {@link ResultSet.ResultSetBuilder}.
     * The return value can be further customized by calling the builder methods.
     */
    public ResultSet.ResultSetBuilder map(ResultSetDTO resultSetDTO) {
        ResultSet.ResultSetBuilder builder = ResultSet.builder();
        modelMapper.map(resultSetDTO, builder);
        return builder;
    }

    /**
     * Maps a {@link ResultSet} to a {@link ResultSetDTO.ResultSetDTOBuilder}.
     * The return value can be further customized by calling the builder methods.
     */
    public ResultSetDTO.ResultSetDTOBuilder map(ResultSet resultSet) {
        ResultSetDTO.ResultSetDTOBuilder builder = ResultSetDTO.builder();
        modelMapper.map(resultSet, builder);
        return builder;
    }

    /**
     * Returns a Builder that contains the properties of {@link ResultSet}, which are updated with the
     * values of a {@link ResultSetDTO} while ignore properties in the {@link ResultSetDTO} that are null.
     * The return value can be further customized by calling the builder methods.
     * This function should be used for PUT requests.
     */
    public ResultSet.ResultSetBuilder map(ResultSetDTO resultSetDTO, ResultSet resultSet) {
        ResultSet.ResultSetBuilder builder = resultSet.toBuilder();
        modelMapper.map(resultSetDTO, builder);
        return builder;
    }

    public ResultFeatureStat.ResultFeatureStatBuilder map(ResultFeatureStatDTO resultFeatureStatDTO) {
        return modelMapper.map(resultFeatureStatDTO, ResultFeatureStat.ResultFeatureStatBuilder.class);
    }

    public ResultFeatureStatDTO.ResultFeatureStatDTOBuilder map(ResultFeatureStat resultFeatureStat) {
        return modelMapper.map(resultFeatureStat, ResultFeatureStatDTO.ResultFeatureStatDTOBuilder.class);
    }

    /**
     * Maps a {@link CurveDTO} to a {@link Curve}.
     */
    public Curve map(CurveDTO curveDTO) {
        Curve curve = new Curve();
        curve.setId(curveDTO.getId());
        curve.setSubstanceName(curveDTO.getSubstanceName());
        curve.setSubstanceType(curveDTO.getSubstanceType());
        curve.setPlateId(curveDTO.getPlateId());
        curve.setFeatureId(curveDTO.getFeatureId());
        curve.setProtocolId(curveDTO.getProtocolId());
        curve.setResultSetId(curveDTO.getResultSetId());
        curve.setFitDate(curveDTO.getFitDate());
        curve.setVersion(curveDTO.getVersion());
        curve.setXAxisLabels(curveDTO.getXAxisLabels());
        curve.setPlotDoseData(curveDTO.getPlotDoseData());
        curve.setPlotPredictionData(curveDTO.getPlotPredictionData());
        curve.setWells(curveDTO.getWells());
        curve.setFeatureValues(curveDTO.getFeatureValues());
        curve.setWellConcentrations(curveDTO.getWellConcentrations());
        curve.setWeights(curveDTO.getWeights());
        return curve;
    }

    /**
     * Maps a {@link Curve} to a {@link CurveDTO}.
     */
    public CurveDTO map(Curve curve) {
        return CurveDTO.builder()
            .id(curve.getId())
            .substanceName(curve.getSubstanceName())
            .substanceType(curve.getSubstanceType())
            .plateId(curve.getPlateId())
            .featureId(curve.getFeatureId())
            .protocolId(curve.getProtocolId())
            .resultSetId(curve.getResultSetId())
            .fitDate(curve.getFitDate())
            .version(curve.getVersion())
            .xAxisLabels(curve.getXAxisLabels())
            .plotDoseData(curve.getPlotDoseData())
            .plotPredictionData(curve.getPlotPredictionData())
            .wells(curve.getWells())
            .featureValues(curve.getFeatureValues())
            .wellConcentrations(curve.getWellConcentrations())
            .weights(curve.getWeights())
            .build();
    }

    /**
     * Maps a {@link CurveOutputParamDTO} to a {@link CurveOutputParameter}.
     */
    public CurveOutputParameter map(CurveOutputParamDTO curveOutputParamDTO) {
        CurveOutputParameter curveOutputParameter = new CurveOutputParameter();
        curveOutputParameter.setCurveId(curveOutputParamDTO.getCurveId());
        curveOutputParameter.setName(curveOutputParamDTO.getName());
        curveOutputParameter.setNumericValue(curveOutputParamDTO.getNumericValue());
        curveOutputParameter.setStringValue(curveOutputParamDTO.getStringValue());
        return curveOutputParameter;
    }

    /**
     * Maps a {@link CurveOutputParameter} to a {@link CurveOutputParamDTO}.
     */
    public CurveOutputParamDTO map(CurveOutputParameter curveOutputParameter) {
        return CurveOutputParamDTO.builder()
            .curveId(curveOutputParameter.getCurveId())
            .name(curveOutputParameter.getName())
            .numericValue(curveOutputParameter.getNumericValue())
            .stringValue(curveOutputParameter.getStringValue())
            .build();
    }

    /**
     *
     * Maps a {@link CurveInputParameter} to a {@link CurveInputParamDTO}.
     */
    public CurveInputParamDTO map(CurveInputParameter curveInputParameter) {
        return CurveInputParamDTO.builder()
            .curveId(curveInputParameter.getCurveId())
            .name(curveInputParameter.getName())
            .numericValue(curveInputParameter.getNumericValue())
            .stringValue(curveInputParameter.getStringValue())
            .build();
    }
}
