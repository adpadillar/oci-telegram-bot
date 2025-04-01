# Backend Component Test Documentation

## Test Name: Create New Developer via API Endpoint

**Requirement ID:** RF-015
**Test ID:** RF-T015
**System and/or Component Test:** Component Test
**Status:** Not-Tested

### Test Data:

```json
{
  "requestBody": {
    "firstName": "John",
    "lastName": "Doe",
    "role": "developer",
    "title": "Senior Software Engineer",
    "telegramId": "123456789"
  }
}
```

### Acceptance Criteria:

#### Description of Expected Result:

**Endpoint:** POST /api/{projectId}/users

**Table: Users**
| Parameter | Type | Required | Description |
|------------|--------|----------|----------------------------|
| firstName | string | Yes | User's first name |
| lastName | string | Yes | User's last name |
| role | string | Yes | Must be "developer" |
| title | string | No | Job title |
| telegramId | string | No | Telegram unique identifier |

**Expected Response:**

```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "role": "developer",
    "title": "Senior Software Engineer",
    "telegramId": "123456789",
    "createdAt": "2024-01-20T10:00:00Z"
  }
]
```

```

To verify the creation of the new user, perform a GET request to the endpoint `/api/{projectId}/users` and ensure the newly created user is included in the response.

**Expected Status Code:** 201 Created

#### Evidence of Expected Result:

1. New user record in the database
2. Response body matches the expected format
3. User is successfully associated with the project

### Observations or Notes:

1. Only project managers should have permission to create new users
2. Validate that telegramId is unique across all users
3. The role field should be automatically set to "developer"
4. The response should include the newly created user's ID
5. All required fields must be validated
6. First name and last name should not be empty strings
7. Creation timestamp should be automatically set
8. Response should include the complete user object
```
