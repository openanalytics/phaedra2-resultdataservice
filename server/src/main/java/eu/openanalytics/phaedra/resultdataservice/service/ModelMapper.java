package eu.openanalytics.phaedra.resultdataservice.service;

import eu.openanalytics.phaedra.resultdataservice.model.ResultData;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.Converter;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.NameTransformers;
import org.modelmapper.convention.NamingConventions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelMapper {

    private final org.modelmapper.ModelMapper modelMapper = new org.modelmapper.ModelMapper();

    public ModelMapper() {
        Configuration builderConfiguration = modelMapper.getConfiguration().copy()
            .setDestinationNameTransformer(NameTransformers.builder())
            .setDestinationNamingConvention(NamingConventions.builder());

        modelMapper.createTypeMap(ResultDataDTO.class, ResultData.ResultDataBuilder.class, builderConfiguration)
            .setPropertyCondition(Conditions.isNotNull());

        modelMapper.createTypeMap(ResultData.class, ResultDataDTO.ResultDataDTOBuilder.class, builderConfiguration)
            .setPropertyCondition(Conditions.isNotNull());

        modelMapper.createTypeMap(ResultSetDTO.class, ResultSet.ResultSetBuilder.class, builderConfiguration)
            .setPropertyCondition(Conditions.isNotNull())
            .addMappings(mapper -> mapper.using((Converter<List<ErrorDTO>, ResultSet.ErrorHolder>) context -> {
                if (context.getSource() != null) {
                    return new ResultSet.ErrorHolder(context.getSource());
                }
                return null;
            }).map(ResultSetDTO::getErrors, ResultSet.ResultSetBuilder::errors));

        modelMapper.createTypeMap(ResultSet.class, ResultSetDTO.ResultSetDTOBuilder.class, builderConfiguration)
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

}
