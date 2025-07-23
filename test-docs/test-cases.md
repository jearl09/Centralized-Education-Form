# Test Cases

## Backend Test Cases

| ID   | Description                                      | Steps                                                                 | Expected Result                                 |
|------|--------------------------------------------------|-----------------------------------------------------------------------|-------------------------------------------------|
| TC1  | Submit form with valid data                      | Call submitForm with valid templateId, formData, userEmail            | Form is saved, notification sent                |
| TC2  | Submit form with missing required field           | Call submitForm with missing field                                    | Exception thrown ("Required field missing")     |
| TC3  | Submit form with inactive template                | Call submitForm with inactive template                                | Exception thrown ("Form template is not active")|
| TC4  | Approve form as valid approver                    | Call approveForm with valid formId, approverEmail                     | Form status updated to Approved, notification   |
| TC5  | Approve form as non-approver                      | Call approveForm as student                                           | Exception thrown ("User is not authorized")     |
| TC6  | Approve form not in pending status                | Call approveForm on already approved form                             | Exception thrown ("Form is not in pending status")|
| TC7  | Reject form as valid approver                     | Call rejectForm with valid formId, approverEmail                      | Form status updated to Rejected, notification   |
| TC8  | Get student forms                                | Call getStudentForms with valid email                                 | List of forms for student                       |
| TC9  | Register user with existing username              | Call registerUser with duplicate username                             | Exception thrown ("Username already exists")    |
| TC10 | Register user with existing email                 | Call registerUser with duplicate email                                | Exception thrown ("Email already exists")       |
| TC11 | Authenticate user with correct credentials        | Call authenticateUser with valid username/email and password          | User returned                                   |
| TC12 | Authenticate user with wrong password             | Call authenticateUser with valid username/email and wrong password    | Empty result                                    |
| TC13 | Create notification                              | Call createNotification with valid user, title, message, type         | Notification saved                              |
| TC14 | Mark notification as read                         | Call markAsRead with valid notificationId                             | Notification status updated                     |
| TC15 | Mark notification as archived (not found)         | Call markAsArchived with invalid notificationId                       | Null returned                                   |
| TC16 | Get user notifications                           | Call getUserNotifications with valid user                             | List of notifications                           |

## Frontend Test Cases

| ID   | Description                                      | Steps                                                                 | Expected Result                                 |
|------|--------------------------------------------------|-----------------------------------------------------------------------|-------------------------------------------------|
| FC1  | Student submits a form                           | Login as student, fill and submit form                                | Success message, form appears in dashboard      |
| FC2  | Approver reviews and approves form               | Login as approver, approve pending form                               | Status changes to Approved                      |
| FC3  | Notification appears for new form                | Submit form, check notifications                                      | Notification is visible                         |
| FC4  | Invalid login attempt                            | Enter wrong credentials                                               | Error message shown                             |
| FC5  | Required field validation                        | Leave required field blank, try to submit form                        | Error message shown, form not submitted         |
| FC6  | Navigation between dashboards                    | Switch between student/approver/admin dashboards                      | Correct dashboard and data shown                |
| FC7  | Responsive layout                                | Resize browser, use on mobile                                         | Layout adapts, no broken UI                     |
| FC8  | Mark notification as read                        | Click to mark notification as read                                    | Notification count decreases                    |
| FC9  | Approver rejects a form                          | Login as approver, reject a pending form                              | Status changes to Rejected, student notified    |
| FC10 | Logout                                           | Click logout button                                                   | User is logged out, redirected to login         | 