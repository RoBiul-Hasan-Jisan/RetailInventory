// Products Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeProductsPage();
});

function initializeProductsPage() {
    // Load products
    loadProducts();
    
    // Initialize modals
    initializeProductModals();
    
    // Initialize search and filters
    initializeProductFilters();
    
    // Initialize bulk actions
    initializeBulkActions();
}

function loadProducts() {
    const products = App.getProducts();
    const tbody = document.querySelector('#productsTable tbody');
    
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (products.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center">
                    <div class="empty-state">
                        <i class="fas fa-box-open"></i>
                        <h4>No Products Found</h4>
                        <p>Add your first product to get started.</p>
                        <button class="btn btn-primary" id="addFirstProductBtn">
                            <i class="fas fa-plus"></i> Add First Product
                        </button>
                    </div>
                </td>
            </tr>
        `;
        
        document.getElementById('addFirstProductBtn')?.addEventListener('click', function() {
            App.openModal('addProductModal');
        });
        
        updateProductCount(0);
        return;
    }
    
    products.forEach(product => {
        const row = createProductRow(product);
        tbody.appendChild(row);
    });
    
    updateProductCount(products.length);
}

function createProductRow(product) {
    const row = document.createElement('tr');
    
    // Determine stock status class
    let statusClass = '';
    let statusText = '';
    
    if (product.stock === 0) {
        statusClass = 'stock-out';
        statusText = 'Out of Stock';
    } else if (product.stock <= product.threshold) {
        statusClass = 'stock-low';
        statusText = 'Low Stock';
    } else {
        statusClass = 'stock-in';
        statusText = 'In Stock';
    }
    
    row.innerHTML = `
        <td>
            <input type="checkbox" class="product-checkbox" value="${product.id}">
        </td>
        <td>
            <div class="d-flex align-items-center gap-2">
                <div class="product-avatar">
                    <i class="fas fa-box"></i>
                </div>
                <div>
                    <strong>${product.name}</strong>
                    <p class="mb-0 text-muted small">${product.brand || 'No Brand'}</p>
                </div>
            </div>
        </td>
        <td>${product.sku}</td>
        <td>${product.category}</td>
        <td>${App.formatCurrency(product.price)}</td>
        <td>
            <span class="${product.stock <= product.threshold ? 'text-warning' : ''}">
                ${product.stock}
            </span>
        </td>
        <td>
            <span class="stock-status ${statusClass}">${statusText}</span>
        </td>
        <td>
            <div class="action-buttons">
                <button class="action-btn edit" title="Edit" data-id="${product.id}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn delete" title="Delete" data-id="${product.id}">
                    <i class="fas fa-trash"></i>
                </button>
                <button class="action-btn view" title="View Details" data-id="${product.id}">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
        </td>
    `;
    
    // Add event listeners to action buttons
    const editBtn = row.querySelector('.action-btn.edit');
    const deleteBtn = row.querySelector('.action-btn.delete');
    const viewBtn = row.querySelector('.action-btn.view');
    
    if (editBtn) {
        editBtn.addEventListener('click', function() {
            editProduct(product.id);
        });
    }
    
    if (deleteBtn) {
        deleteBtn.addEventListener('click', function() {
            deleteProduct(product.id);
        });
    }
    
    if (viewBtn) {
        viewBtn.addEventListener('click', function() {
            viewProduct(product.id);
        });
    }
    
    return row;
}

function updateProductCount(count) {
    const countElement = document.getElementById('productCount');
    if (countElement) {
        countElement.textContent = count;
    }
}

function initializeProductModals() {
    // Add Product Modal
    const addProductBtn = document.getElementById('addProductBtn');
    const addProductForm = document.getElementById('addProductForm');
    
    if (addProductBtn) {
        addProductBtn.addEventListener('click', function() {
            App.openModal('addProductModal');
        });
    }
    
    if (addProductForm) {
        addProductForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addNewProduct();
        });
    }
    
    // Import Products Modal
    const importProductsBtn = document.getElementById('importProductsBtn');
    const csvFileInput = document.getElementById('csvFile');
    const importBtn = document.getElementById('importBtn');
    
    if (importProductsBtn) {
        importProductsBtn.addEventListener('click', function() {
            App.openModal('importProductsModal');
        });
    }
    
    if (csvFileInput) {
        csvFileInput.addEventListener('change', function() {
            const fileName = this.files[0]?.name || 'No file chosen';
            document.getElementById('fileName').textContent = fileName;
            
            if (this.files.length > 0) {
                importBtn.disabled = false;
            } else {
                importBtn.disabled = true;
            }
        });
    }
    
    if (importBtn) {
        importBtn.addEventListener('click', function() {
            importProducts();
        });
    }
}

function addNewProduct() {
    const form = document.getElementById('addProductForm');
    
    // Get form values
    const product = {
        id: App.generateId(),
        name: document.getElementById('productName').value,
        sku: document.getElementById('productSku').value,
        category: document.getElementById('productCategory').value,
        brand: document.getElementById('productBrand').value,
        price: parseFloat(document.getElementById('productPrice').value),
        cost: parseFloat(document.getElementById('productCost').value),
        stock: parseInt(document.getElementById('productStock').value),
        threshold: parseInt(document.getElementById('productThreshold').value) || 10,
        description: document.getElementById('productDescription').value,
        status: 'In Stock'
    };
    
    // Validate SKU uniqueness
    const products = App.getProducts();
    const skuExists = products.some(p => p.sku === product.sku);
    
    if (skuExists) {
        alert('A product with this SKU already exists. Please use a different SKU.');
        return;
    }
    
    // Add to products array
    products.push(product);
    App.saveProducts(products);
    
    // Close modal and reset form
    App.closeModal(document.getElementById('addProductModal'));
    form.reset();
    
    // Reload products table
    loadProducts();
    
    // Show success message
    showNotification('Product added successfully!', 'success');
}

function editProduct(productId) {
    const products = App.getProducts();
    const product = products.find(p => p.id == productId);
    
    if (!product) {
        showNotification('Product not found!', 'error');
        return;
    }
    
    // For now, just show a message
    // In a real application, you would open an edit modal
    showNotification('Edit functionality would open here. For now, you can delete and recreate.', 'info');
}

function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product? This action cannot be undone.')) {
        return;
    }
    
    const products = App.getProducts();
    const updatedProducts = products.filter(p => p.id != productId);
    
    App.saveProducts(updatedProducts);
    loadProducts();
    
    showNotification('Product deleted successfully!', 'success');
}

function viewProduct(productId) {
    const products = App.getProducts();
    const product = products.find(p => p.id == productId);
    
    if (!product) {
        showNotification('Product not found!', 'error');
        return;
    }
    
    // Create and show product details modal
    const modalHtml = `
        <div class="modal active" id="productDetailsModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>Product Details</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="product-details">
                        <div class="product-header">
                            <div class="product-avatar-large">
                                <i class="fas fa-box"></i>
                            </div>
                            <div class="product-info">
                                <h4>${product.name}</h4>
                                <p class="text-muted">${product.brand || 'No Brand'}</p>
                                <div class="product-tags">
                                    <span class="product-tag">${product.category}</span>
                                    <span class="product-tag ${product.stock === 0 ? 'stock-out' : product.stock <= product.threshold ? 'stock-low' : 'stock-in'}">
                                        ${product.stock === 0 ? 'Out of Stock' : product.stock <= product.threshold ? 'Low Stock' : 'In Stock'}
                                    </span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="product-specs">
                            <div class="spec-row">
                                <span class="spec-label">SKU:</span>
                                <span class="spec-value">${product.sku}</span>
                            </div>
                            <div class="spec-row">
                                <span class="spec-label">Price:</span>
                                <span class="spec-value">${App.formatCurrency(product.price)}</span>
                            </div>
                            <div class="spec-row">
                                <span class="spec-label">Cost:</span>
                                <span class="spec-value">${App.formatCurrency(product.cost)}</span>
                            </div>
                            <div class="spec-row">
                                <span class="spec-label">Stock Level:</span>
                                <span class="spec-value">${product.stock} units</span>
                            </div>
                            <div class="spec-row">
                                <span class="spec-label">Low Stock Threshold:</span>
                                <span class="spec-value">${product.threshold} units</span>
                            </div>
                            <div class="spec-row">
                                <span class="spec-label">Profit Margin:</span>
                                <span class="spec-value">${((product.price - product.cost) / product.price * 100).toFixed(1)}%</span>
                            </div>
                        </div>
                        
                        <div class="product-description">
                            <h5>Description</h5>
                            <p>${product.description || 'No description provided.'}</p>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary modal-close">Close</button>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('productDetailsModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    // Add new modal to body
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Initialize modal close functionality
    const newModal = document.getElementById('productDetailsModal');
    const closeBtn = newModal.querySelector('.modal-close');
    
    closeBtn.addEventListener('click', function() {
        newModal.remove();
        document.body.style.overflow = 'auto';
    });
    
    newModal.addEventListener('click', function(e) {
        if (e.target === this) {
            this.remove();
            document.body.style.overflow = 'auto';
        }
    });
}

function importProducts() {
    const csvFile = document.getElementById('csvFile').files[0];
    
    if (!csvFile) {
        showNotification('Please select a CSV file to import.', 'error');
        return;
    }
    
    const reader = new FileReader();
    
    reader.onload = function(e) {
        try {
            const csvContent = e.target.result;
            const lines = csvContent.split('\n');
            const headers = lines[0].split(',').map(h => h.trim());
            
            const products = App.getProducts();
            let importedCount = 0;
            let skippedCount = 0;
            
            // Process each line (skip header)
            for (let i = 1; i < lines.length; i++) {
                if (lines[i].trim() === '') continue;
                
                const values = lines[i].split(',').map(v => v.trim());
                const product = {};
                
                // Map CSV columns to product properties
                headers.forEach((header, index) => {
                    if (values[index]) {
                        switch (header.toLowerCase()) {
                            case 'name':
                                product.name = values[index];
                                break;
                            case 'sku':
                                product.sku = values[index];
                                break;
                            case 'category':
                                product.category = values[index];
                                break;
                            case 'price':
                                product.price = parseFloat(values[index]) || 0;
                                break;
                            case 'cost':
                                product.cost = parseFloat(values[index]) || 0;
                                break;
                            case 'stock':
                                product.stock = parseInt(values[index]) || 0;
                                break;
                            case 'brand':
                                product.brand = values[index];
                                break;
                        }
                    }
                });
                
                // Validate required fields
                if (!product.name || !product.sku) {
                    skippedCount++;
                    continue;
                }
                
                // Generate ID and set defaults
                product.id = App.generateId();
                product.threshold = product.threshold || 10;
                product.description = product.description || '';
                product.status = 'In Stock';
                
                // Check for duplicate SKU
                const skuExists = products.some(p => p.sku === product.sku);
                const updateExisting = document.getElementById('updateExisting').checked;
                
                if (skuExists && updateExisting) {
                    // Update existing product
                    const index = products.findIndex(p => p.sku === product.sku);
                    products[index] = { ...products[index], ...product };
                } else if (!skuExists) {
                    // Add new product
                    products.push(product);
                    importedCount++;
                } else {
                    skippedCount++;
                }
            }
            
            // Save updated products
            App.saveProducts(products);
            
            // Close modal and reload table
            App.closeModal(document.getElementById('importProductsModal'));
            loadProducts();
            
            // Show results
            showNotification(`Import completed! ${importedCount} products imported, ${skippedCount} skipped.`, 'success');
            
        } catch (error) {
            console.error('Error parsing CSV:', error);
            showNotification('Error parsing CSV file. Please check the format.', 'error');
        }
    };
    
    reader.onerror = function() {
        showNotification('Error reading file.', 'error');
    };
    
    reader.readAsText(csvFile);
}

function initializeProductFilters() {
    const searchInput = document.getElementById('productSearch');
    const categoryFilter = document.getElementById('categoryFilter');
    const statusFilter = document.getElementById('statusFilter');
    
    if (searchInput) {
        searchInput.addEventListener('input', filterProducts);
    }
    
    if (categoryFilter) {
        categoryFilter.addEventListener('change', filterProducts);
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', filterProducts);
    }
}

function filterProducts() {
    const searchTerm = document.getElementById('productSearch')?.value.toLowerCase() || '';
    const category = document.getElementById('categoryFilter')?.value || '';
    const status = document.getElementById('statusFilter')?.value || '';
    
    const products = App.getProducts();
    const filteredProducts = products.filter(product => {
        // Search filter
        const matchesSearch = product.name.toLowerCase().includes(searchTerm) ||
                             product.sku.toLowerCase().includes(searchTerm) ||
                             (product.brand && product.brand.toLowerCase().includes(searchTerm));
        
        // Category filter
        const matchesCategory = !category || product.category === category;
        
        // Status filter
        let matchesStatus = true;
        if (status) {
            if (status === 'In Stock') {
                matchesStatus = product.stock > product.threshold;
            } else if (status === 'Low Stock') {
                matchesStatus = product.stock > 0 && product.stock <= product.threshold;
            } else if (status === 'Out of Stock') {
                matchesStatus = product.stock === 0;
            }
        }
        
        return matchesSearch && matchesCategory && matchesStatus;
    });
    
    // Update table with filtered products
    const tbody = document.querySelector('#productsTable tbody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (filteredProducts.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center">
                    <div class="empty-state">
                        <i class="fas fa-search"></i>
                        <h4>No Products Found</h4>
                        <p>Try adjusting your search or filters.</p>
                    </div>
                </td>
            </tr>
        `;
    } else {
        filteredProducts.forEach(product => {
            const row = createProductRow(product);
            tbody.appendChild(row);
        });
    }
    
    updateProductCount(filteredProducts.length);
}

function initializeBulkActions() {
    // Select all checkbox
    const selectAll = document.getElementById('selectAll');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.product-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }
    
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', deleteSelectedProducts);
    }
}

function deleteSelectedProducts() {
    const selectedCheckboxes = document.querySelectorAll('.product-checkbox:checked');
    
    if (selectedCheckboxes.length === 0) {
        showNotification('Please select at least one product to delete.', 'warning');
        return;
    }
    
    if (!confirm(`Are you sure you want to delete ${selectedCheckboxes.length} selected product(s)? This action cannot be undone.`)) {
        return;
    }
    
    const products = App.getProducts();
    const selectedIds = Array.from(selectedCheckboxes).map(cb => cb.value);
    const updatedProducts = products.filter(p => !selectedIds.includes(p.id.toString()));
    
    App.saveProducts(updatedProducts);
    loadProducts();
    
    // Uncheck select all
    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.checked = false;
    }
    
    showNotification(`${selectedIds.length} product(s) deleted successfully!`, 'success');
}

function showNotification(message, type = 'info') {
    // Remove existing notification
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button class="notification-close">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Add styles
    const style = document.createElement('style');
    style.textContent = `
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            background: white;
            border-radius: var(--border-radius);
            padding: 1rem 1.5rem;
            box-shadow: var(--shadow-lg);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 1rem;
            min-width: 300px;
            max-width: 400px;
            z-index: 9999;
            animation: slideIn 0.3s ease;
            border-left: 4px solid var(--primary-color);
        }
        
        .notification-success {
            border-left-color: #10b981;
        }
        
        .notification-error {
            border-left-color: #f72585;
        }
        
        .notification-warning {
            border-left-color: #f8961e;
        }
        
        .notification-content {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            flex: 1;
        }
        
        .notification-content i {
            font-size: 1.25rem;
        }
        
        .notification-success .notification-content i {
            color: #10b981;
        }
        
        .notification-error .notification-content i {
            color: #f72585;
        }
        
        .notification-warning .notification-content i {
            color: #f8961e;
        }
        
        .notification-close {
            background: none;
            border: none;
            color: var(--gray-color);
            cursor: pointer;
            padding: 0.25rem;
        }
        
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
    `;
    
    document.head.appendChild(style);
    document.body.appendChild(notification);
    
    // Add close functionality
    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.addEventListener('click', function() {
        notification.remove();
    });
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}