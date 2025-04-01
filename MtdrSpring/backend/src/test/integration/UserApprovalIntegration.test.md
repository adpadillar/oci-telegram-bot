# Integration Test Documentation

## Test Name: User Approval and Telegram Bot Access Verification

**Requirement ID:** RF-017
**Test ID:** RF-I002
**System and/or Component:** Integration Test
**Status:** Not-Tested

### Test Data:

```json
{
  "pendingUser": {
    "id": 1,
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "user-pending-activation",
    "telegramId": "123456789",
    "title": "Junior Developer"
  },
  "approvalRequest": {
    "userId": 1,
    "role": "developer",
    "approved": true
  },
  "telegramCommands": ["/start", "/mytasks", "/createtask", "/sprints"]
}
```

### Acceptance Criteria:

#### Steps to Perform the Integration Test:

1. **Initial Setup:**

   - Ensure test database contains a pending user
   - Start web application server
   - Start Telegram bot service
   - Authenticate as administrator

2. **User Approval Process (Web Application):**

   - Navigate to pending users list
   - Select user to approve
   - Submit approval with role assignment
   - Verify successful approval response

3. **Web Application Verification:**

   - Check user list shows approved status
   - Verify user role updated to "developer"
   - Confirm approval timestamp is recorded

4. **Telegram Bot Access Verification:**
   - User initiates Telegram bot with `/start`
   - Verify bot recognizes approved user
   - Test each available command:
     - View tasks (`/mytasks`)
     - Create task (`/createtask`)
     - View sprints (`/sprints`)
   - Verify all responses are successful

#### Description of Final Expected Result:

1. User should be successfully approved via web interface
2. User's role should be updated in database
3. User should have immediate access to Telegram bot
4. All bot commands should work for approved user
5. System should maintain approval audit trail

#### Evidence of Expected Result:

**Web Application Response:**

```json
{
  "status": "success",
  "data": {
    "user": {
      "id": 1,
      "firstName": "Jane",
      "lastName": "Smith",
      "role": "developer",
      "telegramId": "123456789",
      "approvedAt": "2024-01-20T10:00:00Z",
      "approvedBy": {
        "id": 2,
        "firstName": "Admin",
        "lastName": "User",
        "role": "manager"
      }
    }
  }
}
```

**Telegram Bot Verification:**

```
✅ User Authentication Successful
Welcome Jane Smith!
Available commands:
/mytasks - View your assigned tasks
/createtask - Create a new task
/sprints - View active sprints
```

### Observations or Notes:

1. **Prerequisites:**

   - Administrator account must be available
   - Telegram bot must be running
   - Test database must contain pending user
   - Proper role permissions must be configured

2. **Security Considerations:**

   - Verify only administrators can approve users
   - Check role assignment restrictions
   - Ensure proper audit logging
   - Validate Telegram ID verification

3. **Integration Points:**

   - User approval API
   - Role management system
   - Telegram bot authentication
   - Audit logging system

4. **Error Scenarios:**

   - Invalid Telegram ID
   - Network connectivity issues
   - Concurrent approval attempts
   - Database consistency issues

5. **Performance Metrics:**
   - Approval process completion time
   - Bot response time for new user
   - Permission propagation delay
