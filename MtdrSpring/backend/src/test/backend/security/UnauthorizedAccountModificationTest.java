import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UnauthorizedAccountModificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        String updateJson = """
            {
                "firstName": "Hacked",
                "role": "manager"
            }
            """;

        mockMvc.perform(patch("/api/1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "developer1", roles = {"DEVELOPER"})
    void shouldRejectUnauthorizedModification() throws Exception {
        String updateJson = """
            {
                "firstName": "Hacked",
                "role": "manager"
            }
            """;

        mockMvc.perform(patch("/api/1/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "developer1", roles = {"DEVELOPER"})
    void shouldRejectRoleEscalation() throws Exception {
        String updateJson = """
            {
                "role": "manager"
            }
            """;

        mockMvc.perform(patch("/api/1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @WithMockUser(username = "developer1", roles = {"DEVELOPER"})
    void shouldAllowModificationOfOwnBasicInfo() throws Exception {
        String updateJson = """
            {
                "firstName": "John",
                "lastName": "Updated"
            }
            """;

        mockMvc.perform(patch("/api/1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());
    }
}
