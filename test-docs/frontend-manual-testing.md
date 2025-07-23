# Frontend Manual Testing Checklist

## 1. Login & Authentication
- ✅ Can log in as student, approver, and admin
- ❌ Invalid credentials show error message
- ✅ Google OAuth login works (if enabled)

## 2. Form Submission
- ✅ Student can fill and submit a form
- ✅ Required fields are validated
- ✅ Success message appears after submission
- ✅ Submitted form appears in student dashboard

## 3. Approver/Admin Dashboard
- ✅ Approver sees pending forms
- ✅ Can approve/reject forms with comments
- ❌ Status updates are reflected in UI

## 4. Notifications
- ✅ New notifications appear after form actions
- ✅ Can mark notifications as read
- ❌ Notification count updates correctly

## 5. Error Handling
- ✅ Invalid actions show appropriate error messages
- ✅ Network/API errors are handled gracefully

## 6. UI/UX
- ❌ Layout is responsive on desktop and mobile
- ✅ Navigation between pages works as expected
- ❌ No broken links or missing assets

---

**Instructions:**
- Test each item above for all user roles.
- Record any bugs or issues in the [Bug Tracker](bug-tracker.md).
- Suggest improvements or usability issues in the test summary report. 