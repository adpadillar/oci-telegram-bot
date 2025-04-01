import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdateProjectNameAndVerifyInTasks() throws Exception {
        // 1. Update project name
        String updateJson = """
            {
                "name": "Updated Project Name"
            }
            """;

        ResultActions updateResult = mockMvc.perform(patch("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Project Name"));

        // 2. Verify update in tasks
        mockMvc.perform(get("/api/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].project.name").value("Updated Project Name"))
                .andExpect(jsonPath("$[*].project.id").value(1));
    }

    @Test
    void shouldRejectEmptyProjectName() throws Exception {
        String invalidJson = """
            {
                "name": ""
            }
            """;

        mockMvc.perform(patch("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleNonExistentProject() throws Exception {
        String updateJson = """
            {
                "name": "Updated Project Name"
            }
            """;

        mockMvc.perform(patch("/api/projects/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreserveProjectDataConsistency() throws Exception {
        // 1. Get initial task count
        ResultActions initialTasks = mockMvc.perform(get("/api/1/tasks"))
                .andExpect(status().isOk());

        // 2. Update project name
        String updateJson = """
            {
                "name": "New Project Name"
            }
            """;

        mockMvc.perform(patch("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // 3. Verify task count remains the same
        ResultActions updatedTasks = mockMvc.perform(get("/api/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(jsonPath("$.length()", initialTasks)));
    }
}
