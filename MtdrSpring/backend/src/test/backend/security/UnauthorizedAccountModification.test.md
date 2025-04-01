# Backend Non-Functional Test Documentation

## Test Name: Unauthorized Account Modification Prevention

**Requirement ID:** RNF-003
**Test ID:** RNF-S001
**System and/or Component Test:** System Test
**Status:** Not-Tested

### Test Data:

```json
{
  "validUser": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "role": "developer",
    "telegramId": "123456789"
  },
  "unauthorizedModification": {
    "firstName": "Hacked",
    "lastName": "Account",
    "role": "manager",
    "telegramId": "987654321"
  },
  "testCases": [
    {
      "scenario": "Developer attempting to modify another developer's account",
      "endpoint": "PATCH /api/1/users/2",
      "expectedResponse": 403
    },
    {
      "scenario": "Developer attempting to escalate their role to manager",
      "endpoint": "PATCH /api/1/users/1",
      "expectedResponse": 403
    },
    {
      "scenario": "Unauthenticated user attempting to modify any account",
      "endpoint": "PATCH /api/1/users/1",
      "expectedResponse": 401
    }
  ]
}
```

### Acceptance Criteria:

#### Description of Expected Result:

1. The system must reject all unauthorized modification attempts
2. Only authenticated users can access account modification endpoints
3. Users can only modify their own account information
4. Role modifications require manager approval
5. All modification attempts must be logged for security audit

#### Steps to Verify:

1. Authentication Test:

   - Attempt to modify account without authentication token
   - Expected: 401 Unauthorized response

2. Authorization Test:

   - Authenticate as regular developer
   - Attempt to modify another user's account
   - Expected: 403 Forbidden response

3. Role Escalation Test:

   - Authenticate as developer
   - Attempt to change own role to manager
   - Expected: 403 Forbidden response

4. Audit Log Verification:
   - Verify all unauthorized attempts are logged with:
     - Timestamp
     - IP address
     - Attempted action
     - Target account
     - Response code

#### Evidence of Expected Result:

```json
{
  "error": {
    "status": 403,
    "message": "Forbidden: You don't have permission to modify this account",
    "timestamp": "2024-01-20T10:00:00Z",
    "path": "/api/1/users/2"
  },
  "auditLog": {
    "timestamp": "2024-01-20T10:00:00Z",
    "action": "PATCH",
    "targetUser": 2,
    "requestingUser": 1,
    "ipAddress": "192.168.1.100",
    "status": "DENIED",
    "reason": "UNAUTHORIZED_MODIFICATION"
  }
}
```

### Observations or Notes:

1. All failed attempts should be rate-limited to prevent brute force attacks
2. Security headers should be properly configured
3. Sensitive data should not be exposed in error messages
4. Consider implementing account lockout after multiple failed attempts
5. Ensure proper CORS configuration
6. Monitor and alert on suspicious patterns
7. Use HTTPS for all requests
8. Consider implementing JWT token validation
9. Log all security-related events
