import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import '../styles/file-upload.css';

const FileUpload = ({ formId, onFileUploaded }) => {
  // Get formId from URL params if not provided as prop
  const urlParams = new URLSearchParams(window.location.search);
  const formIdFromUrl = urlParams.get('formId');
  const finalFormId = formId || formIdFromUrl;
  const [files, setFiles] = useState([]);
  const [uploadedFiles, setUploadedFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (finalFormId) {
      fetchFiles();
    }
  }, [finalFormId]);

  const fetchFiles = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`/api/files/form/${finalFormId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setUploadedFiles(response.data);
    } catch (err) {
      console.error('Error fetching files:', err);
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleFiles = (fileList) => {
    const newFiles = Array.from(fileList).map(file => ({
      file,
      id: Math.random().toString(36).substr(2, 9),
      name: file.name,
      size: file.size,
      type: file.type,
      status: 'pending'
    }));
    
    setFiles(prev => [...prev, ...newFiles]);
  };

  const handleFileSelect = (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFiles(e.target.files);
    }
  };

  const uploadFiles = async () => {
    if (files.length === 0) return;

    setUploading(true);
    setError(null);
    setSuccess(null);

    const uploadPromises = files.map(async (fileObj) => {
      try {
        const formData = new FormData();
        formData.append('file', fileObj.file);
        formData.append('description', fileObj.description || '');

        const token = localStorage.getItem('token');
        const response = await axios.post(`/api/files/upload/${finalFormId}`, formData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'multipart/form-data'
          }
        });

        return { ...fileObj, status: 'success', uploadedFile: response.data };
      } catch (err) {
        return { ...fileObj, status: 'error', error: err.response?.data?.message || 'Upload failed' };
      }
    });

    try {
      const results = await Promise.all(uploadPromises);
      
      const successfulUploads = results.filter(r => r.status === 'success');
      const failedUploads = results.filter(r => r.status === 'error');
      
      if (successfulUploads.length > 0) {
        setSuccess(`${successfulUploads.length} file(s) uploaded successfully`);
        if (onFileUploaded) {
          onFileUploaded();
        }
        fetchFiles(); // Refresh the file list
      }
      
      if (failedUploads.length > 0) {
        setError(`${failedUploads.length} file(s) failed to upload`);
      }
      
      // Clear uploaded files from the list
      setFiles([]);
    } catch (err) {
      setError('Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const removeFile = (fileId) => {
    setFiles(prev => prev.filter(f => f.id !== fileId));
  };

  const updateFileDescription = (fileId, description) => {
    setFiles(prev => prev.map(f => 
      f.id === fileId ? { ...f, description } : f
    ));
  };

  const deleteUploadedFile = async (fileId) => {
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`/api/files/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setSuccess('File deleted successfully');
      fetchFiles(); // Refresh the file list
    } catch (err) {
      setError('Failed to delete file');
    }
  };

  const downloadFile = async (fileId, fileName) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`/api/files/download/${fileId}`, {
        headers: { Authorization: `Bearer ${token}` },
        responseType: 'blob'
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Failed to download file');
    }
  };

  const viewFile = (fileId) => {
    const token = localStorage.getItem('token');
    window.open(`/api/files/view/${fileId}?token=${token}`, '_blank');
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getFileIcon = (fileType) => {
    if (fileType.startsWith('image/')) return '🖼️';
    if (fileType === 'application/pdf') return '📄';
    if (fileType.includes('word') || fileType.includes('document')) return '📝';
    if (fileType.includes('excel') || fileType.includes('spreadsheet')) return '📊';
    if (fileType.includes('powerpoint') || fileType.includes('presentation')) return '📽️';
    return '📎';
  };

  if (!finalFormId) {
    return (
      <div className="file-upload">
        <div className="error-message">
          <h2>❌ Error</h2>
          <p>No form ID provided. Please submit a form first.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="file-upload">
      <div className="upload-header">
        <h2>File Upload</h2>
        <p>Upload supporting documents for your form submission</p>
      </div>

      {/* Error and Success Messages */}
      {error && (
        <div className="message error">
          <span>❌ {error}</span>
          <button onClick={() => setError(null)}>×</button>
        </div>
      )}
      
      {success && (
        <div className="message success">
          <span>✅ {success}</span>
          <button onClick={() => setSuccess(null)}>×</button>
        </div>
      )}

      {/* Drag and Drop Area */}
      <div 
        className={`drag-drop-area ${dragActive ? 'drag-active' : ''}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
        onClick={() => fileInputRef.current?.click()}
      >
        <div className="drag-content">
          <div className="drag-icon">📁</div>
          <h3>Drag & Drop files here</h3>
          <p>or click to browse files</p>
          <p className="file-types">Supported: Images, PDFs, Documents, Spreadsheets</p>
        </div>
        <input
          ref={fileInputRef}
          type="file"
          multiple
          onChange={handleFileSelect}
          accept="image/*,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          style={{ display: 'none' }}
        />
      </div>

      {/* File List */}
      {files.length > 0 && (
        <div className="file-list">
          <h3>Files to Upload ({files.length})</h3>
          {files.map((fileObj) => (
            <div key={fileObj.id} className="file-item">
              <div className="file-info">
                <span className="file-icon">{getFileIcon(fileObj.type)}</span>
                <div className="file-details">
                  <span className="file-name">{fileObj.name}</span>
                  <span className="file-size">{formatFileSize(fileObj.size)}</span>
                </div>
              </div>
              <div className="file-actions">
                <input
                  type="text"
                  placeholder="Description (optional)"
                  value={fileObj.description || ''}
                  onChange={(e) => updateFileDescription(fileObj.id, e.target.value)}
                  className="file-description"
                />
                <button 
                  className="btn-remove"
                  onClick={() => removeFile(fileObj.id)}
                >
                  🗑️
                </button>
              </div>
            </div>
          ))}
          <button 
            className="btn-upload"
            onClick={uploadFiles}
            disabled={uploading}
          >
            {uploading ? '📤 Uploading...' : '📤 Upload Files'}
          </button>
        </div>
      )}

      {/* Uploaded Files */}
      {uploadedFiles.length > 0 && (
        <div className="uploaded-files">
          <h3>Uploaded Files ({uploadedFiles.length})</h3>
          <div className="files-grid">
            {uploadedFiles.map((file) => (
              <div key={file.id} className="uploaded-file-card">
                <div className="file-header">
                  <span className="file-icon">{getFileIcon(file.fileType)}</span>
                  <div className="file-info">
                    <span className="file-name">{file.originalFileName}</span>
                    <span className="file-size">{file.getFileSizeFormatted()}</span>
                    <span className="upload-date">
                      {new Date(file.uploadedAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>
                
                {file.description && (
                  <p className="file-description">{file.description}</p>
                )}
                
                <div className="file-actions">
                  <button 
                    className="btn-view"
                    onClick={() => viewFile(file.id)}
                    title="View file"
                  >
                    👁️ View
                  </button>
                  <button 
                    className="btn-download"
                    onClick={() => downloadFile(file.id, file.originalFileName)}
                    title="Download file"
                  >
                    ⬇️ Download
                  </button>
                  <button 
                    className="btn-delete"
                    onClick={() => deleteUploadedFile(file.id)}
                    title="Delete file"
                  >
                    🗑️ Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upload Progress */}
      {uploading && (
        <div className="upload-progress">
          <div className="progress-bar">
            <div className="progress-fill"></div>
          </div>
          <p>Uploading files...</p>
        </div>
      )}
    </div>
  );
};

export default FileUpload; 