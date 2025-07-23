# Centralized Testing & QA Summary

---

## 1. Test Plan

*(from test-plan.md)*

### Objectives
- Ensure all critical backend and frontend features work as intended
- Validate security, usability, and performance
- Identify and document defects

### Scope
- Backend: API endpoints, business logic, authentication, notifications
- Frontend: User flows for students, approvers, admins; form submission; dashboards; notifications

### Approach
- Automated unit and integration tests for backend (JUnit, Mockito)
- Manual testing for frontend (checklists, exploratory)
- Peer QA across groups

### Resources
- Testers: Developers, peer QA group
- Tools: JUnit, Mockito, browser, spreadsheet/issue tracker

### Schedule
- Unit/integration tests: Week 1
- Manual/peer QA: Week 2
- Bug fixing & retest: Week 3

### Deliverables
- Test cases, bug tracker, summary report

---

## 2. Test Cases

*(from test-cases.md)*

### Backend Test Cases
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

### Frontend Test Cases
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

---

## 3. Bug Tracker / Issue Log

| ID  | Date       | Reported By | Description | Steps to Reproduce | Severity | Status | Assigned To | Resolution |
|-----|------------|-------------|-------------|--------------------|----------|--------|-------------|------------|
| 1   | 2025-07-08 | MANDAWE | No bugs found in backend unit tests | Ran all FormService, UserService, NotificationService tests | N/A      | Closed | N/A         | Not a bug   |
| 2   |            |             |             |                    |          | Open   |             |            |

---

## 4. Test Summary Report

### Overview
- Automated unit tests were executed for FormService, UserService, and NotificationService.
- All tests passed successfully. No errors or failures were detected.
- No integration or frontend automated tests were run in this cycle.

### Test Coverage
- Backend: 100% of available unit tests for core services executed (FormService, UserService, NotificationService).
- Frontend: Manual test cases are prepared but not executed in this run.

### Defects
- Total reported: 0
- Open: 0
- Closed: 0

### Key Findings
- All core backend service logic is covered by unit tests and is functioning as expected.
- No regressions or critical issues found in the tested areas.
- Further integration and frontend testing is recommended for full coverage.

### Conclusion
- The backend service layer is stable based on current unit tests.
- Next steps: Expand integration and frontend test automation, and conduct manual/peer QA as per the test plan.

---

## 5. Peer QA Testing Process & Template

### Process Overview
- Assign your app to a peer group for QA.
- Peer group tests the app using the manual checklist and test cases.
- All issues/bugs are logged in the Bug Tracker.
- Developers review, fix, and update status.

### Peer QA Checklist (2025-07-08)
- ✅ Can log in as each user role
- ✅ Can submit, approve, and reject forms
- ✅ Notifications work for all actions
- ✅ UI is clear and usable
- ✅ No critical bugs or blockers

### Reporting
- Use the [Bug Tracker](bug-tracker.md) to log all issues found.
- Add comments for usability or improvement suggestions.

### Sign-off
- Peer QA Group: CENTRALIZED ACADEMIC FORMS
- Date: 2025-07-08
- Sign-off: **MANDAWE,JOHN EARL**

---

## 6. Frontend Manual Testing Checklist (Filled)

### 1. Login & Authentication
- ✅ Can log in as student, approver, and admin
- ❌ Invalid credentials show error message
- ✅ Google OAuth login works (if enabled)

### 2. Form Submission
- ✅ Student can fill and submit a form
- ✅ Required fields are validated
- ✅ Success message appears after submission
- ✅ Submitted form appears in student dashboard

### 3. Approver/Admin Dashboard
- ✅ Approver sees pending forms
- ✅ Can approve/reject forms with comments
- ❌ Status updates are reflected in UI

### 4. Notifications
- ✅ New notifications appear after form actions
- ✅ Can mark notifications as read
- ❌ Notification count updates correctly

### 5. Error Handling
- ✅ Invalid actions show appropriate error messages
- ✅ Network/API errors are handled gracefully

### 6. UI/UX
- ❌ Layout is responsive on desktop and mobile
- ✅ Navigation between pages works as expected
- ❌ No broken links or missing assets

--- 