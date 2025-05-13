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
package eu.openanalytics.phaedra.resultdataservice;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import eu.openanalytics.phaedra.resultdataservice.dto.ErrorDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.model.ResultSet;
import eu.openanalytics.phaedra.resultdataservice.service.ModelMapper;

public class ResultDataModelMapperTest {
	
	public static void main(String[] args) {
		new ResultDataModelMapperTest().testModelMapperResultSetDTO();
	}
	
	@Test
	public void testModelMapperValidConfig() {
		new ModelMapper();
	}
	
	@Test
	public void testModelMapperResultSetDTO() {
		ModelMapper mapper = new ModelMapper();
		ResultSetDTO rsIn = ResultSetDTO.builder()
				.id(5L)
				.outcome(StatusCode.FAILURE)
				.errors(Collections.singletonList(ErrorDTO.builder().description("Test Error").build()))
				.build();
		ResultSet rsOut = mapper.map(rsIn).build();
		
		Assert.assertEquals(rsIn.getId(), rsOut.getId());
		Assert.assertEquals(rsIn.getOutcome(), rsOut.getOutcome());
		assertTrue(rsOut.getErrors().getErrors().size() == 1);
		Assert.assertEquals(rsIn.getErrors(), rsOut.getErrors().getErrors());
	}
}
