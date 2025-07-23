## Centralized Academic Forms

Students frequently need to submit academic forms such as requests for shifting, overload, or subejct petitions. These are often processed manually or through scattered system, resulting in delays, lost paperwork, and confusion over approval status.

The Centralized Academic Forms System (CAFS) aims to consolidate all academic form requests into a unified online platform. It will automate and standardize the multi-step approval process involving students, department heads, and other academic offices.

## Functional Requirements

| ID   | Feature               | Description                                                                 |
|------|-----------------------|-----------------------------------------------------------------------------|
| FR1  | Google OAuth Login    | Users log in via Google and are authenticated with role-based access.       |
| FR2  | Role Management       | Define and assign roles: Student, Department Head, Academic Staff.          |
| FR3  | Form Submission       | Students can fill and submit requests (shifting, overload, petition).       |
| FR4  | Approval Workflow     | Forms pass through multiple approval steps (configurable).                  |
| FR5  | Approver Dashboards   | Approvers see pending requests, take action, and leave remarks.             |
| FR6  | Form Status Tracking  | Each form displays real-time status and approval history.                   |
| FR7  | RESTful APIs          | APIs for all core operations (form CRUD, approvals, logs).                  |  
| FR8  | Audit Trail           | Record all actions per form: submission, approval, rejection.               |
| FR9  | Notifications         | Email or in-app notifications for form updates.                             |
| FR10 | Form Templates        | Admin can create/update form types and approval paths.                      |
| FR11 | Reports & Logs        | Export form status and audit trails for compliance.                         |

## Developers List

# MANDAWE
- FR3  | Form Submission       | Students can fill and submit requests (shifting, overload, petition).
- FR6  | Form Status Tracking  | Each form displays real-time status and approval history.
- FR5  | Approver Dashboards   | Approvers see pending requests, take action, and leave remarks.

## Testing & QA Deliverables

This project includes the following testing and QA documentation:

- **Test Plan:** [`/test-docs/test-plan.md`](test-docs/test-plan.md)
- **Test Cases:** [`/test-docs/test-cases.md`](test-docs/test-cases.md)
- **Bug Tracker / Issue Log:** [`/test-docs/bug-tracker.md`](test-docs/bug-tracker.md)
- **Test Summary Report:** [`/test-docs/test-summary-report.md`](test-docs/test-summary-report.md)

See the `test-docs/` directory for all QA documentation and templates.

