package eu.openanalytics.phaedra.resultdataservice;

import eu.openanalytics.phaedra.resultdataservice.dto.ResultSetDTO;
import eu.openanalytics.phaedra.resultdataservice.enumeration.StatusCode;
import eu.openanalytics.phaedra.resultdataservice.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ResultSetIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void simpleCreateAndGetTest() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        var res1 = performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);
        Assertions.assertEquals(1L, res1.getId());
        Assertions.assertEquals(2L, res1.getPlateId());
        Assertions.assertEquals(3L, res1.getMeasId());
        Assertions.assertNotNull(res1.getExecutionStartTimeStamp());
        Assertions.assertEquals(StatusCode.SCHEDULED, res1.getOutcome());
        Assertions.assertNull(res1.getExecutionEndTimeStamp());

        // 2. get simple resultSet
        var res2 = performRequest(get("/resultset/1"), HttpStatus.OK, ResultSetDTO.class);
        Assertions.assertEquals(1L, res2.getId());
        Assertions.assertEquals(2L, res2.getPlateId());
        Assertions.assertEquals(3L, res2.getMeasId());
        Assertions.assertNotNull(res2.getExecutionStartTimeStamp());
        Assertions.assertEquals(StatusCode.SCHEDULED,res2.getOutcome());
        Assertions.assertNull(res2.getExecutionEndTimeStamp());

        // 3. update outcome
        var input2 = ResultSetDTO.builder()
            .outcome(StatusCode.SUCCESS)
            .errors(Collections.emptyList())
            .errorsText("")
            .build();
        var res3 = performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);
        Assertions.assertEquals(1L, res3.getId());
        Assertions.assertEquals(2L, res3.getPlateId());
        Assertions.assertEquals(3L, res3.getMeasId());
        Assertions.assertNotNull(res3.getExecutionStartTimeStamp());
        Assertions.assertEquals(StatusCode.SUCCESS, res3.getOutcome());
        Assertions.assertNotNull(res3.getExecutionEndTimeStamp());

        // 4. get resultSet again
        var res4 = performRequest(get("/resultset/1"), HttpStatus.OK, ResultSetDTO.class);
        Assertions.assertEquals(1L, res4.getId());
        Assertions.assertEquals(2L, res4.getPlateId());
        Assertions.assertEquals(3L, res4.getMeasId());
        Assertions.assertNotNull(res4.getExecutionStartTimeStamp());
        Assertions.assertEquals(StatusCode.SUCCESS, res4.getOutcome());
        Assertions.assertNotNull(res4.getExecutionEndTimeStamp());


        // 5. delete object
        performRequest(delete("/resultset/1"), HttpStatus.NO_CONTENT);

        // 6. get resultSet again
        var res5 = performRequest(get("/resultset/1"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 1 not found!\",\"status\":\"error\"}", res5);
    }

    @Test
    public void testCreationValidationTest() throws Exception {
        // 1. missing fields
        var input1 = ResultSetDTO.builder().build();
        var res1 = performRequest(post("/resultset", input1), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"measId\":\"MeasId is mandatory\"," +
            "\"plateId\":\"PlateId is mandatory\"," +
            "\"protocolId\":\"ProtocolId is mandatory\"},\"status\":\"error\"}", res1);

        // 2. too many fields
        var input2 = ResultSetDTO.builder()
            .plateId(1L)
            .protocolId(2L)
            .measId(3L)
            .id(1L)
            .outcome(StatusCode.SUCCESS)
            .executionStartTimeStamp(LocalDateTime.now())
            .executionEndTimeStamp(LocalDateTime.now())
            .build();

        var res2 = performRequest(post("/resultset", input2), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"executionEndTimeStamp\":\"ExecutionEndTimeStamp must be null when creating a ResultSet\"," +
            "\"executionStartTimeStamp\":\"ExecutionStartTimeStamp must be null when creating a ResultSet\"," +
            "\"id\":\"Id must be null when creating a ResultSet\"," +
            "\"outcome\":\"Outcome must be null when creating a ResultSet\"},\"status\":\"error\"}", res2);

        // 3. invalid data
        var input3 = new HashMap<String, String>() {{
            put("protocolId", "ABC");
        }};
        var res3 = performRequest(post("/resultset", input3), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"protocolId\":\"Invalid value (\\\"ABC\\\") provided\"},\"status\":\"error\"}", res3);
    }

    @Test
    public void testUpdatingValidationTest() throws Exception {
        // 1. missing fields
        var input1 = ResultSetDTO.builder().build();
        var res1 = performRequest(put("/resultset/1", input1), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"errors\":\"Errors is mandatory when updating a ResultSet\",\"errorsText\":\"ErrorsText is mandatory when updating a ResultSet\",\"outcome\":\"Outcome is mandatory when updating a ResultSet\"},\"status\":\"error\"}", res1);

        // 2. wrong outcome
        var input2 = new HashMap<>() {{
            put("outcome", "NON_EXISTING_CODE");
            put("errors", Collections.emptyList());
            put("errorsText", "");
        }};

        var res2 = performRequest(put("/resultset/1", input2), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"outcome\":\"Invalid value (\\\"NON_EXISTING_CODE\\\") provided\"},\"status\":\"error\"}", res2);

        // 3. too many fields
        var input3 = ResultSetDTO.builder()
            .errors(Collections.emptyList())
            .errorsText("")
            .id(1L)
            .plateId(1L)
            .protocolId(1L)
            .measId(1L)
            .executionStartTimeStamp(LocalDateTime.now())
            .executionEndTimeStamp(LocalDateTime.now())
            .build();

        var res3 = performRequest(put("/resultset/1", input3), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"executionEndTimeStamp\":\"ExecutionEndTimeStamp cannot be changed\"," +
            "\"executionStartTimeStamp\":\"ExecutionStartTimeStamp cannot be changed\"," +
            "\"id\":\"Id must be specified in URL and not repeated in body\"," +
            "\"measId\":\"MeasId cannot be changed\"," +
            "\"outcome\":\"Outcome is mandatory when updating a ResultSet\"," +
            "\"plateId\":\"PlateId cannot be changed\"," +
            "\"protocolId\":\"ProtocolId cannot be changed\"},\"status\":\"error\"}", res3);
    }

    @Test
    public void updateNotExistingResultSet() throws Exception {
        var input1 = ResultSetDTO.builder()
            .outcome(StatusCode.SUCCESS)
            .errors(Collections.emptyList())
            .errorsText("")
            .build();
        var res1 = performRequest(put("/resultset/4", input1), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 4 not found!\",\"status\":\"error\"}", res1);
    }

    @Test
    public void deleteNotExistingResultSet() throws Exception {
        var res1 = performRequest(delete("/resultset/4"), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultSet with id 4 not found!\",\"status\":\"error\"}", res1);
    }


    @Test
    public void testDataSetAlreadyCompleted() throws Exception {
        // 1. create simple ResultSet
        var input1 = ResultSetDTO.builder()
            .protocolId(1L)
            .plateId(2L)
            .measId(3L)
            .build();

        performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        // 2. update outcome
        var input2 = ResultSetDTO.builder()
            .outcome(StatusCode.SUCCESS)
            .errors(Collections.emptyList())
            .errorsText("")
            .build();
        performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);

        // 3. update outcome again
        var input3 = ResultSetDTO.builder()
            .outcome(StatusCode.FAILURE)
            .errors(Collections.emptyList())
            .errorsText("")
            .build();
        var res3 = performRequest(put("/resultset/1", input3), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultDataSet already contains a complete message or end timestamp.\",\"status\":\"error\"}", res3);
    }

    @Test
    public void testPagedQueries() throws Exception {
        // 1. create 35 ResultSets
        for (long i = 1; i <= 35; i++) {
            var input = ResultSetDTO.builder()
                .protocolId(i)
                .plateId(i)
                .measId(i)
                .build();

            performRequest(post("/resultset", input), HttpStatus.CREATED, ResultSetDTO.class);
        }

        // 2. query first page
        var res2 = performRequest(get("/resultset"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":1,\"measId\":1,\"outcome\":\"SCHEDULED\",\"plateId\":1,\"protocolId\":1}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":2,\"measId\":2,\"outcome\":\"SCHEDULED\",\"plateId\":2,\"protocolId\":2}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":3,\"measId\":3,\"outcome\":\"SCHEDULED\",\"plateId\":3,\"protocolId\":3}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":4,\"measId\":4,\"outcome\":\"SCHEDULED\",\"plateId\":4,\"protocolId\":4}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":5,\"measId\":5,\"outcome\":\"SCHEDULED\",\"plateId\":5,\"protocolId\":5}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":6,\"measId\":6,\"outcome\":\"SCHEDULED\",\"plateId\":6,\"protocolId\":6}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":7,\"measId\":7,\"outcome\":\"SCHEDULED\",\"plateId\":7,\"protocolId\":7}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":8,\"measId\":8,\"outcome\":\"SCHEDULED\",\"plateId\":8,\"protocolId\":8}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":9,\"measId\":9,\"outcome\":\"SCHEDULED\",\"plateId\":9,\"protocolId\":9}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":10,\"measId\":10,\"outcome\":\"SCHEDULED\",\"plateId\":10,\"protocolId\":10}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":11,\"measId\":11,\"outcome\":\"SCHEDULED\",\"plateId\":11,\"protocolId\":11}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":12,\"measId\":12,\"outcome\":\"SCHEDULED\",\"plateId\":12,\"protocolId\":12}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":13,\"measId\":13,\"outcome\":\"SCHEDULED\",\"plateId\":13,\"protocolId\":13}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":14,\"measId\":14,\"outcome\":\"SCHEDULED\",\"plateId\":14,\"protocolId\":14}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":15,\"measId\":15,\"outcome\":\"SCHEDULED\",\"plateId\":15,\"protocolId\":15}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":16,\"measId\":16,\"outcome\":\"SCHEDULED\",\"plateId\":16,\"protocolId\":16}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":17,\"measId\":17,\"outcome\":\"SCHEDULED\",\"plateId\":17,\"protocolId\":17}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":18,\"measId\":18,\"outcome\":\"SCHEDULED\",\"plateId\":18,\"protocolId\":18}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":19,\"measId\":19,\"outcome\":\"SCHEDULED\",\"plateId\":19,\"protocolId\":19}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":20,\"measId\":20,\"outcome\":\"SCHEDULED\",\"plateId\":20,\"protocolId\":20}" +
            "],\"status\":{\"first\":true,\"last\":false,\"totalElements\":35,\"totalPages\":2}}", res2);


        // 3. query second page
        var res3 = performRequest(get("/resultset?page=1"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":21,\"measId\":21,\"outcome\":\"SCHEDULED\",\"plateId\":21,\"protocolId\":21}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":22,\"measId\":22,\"outcome\":\"SCHEDULED\",\"plateId\":22,\"protocolId\":22}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":23,\"measId\":23,\"outcome\":\"SCHEDULED\",\"plateId\":23,\"protocolId\":23}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":24,\"measId\":24,\"outcome\":\"SCHEDULED\",\"plateId\":24,\"protocolId\":24}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":25,\"measId\":25,\"outcome\":\"SCHEDULED\",\"plateId\":25,\"protocolId\":25}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":26,\"measId\":26,\"outcome\":\"SCHEDULED\",\"plateId\":26,\"protocolId\":26}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":27,\"measId\":27,\"outcome\":\"SCHEDULED\",\"plateId\":27,\"protocolId\":27}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":28,\"measId\":28,\"outcome\":\"SCHEDULED\",\"plateId\":28,\"protocolId\":28}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":29,\"measId\":29,\"outcome\":\"SCHEDULED\",\"plateId\":29,\"protocolId\":29}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":30,\"measId\":30,\"outcome\":\"SCHEDULED\",\"plateId\":30,\"protocolId\":30}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":31,\"measId\":31,\"outcome\":\"SCHEDULED\",\"plateId\":31,\"protocolId\":31}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":32,\"measId\":32,\"outcome\":\"SCHEDULED\",\"plateId\":32,\"protocolId\":32}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":33,\"measId\":33,\"outcome\":\"SCHEDULED\",\"plateId\":33,\"protocolId\":33}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":34,\"measId\":34,\"outcome\":\"SCHEDULED\",\"plateId\":34,\"protocolId\":34}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":35,\"measId\":35,\"outcome\":\"SCHEDULED\",\"plateId\":35,\"protocolId\":35}" +
            "],\"status\":{\"first\":false,\"last\":true,\"totalElements\":35,\"totalPages\":2}}", res3);

        // 4. query random page
        var res4 = performRequest(get("/resultset?page=50"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[],\"status\":{\"first\":false,\"last\":true,\"totalElements\":35,\"totalPages\":2}}", res4);
    }

    @Test
    public void invalidJsonTest() throws Exception {
        var res1 = performRequest(
            MockMvcRequestBuilders.post("/resultset")
                .contentType("application/json")
                .content("{\"test"),
            HttpStatus.BAD_REQUEST);

        Assertions.assertEquals("{\"error\":\"Validation error\",\"status\":\"error\"}", res1);
    }

}
