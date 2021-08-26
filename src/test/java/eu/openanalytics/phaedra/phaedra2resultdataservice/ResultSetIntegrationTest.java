package eu.openanalytics.phaedra.phaedra2resultdataservice;

import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
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
        Assertions.assertNull(res1.getOutcome());
        Assertions.assertNull(res1.getExecutionEndTimeStamp());

        // 2. get simple resultSet
        var res2 = performRequest(get("/resultset/1"), HttpStatus.OK, ResultSetDTO.class);
        Assertions.assertEquals(1L, res2.getId());
        Assertions.assertEquals(2L, res2.getPlateId());
        Assertions.assertEquals(3L, res2.getMeasId());
        Assertions.assertNotNull(res2.getExecutionStartTimeStamp());
        Assertions.assertNull(res2.getOutcome());
        Assertions.assertNull(res2.getExecutionEndTimeStamp());

        // 3. update outcome
        var input2 = ResultSetDTO.builder()
            .outcome("MyOutcome!")
            .build();
        var res3 = performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);
        Assertions.assertEquals(1L, res3.getId());
        Assertions.assertEquals(2L, res3.getPlateId());
        Assertions.assertEquals(3L, res3.getMeasId());
        Assertions.assertNotNull(res3.getExecutionStartTimeStamp());
        Assertions.assertEquals("MyOutcome!", res3.getOutcome());
        Assertions.assertNotNull(res3.getExecutionEndTimeStamp());

        // 4. get resultSet again
        var res4 = performRequest(get("/resultset/1"), HttpStatus.OK, ResultSetDTO.class);
        Assertions.assertEquals(1L, res4.getId());
        Assertions.assertEquals(2L, res4.getPlateId());
        Assertions.assertEquals(3L, res4.getMeasId());
        Assertions.assertNotNull(res4.getExecutionStartTimeStamp());
        Assertions.assertEquals("MyOutcome!", res4.getOutcome());
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
            .outcome("my outcome")
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
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"protocolId\":\"Invalid value provided\"},\"status\":\"error\"}", res3);
    }

    @Test
    public void testUpdatingValidationTest() throws Exception {
        // 1. missing fields
        var input1 = ResultSetDTO.builder().build();
        var res1 = performRequest(put("/resultset/1", input1), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"outcome\":\"Outcome is mandatory when updating a ResultSet\"},\"status\":\"error\"}", res1);

        // 2. too long outcome
        var input2 = ResultSetDTO.builder()
            .outcome("a".repeat(260))
            .build();
        var res2 = performRequest(put("/resultset/1", input2), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"outcome\":\"Outcome may only contain 255 characters\"},\"status\":\"error\"}", res2);

        // 3. too many fields
        var input3 = ResultSetDTO.builder()
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
            .outcome("MyOutcome!")
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
            .outcome("MyOutcome!")
            .build();
        performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);

        // 3. update outcome again
        var input3 = ResultSetDTO.builder()
            .outcome("MyOutcome33!")
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
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":1,\"measId\":1,\"plateId\":1,\"protocolId\":1}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":2,\"measId\":2,\"plateId\":2,\"protocolId\":2}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":3,\"measId\":3,\"plateId\":3,\"protocolId\":3}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":4,\"measId\":4,\"plateId\":4,\"protocolId\":4}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":5,\"measId\":5,\"plateId\":5,\"protocolId\":5}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":6,\"measId\":6,\"plateId\":6,\"protocolId\":6}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":7,\"measId\":7,\"plateId\":7,\"protocolId\":7}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":8,\"measId\":8,\"plateId\":8,\"protocolId\":8}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":9,\"measId\":9,\"plateId\":9,\"protocolId\":9}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":10,\"measId\":10,\"plateId\":10,\"protocolId\":10}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":11,\"measId\":11,\"plateId\":11,\"protocolId\":11}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":12,\"measId\":12,\"plateId\":12,\"protocolId\":12}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":13,\"measId\":13,\"plateId\":13,\"protocolId\":13}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":14,\"measId\":14,\"plateId\":14,\"protocolId\":14}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":15,\"measId\":15,\"plateId\":15,\"protocolId\":15}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":16,\"measId\":16,\"plateId\":16,\"protocolId\":16}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":17,\"measId\":17,\"plateId\":17,\"protocolId\":17}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":18,\"measId\":18,\"plateId\":18,\"protocolId\":18}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":19,\"measId\":19,\"plateId\":19,\"protocolId\":19}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":20,\"measId\":20,\"plateId\":20,\"protocolId\":20}" +
            "],\"status\":{\"first\":true,\"last\":false,\"totalElements\":35,\"totalPages\":2}}", res2);


        // 3. query second page
        var res3 = performRequest(get("/resultset?page=1"), HttpStatus.OK);
        Assertions.assertEquals("{\"data\":[" +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":21,\"measId\":21,\"plateId\":21,\"protocolId\":21}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":22,\"measId\":22,\"plateId\":22,\"protocolId\":22}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":23,\"measId\":23,\"plateId\":23,\"protocolId\":23}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":24,\"measId\":24,\"plateId\":24,\"protocolId\":24}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":25,\"measId\":25,\"plateId\":25,\"protocolId\":25}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":26,\"measId\":26,\"plateId\":26,\"protocolId\":26}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":27,\"measId\":27,\"plateId\":27,\"protocolId\":27}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":28,\"measId\":28,\"plateId\":28,\"protocolId\":28}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":29,\"measId\":29,\"plateId\":29,\"protocolId\":29}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":30,\"measId\":30,\"plateId\":30,\"protocolId\":30}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":31,\"measId\":31,\"plateId\":31,\"protocolId\":31}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":32,\"measId\":32,\"plateId\":32,\"protocolId\":32}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":33,\"measId\":33,\"plateId\":33,\"protocolId\":33}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":34,\"measId\":34,\"plateId\":34,\"protocolId\":34}," +
            "{\"executionStartTimeStamp\":\"2042-12-31T23:59:59\",\"id\":35,\"measId\":35,\"plateId\":35,\"protocolId\":35}]," +
            "\"status\":{\"first\":false,\"last\":true,\"totalElements\":35,\"totalPages\":2}}", res3);

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
