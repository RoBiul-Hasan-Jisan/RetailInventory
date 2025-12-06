// Utility Functions for Retail Inventory System

// Format date to readable string
function formatDate(date, includeTime = false) {
    const d = new Date(date);
    
    const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    };
    
    if (includeTime) {
        options.hour = '2-digit';
        options.minute = '2-digit';
    }
    
    return d.toLocaleDateString('en-US', options);
}

// Format date for input fields (YYYY-MM-DD)
function formatDateForInput(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Format number with commas
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

// Calculate percentage
function calculatePercentage(part, total) {
    if (total === 0) return 0;
    return Math.round((part / total) * 100);
}

// Generate random ID
function generateRandomId(prefix = '') {
    const timestamp = Date.now().toString(36);
    const random = Math.random().toString(36).substring(2, 8);
    return `${prefix}${timestamp}${random}`.toUpperCase();
}

// Debounce function for search inputs
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function for scroll/resize events
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Validate email format
function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Validate phone number (simple validation)
function isValidPhone(phone) {
    const re = /^[\+]?[1-9][\d]{0,15}$/;
    return re.test(phone.replace(/[\s\-\(\)]/g, ''));
}

// Get file extension
function getFileExtension(filename) {
    return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2);
}

// Check if file is an image
function isImageFile(filename) {
    const extensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];
    const ext = getFileExtension(filename).toLowerCase();
    return extensions.includes(ext);
}

// Read file as text
function readFileAsText(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.onerror = (e) => reject(e);
        reader.readAsText(file);
    });
}

// Read file as data URL (for images)
function readFileAsDataURL(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = (e) => resolve(e.target.result);
        reader.onerror = (e) => reject(e);
        reader.readAsDataURL(file);
    });
}

// Parse CSV string to array of objects
function parseCSV(csvString, delimiter = ',') {
    const lines = csvString.split('\n').filter(line => line.trim() !== '');
    if (lines.length === 0) return [];
    
    const headers = lines[0].split(delimiter).map(h => h.trim().replace(/^"|"$/g, ''));
    const result = [];
    
    for (let i = 1; i < lines.length; i++) {
        const values = lines[i].split(delimiter).map(v => v.trim().replace(/^"|"$/g, ''));
        const obj = {};
        
        headers.forEach((header, index) => {
            obj[header] = values[index] || '';
        });
        
        result.push(obj);
    }
    
    return result;
}

// Convert array of objects to CSV string
function convertToCSV(data, headers = null) {
    if (data.length === 0) return '';
    
    const actualHeaders = headers || Object.keys(data[0]);
    const csvRows = [];
    
    // Add header row
    csvRows.push(actualHeaders.join(','));
    
    // Add data rows
    data.forEach(item => {
        const row = actualHeaders.map(header => {
            const value = item[header];
            // Escape commas and quotes
            const stringValue = String(value);
            if (stringValue.includes(',') || stringValue.includes('"') || stringValue.includes('\n')) {
                return `"${stringValue.replace(/"/g, '""')}"`;
            }
            return stringValue;
        });
        csvRows.push(row.join(','));
    });
    
    return csvRows.join('\n');
}

// Download file
function downloadFile(content, filename, type = 'text/plain') {
    const blob = new Blob([content], { type });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Copy text to clipboard
function copyToClipboard(text) {
    return new Promise((resolve, reject) => {
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(text).then(resolve).catch(reject);
        } else {
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            textArea.style.top = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            
            try {
                document.execCommand('copy');
                resolve();
            } catch (err) {
                reject(err);
            }
            
            document.body.removeChild(textArea);
        }
    });
}

// Get query parameters from URL
function getQueryParams() {
    const params = {};
    const queryString = window.location.search.substring(1);
    const pairs = queryString.split('&');
    
    pairs.forEach(pair => {
        const [key, value] = pair.split('=');
        if (key) {
            params[decodeURIComponent(key)] = decodeURIComponent(value || '');
        }
    });
    
    return params;
}

// Set query parameter in URL without reloading
function setQueryParam(key, value) {
    const params = new URLSearchParams(window.location.search);
    params.set(key, value);
    const newUrl = `${window.location.pathname}?${params.toString()}`;
    window.history.pushState({}, '', newUrl);
}

// Remove query parameter from URL
function removeQueryParam(key) {
    const params = new URLSearchParams(window.location.search);
    params.delete(key);
    const newUrl = `${window.location.pathname}${params.toString() ? '?' + params.toString() : ''}`;
    window.history.pushState({}, '', newUrl);
}

// Capitalize first letter of each word
function capitalizeWords(str) {
    return str.replace(/\b\w/g, char => char.toUpperCase());
}

// Truncate text with ellipsis
function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// Generate random color
function getRandomColor() {
    const colors = [
        '#4361ee', '#7209b7', '#3a0ca3', '#4cc9f0', '#f72585',
        '#4895ef', '#560bad', '#b5179e', '#f15bb5', '#00bbf9',
        '#00f5d4', '#fee440', '#ff9e00', '#ff0054', '#9b5de5'
    ];
    return colors[Math.floor(Math.random() * colors.length)];
}

// Format time duration
function formatDuration(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    const parts = [];
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);
    if (secs > 0 || parts.length === 0) parts.push(`${secs}s`);
    
    return parts.join(' ');
}

// Get month name from number (1-12)
function getMonthName(monthNumber) {
    const months = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[monthNumber - 1] || '';
}

// Get day name from number (0-6)
function getDayName(dayNumber) {
    const days = [
        'Sunday', 'Monday', 'Tuesday', 'Wednesday',
        'Thursday', 'Friday', 'Saturday'
    ];
    return days[dayNumber] || '';
}

// Generate array of dates for a range
function getDateRange(startDate, endDate) {
    const dates = [];
    const current = new Date(startDate);
    const end = new Date(endDate);
    
    while (current <= end) {
        dates.push(new Date(current));
        current.setDate(current.getDate() + 1);
    }
    
    return dates;
}

// Calculate age from birth date
function calculateAge(birthDate) {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
        age--;
    }
    
    return age;
}

// Sort array of objects by property
function sortByProperty(array, property, ascending = true) {
    return [...array].sort((a, b) => {
        let aValue = a[property];
        let bValue = b[property];
        
        // Handle undefined values
        if (aValue === undefined) aValue = '';
        if (bValue === undefined) bValue = '';
        
        // Handle numbers
        if (typeof aValue === 'number' && typeof bValue === 'number') {
            return ascending ? aValue - bValue : bValue - aValue;
        }
        
        // Handle strings
        aValue = String(aValue).toLowerCase();
        bValue = String(bValue).toLowerCase();
        
        if (aValue < bValue) return ascending ? -1 : 1;
        if (aValue > bValue) return ascending ? 1 : -1;
        return 0;
    });
}

// Filter array by multiple criteria
function filterByCriteria(array, criteria) {
    return array.filter(item => {
        return Object.keys(criteria).every(key => {
            const filterValue = criteria[key];
            const itemValue = item[key];
            
            if (filterValue === undefined || filterValue === null || filterValue === '') {
                return true;
            }
            
            if (typeof filterValue === 'string') {
                return String(itemValue).toLowerCase().includes(filterValue.toLowerCase());
            }
            
            if (typeof filterValue === 'number') {
                return itemValue === filterValue;
            }
            
            if (Array.isArray(filterValue)) {
                return filterValue.includes(itemValue);
            }
            
            return itemValue === filterValue;
        });
    });
}

// Group array by property
function groupBy(array, property) {
    return array.reduce((groups, item) => {
        const key = item[property];
        if (!groups[key]) {
            groups[key] = [];
        }
        groups[key].push(item);
        return groups;
    }, {});
}

// Calculate statistics for numeric array
function calculateStats(numbers) {
    if (numbers.length === 0) {
        return {
            min: 0,
            max: 0,
            sum: 0,
            avg: 0,
            count: 0
        };
    }
    
    const sorted = [...numbers].sort((a, b) => a - b);
    const sum = sorted.reduce((a, b) => a + b, 0);
    
    return {
        min: sorted[0],
        max: sorted[sorted.length - 1],
        sum: sum,
        avg: sum / sorted.length,
        count: sorted.length
    };
}

// Create pagination data
function paginate(array, page = 1, perPage = 10) {
    const start = (page - 1) * perPage;
    const end = start + perPage;
    const totalPages = Math.ceil(array.length / perPage);
    
    return {
        data: array.slice(start, end),
        page: page,
        perPage: perPage,
        total: array.length,
        totalPages: totalPages,
        hasNext: page < totalPages,
        hasPrev: page > 1
    };
}

// Deep clone object (simple implementation)
function deepClone(obj) {
    return JSON.parse(JSON.stringify(obj));
}

// Merge objects deeply
function deepMerge(target, source) {
    const output = Object.assign({}, target);
    
    if (isObject(target) && isObject(source)) {
        Object.keys(source).forEach(key => {
            if (isObject(source[key])) {
                if (!(key in target)) {
                    Object.assign(output, { [key]: source[key] });
                } else {
                    output[key] = deepMerge(target[key], source[key]);
                }
            } else {
                Object.assign(output, { [key]: source[key] });
            }
        });
    }
    
    return output;
}

function isObject(item) {
    return item && typeof item === 'object' && !Array.isArray(item);
}

// Generate breadcrumb trail
function generateBreadcrumbs(path) {
    const parts = path.split('/').filter(part => part !== '');
    const breadcrumbs = [{ name: 'Home', path: '/' }];
    
    let currentPath = '';
    parts.forEach((part, index) => {
        currentPath += `/${part}`;
        breadcrumbs.push({
            name: capitalizeWords(part.replace(/-/g, ' ')),
            path: currentPath
        });
    });
    
    return breadcrumbs;
}

// Format bytes to human readable size
function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

// Create data URL for QR code
function createQRCodeDataURL(text, size = 128) {
    // This is a simplified version - in reality you'd use a QR code library
    return `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}`;
}

// Export all utility functions
window.Utils = {
    formatDate,
    formatDateForInput,
    formatNumber,
    calculatePercentage,
    generateRandomId,
    debounce,
    throttle,
    isValidEmail,
    isValidPhone,
    getFileExtension,
    isImageFile,
    readFileAsText,
    readFileAsDataURL,
    parseCSV,
    convertToCSV,
    downloadFile,
    copyToClipboard,
    getQueryParams,
    setQueryParam,
    removeQueryParam,
    capitalizeWords,
    truncateText,
    getRandomColor,
    formatDuration,
    getMonthName,
    getDayName,
    getDateRange,
    calculateAge,
    sortByProperty,
    filterByCriteria,
    groupBy,
    calculateStats,
    paginate,
    deepClone,
    deepMerge,
    generateBreadcrumbs,
    formatBytes,
    createQRCodeDataURL
};