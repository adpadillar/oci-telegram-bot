# Backend System Test Documentation

## Test Name: Update Project Name and Verify Changes

**Requirement ID:** RF-SYS-001
**Test ID:** RF-SYS-T001
**System and/or Component Test:** System Test
**Status:** Not-Tested

### Test Data:

```json
{
    "updateRequest": {
        "name": "Updated Project Name",

    "[projectId]": {
        "id": 1,
        "name": "Original Project Name",
        "description": "Original Project Description",
        "started_at": "2022-01-01T00:00:00Z",
        "ended_at": "2022-12-31T23:59:59Z",
        "tasks": [
            {
                "id": 101,
                "description": "Sample Task",
                "project": {
                    "id": 1,
                    "name": "Original Project Name",
                    "description": "Original Project Description",
                    "started_at": "2022-01-01T00:00:00Z",
                    "ended_at": "2022-12-31T23:59:59Z"
                }
            }
        ]
    }
}
```

### Acceptance Criteria:

#### Description of Expected Result:

1. First Request:
   **Endpoint:** PATCH /api/projects/{projectId}
   **Expected Status Code:** 200 OK
2. Second Request (Verification):
   **Endpoint:** GET /api/{projectId}/tasks
   **Expected Status Code:** 200 OK

#### Test Flow:

1. Send PATCH request to update project name
2. Verify PATCH response contains updated name
3. Send GET request to fetch tasks
4. Verify project name is updated in all task references

#### Evidence of Expected Result:

**PATCH Response:**

```json
{
  "id": 1,
  "name": "Updated Project Name"
}
```

**GET Tasks Response:**

```json
[
  {
    "id": 101,
    "description": "Sample Task",
    "project": {
      "id": 1,
      "name": "Updated Project Name"
    }
  }
]
```

### Observations or Notes:

1. Project name should be updated across all related entities
2. The update should be atomic - either complete successfully or fail entirely
3. Project ID should remain unchanged
4. System should validate the new project name (not empty, length limits, etc.)
5. Test should verify data consistency across related entities
6. Consider testing with concurrent requests
7. Should handle special characters in project name appropriately
