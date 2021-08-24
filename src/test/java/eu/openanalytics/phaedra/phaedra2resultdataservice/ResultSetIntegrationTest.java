package eu.openanalytics.phaedra.phaedra2resultdataservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.openanalytics.phaedra.phaedra2resultdataservice.dto.ResultSetDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Phaedra2ResultDataServiceApplication.class, ResultSetIntegrationTest.IntegrationTestConfiguration.class})
@WebAppConfiguration
@AutoConfigureMockMvc
public class ResultSetIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13-alpine");

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

    @Autowired
    private DataSource dataSource;

    /**
     * Clean tables and sequences before every test (this is aster than restarting the container and Spring context).
     */
    @BeforeEach
    public void initEach() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement("TRUNCATE result_data RESTART IDENTITY CASCADE ;")) {
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = con.prepareStatement("TRUNCATE result_set RESTART IDENTITY CASCADE;")) {
                stmt.executeUpdate();
            }
        }
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
        Assertions.assertEquals("{\"error\":\"ResultSet with id 1 not found!\",\"status\":\"error\"}", res5);
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

    @Test
    public void testPagedQueries() throws Exception {
        // 1. create 35 ResultSets
        for (int i = 1; i <= 35; i++) {
            var input = new ResultSetDTO();
            input.setProtocolId((long) i);
            input.setPlateId((long) i);
            input.setMeasId((long) i);
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

    @Configuration
    public static class IntegrationTestConfiguration {

        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.parse("2042-12-31T23:59:59.00Z"), ZoneId.of("UTC"));
        }

    }
}
