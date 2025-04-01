import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TasksApiResponseTimeTest {

    @Autowired
    private MockMvc mockMvc;

    private static final int NUMBER_OF_REQUESTS = 100;
    private static final int CONCURRENT_USERS = 10;
    private static final long MAX_RESPONSE_TIME = 200L; // milliseconds

    @Test
    void shouldRespondWithinTimeLimit() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        List<Long> responseTimes = new ArrayList<>();

        // Execute requests
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    MvcResult result = mockMvc.perform(get("/api/1/tasks"))
                            .andExpect(status().isOk())
                            .andReturn();
                    long endTime = System.currentTimeMillis();
                    return endTime - startTime;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            futures.add(future);
        }

        // Collect results
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        for (CompletableFuture<Long> future : futures) {
            responseTimes.add(future.get());
        }

        // Calculate metrics
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        // Sort for percentile calculation
        List<Long> sortedTimes = new ArrayList<>(responseTimes);
        sortedTimes.sort(Long::compareTo);
        int index95 = (int) Math.ceil(0.95 * sortedTimes.size()) - 1;
        long percentile95 = sortedTimes.get(index95);

        // Assertions
        assertTrue(averageResponseTime < MAX_RESPONSE_TIME, 
                "Average response time (" + averageResponseTime + "ms) exceeded limit of " + MAX_RESPONSE_TIME + "ms");
        assertTrue(percentile95 < MAX_RESPONSE_TIME,
                "95th percentile response time (" + percentile95 + "ms) exceeded limit of " + MAX_RESPONSE_TIME + "ms");
        assertTrue(maxResponseTime < MAX_RESPONSE_TIME * 2.5,
                "Maximum response time (" + maxResponseTime + "ms) exceeded limit of " + (MAX_RESPONSE_TIME * 2.5) + "ms");

        executor.shutdown();
    }
}
