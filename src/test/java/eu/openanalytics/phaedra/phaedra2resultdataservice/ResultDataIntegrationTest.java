package eu.openanalytics.phaedra.phaedra2resultdataservice;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultDataDTO;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.phaedra2resultdataservice.model.StatusCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ResultDataIntegrationTest extends AbstractIntegrationTest {

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

        // 2. create simple ResultData
        var input2 = ResultDataDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();

        var res2 = performRequest(post("/resultset/1/resultdata", input2), HttpStatus.CREATED, ResultDataDTO.class);
        Assertions.assertEquals(1, res2.getId());
        Assertions.assertEquals(1, res2.getResultSetId());
        Assertions.assertEquals(StatusCode.SUCCESS, res2.getStatusCode());
        Assertions.assertEquals("Ok", res2.getStatusMessage());
        Assertions.assertEquals(42L, res2.getFeatureId());
        Assertions.assertArrayEquals(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F}, res2.getValues());

        // 3. get specific ResultData
        var res3 = performRequest(get("/resultset/1/resultdata/1"), HttpStatus.OK, ResultDataDTO.class);
        Assertions.assertEquals(1, res3.getId());
        Assertions.assertEquals(1, res3.getResultSetId());
        Assertions.assertEquals(StatusCode.SUCCESS, res3.getStatusCode());
        Assertions.assertEquals("Ok", res3.getStatusMessage());
        Assertions.assertEquals(42L, res3.getFeatureId());
        Assertions.assertArrayEquals(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F}, res3.getValues());

        // 4. Delete resultData
        performRequest(delete("/resultset/1/resultdata/1"), HttpStatus.NO_CONTENT);

        // 6. get resultSet again
        var res5 = performRequest(get("/resultset/1/resultdata/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultData with id 1 not found!\",\"status\":\"error\"}", res5);
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
            .outcome("MyOutcome!")
            .build();
        performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);

        // 3. create simple ResultData
        var input3 = ResultDataDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();

        var res3 = performRequest(post("/resultset/1/resultdata", input3), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultSet is already completed, cannot add new ResultData to this set.\",\"status\":\"error\"}", res3);
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

        // 2. create simple ResultData
        var input2 = ResultDataDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();

        performRequest(post("/resultset/1/resultdata", input2), HttpStatus.CREATED);

        // 3. complete the ResultSet
        var input3 = ResultSetDTO.builder()
            .outcome("MyOutcome!")
            .build();
        performRequest(put("/resultset/1", input3), HttpStatus.OK, ResultSetDTO.class);

        // 4. delete ResultData
        var res4 = performRequest(delete("/resultset/1/resultdata/1"), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultSet is already completed, cannot delete a ResultData from this set.\",\"status\":\"error\"}", res4);
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

        // 2. create simple ResultData
        var input2 = ResultDataDTO.builder()
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();

        performRequest(post("/resultset/1/resultdata", input2), HttpStatus.CREATED);

        // 3. update Resultdata
        var res3 = performRequest(put("/resultset/1/resultdata/1", ""), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultData cannot be updated (it can be deleted).\",\"status\":\"error\"}", res3);
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

        // 2. create 25 ResultDatas for ResultSet 1
        for (int i = 1; i <= 25; i++) {
            var input = ResultDataDTO.builder()
                .exitCode(0)
                .statusCode(StatusCode.SUCCESS)
                .statusMessage("Ok")
                .featureId(42L)
                .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
                .build();
            performRequest(post("/resultset/1/resultdata", input), HttpStatus.CREATED);
        }

        // 3. create 15 ResultDatas for ResultSet 2
        for (int i = 1; i <= 15; i++) {
            var input = ResultDataDTO.builder()
                .exitCode(0)
                .statusCode(StatusCode.SUCCESS)
                .statusMessage("Ok")
                .featureId(32L)
                .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
                .build();
            performRequest(post("/resultset/2/resultdata", input), HttpStatus.CREATED);
        }

        // 4. query first page of ResultSet 1
        var res4 = performRequest(get("/resultset/1/resultdata"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":1,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":2,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":3,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":4,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":5,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":6,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":7,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":8,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":9,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":10,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":11,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":12,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":13,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":14,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":15,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":16,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":17,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":18,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":19,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":20,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}]," +
            "\"status\":{\"first\":true,\"last\":false,\"totalElements\":25,\"totalPages\":2}}", res4);

        // 5. query second page of ResultSet 1
        var res5 = performRequest(get("/resultset/1/resultdata?page=1"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":21,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":22,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":23,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":24,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":42,\"id\":25,\"resultSetId\":1,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}" +
            "],\"status\":{\"first\":false,\"last\":true,\"totalElements\":25,\"totalPages\":2}}", res5);

        // 6. query first page of ResultSet 2
        var res6 = performRequest(get("/resultset/2/resultdata"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":26,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":27,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":28,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":29,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":30,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":31,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":32,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":33,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":34,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":35,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":36,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":37,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":38,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":39,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}," +
            "{\"createdTimestamp\":\"2042-12-31T23:59:59\",\"exitCode\":0,\"featureId\":32,\"id\":40,\"resultSetId\":2,\"statusCode\":\"SUCCESS\",\"statusMessage\":\"Ok\",\"values\":[1.0,2.0,3.0,5.0,8.0]}" +
            "],\"status\":{\"first\":true,\"last\":true,\"totalElements\":15,\"totalPages\":1}}", res6);

        // 7. query first page of ResultSet 2
        var res7 = performRequest(get("/resultset/2/resultdata?page=1"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[],\"status\":{\"first\":false,\"last\":true,\"totalElements\":15,\"totalPages\":1}}", res7);
    }

    @Test
    public void testGetResultDataNotExisting() throws Exception {
        // 1. query using paginated endpoint of non existing ResultSet
        var res1 = performRequest(get("/resultset/42/resultdata"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 42 not found!\",\"status\":\"error\"}", res1);

        // 2. query using specific endpoint of non existing ResultSet
        var res2 = performRequest(get("/resultset/42/resultdata/32"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 42 not found!\",\"status\":\"error\"}", res2);

        // 3. create ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        // 4. query using paginated endpoint -> just 200 but without results
        var res4 = performRequest(get("/resultset/1/resultdata"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[],\"status\":{\"first\":true,\"last\":true,\"totalElements\":0,\"totalPages\":0}}", res4);

        // 5. query using specific endpoint of non existing ResultData
        var res5 = performRequest(get("/resultset/1/resultdata/4"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultData with id 4 not found!\",\"status\":\"error\"}", res5);
    }

    @Test
    public void testDeleteDataSetNotFound() throws Exception {
        // 1. delete DataSet of non-existing ResultSet
        var res1 = performRequest(get("/resultset/1/resultdata/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 1 not found!\",\"status\":\"error\"}", res1);

        // 2. create ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        // 3. delete non-existing
        var res2 = performRequest(get("/resultset/1/resultdata/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultData with id 1 not found!\",\"status\":\"error\"}", res2);
    }

    @Test
    public void testDeleteSetNotOwnerOfData() throws Exception {
        // 1. create two ResultSets which each contain one ResultData
        for (long i = 1; i <= 2; i++) {
            var input = ResultSetDTO.builder()
                .protocolId(i)
                .plateId(i)
                .measId(i)
                .build();

            performRequest(post("/resultset", input), HttpStatus.CREATED, ResultSetDTO.class);

            var input2 = ResultDataDTO.builder()
                .exitCode(0)
                .statusCode(StatusCode.SUCCESS)
                .statusMessage("Ok")
                .featureId(42L)
                .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
                .build();

            performRequest(post("/resultset/" + i + "/resultdata", input2), HttpStatus.CREATED);
        }

        // 2. delete Dataset of different ResultSet
        var res2 = performRequest(delete("/resultset/1/resultdata/2"), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"The ResultData with id 2 is not owned by the ResultSet with id 1\",\"status\":\"error\"}", res2);
    }

    @Test
    public void testDeleteNonExistingData() throws Exception {
        // 1. create ResultSet
        var input = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        performRequest(post("/resultset", input), HttpStatus.CREATED, ResultSetDTO.class);

        // 2. delete Dataset of this ResultSet (which does not exsits)
        var res2 = performRequest(delete("/resultset/1/resultdata/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultData with id 1 not found!\",\"status\":\"error\"}", res2);
    }

    @Test
    public void invalidJsonTest() throws Exception {
        var res1 = performRequest(
            MockMvcRequestBuilders.post("/resultset/1/resultdata")
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
        var input2 = ResultDataDTO.builder().build();
        var res2 = performRequest(post("/resultset/1/resultdata", input2), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"exitCode\":\"ExitCode is mandatory\"," +
            "\"featureId\":\"FeatureId is mandatory\"," +
            "\"statusCode\":\"StatusCode is mandatory\"," +
            "\"statusMessage\":\"StatusMessage is mandatory\"," +
            "\"values\":\"Values is mandatory\"" +
            "},\"status\":\"error\"}", res2);

        // 3. too many fields
        var input3 = ResultDataDTO.builder()
            .resultSetId(1L)
            .id(10L)
            .createdTimestamp(LocalDateTime.now())
            .exitCode(0)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();

        var res3 = performRequest(post("/resultset/1/resultdata", input3), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"createdTimestamp\":\"CreatedTimestamp must be null when creating ResultData\"," +
            "\"id\":\"Id must be specified in URL and not repeated in body\"," +
            "\"resultSetId\":\"ResultSetId must be specified in URL and not repeated in body\"" +
            "},\"status\":\"error\"}", res3);

        // 4. validate exitcode
        var input4 = ResultDataDTO.builder()
            .exitCode(260)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("Ok")
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();
        var res4 = performRequest(post("/resultset/1/resultdata", input4), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"exitCode\":\"ExitCode must be in the range [0-255]\"},\"status\":\"error\"}", res4);

        // 5. too long status message
        var input5 = ResultDataDTO.builder()
            .exitCode(255)
            .statusCode(StatusCode.SUCCESS)
            .statusMessage("a".repeat(260))
            .featureId(42L)
            .values(new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F})
            .build();

        var res5 = performRequest(post("/resultset/1/resultdata", input5), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"statusMessage\":\"StatusMessage may only contain 255 characters\"},\"status\":\"error\"}", res5);

        // 6. invalid status message
        var res6 = performRequest(post("/resultset/1/resultdata", new HashMap<>() {{
            put("exitCode", 10);
            put("statusCode", "INVALID_STATUSCODE");
            put("statusMesage", "test");
            put("featureId", 42L);
            put("values", new float[]{1.0F, 2.0F, 3.0F, 5.0F, 8.0F});
        }}), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"statusCode\":\"Invalid value provided\"},\"status\":\"error\"}", res6);
    }

}
