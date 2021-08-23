package eu.openanalytics.phaedra.phaedra2resultdataservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SuppressWarnings("rawtypes")
@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Phaedra2ResultDataServiceApplication.class)
@WebAppConfiguration
@AutoConfigureMockMvc
public class ResultSetIntegrationTest {

    @Container
    public static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:13-alpine");

    private final ObjectMapper om;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("phaedra2.result-data-service.db.url", postgreSQLContainer::getJdbcUrl);
        registry.add("phaedra2.result-data-service.db.username", postgreSQLContainer::getUsername);
        registry.add("phaedra2.result-data-service.db.password", postgreSQLContainer::getPassword);
    }

    public ResultSetIntegrationTest() {
        om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ensure we don't send null values to the API (e.g. when doing updates)
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        om.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    private <T> T performRequest(RequestBuilder requestBuilder, HttpStatus responseStatusCode, Class<T> resultType) throws Exception {
        var mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals("application/json", mvcResult.getResponse().getContentType());
        Assertions.assertEquals(responseStatusCode.value(), mvcResult.getResponse().getStatus());

        Assertions.assertNotNull(mvcResult.getResponse().getContentAsString());
        var res = om.readValue(mvcResult.getResponse().getContentAsString(), resultType);
        Assertions.assertNotNull(res);
        return res;
    }

    private String performRequest(RequestBuilder requestBuilder, HttpStatus responseStatusCode) throws Exception {
        var mvcResult = mockMvc.perform(requestBuilder).andReturn();

        Assertions.assertEquals(responseStatusCode.value(), mvcResult.getResponse().getStatus());
        if (!mvcResult.getResponse().getContentAsString().equals("")) {
            Assertions.assertEquals("application/json", mvcResult.getResponse().getContentType());
            // de-serialize and serialize responses in order to have a consistent response
            Object parsedConfig = om.readValue(mvcResult.getResponse().getContentAsString(), Object.class);
            return om.writeValueAsString(parsedConfig);
        }
        return null;
    }

    private RequestBuilder post(String url, Object input) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(url)
            .contentType("application/json")
            .content(om.writeValueAsString(input));
    }

    private RequestBuilder put(String url, Object input) throws JsonProcessingException {
        return MockMvcRequestBuilders.put(url)
            .contentType("application/json")
            .content(om.writeValueAsString(input));
    }

    @Test
    public void simpleCreateAndGetTest() throws Exception {
        // 1. create simple ResultSet
        var input1 = new ResultSetDTO();
        input1.setProtocolId(1L);
        input1.setPlateId(2L);
        input1.setMeasId(3L);

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
        var input2 = new ResultSetDTO();
        input2.setOutcome("MyOutcome!");
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
        Assertions.assertEquals("{\"error\":\"ResultDataSet with id 1 not found!\",\"status\":\"error\"}", res5);
    }

    @Test
    public void testCreationValidationTest() throws Exception {
        // 1. missing fields
        var input1 = new ResultSetDTO();
        var res1 = performRequest(post("/resultset", input1), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{" +
            "\"measId\":\"MeasId is mandatory\"," +
            "\"plateId\":\"PlateId is mandatory\"," +
            "\"protocolId\":\"ProtocolId is mandatory\"},\"status\":\"error\"}", res1);

        // 2. too many fields
        var input2 = new ResultSetDTO();
        input2.setPlateId(1L);
        input2.setProtocolId(2L);
        input2.setMeasId(3L);
        input2.setId(1L);
        input2.setOutcome("my outcome");
        input2.setExecutionStartTimeStamp(LocalDateTime.now());
        input2.setExecutionEndTimeStamp(LocalDateTime.now());
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
        var input1 = new ResultSetDTO();
        var res1 = performRequest(put("/resultset/1", input1), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"outcome\":\"Outcome is mandatory when updating a ResultSet\"},\"status\":\"error\"}", res1);

        // 2. too long outcome
        var input2 = new ResultSetDTO();
        input2.setOutcome("a".repeat(260));
        var res2 = performRequest(put("/resultset/1", input2), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"Validation error\",\"malformed_fields\":{\"outcome\":\"Outcome may only contain 255 characters\"},\"status\":\"error\"}", res2);

        // 3. too many fields
        var input3 = new ResultSetDTO();
        input3.setId(1L);
        input3.setPlateId(1L);
        input3.setProtocolId(1L);
        input3.setMeasId(1L);
        input3.setExecutionStartTimeStamp(LocalDateTime.now());
        input3.setExecutionEndTimeStamp(LocalDateTime.now());
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
        var input1 = new ResultSetDTO();
        input1.setOutcome("MyOutcome!");
        var res1 = performRequest(put("/resultset/4", input1), HttpStatus.NOT_FOUND);
        Assertions.assertEquals("{\"error\":\"ResultDataSet with id 4 not found!\",\"status\":\"error\"}", res1);

    }


    @Test
    public void testDataSetAlreadyCompleted() throws Exception {
        // 1. create simple ResultSet
        var input1 = new ResultSetDTO();
        input1.setProtocolId(1L);
        input1.setPlateId(2L);
        input1.setMeasId(3L);

        performRequest(post("/resultset", input1), HttpStatus.CREATED, ResultSetDTO.class);

        // 2. update outcome
        var input2 = new ResultSetDTO();
        input2.setOutcome("MyOutcome!");
        performRequest(put("/resultset/1", input2), HttpStatus.OK, ResultSetDTO.class);

        // 3. update outcome again
        var input3 = new ResultSetDTO();
        input3.setOutcome("MyOutcome33!");
        var res3 = performRequest(put("/resultset/1", input3), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals("{\"error\":\"ResultDataSet already contains a complete message or end timestamp.\",\"status\":\"error\"}", res3);
    }

}
