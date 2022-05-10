/**
 * Phaedra II
 *
 * Copyright (C) 2016-2022 Open Analytics
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

import com.fasterxml.jackson.core.type.TypeReference;
import eu.openanalytics.phaedra.resultdataservice.dto.PageDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultFeatureStatDTO;
import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Disabled //TODO: Fix tests
public class ResultFeatureStatIntegrationTest extends AbstractIntegrationTest {

    private final static TypeReference<PageDTO<ResultFeatureStatDTO>> PAGED_RESULT_FEATURE_STAT_TYPE = new TypeReference<>() {};

    @Test
    public void simpleCreateAndGetTest() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);
        Assertions.assertEquals(1, res1.getId());

        // 2. create simple ResultFeatureStat
        var input2 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        var res2 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input2)), HttpStatus.CREATED, ResultFeatureStatDTO[].class)[0];
        Assertions.assertEquals(1, res2.getId());
        Assertions.assertEquals(1, res2.getResultSetId());
        Assertions.assertEquals(StatusCode.SUCCESS, res2.getStatusCode());
        Assertions.assertEquals("Ok", res2.getStatusMessage());
        Assertions.assertEquals(42L, res2.getFeatureId());
        Assertions.assertEquals(45L, res2.getFeatureStatId());
        Assertions.assertEquals("count", res2.getStatisticName());
        Assertions.assertEquals(42f, res2.getValue());
        Assertions.assertNull(res2.getWelltype());

        // 3. get specific ResultFeatureStat
        var res3 = performRequest(get("/resultset/1/resultfeaturestat/1"), HttpStatus.OK, ResultFeatureStatDTO.class);
        Assertions.assertEquals(1, res3.getId());
        Assertions.assertEquals(1, res3.getResultSetId());
        Assertions.assertEquals(StatusCode.SUCCESS, res3.getStatusCode());
        Assertions.assertEquals("Ok", res3.getStatusMessage());
        Assertions.assertEquals(42L, res3.getFeatureId());
        Assertions.assertEquals(45L, res3.getFeatureStatId());
        Assertions.assertEquals("count", res3.getStatisticName());
        Assertions.assertEquals(42f, res3.getValue());
        Assertions.assertNull(res3.getWelltype());

        // 4. Delete ResultFeatureStat
        performRequest(delete("/resultset/1/resultfeaturestat/1"), HttpStatus.NO_CONTENT);

        // 6. get resultSet again
        var res5 = performRequest(get("/resultset/1/resultfeaturestat/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat with id 1 not found!\",\"status\":\"error\"}", res5);
    }


    @Test
    public void addDataToCompletedSet() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        Assertions.assertEquals(1, res1.getId());

        // 2. complete the ResultSet
        var input2 = ResultSetDTO.builder()
            .outcome(StatusCode.SUCCESS)
            .errors(Collections.emptyList())
            .errorsText("")
            .build();
        performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);

        // 3. create simple ResultFeatureStat
        var input3 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        var res3 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input3)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultSet is already completed, cannot add new ResultFeatureStat to this set.\",\"status\":\"error\"}", res3);
    }


    @Test
    public void removeDataFromCompletedSet() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        Assertions.assertEquals(1, res1.getId());

        // 2. create simple ResultFeatureStat
        var input2 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        performRequest(post("/resultset/1/resultfeaturestat", List.of(input2)), HttpStatus.CREATED);

        // 3. complete the ResultSet
        var input3 = ResultSetDTO.builder()
            .outcome(StatusCode.SUCCESS)
            .errors(Collections.emptyList())
            .errorsText("")
            .build();
        performRequest(put("/resultset/1", input3), HttpStatus.OK, ResultSetDTO.class);

        // 4. delete ResultData
        var res4 = performRequest(delete("/resultset/1/resultfeaturestat/1"), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultSet is already completed, cannot delete a ResultFeatureStat from this set.\",\"status\":\"error\"}", res4);
    }

    @Test
    public void updateDataSet() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        Assertions.assertEquals(1, res1.getId());

        // 2. create simple ResultFeatureStat
        var input2 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        performRequest(post("/resultset/1/resultfeaturestat", List.of(input2)), HttpStatus.CREATED);

        // 3. update ResultFeatureStat
        var res3 = performRequest(put("/resultset/1/resultfeaturestat/1", ""), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat cannot be updated (it can be deleted).\",\"status\":\"error\"}", res3);
    }


    @Test
    public void testPagedQueries() throws Exception {
        // 1. create two ResultSets
        for (long i = 1; i <= 2; i++) {
            var input = ResultSetDTO.builder()
                .protocolId(i)
                .plateId(i)
                .measId(i)
                .build();
            performRequest(post("/resultset", input), HttpStatus.CREATED, ResultSetDTO.class);
        }

        // 2. create 25 ResultFeatureStats for ResultSet 1
        for (int i = 1; i <= 25; i++) {
            var input = ResultFeatureStatDTO.builder()
                .exitCode(0)
                .statusCode(StatusCode.SUCCESS)
                .statusMessage("Ok")
                .featureId(42L)
                .featureStatId(45L + i)
                .statisticName(String.format("count-%s", i))
                .value(42f)
                .welltype(null)
                .build();

            performRequest(post("/resultset/1/resultfeaturestat", List.of(input)), HttpStatus.CREATED);
        }

        // 3. create 15 ResultFeatureStats for ResultSet 2
        for (int i = 1; i <= 15; i++) {
            var input = ResultFeatureStatDTO.builder()
                .exitCode(0)
                .statusCode(StatusCode.SUCCESS)
                .statusMessage("Ok")
                .featureId(24L)
                .featureStatId(15L + i)
                .statisticName(String.format("count-%s", i))
                .value(42f)
                .welltype(null)
                .build();

            performRequest(post("/resultset/2/resultfeaturestat", List.of(input)), HttpStatus.CREATED);
        }

        // 4. query first page of ResultSet 1
        var res4 = performRequest(get("/resultset/1/resultfeaturestat"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":46,\"id\":1,\"resultSetId\":1,\"statisticName\":\"count-1\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":47,\"id\":2,\"resultSetId\":1,\"statisticName\":\"count-2\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":48,\"id\":3,\"resultSetId\":1,\"statisticName\":\"count-3\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":49,\"id\":4,\"resultSetId\":1,\"statisticName\":\"count-4\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":50,\"id\":5,\"resultSetId\":1,\"statisticName\":\"count-5\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":51,\"id\":6,\"resultSetId\":1,\"statisticName\":\"count-6\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":52,\"id\":7,\"resultSetId\":1,\"statisticName\":\"count-7\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":53,\"id\":8,\"resultSetId\":1,\"statisticName\":\"count-8\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":54,\"id\":9,\"resultSetId\":1,\"statisticName\":\"count-9\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":55,\"id\":10,\"resultSetId\":1,\"statisticName\":\"count-10\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":56,\"id\":11,\"resultSetId\":1,\"statisticName\":\"count-11\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":57,\"id\":12,\"resultSetId\":1,\"statisticName\":\"count-12\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":58,\"id\":13,\"resultSetId\":1,\"statisticName\":\"count-13\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":59,\"id\":14,\"resultSetId\":1,\"statisticName\":\"count-14\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":60,\"id\":15,\"resultSetId\":1,\"statisticName\":\"count-15\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":61,\"id\":16,\"resultSetId\":1,\"statisticName\":\"count-16\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":62,\"id\":17,\"resultSetId\":1,\"statisticName\":\"count-17\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":63,\"id\":18,\"resultSetId\":1,\"statisticName\":\"count-18\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":64,\"id\":19,\"resultSetId\":1,\"statisticName\":\"count-19\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":65,\"id\":20,\"resultSetId\":1,\"statisticName\":\"count-20\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}]" +
            ",\"status\":{\"first\":true,\"last\":false,\"totalElements\":25,\"totalPages\":2}}", res4);

        // 5. query second page of ResultSet 1
        var res5 = performRequest(get("/resultset/1/resultfeaturestat?page=1"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":66,\"id\":21,\"resultSetId\":1,\"statisticName\":\"count-21\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":67,\"id\":22,\"resultSetId\":1,\"statisticName\":\"count-22\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":68,\"id\":23,\"resultSetId\":1,\"statisticName\":\"count-23\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":69,\"id\":24,\"resultSetId\":1,\"statisticName\":\"count-24\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":70,\"id\":25,\"resultSetId\":1,\"statisticName\":\"count-25\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}]" +
            ",\"status\":{\"first\":false,\"last\":true,\"totalElements\":25,\"totalPages\":2}}", res5);

        // 6. query first page of ResultSet 2
        var res6 = performRequest(get("/resultset/2/resultfeaturestat"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":16,\"id\":26,\"resultSetId\":2,\"statisticName\":\"count-1\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":17,\"id\":27,\"resultSetId\":2,\"statisticName\":\"count-2\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":18,\"id\":28,\"resultSetId\":2,\"statisticName\":\"count-3\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":19,\"id\":29,\"resultSetId\":2,\"statisticName\":\"count-4\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":20,\"id\":30,\"resultSetId\":2,\"statisticName\":\"count-5\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":21,\"id\":31,\"resultSetId\":2,\"statisticName\":\"count-6\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":22,\"id\":32,\"resultSetId\":2,\"statisticName\":\"count-7\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":23,\"id\":33,\"resultSetId\":2,\"statisticName\":\"count-8\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":24,\"id\":34,\"resultSetId\":2,\"statisticName\":\"count-9\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":25,\"id\":35,\"resultSetId\":2,\"statisticName\":\"count-10\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":26,\"id\":36,\"resultSetId\":2,\"statisticName\":\"count-11\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":27,\"id\":37,\"resultSetId\":2,\"statisticName\":\"count-12\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":28,\"id\":38,\"resultSetId\":2,\"statisticName\":\"count-13\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":29,\"id\":39,\"resultSetId\":2,\"statisticName\":\"count-14\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":24,\"featureStatId\":30,\"id\":40,\"resultSetId\":2,\"statisticName\":\"count-15\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}" +
            "],\"status\":{\"first\":true,\"last\":true,\"totalElements\":15,\"totalPages\":1}}", res6);

        // 7. query first page of ResultSet 2
        var res7 = performRequest(get("/resultset/2/resultfeaturestat?page=1"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[],\"status\":{\"first\":false,\"last\":true,\"totalElements\":15,\"totalPages\":1}}", res7);

        // 8. get by featureId
        var res8 = performRequest(get("/resultset/1/resultfeaturestat?featureId=42"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":46,\"id\":1,\"resultSetId\":1,\"statisticName\":\"count-1\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":47,\"id\":2,\"resultSetId\":1,\"statisticName\":\"count-2\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":48,\"id\":3,\"resultSetId\":1,\"statisticName\":\"count-3\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":49,\"id\":4,\"resultSetId\":1,\"statisticName\":\"count-4\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":50,\"id\":5,\"resultSetId\":1,\"statisticName\":\"count-5\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":51,\"id\":6,\"resultSetId\":1,\"statisticName\":\"count-6\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":52,\"id\":7,\"resultSetId\":1,\"statisticName\":\"count-7\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":53,\"id\":8,\"resultSetId\":1,\"statisticName\":\"count-8\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":54,\"id\":9,\"resultSetId\":1,\"statisticName\":\"count-9\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":55,\"id\":10,\"resultSetId\":1,\"statisticName\":\"count-10\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":56,\"id\":11,\"resultSetId\":1,\"statisticName\":\"count-11\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":57,\"id\":12,\"resultSetId\":1,\"statisticName\":\"count-12\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":58,\"id\":13,\"resultSetId\":1,\"statisticName\":\"count-13\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":59,\"id\":14,\"resultSetId\":1,\"statisticName\":\"count-14\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":60,\"id\":15,\"resultSetId\":1,\"statisticName\":\"count-15\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":61,\"id\":16,\"resultSetId\":1,\"statisticName\":\"count-16\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":62,\"id\":17,\"resultSetId\":1,\"statisticName\":\"count-17\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":63,\"id\":18,\"resultSetId\":1,\"statisticName\":\"count-18\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":64,\"id\":19,\"resultSetId\":1,\"statisticName\":\"count-19\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"featureStatId\":65,\"id\":20,\"resultSetId\":1,\"statisticName\":\"count-20\",\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"value\":42.0}]" +
            ",\"status\":{\"first\":true,\"last\":false,\"totalElements\":25,\"totalPages\":2}}", res8);

        // 9. gey by non-existing featureId
        var res9 = performRequest(get("/resultset/1/resultfeaturestat?featureId=120"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[],\"status\":{\"first\":true,\"last\":true,\"totalElements\":0,\"totalPages\":0}}", res9);

        // 10. gey by non-existing featureId and non-existing resultSet
        var res10 = performRequest(get("/resultset/60/resultfeaturestat?featureId=120"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 60 not found!\",\"status\":\"error\"}", res10);
    }

    @Test
    public void testGetResultFeatureSTatNotExisting() throws Exception {
        // 1. query using paginated endpoint of non existing ResultSet
        var res1 = performRequest(get("/resultset/42/resultfeaturestat"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 42 not found!\",\"status\":\"error\"}", res1);

        // 2. query using specific endpoint of non existing ResultSet
        var res2 = performRequest(get("/resultset/42/resultfeaturestat/32"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 42 not found!\",\"status\":\"error\"}", res2);

        // 3. create ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        // 4. query using paginated endpoint -> just 200 but without results
        var res4 = performRequest(get("/resultset/1/resultfeaturestat"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[],\"status\":{\"first\":true,\"last\":true,\"totalElements\":0,\"totalPages\":0}}", res4);

        // 5. query using specific endpoint of non existing ResultFeatureStat
        var res5 = performRequest(get("/resultset/1/resultfeaturestat/4"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat with id 4 not found!\",\"status\":\"error\"}", res5);
    }

    @Test
    public void testDeleteDataSetNotFound() throws Exception {
        // 1. delete DataSet of non-existing ResultSet
        var res1 = performRequest(get("/resultset/1/resultfeaturestat/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 1 not found!\",\"status\":\"error\"}", res1);

        // 2. create ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        // 3. delete non-existing
        var res2 = performRequest(delete("/resultset/1/resultfeaturestat/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat with id 1 not found!\",\"status\":\"error\"}", res2);
    }

    @Test
    public void testDeleteSetNotOwnerOfData() throws Exception {
        // 1. create two ResultSets which each contain one ResultFeatureStat
        for (long i = 1; i <= 2; i++) {
            var input = ResultSetDTO.builder()
                .protocolId(i)
                .plateId(i)
                .measId(i)
                .build();

            performRequest(post("/resultset", input), HttpStatus.CREATED, ResultSetDTO.class);

            var input2 = ResultFeatureStatDTO.builder()
                .exitCode(0)
                .statusCode(StatusCode.SUCCESS)
                .statusMessage("Ok")
                .featureId(42L)
                .featureStatId(45L + i)
                .statisticName(String.format("count-%s", i))
                .value(42f)
                .welltype(null)
                .build();

            performRequest(post("/resultset/" + i + "/resultfeaturestat", List.of(input2)), HttpStatus.CREATED);
        }

        // 2. delete Dataset of different ResultSet
        var res2 = performRequest(delete("/resultset/1/resultfeaturestat/2"), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"The ResultFeatureStat with id 2 is not owned by the ResultSet with id 1\",\"status\":\"error\"}", res2);
    }

    @Test
    public void invalidJsonTest() throws Exception {
        var res1 = performRequest(
            MockMvcRequestBuilders.post("/resultset/1/resultfeaturestat")
                .contentType("application/json")
                .content("{\"test"),
            HttpStatus.BAD_REQUEST);

        Assertions.assertEquals("{\"error\":\"Validation error\",\"status\":\"error\"}", res1);
    }

    @Test
    public void testCreationValidationTest() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        Assertions.assertEquals(1, res1.getId());

        // 2. test missing fields
        var input2 = ResultFeatureStatDTO.builder().build();
        var res2 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input2)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"list[0].exitCode\":\"ExitCode is mandatory\"," +
            "\"list[0].featureId\":\"FeatureId is mandatory\"," +
            "\"list[0].featureStatId\":\"FeatureStatId is mandatory\"," +
            "\"list[0].statisticName\":\"StatisticName is mandatory\"," +
            "\"list[0].statusCode\":\"StatusCode is mandatory\"," +
            "\"list[0].statusMessage\":\"StatusMessage is mandatory\"" +
            "},\"status\":\"error\"}", res2);

        // 3. too many fields
        var input3 = ResultFeatureStatDTO.builder()
            .resultSetId(1L)
            .id(10L)
            .createdTimestamp(LocalDateTime.now())
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .value(42f)
            .welltype("welltype")
            .statisticName("count")
            .build();

        var res3 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input3)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"list[0].createdTimestamp\":\"CreatedTimestamp must be null when creating ResultFeatureSta\"," +
            "\"list[0].id\":\"Id must be null when creating a ResultFeatureStat\"," +
            "\"list[0].resultSetId\":\"ResultSetId must be specified in URL and not repeated in body\"" +
            "},\"status\":\"error\"}", res3);

        // 4. validate exitcode
        var input4 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .value(42f)
            .welltype("welltype")
            .statisticName("count")
            .exitCode(260)
            .build();
        var res4 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input4)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"list[0].exitCode\":\"ExitCode must be in the range [0-255]\"},\"status\":\"error\"}", res4);

        // 5. too long status message
        var input5 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("a".repeat(260))
            .featureId(42L)
            .featureStatId(45L)
            .value(42f)
            .welltype("welltype")
            .statisticName("count")
            .exitCode(255)
            .build();
        var res5 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input5)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"list[0].statusMessage\":\"StatusMessage may only contain 255 characters\"},\"status\":\"error\"}", res5);

        // 6. invalid status message
        var res6 = performRequest(post("/resultset/1/resultfeaturestat", List.of(new HashMap<>() {{
            put("exitCode", 10);
            put("statusCode", "INVALID_STATUSCODE");
            put("statusMessage", "test");
            put("featureId", 42L);
            put("value", 42f);
            put("statisticName", "count");
            put("featureStatId", 45L);
        }})), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"statusCode\":\"Invalid value (\\\"INVALID_STATUSCODE\\\") provided\"},\"status\":\"error\"}", res6);
    }

    @Test
    public void testOneValidAndOneInvalidObjectProvided() throws Exception {
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);
        Assertions.assertEquals(1, res1.getId());

        // 1. create invalid input
        var input2 = ResultFeatureStatDTO.builder().build();
        // 1. create invalid input
        var input3 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();
        // send input
        var res2 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input2, input3)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"list[0].exitCode\":\"ExitCode is mandatory\"," +
            "\"list[0].featureId\":\"FeatureId is mandatory\"," +
            "\"list[0].featureStatId\":\"FeatureStatId is mandatory\"," +
            "\"list[0].statisticName\":\"StatisticName is mandatory\"," +
            "\"list[0].statusCode\":\"StatusCode is mandatory\"," +
            "\"list[0].statusMessage\":\"StatusMessage is mandatory\"" +
            "},\"status\":\"error\"}", res2);
        // assert that none of them are created
        var res3 = performRequest(get("/resultset/1/resultfeaturestat"), HttpStatus.OK, PAGED_RESULT_FEATURE_STAT_TYPE);
        Assertions.assertEquals(0, res3.getStatus().getTotalElements());
    }

    @Test
    public void testDuplicatePlateValue() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        Assertions.assertEquals(1, res1.getId());

        // 2. create simple ResultFeatureStat
        var input2 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        var res2 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input2)), HttpStatus.CREATED, ResultFeatureStatDTO[].class)[0];
        Assertions.assertEquals(1, res2.getId());
        Assertions.assertEquals(1, res2.getResultSetId());
        Assertions.assertEquals(StatusCode.SUCCESS, res2.getStatusCode());
        Assertions.assertEquals("Ok", res2.getStatusMessage());
        Assertions.assertEquals(42L, res2.getFeatureId());
        Assertions.assertEquals(45L, res2.getFeatureStatId());
        Assertions.assertEquals("count", res2.getStatisticName());
        Assertions.assertEquals(42f, res2.getValue());
        Assertions.assertNull(res2.getWelltype());

        // 3. create simple ResultFeatureStat with the same resultSetId, featureStatId (welltype is null -> plate value)
        var input3 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        var res3 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input3)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat with one of these parameters already exists!\",\"status\":\"error\"}", res3);
    }

    @Test
    public void testDuplicateWelltypeValue() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        Assertions.assertEquals(1, res1.getId());

        // 2. create simple ResultFeatureStat
        var input2 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype("SAMPLE")
            .build();

        var res2 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input2)), HttpStatus.CREATED, ResultFeatureStatDTO[].class)[0];
        Assertions.assertEquals(1, res2.getId());
        Assertions.assertEquals(1, res2.getResultSetId());
        Assertions.assertEquals(StatusCode.SUCCESS, res2.getStatusCode());
        Assertions.assertEquals("Ok", res2.getStatusMessage());
        Assertions.assertEquals(42L, res2.getFeatureId());
        Assertions.assertEquals(45L, res2.getFeatureStatId());
        Assertions.assertEquals("count", res2.getStatisticName());
        Assertions.assertEquals(42f, res2.getValue());
        Assertions.assertEquals("SAMPLE", res2.getWelltype());

        // 3. create simple ResultFeatureStat with the same resultSetId, featureStatId and welltype
        var input3 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype("SAMPLE")
            .build();

        var res3 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input3)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat with one of these parameters already exists!\",\"status\":\"error\"}", res3);
    }

    @Test
    public void testDuplicatePlateValueInOneRequest() throws Exception {
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);
        Assertions.assertEquals(1, res1.getId());

        // 2. create input 1
        var input2 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        // 3. create simple ResultFeatureStat with the same resultSetId, featureStatId (welltype is null -> plate value)
        var input3 = ResultFeatureStatDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .featureStatId(45L)
            .statisticName("count")
            .value(42f)
            .welltype(null)
            .build();

        // send input
        var res2 = performRequest(post("/resultset/1/resultfeaturestat", List.of(input2, input3)), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultFeatureStat with one of these parameters already exists!\",\"status\":\"error\"}", res2);
        // assert that none of them are created
        var res3 = performRequest(get("/resultset/1/resultfeaturestat"), HttpStatus.OK, PAGED_RESULT_FEATURE_STAT_TYPE);Assertions.assertEquals(0, res3.getStatus().getTotalElements());
    }


}

