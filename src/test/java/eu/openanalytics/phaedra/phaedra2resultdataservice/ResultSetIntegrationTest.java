package eu.openanalytics.phaedra.phaedra2resultdataservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SuppressWarnings("rawtypes")
@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Phaedra2ResultDataServiceApplication.class)
@WebAppConfiguration
@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application-integration-test.properties")
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

//    @Autowired
//    private ResultSetController resultSetController;

//    @Autowired
//    private ObjectMapper om;
//

    public ResultSetIntegrationTest() {
        om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ensure we don't send null values to the API (e.g. when doing updates)
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
        return mvcResult.getResponse().getContentAsString();
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


}
