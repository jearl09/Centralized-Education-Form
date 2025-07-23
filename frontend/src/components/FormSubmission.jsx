import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/form-submission.css';
import { AuthContext } from '../services/authContext.jsx';
import FileUpload from './FileUpload';

const FormSubmission = () => {
  const { token, user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [selectedForm, setSelectedForm] = useState('');
  const [formData, setFormData] = useState({});
  const [submittedForms, setSubmittedForms] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedFormDetails, setSelectedFormDetails] = useState(null);
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [formTemplates, setFormTemplates] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [commentsLoading, setCommentsLoading] = useState(false);

  // Student-specific form types
  const studentFormTypes = [
    'Shifting Request',
    'Overload Request',
    'Petition Form',
    'Leave of Absence',
    'Graduation Application',
    'Scholarship Application',
    'Course Substitution',
    'Grade Appeal'
  ];

  // Fetch forms from backend on component mount
  useEffect(() => {
    fetchForms();
  }, []);

  // Fetch form templates on mount
  useEffect(() => {
    fetch('/api/forms/templates', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
      .then(res => res.json())
      .then(setFormTemplates)
      .catch(() => setFormTemplates([]));
  }, [token]);

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchForms = async () => {
    try {
      const response = await fetch('/api/student/forms', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (response.ok) {
        const forms = await response.json();
        setSubmittedForms(forms);
      }
    } catch (error) {
      console.error('Error fetching forms:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchTemplates = async () => {
    try {
      const response = await fetch('/api/form-templates', {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setTemplates(data);
      }
    } catch (error) {
      setError('Failed to load form templates');
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'approved': return '#28a745';
      case 'pending': return '#ffc107';
      case 'rejected': return '#dc3545';
      case 'under_review': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  const getStatusIcon = (status) => {
    switch (status?.toLowerCase()) {
      case 'approved': return '✅';
      case 'pending': return '⏳';
      case 'rejected': return '❌';
      case 'under_review': return '🔍';
      default: return '📋';
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setSubmitError(null);
    
    const formDataToSend = {
      type: selectedForm,
      ...formData
    };

    try {
      const response = await fetch('/api/student/forms', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(formDataToSend)
      });
      
      if (!response.ok) {
        throw new Error('Submission failed');
      }
      
      const newForm = await response.json();
      setSubmittedForms([newForm, ...submittedForms]);
      
      // Create a notification for form submission
      try {
        await fetch('/api/student/notifications', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({
            title: 'Form Submitted Successfully',
            message: `Your ${selectedForm} has been submitted and is now under review.`,
            type: 'form_status',
            relatedFormId: newForm.id.toString()
          })
        });
      } catch (error) {
        console.error('Error creating notification:', error);
      }
      
      setShowSuccess(true);
      setShowForm(false);
      setSelectedForm('');
      setFormData({});
      setTimeout(() => setShowSuccess(false), 3000);
    } catch (err) {
      setSubmitError('Failed to submit form. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleViewDetails = async (formId) => {
    try {
      const response = await fetch(`/api/student/forms/${formId}/status`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        const formDetails = await response.json();
        setSelectedFormDetails(formDetails);
        setShowStatusModal(true);
      }
    } catch (error) {
      console.error('Error fetching form details:', error);
    }
  };

  const exportToCSV = () => {
    const headers = ['ID', 'Form Type', 'Status', 'Submitted Date', 'Current Step', 'Total Steps'];
    const csvContent = [
      headers.join(','),
      ...submittedForms.map(form => [
        form.id,
        form.type,
        form.status,
        form.submittedDate,
        form.currentStep,
        form.totalSteps
      ].join(','))
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'my_submitted_forms.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const getProgressPercentage = (current, total) => {
    return (current / total) * 100;
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  const handleTemplateSelect = (template) => {
    setSelectedTemplate(template);
    setFormData({});
    setCurrentStep(1);
    setError(null);
  };

  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const validateStep = () => {
    if (!selectedTemplate) return false;
    
    try {
      const requiredFields = JSON.parse(selectedTemplate.requiredFields);
      const formFields = JSON.parse(selectedTemplate.formFields);
      
      // Get fields for current step (simplified - assuming first step has all required fields)
      const stepFields = Object.keys(formFields).slice(0, Math.ceil(Object.keys(formFields).length / selectedTemplate.totalSteps));
      
      for (const field of stepFields) {
        if (requiredFields.includes(field) && (!formData[field] || formData[field].toString().trim() === '')) {
          setError(`Please fill in ${field}`);
          return false;
        }
      }
      return true;
    } catch (e) {
      return true; // Fallback validation
    }
  };

  const handleNextStep = () => {
    if (!validateStep()) return;
    
    if (currentStep < selectedTemplate.totalSteps) {
      setCurrentStep(prev => prev + 1);
      setError(null);
    } else {
      handleSubmit();
    }
  };

  const handlePreviousStep = () => {
    if (currentStep > 1) {
      setCurrentStep(prev => prev - 1);
      setError(null);
    }
  };

  const [submittedFormId, setSubmittedFormId] = useState(null);
  const [showFileUpload, setShowFileUpload] = useState(false);

  const handleSubmit = async () => {
    if (!validateStep()) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('/api/forms?templateId=' + selectedTemplate.id, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        const submittedForm = await response.json();
        setSubmittedFormId(submittedForm.id);
        setShowFileUpload(true);
        setShowSuccess(true);
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to submit form');
      }
    } catch (error) {
      setError('Failed to submit form');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUploadComplete = () => {
    setShowFileUpload(false);
    setTimeout(() => {
      navigate('/student/dashboard');
    }, 2000);
  };

  const renderField = (fieldName, fieldConfig) => {
    const value = formData[fieldName] || '';
    
    switch (fieldConfig.type) {
      case 'text':
        return (
          <input
            type="text"
            value={value}
            onChange={(e) => handleInputChange(fieldName, e.target.value)}
            placeholder={fieldConfig.label}
            className="form-input"
          />
        );
      
      case 'textarea':
        return (
          <textarea
            value={value}
            onChange={(e) => handleInputChange(fieldName, e.target.value)}
            placeholder={fieldConfig.label}
            className="form-textarea"
            rows={4}
          />
        );
      
      case 'number':
        return (
          <input
            type="number"
            value={value}
            onChange={(e) => handleInputChange(fieldName, e.target.value)}
            placeholder={fieldConfig.label}
            step={fieldConfig.step || 1}
            className="form-input"
          />
        );
      
      case 'select':
        return (
          <select
            value={value}
            onChange={(e) => handleInputChange(fieldName, e.target.value)}
            className="form-select"
          >
            <option value="">Select {fieldConfig.label}</option>
            {fieldConfig.options?.map(option => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
        );
      
      case 'date':
        return (
          <input
            type="date"
            value={value}
            onChange={(e) => handleInputChange(fieldName, e.target.value)}
            className="form-input"
          />
        );
      
      case 'file':
        return (
          <input
            type="file"
            onChange={(e) => handleInputChange(fieldName, e.target.files[0]?.name || '')}
            className="form-file"
            accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
          />
        );
      
      default:
        return (
          <input
            type="text"
            value={value}
            onChange={(e) => handleInputChange(fieldName, e.target.value)}
            placeholder={fieldConfig.label}
            className="form-input"
          />
        );
    }
  };

  const renderFormStep = () => {
    if (!selectedTemplate) return null;
    
    try {
      const formFields = JSON.parse(selectedTemplate.formFields);
      const fieldNames = Object.keys(formFields);
      const fieldsPerStep = Math.ceil(fieldNames.length / selectedTemplate.totalSteps);
      const startIndex = (currentStep - 1) * fieldsPerStep;
      const endIndex = Math.min(startIndex + fieldsPerStep, fieldNames.length);
      const stepFields = fieldNames.slice(startIndex, endIndex);
      
      return (
        <div className="form-step">
          <h3>Step {currentStep} of {selectedTemplate.totalSteps}</h3>
          {stepFields.map(fieldName => {
            const fieldConfig = formFields[fieldName];
            const requiredFields = JSON.parse(selectedTemplate.requiredFields);
            const isRequired = requiredFields.includes(fieldName);
            
            return (
              <div key={fieldName} className="form-field">
                <label className="form-label">
                  {fieldConfig.label}
                  {isRequired && <span className="required">*</span>}
                </label>
                {renderField(fieldName, fieldConfig)}
                {fieldConfig.description && (
                  <small className="field-description">{fieldConfig.description}</small>
                )}
              </div>
            );
          })}
        </div>
      );
    } catch (e) {
      return <div>Error loading form fields</div>;
    }
  };

  const fetchComments = async (formId) => {
    setCommentsLoading(true);
    try {
      const response = await fetch(`/api/forms/${formId}/comments`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (response.ok) {
        setComments(await response.json());
      }
    } catch {}
    setCommentsLoading(false);
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    try {
      const response = await fetch(`/api/forms/${selectedFormDetails.id}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({ userId: user?.id, comment: newComment })
      });
      if (response.ok) {
        setNewComment('');
        fetchComments(selectedFormDetails.id);
      }
    } catch {}
  };

  useEffect(() => {
    if (showStatusModal && selectedFormDetails) {
      fetchComments(selectedFormDetails.id);
    }
    // eslint-disable-next-line
  }, [showStatusModal, selectedFormDetails]);

  if (isLoading) {
    return (
      <div className="form-submission-container">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading your forms...</p>
        </div>
      </div>
    );
  }

  if (showSuccess && showFileUpload && submittedFormId) {
    return (
      <div className="form-submission-container">
        <div className="success-message">
          <h2>✅ Form Submitted Successfully!</h2>
          <p>Your {selectedTemplate?.name} has been submitted and is pending approval.</p>
          <p>You can now upload supporting documents (optional):</p>
        </div>
        <FileUpload formId={submittedFormId} onFileUploaded={handleFileUploadComplete} />
        <div className="file-upload-actions">
          <button 
            className="btn btn-secondary"
            onClick={handleFileUploadComplete}
          >
            Skip File Upload
          </button>
        </div>
      </div>
    );
  }

  if (showSuccess) {
    return (
      <div className="form-submission-container">
        <div className="success-message">
          <h2>✅ Form Submitted Successfully!</h2>
          <p>Your {selectedTemplate?.name} has been submitted and is pending approval.</p>
          <p>Redirecting to dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="form-submission-container">
      <div className="form-submission-header">
        <h2>My Forms</h2>
        <div className="header-actions">
          <button 
            className="btn btn-primary"
            onClick={() => setShowForm(true)}
          >
            📝 Submit New Form
          </button>
          <button 
            className="btn btn-secondary"
            onClick={exportToCSV}
            disabled={submittedForms.length === 0}
          >
            📊 Export My Forms
          </button>
        </div>
      </div>

      {/* Success Message */}
      {showSuccess && (
        <div className="success-message">
          <span>✔️</span>
          <p>Form submitted successfully! You will receive notifications on status updates.</p>
        </div>
      )}

      {/* Submit Error Message */}
      {submitError && <div className="error-message">{submitError}</div>}

      {/* Form Submission Modal */}
      {showForm && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Submit New Form</h3>
              <button 
                className="close-btn"
                onClick={() => setShowForm(false)}
              >
                ✕
              </button>
            </div>
            <div className="available-forms-section">
              <h4>Available Forms</h4>
              <div className="form-templates-list">
                {templates.map(template => (
                  <div key={template.type} className="form-template-card">
                    <div className="form-template-title">{template.name}</div>
                    <div className="form-template-desc">{template.description}</div>
                    <ul className="form-template-reqs">
                      {template.requirements.map((req, i) => <li key={i}>{req}</li>)}
                    </ul>
                  </div>
                ))}
              </div>
            </div>
            <form onSubmit={handleFormSubmit}>
              <div className="form-group">
                <label>Form Type *</label>
                <select 
                  value={selectedForm}
                  onChange={(e) => setSelectedForm(e.target.value)}
                  required
                >
                  <option value="">Select a form type</option>
                  {studentFormTypes.map(type => (
                    <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label>Additional Notes</label>
                <textarea 
                  placeholder="Please provide any additional information or reasons for your request..."
                  rows="4"
                  value={formData.notes || ''}
                  onChange={(e) => setFormData({...formData, notes: e.target.value})}
                />
              </div>
              
              <div className="form-group">
                <label>Supporting Documents (Optional)</label>
                <input 
                  type="file" 
                  multiple
                  accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                />
                <small>Accepted formats: PDF, DOC, DOCX, JPG, PNG (Max 5MB each)</small>
              </div>
              
              <div className="form-actions">
                <button 
                  type="button" 
                  className="btn btn-secondary"
                  onClick={() => setShowForm(false)}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="btn btn-primary"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Submitting...' : 'Submit Form'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Form Status Modal */}
      {showStatusModal && selectedFormDetails && (
        <div className="modal-overlay">
          <div className="modal-content status-modal">
            <div className="modal-header">
              <h3>Form Details - {selectedFormDetails.type}</h3>
              <button 
                className="close-btn"
                onClick={() => {
                  setShowStatusModal(false);
                  setSelectedFormDetails(null);
                }}
              >
                ✕
              </button>
            </div>
            
            <div className="status-content">
              <div className="status-header">
                <span 
                  className="status-badge large"
                  style={{ backgroundColor: getStatusColor(selectedFormDetails.status) }}
                >
                  {getStatusIcon(selectedFormDetails.status)} {selectedFormDetails.status}
                </span>
              </div>
              
              <div className="form-progress-detailed">
                <div className="progress-bar">
                  <div 
                    className="progress-fill"
                    style={{ width: `${getProgressPercentage(selectedFormDetails.currentStep, selectedFormDetails.totalSteps)}%` }}
                  ></div>
                </div>
                <span className="progress-text">
                  Step {selectedFormDetails.currentStep} of {selectedFormDetails.totalSteps}
                </span>
              </div>
              
              <div className="form-details-grid">
                <div className="detail-item">
                  <label>Form ID:</label>
                  <span>{selectedFormDetails.id}</span>
                </div>
                <div className="detail-item">
                  <label>Form Type:</label>
                  <span>{selectedFormDetails.type}</span>
                </div>
                <div className="detail-item">
                  <label>Submitted Date:</label>
                  <span>{formatDate(selectedFormDetails.submittedDate)}</span>
                </div>
                <div className="detail-item">
                  <label>Current Status:</label>
                  <span>{selectedFormDetails.status}</span>
                </div>
                <div className="detail-item">
                  <label>Current Step:</label>
                  <span>{selectedFormDetails.currentStep} of {selectedFormDetails.totalSteps}</span>
                </div>
              </div>
              
              <div className="approval-history">
                <h4>Approval History</h4>
                <div className="history-timeline">
                  <div className="timeline-item completed">
                    <div className="timeline-marker">✓</div>
                    <div className="timeline-content">
                      <strong>Form Submitted</strong>
                      <p>{formatDate(selectedFormDetails.submittedDate)}</p>
                    </div>
                  </div>
                  {selectedFormDetails.currentStep > 1 && (
                    <div className="timeline-item completed">
                      <div className="timeline-marker">✓</div>
                      <div className="timeline-content">
                        <strong>Under Review</strong>
                        <p>Currently being reviewed by approvers</p>
                      </div>
                    </div>
                  )}
                  {selectedFormDetails.status === 'approved' && (
                    <div className="timeline-item completed">
                      <div className="timeline-marker">✓</div>
                      <div className="timeline-content">
                        <strong>Approved</strong>
                        <p>Your request has been approved</p>
                      </div>
                    </div>
                  )}
                  {selectedFormDetails.status === 'rejected' && (
                    <div className="timeline-item rejected">
                      <div className="timeline-marker">✗</div>
                      <div className="timeline-content">
                        <strong>Rejected</strong>
                        <p>Your request was not approved</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
            
            <div className="comments-section">
              <h4>Comments & Notes</h4>
              {commentsLoading ? (
                <div>Loading comments...</div>
              ) : comments.length === 0 ? (
                <div className="no-comments">No comments yet.</div>
              ) : (
                <div className="comments-list">
                  {comments.map(c => (
                    <div key={c.id} className="comment-item">
                      <div className="comment-meta">
                        <span className="comment-author">{c.user?.username || c.user?.email || 'User'}</span>
                        <span className="comment-date">{formatDate(c.createdAt)}</span>
                      </div>
                      <div className="comment-text">{c.comment}</div>
                    </div>
                  ))}
                </div>
              )}
              <div className="add-comment-form">
                <textarea
                  value={newComment}
                  onChange={e => setNewComment(e.target.value)}
                  placeholder="Add a comment or note..."
                  rows={2}
                  className="comment-input"
                />
                <button className="btn btn-primary" onClick={handleAddComment} disabled={!newComment.trim()} style={{ marginTop: 8 }}>
                  Add Comment
                </button>
              </div>
            </div>
            
            <div className="modal-actions">
              <button 
                className="btn btn-primary"
                onClick={() => {
                  setShowStatusModal(false);
                  setSelectedFormDetails(null);
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Submitted Forms List */}
      <div className="submitted-forms">
        <h3>My Submitted Forms</h3>
        
        {submittedForms.length === 0 ? (
          <div className="no-forms">
            <p>You haven't submitted any forms yet.</p>
            <button 
              className="btn btn-primary"
              onClick={() => setShowForm(true)}
            >
              Submit Your First Form
            </button>
          </div>
        ) : (
          <div className="forms-grid">
            {submittedForms.map(form => (
              <div key={form.id} className="form-card">
                <div className="form-card-header">
                  <h4>{form.type}</h4>
                  <span 
                    className="status-badge"
                    style={{ backgroundColor: getStatusColor(form.status) }}
                  >
                    {getStatusIcon(form.status)} {form.status}
                  </span>
                </div>
                
                <div className="form-progress">
                  <div className="progress-bar">
                    <div 
                      className="progress-fill"
                      style={{ width: `${getProgressPercentage(form.currentStep, form.totalSteps)}%` }}
                    ></div>
                  </div>
                  <span className="progress-text">
                    Step {form.currentStep} of {form.totalSteps}
                  </span>
                </div>
                
                <div className="form-details">
                  <p><strong>Submitted:</strong> {formatDate(form.submittedDate)}</p>
                  <p><strong>Form ID:</strong> #{form.id}</p>
                </div>
                
                <div className="form-actions">
                  <button 
                    className="btn btn-sm btn-outline"
                    onClick={() => handleViewDetails(form.id)}
                  >
                    View Details
                  </button>
                  {form.status === 'pending' && (
                    <button className="btn btn-sm btn-warning">Cancel Request</button>
                  )}
                  {form.status === 'rejected' && (
                    <button className="btn btn-sm btn-primary">Resubmit</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {!selectedTemplate && (
        <div className="template-selection">
          <h2>Select Form Type</h2>
          <div className="template-grid">
            {templates.map(template => (
              <div
                key={template.id}
                className="template-card"
                onClick={() => handleTemplateSelect(template)}
              >
                <h3>{template.name}</h3>
                <p>{template.description}</p>
                <div className="template-meta">
                  <span>Steps: {template.totalSteps}</span>
                  <span>Approval Required: {template.requiresApproval ? 'Yes' : 'No'}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {selectedTemplate && (
        <div className="form-container">
          <div className="form-header">
            <h2>{selectedTemplate.name}</h2>
            <p>{selectedTemplate.description}</p>
            <div className="progress-bar">
              <div 
                className="progress-fill" 
                style={{ width: `${(currentStep / selectedTemplate.totalSteps) * 100}%` }}
              ></div>
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          {renderFormStep()}

          <div className="form-actions">
            {currentStep > 1 && (
              <button 
                type="button" 
                onClick={handlePreviousStep}
                className="btn btn-secondary"
              >
                Previous
              </button>
            )}
            
            <button
              type="button"
              onClick={handleNextStep}
              disabled={loading}
              className="btn btn-primary"
            >
              {loading ? 'Submitting...' : 
               currentStep === selectedTemplate.totalSteps ? 'Submit Form' : 'Next'}
            </button>
            
            <button
              type="button"
              onClick={() => setSelectedTemplate(null)}
              className="btn btn-outline"
            >
              Back to Templates
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FormSubmission; 