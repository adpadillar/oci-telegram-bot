import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserCreationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateNewDeveloper() throws Exception {
        String newUserJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "role": "developer",
                "title": "Senior Software Engineer",
                "telegramId": "123456789"
            }
            """;

        mockMvc.perform(post("/api/1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("developer"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldRejectInvalidRole() throws Exception {
        String invalidUserJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "role": "invalid-role",
                "telegramId": "123456789"
            }
            """;

        mockMvc.perform(post("/api/1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyRequiredFields() throws Exception {
        String invalidUserJson = """
            {
                "firstName": "",
                "lastName": "Doe",
                "role": "developer",
                "telegramId": "123456789"
            }
            """;

        mockMvc.perform(post("/api/1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
                .andExpect(status().isBadRequest());
    }
}
