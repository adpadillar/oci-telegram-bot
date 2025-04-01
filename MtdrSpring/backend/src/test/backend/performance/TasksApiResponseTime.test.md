# Backend Non-Functional Test Documentation

## Test Name: Tasks API Response Time Measurement

**Requirement ID:** RNF-001
**Test ID:** RNF-T001
**System and/or Component Test:** System Test
**Status:** Not-Tested

### Test Data:

```json
{
  "testConfiguration": {
    "numberOfRequests": 100,
    "concurrentUsers": 10,
    "endpoint": "/api/1/tasks",
    "method": "GET",
    "expectedMaxResponseTime": 200,
    "testDuration": "5 minutes"
  },
  "sampleDataset": {
    "numberOfTasks": 50,
    "tasksWithAttachments": 10,
    "tasksWithComments": 20
  }
}
```

### Acceptance Criteria:

#### Description of Expected Result:

The GET /api/{projectId}/tasks endpoint should consistently respond within 200 milliseconds under normal load conditions.

#### Steps to Verify:

1. **Setup Phase:**

   - Prepare test database with sample dataset
   - Configure monitoring tools
   - Ensure system is in a clean state

2. **Execution Phase:**

   - Execute 100 requests with 10 concurrent users
   - Record response times for each request
   - Monitor system resources during test

3. **Measurement Phase:**

   - Calculate average response time
   - Calculate 95th percentile response time
   - Record maximum response time
   - Count any responses exceeding 200ms threshold

4. **Success Criteria:**
   - Average response time < 200ms
   - 95th percentile response time < 200ms
   - No single response > 500ms
   - Zero errors or timeouts

#### Evidence of Expected Result:

Example performance metrics report:

```json
{
  "testResults": {
    "totalRequests": 100,
    "successfulRequests": 100,
    "failedRequests": 0,
    "averageResponseTime": 150,
    "95thPercentileResponseTime": 180,
    "maxResponseTime": 190,
    "requestsExceeding200ms": 0,
    "errorRate": "0%"
  },
  "systemMetrics": {
    "averageCpuUsage": "45%",
    "averageMemoryUsage": "60%",
    "averageDatabaseConnectionPool": "30%"
  }
}
```

### Observations or Notes:

1. Test should be run during both peak and off-peak hours
2. System should be monitored for:
   - CPU usage
   - Memory consumption
   - Database connection pool status
   - Network latency
3. Factors that might affect results:
   - Database size
   - Number of concurrent users
   - Network conditions
   - Server specifications
4. Recommended tools:
   - JMeter or k6 for load testing
   - Prometheus for metrics collection
   - Grafana for visualization
5. Test environment should mirror production specifications
6. Consider testing with different dataset sizes
7. Document any caching mechanisms in place
