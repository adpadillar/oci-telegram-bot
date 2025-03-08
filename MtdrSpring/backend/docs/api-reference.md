# API Reference

### Project API

#### Get all projects

**HTTP GET /api/projects**
Example Output: 200 OK

```json
[
  {
    "id": 1,
    "name": "Project 1",
    "startTime": "2025-01-01T00:00:00Z",
    "endTime": "2025-12-31T23:59:59Z"
  },
  ...
]
```

#### Add a project

**HTTP POST /api/projects**
Example Input:

```json
{
  "name": "New Project",
  "startTime": "2025-01-01T00:00:00Z",
  "endTime": "2025-12-31T23:59:59Z"
}
```

Example Output: 200 OK

#### Get project by ID

**HTTP GET /api/projects/{id}**
Example Output: 200 OK

```json
{
  "id": 1,
  "name": "Project 1",
  "startTime": "2025-01-01T00:00:00Z",
  "endTime": "2025-12-31T23:59:59Z"
}
```

Example Output: 404 NOT FOUND

### Sprint API

#### Get all sprints for a project

**HTTP GET /api/{project}/sprints**
Example Output: 200 OK

```json
[
  {
    "id": 1,
    "name": "Sprint 1",
    "description": "First sprint",
    "startedAt": "2025-01-01T00:00:00Z",
    "endsAt": "2025-01-15T23:59:59Z"
  },
  ...
]
```

#### Add a sprint to a project

**HTTP POST /api/{project}/sprints**
Example Input:

```json
{
  "name": "Sprint 1",
  "description": "First sprint",
  "startedAt": "2025-01-01T00:00:00Z",
  "endsAt": "2025-01-15T23:59:59Z"
}
```

Example Output: 200 OK

#### Get sprint by ID

**HTTP GET /api/{project}/sprints/{id}**
Example Output: 200 OK

```json
{
  "id": 1,
  "name": "Sprint 1",
  "description": "First sprint",
  "startedAt": "2025-01-01T00:00:00Z",
  "endsAt": "2025-01-15T23:59:59Z"
}
```

Example Output: 404 NOT FOUND

#### Update sprint by ID

**HTTP PATCH /api/{project}/sprints/{id}**
Example Input:

```json
{
  "name": "Updated Sprint",
  "description": "Updated description",
  "startedAt": "2025-01-02T00:00:00Z",
  "endsAt": "2025-01-16T23:59:59Z"
}
```

Example Output: 200 OK

### Task API

#### Get all tasks for a project

**HTTP GET /api/{project}/tasks**
Example Output: 200 OK

```json
[
  {
    "id": 1,
    "description": "Task 1",
    "status": "created",
    "createdBy": 1,
    "assignedTo": 2,
    "estimateHours": 5.0,
    "realHours": 4.0,
    "sprint": 1,
    "category": "development"
  },
  ...
]
```

#### Add a task to a project

**HTTP POST /api/{project}/tasks**
Example Input:

```json
{
  "description": "Task 1",
  "status": "created",
  "createdBy": 1,
  "assignedTo": 2,
  "estimateHours": 5.0,
  "realHours": 4.0,
  "sprint": 1,
  "category": "development"
}
```

Example Output: 200 OK

#### Get task by ID

**HTTP GET /api/{project}/tasks/{id}**
Example Output: 200 OK

```json
{
  "id": 1,
  "description": "Task 1",
  "status": "created",
  "createdBy": 1,
  "assignedTo": 2,
  "estimateHours": 5.0,
  "realHours": 4.0,
  "sprint": 1,
  "category": "development"
}
```

Example Output: 404 NOT FOUND

#### Update task by ID

**HTTP PATCH /api/{project}/tasks/{id}**
Example Input:

```json
{
  "description": "Updated Task",
  "status": "in progress",
  "createdBy": 1,
  "assignedTo": 2,
  "estimateHours": 6.0,
  "realHours": 5.0,
  "sprint": 1,
  "category": "testing"
}
```

Example Output: 200 OK

#### Delete task by ID

**HTTP DELETE /api/{project}/tasks/{id}**
Example Output: 200 OK
Example Output: 404 NOT FOUND

### User API

#### Get all users for a project

**HTTP GET /api/{project}/users**
Example Output: 200 OK

```json
[
  {
    "id": 1,
    "telegramId": "123456",
    "firstName": "John",
    "lastName": "Doe",
    "role": "developer"
  },
  ...
]
```

#### Add a user to a project

**HTTP POST /api/{project}/users**
Example Input:

```json
{
  "telegramId": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "role": "developer"
}
```

Example Output: 200 OK

#### Get user by ID

**HTTP GET /api/{project}/users/{id}**
Example Output: 200 OK

```json
{
  "id": 1,
  "telegramId": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "role": "developer"
}
```

Example Output: 404 NOT FOUND

#### Update user by ID

**HTTP PATCH /api/{project}/users/{id}**
Example Input:

```json
{
  "telegramId": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "role": "developer"
}
```

Example Output: 200 OK

#### Delete user by ID

**HTTP DELETE /api/{project}/users/{id}**
Example Output: 200 OK
Example Output: 404 NOT FOUND
