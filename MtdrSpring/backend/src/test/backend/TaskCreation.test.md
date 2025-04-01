# Backend Component Test Documentation

## Test Name: Create New Task via API Endpoint

**Requirement ID:** RF-002
**Test ID:** RF-T002
**System and/or Component Test:** Component Test
**Status:** Not-Tested

### Test Data:

```json
{
  "newTask": {
    "description": "Implement login feature",
    "status": "created",
    "category": "feature",
    "estimateHours": 8,
    "realHours": null,
    "priority": "high",
    "deadline": "2024-02-01T23:59:59Z",
    "assignedTo": 1,
    "createdBy": 2
  }
}
```

### Acceptance Criteria:

#### Description of Expected Result:

**Endpoint:** POST /api/{projectId}/tasks
**Expected Status Code:** 201 Created

**Response Body:**

```json
{
  "id": 1,
  "description": "Implement login feature",
  "status": "created",
  "category": "feature",
  "estimateHours": 8,
  "realHours": null,
  "priority": "high",
  "deadline": "2024-02-01T23:59:59Z",
  "assignedTo": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "role": "developer"
  },
  "createdBy": {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "manager"
  },
  "createdAt": "2024-01-20T10:00:00Z"
}
```

**Table: Tasks**
| Parameter | Type | Required | Description |
|--------------|----------|----------|--------------------------------|
| description | string | Yes | Task description |
| status | string | Yes | Initial status |
| category | string | Yes | Task category |
| estimateHours| number | No | Estimated completion hours |
| realHours | number | No | Actual hours spent |
| priority | string | Yes | Task priority level |
| deadline | datetime | No | Task completion deadline |
| assignedTo | number | No | User ID of assigned developer |
| createdBy | number | Yes | User ID of task creator |

#### Evidence of Expected Result:

```json
{
  "id": 1,
  "description": "Implement login feature",
  "status": "created",
  "category": "feature",
  "estimateHours": 8,
  "realHours": null,
  "priority": "high",
  "deadline": "2024-02-01T23:59:59Z",
  "assignedTo": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "role": "developer"
  },
  "createdBy": {
    "id": 2,
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "manager"
  },
  "createdAt": "2024-01-20T10:00:00Z"
}
```

### Observations or Notes:

1. Status should be validated against allowed values: ["created", "in-progress", "in-review", "testing", "done"]
2. Category should be validated against allowed values: ["feature", "bug", "issue"]
3. Priority should be validated against allowed values: ["low", "medium", "high", "critical"]
4. The assigned user must exist and be a developer
5. The creator must exist and be either a developer or manager
6. All timestamps should be in UTC
7. Task IDs should be auto-generated
