// Inventory Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeInventoryPage();
});

function initializeInventoryPage() {
    // Load inventory data
    loadInventory();
    
    // Initialize summary statistics
    updateInventorySummary();
    
    // Initialize filters
    initializeInventoryFilters();
    
    // Initialize modals
    initializeInventoryModals();
    
    // Load stock movements
    loadStockMovements();
    
    // Initialize bulk actions
    initializeInventoryBulkActions();
}

function loadInventory() {
    const products = App.getProducts();
    const tbody = document.querySelector('#inventoryTable tbody');
    
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (products.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center">
                    <div class="empty-state">
                        <i class="fas fa-box-open"></i>
                        <h4>No Inventory Items</h4>
                        <p>Add products to manage inventory.</p>
                    </div>
                </td>
            </tr>
        `;
        updateInventoryCount(0);
        return;
    }
    
    // Sort products by stock status (out of stock first, then low stock)
    const sortedProducts = [...products].sort((a, b) => {
        const statusA = getStockStatus(a);
        const statusB = getStockStatus(b);
        
        const statusOrder = { 'out': 0, 'low': 1, 'in': 2 };
        return statusOrder[statusA] - statusOrder[statusB];
    });
    
    sortedProducts.forEach(product => {
        const row = createInventoryRow(product);
        tbody.appendChild(row);
    });
    
    updateInventoryCount(products.length);
}

function getStockStatus(product) {
    if (product.stock === 0) return 'out';
    if (product.stock <= product.threshold) return 'low';
    return 'in';
}

function createInventoryRow(product) {
    const row = document.createElement('tr');
    
    // Determine stock status
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
    
    // Format last updated date
    const lastUpdated = product.lastUpdated ? 
        new Date(product.lastUpdated).toLocaleDateString() : 
        'Never';
    
    row.innerHTML = `
        <td>
            <input type="checkbox" class="inventory-checkbox" value="${product.id}">
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
        <td>
            <span class="${product.stock <= product.threshold ? 'text-warning fw-bold' : ''}">
                ${product.stock}
            </span>
        </td>
        <td>${product.threshold}</td>
        <td>
            <span class="stock-status ${statusClass}">${statusText}</span>
        </td>
        <td>${lastUpdated}</td>
        <td>
            <div class="action-buttons">
                <button class="action-btn edit" title="Adjust Stock" data-id="${product.id}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn view" title="View Details" data-id="${product.id}">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
        </td>
    `;
    
    // Add event listeners
    const editBtn = row.querySelector('.action-btn.edit');
    const viewBtn = row.querySelector('.action-btn.view');
    
    if (editBtn) {
        editBtn.addEventListener('click', function() {
            openAdjustStockModal(product.id);
        });
    }
    
    if (viewBtn) {
        viewBtn.addEventListener('click', function() {
            viewInventoryItem(product.id);
        });
    }
    
    return row;
}

function updateInventoryCount(count) {
    const countElement = document.getElementById('inventoryCount');
    if (countElement) {
        countElement.textContent = count;
    }
}

function updateInventorySummary() {
    const products = App.getProducts();
    
    // Calculate statistics
    const totalProducts = products.length;
    const inStockProducts = products.filter(p => p.stock > p.threshold).length;
    const lowStockProducts = products.filter(p => p.stock > 0 && p.stock <= p.threshold).length;
    const outOfStockProducts = products.filter(p => p.stock === 0).length;
    
    // Update UI
    document.getElementById('totalProducts').textContent = totalProducts;
    document.getElementById('inStockProducts').textContent = inStockProducts;
    document.getElementById('lowStockProducts').textContent = lowStockProducts;
    document.getElementById('outOfStockProducts').textContent = outOfStockProducts;
    
    // Update low stock badge in sidebar
    updateLowStockBadge(lowStockProducts);
}

function updateLowStockBadge(count) {
    const badge = document.querySelector('.sidebar .badge.low-stock');
    if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? 'flex' : 'none';
    }
}

function initializeInventoryFilters() {
    const searchInput = document.getElementById('inventorySearch');
    const categoryFilter = document.getElementById('inventoryCategoryFilter');
    const statusFilter = document.getElementById('stockStatusFilter');
    
    if (searchInput) {
        searchInput.addEventListener('input', filterInventory);
    }
    
    if (categoryFilter) {
        categoryFilter.addEventListener('change', filterInventory);
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', filterInventory);
    }
}

function filterInventory() {
    const searchTerm = document.getElementById('inventorySearch')?.value.toLowerCase() || '';
    const category = document.getElementById('inventoryCategoryFilter')?.value || '';
    const status = document.getElementById('stockStatusFilter')?.value || '';
    
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
            if (status === 'in-stock') {
                matchesStatus = product.stock > product.threshold;
            } else if (status === 'low-stock') {
                matchesStatus = product.stock > 0 && product.stock <= product.threshold;
            } else if (status === 'out-of-stock') {
                matchesStatus = product.stock === 0;
            }
        }
        
        return matchesSearch && matchesCategory && matchesStatus;
    });
    
    // Update table with filtered products
    const tbody = document.querySelector('#inventoryTable tbody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (filteredProducts.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center">
                    <div class="empty-state">
                        <i class="fas fa-search"></i>
                        <h4>No Inventory Items Found</h4>
                        <p>Try adjusting your search or filters.</p>
                    </div>
                </td>
            </tr>
        `;
    } else {
        filteredProducts.forEach(product => {
            const row = createInventoryRow(product);
            tbody.appendChild(row);
        });
    }
    
    updateInventoryCount(filteredProducts.length);
}

function initializeInventoryModals() {
    // Adjust Stock Modal
    const adjustStockBtn = document.getElementById('adjustStockBtn');
    const adjustStockForm = document.getElementById('adjustStockForm');
    
    if (adjustStockBtn) {
        adjustStockBtn.addEventListener('click', function() {
            openAdjustStockModal();
        });
    }
    
    if (adjustStockForm) {
        adjustStockForm.addEventListener('submit', function(e) {
            e.preventDefault();
            applyStockAdjustment();
        });
    }
    
    // Populate product dropdown in adjust modal
    populateAdjustProductDropdown();
    
    // Inventory Report Button
    const reportBtn = document.getElementById('inventoryReportBtn');
    if (reportBtn) {
        reportBtn.addEventListener('click', generateInventoryReport);
    }
    
    // Bulk Update Modal
    const bulkUpdateBtn = document.getElementById('bulkUpdateBtn');
    const bulkUpdateForm = document.getElementById('bulkUpdateForm');
    const bulkActionSelect = document.getElementById('bulkAction');
    
    if (bulkUpdateBtn) {
        bulkUpdateBtn.addEventListener('click', function() {
            openBulkUpdateModal();
        });
    }
    
    if (bulkUpdateForm) {
        bulkUpdateForm.addEventListener('submit', function(e) {
            e.preventDefault();
            applyBulkUpdate();
        });
    }
    
    if (bulkActionSelect) {
        bulkActionSelect.addEventListener('change', function() {
            updateBulkActionForm(this.value);
        });
    }
    
    // Export Inventory Button
    const exportBtn = document.getElementById('exportInventoryBtn');
    if (exportBtn) {
        exportBtn.addEventListener('click', exportInventory);
    }
}

function populateAdjustProductDropdown() {
    const products = App.getProducts();
    const dropdown = document.getElementById('adjustProduct');
    
    if (!dropdown) return;
    
    // Clear existing options except first
    while (dropdown.options.length > 1) {
        dropdown.remove(1);
    }
    
    // Add product options
    products.forEach(product => {
        const option = document.createElement('option');
        option.value = product.id;
        option.textContent = `${product.name} (${product.sku}) - Stock: ${product.stock}`;
        dropdown.appendChild(option);
    });
    
    // Add change event listener
    dropdown.addEventListener('change', function() {
        updateCurrentStockDisplay(this.value);
    });
}

function openAdjustStockModal(productId = null) {
    // Reset form
    const form = document.getElementById('adjustStockForm');
    form.reset();
    
    // Set default date to today
    const today = new Date().toISOString().slice(0, 16);
    document.getElementById('adjustDate').value = today;
    
    // If productId is provided, select that product
    if (productId) {
        document.getElementById('adjustProduct').value = productId;
        updateCurrentStockDisplay(productId);
    } else {
        // Update current stock display for first product
        const firstProduct = document.getElementById('adjustProduct').options[1]?.value;
        if (firstProduct) {
            updateCurrentStockDisplay(firstProduct);
        }
    }
    
    // Open modal
    App.openModal('adjustStockModal');
}

function updateCurrentStockDisplay(productId) {
    const products = App.getProducts();
    const product = products.find(p => p.id == productId);
    
    const currentStockInput = document.getElementById('currentStock');
    if (currentStockInput && product) {
        currentStockInput.value = product.stock;
    }
}

function applyStockAdjustment() {
    const productId = document.getElementById('adjustProduct').value;
    const adjustType = document.getElementById('adjustType').value;
    const adjustQuantity = parseInt(document.getElementById('adjustQuantity').value);
    const adjustReason = document.getElementById('adjustReason').value;
    const adjustDate = document.getElementById('adjustDate').value;
    
    if (!productId) {
        showNotification('Please select a product.', 'error');
        return;
    }
    
    if (isNaN(adjustQuantity) || adjustQuantity === 0) {
        showNotification('Please enter a valid quantity.', 'error');
        return;
    }
    
    // Get products
    const products = App.getProducts();
    const productIndex = products.findIndex(p => p.id == productId);
    
    if (productIndex === -1) {
        showNotification('Product not found!', 'error');
        return;
    }
    
    const product = products[productIndex];
    const oldStock = product.stock;
    
    // Calculate new stock based on adjustment type
    let newStock = oldStock;
    let movementType = '';
    
    switch (adjustType) {
        case 'restock':
            newStock = oldStock + Math.abs(adjustQuantity);
            movementType = 'restock';
            break;
        case 'damage':
            newStock = oldStock - Math.abs(adjustQuantity);
            movementType = 'damage';
            break;
        case 'correction':
            newStock = adjustQuantity;
            movementType = 'correction';
            break;
        case 'transfer':
            newStock = oldStock - Math.abs(adjustQuantity);
            movementType = 'transfer';
            break;
    }
    
    // Ensure stock doesn't go negative
    if (newStock < 0) {
        showNotification('Cannot adjust stock below 0.', 'error');
        return;
    }
    
    // Update product stock
    products[productIndex].stock = newStock;
    
    // Update product status
    if (newStock === 0) {
        products[productIndex].status = 'Out of Stock';
    } else if (newStock <= products[productIndex].threshold) {
        products[productIndex].status = 'Low Stock';
    } else {
        products[productIndex].status = 'In Stock';
    }
    
    // Update last updated timestamp
    products[productIndex].lastUpdated = new Date().toISOString();
    
    // Save products
    App.saveProducts(products);
    
    // Record stock movement
    recordStockMovement({
        productId: product.id,
        productName: product.name,
        sku: product.sku,
        movementType: movementType,
        quantityChange: newStock - oldStock,
        newStock: newStock,
        reason: adjustReason || `Stock ${movementType}`,
        date: adjustDate || new Date().toISOString(),
        userId: 'admin'
    });
    
    // Close modal
    App.closeModal(document.getElementById('adjustStockModal'));
    
    // Reload data
    loadInventory();
    updateInventorySummary();
    loadStockMovements();
    
    showNotification(`Stock adjusted successfully. New stock: ${newStock}`, 'success');
}

function recordStockMovement(movement) {
    // Get existing movements
    let movements = JSON.parse(localStorage.getItem('stockMovements')) || [];
    
    // Add new movement
    movements.unshift(movement);
    
    // Keep only last 100 movements
    if (movements.length > 100) {
        movements = movements.slice(0, 100);
    }
    
    // Save movements
    localStorage.setItem('stockMovements', JSON.stringify(movements));
}

function loadStockMovements() {
    const movements = JSON.parse(localStorage.getItem('stockMovements')) || [];
    const tbody = document.querySelector('#stockMovementTable tbody');
    const movementTypeFilter = document.getElementById('movementTypeFilter');
    
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    // Filter movements by type if specified
    let filteredMovements = movements;
    if (movementTypeFilter && movementTypeFilter.value) {
        filteredMovements = movements.filter(m => m.movementType === movementTypeFilter.value);
    }
    
    // Sort by date (newest first)
    filteredMovements.sort((a, b) => new Date(b.date) - new Date(a.date));
    
    if (filteredMovements.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center">
                    <div class="empty-state">
                        <i class="fas fa-exchange-alt"></i>
                        <h4>No Stock Movements</h4>
                        <p>Stock movements will appear here when inventory is updated.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    // Show only last 20 movements
    const displayMovements = filteredMovements.slice(0, 20);
    
    displayMovements.forEach(movement => {
        const row = document.createElement('tr');
        
        // Format date
        const date = new Date(movement.date);
        const formattedDate = date.toLocaleString();
        
        // Determine movement type display
        let typeDisplay = '';
        let typeClass = '';
        
        switch (movement.movementType) {
            case 'sale':
                typeDisplay = 'Sale';
                typeClass = 'text-danger';
                break;
            case 'restock':
                typeDisplay = 'Restock';
                typeClass = 'text-success';
                break;
            case 'adjustment':
                typeDisplay = 'Adjustment';
                typeClass = 'text-warning';
                break;
            case 'damage':
                typeDisplay = 'Damage/Waste';
                typeClass = 'text-danger';
                break;
            case 'transfer':
                typeDisplay = 'Transfer';
                typeClass = 'text-info';
                break;
            default:
                typeDisplay = movement.movementType;
        }
        
        // Format quantity change
        const quantityChange = movement.quantityChange;
        const changeDisplay = quantityChange > 0 ? 
            `+${quantityChange}` : 
            quantityChange.toString();
        
        row.innerHTML = `
            <td>${formattedDate}</td>
            <td>${movement.productName}</td>
            <td>${movement.sku}</td>
            <td><span class="${typeClass}">${typeDisplay}</span></td>
            <td>${changeDisplay}</td>
            <td>${movement.newStock}</td>
            <td>${movement.userId}</td>
            <td>${movement.reason || '-'}</td>
        `;
        
        tbody.appendChild(row);
    });
}

function viewInventoryItem(productId) {
    const products = App.getProducts();
    const product = products.find(p => p.id == productId);
    
    if (!product) {
        showNotification('Product not found!', 'error');
        return;
    }
    
    // Calculate inventory value
    const costValue = product.stock * product.cost;
    const retailValue = product.stock * product.price;
    
    const modalHtml = `
        <div class="modal active" id="inventoryDetailsModal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>Inventory Details</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="inventory-details">
                        <div class="inventory-header">
                            <h4>${product.name}</h4>
                            <p class="text-muted">${product.sku}</p>
                        </div>
                        
                        <div class="inventory-stats">
                            <div class="stat-row">
                                <span class="stat-label">Current Stock:</span>
                                <span class="stat-value ${product.stock <= product.threshold ? 'text-warning' : ''}">
                                    ${product.stock} units
                                </span>
                            </div>
                            <div class="stat-row">
                                <span class="stat-label">Low Stock Threshold:</span>
                                <span class="stat-value">${product.threshold} units</span>
                            </div>
                            <div class="stat-row">
                                <span class="stat-label">Status:</span>
                                <span class="stat-value">
                                    <span class="stock-status ${product.stock === 0 ? 'stock-out' : product.stock <= product.threshold ? 'stock-low' : 'stock-in'}">
                                        ${product.stock === 0 ? 'Out of Stock' : product.stock <= product.threshold ? 'Low Stock' : 'In Stock'}
                                    </span>
                                </span>
                            </div>
                            <div class="stat-row">
                                <span class="stat-label">Category:</span>
                                <span class="stat-value">${product.category}</span>
                            </div>
                            <div class="stat-row">
                                <span class="stat-label">Last Updated:</span>
                                <span class="stat-value">
                                    ${product.lastUpdated ? new Date(product.lastUpdated).toLocaleString() : 'Never'}
                                </span>
                            </div>
                        </div>
                        
                        <div class="inventory-values mt-3">
                            <h5>Inventory Value</h5>
                            <div class="value-row">
                                <span>At Cost (${App.formatCurrency(product.cost)}/unit):</span>
                                <span class="fw-bold">${App.formatCurrency(costValue)}</span>
                            </div>
                            <div class="value-row">
                                <span>At Retail (${App.formatCurrency(product.price)}/unit):</span>
                                <span class="fw-bold">${App.formatCurrency(retailValue)}</span>
                            </div>
                            <div class="value-row">
                                <span>Potential Profit:</span>
                                <span class="fw-bold text-success">${App.formatCurrency(retailValue - costValue)}</span>
                            </div>
                        </div>
                        
                        <div class="inventory-history mt-3">
                            <h5>Recent Stock Movements</h5>
                            <div class="history-list" id="productStockHistory">
                                <!-- Stock history will be loaded here -->
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary modal-close">Close</button>
                    <button class="btn btn-primary" id="adjustFromDetailsBtn" data-id="${product.id}">
                        <i class="fas fa-edit"></i> Adjust Stock
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('inventoryDetailsModal');
    if (existingModal) existingModal.remove();
    
    // Add new modal
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Load stock history for this product
    loadProductStockHistory(productId);
    
    // Initialize modal close
    const modal = document.getElementById('inventoryDetailsModal');
    const closeBtn = modal.querySelector('.modal-close');
    
    closeBtn.addEventListener('click', function() {
        modal.remove();
        document.body.style.overflow = 'auto';
    });
    
    modal.addEventListener('click', function(e) {
        if (e.target === this) {
            this.remove();
            document.body.style.overflow = 'auto';
        }
    });
    
    // Adjust stock button
    const adjustBtn = document.getElementById('adjustFromDetailsBtn');
    if (adjustBtn) {
        adjustBtn.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            modal.remove();
            document.body.style.overflow = 'auto';
            openAdjustStockModal(productId);
        });
    }
}

function loadProductStockHistory(productId) {
    const movements = JSON.parse(localStorage.getItem('stockMovements')) || [];
    const productMovements = movements.filter(m => m.productId == productId).slice(0, 10);
    const historyList = document.getElementById('productStockHistory');
    
    if (!historyList) return;
    
    if (productMovements.length === 0) {
        historyList.innerHTML = '<p class="text-muted">No stock movements recorded for this product.</p>';
        return;
    }
    
    historyList.innerHTML = '';
    
    productMovements.forEach(movement => {
        const historyItem = document.createElement('div');
        historyItem.className = 'history-item';
        
        const date = new Date(movement.date);
        const formattedDate = date.toLocaleString();
        
        // Determine movement type color
        let typeColor = '';
        switch (movement.movementType) {
            case 'sale':
            case 'damage':
                typeColor = 'text-danger';
                break;
            case 'restock':
                typeColor = 'text-success';
                break;
            default:
                typeColor = 'text-warning';
        }
        
        // Format quantity change
        const change = movement.quantityChange;
        const changeDisplay = change > 0 ? `+${change}` : change;
        
        historyItem.innerHTML = `
            <div class="history-item-header">
                <span class="history-date">${formattedDate}</span>
                <span class="history-type ${typeColor}">
                    ${movement.movementType.charAt(0).toUpperCase() + movement.movementType.slice(1)}
                </span>
            </div>
            <div class="history-item-body">
                <span class="history-change">${changeDisplay} units</span>
                <span class="history-new-stock">→ ${movement.newStock} units</span>
            </div>
            ${movement.reason ? `<div class="history-item-footer text-muted small">${movement.reason}</div>` : ''}
        `;
        
        historyList.appendChild(historyItem);
    });
}

function generateInventoryReport() {
    const products = App.getProducts();
    
    if (products.length === 0) {
        showNotification('No inventory data to report.', 'warning');
        return;
    }
    
    // Calculate summary statistics
    let totalCostValue = 0;
    let totalRetailValue = 0;
    let lowStockCount = 0;
    let outOfStockCount = 0;
    
    products.forEach(product => {
        totalCostValue += product.stock * product.cost;
        totalRetailValue += product.stock * product.price;
        
        if (product.stock === 0) {
            outOfStockCount++;
        } else if (product.stock <= product.threshold) {
            lowStockCount++;
        }
    });
    
    // Create report HTML
    const reportHtml = `
        <!DOCTYPE html>
        <html>
        <head>
            <title>Inventory Report - ${new Date().toLocaleDateString()}</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                h1 { color: #333; }
                .summary { background: #f5f5f5; padding: 20px; border-radius: 5px; margin-bottom: 30px; }
                .summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
                .summary-card { background: white; padding: 15px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
                th { background-color: #f8f9fa; }
                .low-stock { color: #f8961e; }
                .out-of-stock { color: #f72585; }
                .footer { margin-top: 30px; text-align: center; color: #666; font-size: 0.9em; }
            </style>
        </head>
        <body>
            <h1>Inventory Report</h1>
            <p>Generated: ${new Date().toLocaleString()}</p>
            
            <div class="summary">
                <h2>Summary</h2>
                <div class="summary-grid">
                    <div class="summary-card">
                        <h3>${products.length}</h3>
                        <p>Total Products</p>
                    </div>
                    <div class="summary-card">
                        <h3>${lowStockCount}</h3>
                        <p>Low Stock Items</p>
                    </div>
                    <div class="summary-card">
                        <h3>${outOfStockCount}</h3>
                        <p>Out of Stock</p>
                    </div>
                    <div class="summary-card">
                        <h3>${App.formatCurrency(totalRetailValue)}</h3>
                        <p>Total Retail Value</p>
                    </div>
                </div>
            </div>
            
            <h2>Inventory Details</h2>
            <table>
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>SKU</th>
                        <th>Category</th>
                        <th>Stock</th>
                        <th>Threshold</th>
                        <th>Status</th>
                        <th>Cost Value</th>
                        <th>Retail Value</th>
                    </tr>
                </thead>
                <tbody>
                    ${products.map(product => {
                        const costValue = product.stock * product.cost;
                        const retailValue = product.stock * product.price;
                        let statusClass = '';
                        let statusText = '';
                        
                        if (product.stock === 0) {
                            statusClass = 'out-of-stock';
                            statusText = 'Out of Stock';
                        } else if (product.stock <= product.threshold) {
                            statusClass = 'low-stock';
                            statusText = 'Low Stock';
                        } else {
                            statusText = 'In Stock';
                        }
                        
                        return `
                            <tr>
                                <td>${product.name}</td>
                                <td>${product.sku}</td>
                                <td>${product.category}</td>
                                <td class="${product.stock <= product.threshold ? statusClass : ''}">${product.stock}</td>
                                <td>${product.threshold}</td>
                                <td class="${statusClass}">${statusText}</td>
                                <td>${App.formatCurrency(costValue)}</td>
                                <td>${App.formatCurrency(retailValue)}</td>
                            </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
            
            <div class="footer">
                <p>Report generated by RetailPro Inventory System</p>
            </div>
        </body>
        </html>
    `;
    
    // Open report in new window
    const reportWindow = window.open('', '_blank');
    reportWindow.document.write(reportHtml);
    reportWindow.document.close();
    
    showNotification('Inventory report generated!', 'success');
}

function openBulkUpdateModal() {
    // Reset form
    const form = document.getElementById('bulkUpdateForm');
    form.reset();
    
    // Reset selected products list
    const selectedList = document.getElementById('selectedProductsList');
    if (selectedList) {
        selectedList.innerHTML = '';
    }
    
    // Update selected count
    updateSelectedCount();
    
    // Set default bulk action
    document.getElementById('bulkAction').value = 'adjust';
    updateBulkActionForm('adjust');
    
    // Open modal
    App.openModal('bulkUpdateModal');
}

function updateBulkActionForm(action) {
    // Hide all form sections
    document.getElementById('adjustStockFormSection').style.display = 'none';
    document.getElementById('updateThresholdFormSection').style.display = 'none';
    document.getElementById('updateStatusFormSection').style.display = 'none';
    
    // Show selected form section
    switch (action) {
        case 'adjust':
            document.getElementById('adjustStockFormSection').style.display = 'block';
            break;
        case 'threshold':
            document.getElementById('updateThresholdFormSection').style.display = 'block';
            break;
        case 'status':
            document.getElementById('updateStatusFormSection').style.display = 'block';
            break;
    }
}

function initializeInventoryBulkActions() {
    // Select all checkbox
    const selectAll = document.getElementById('inventorySelectAll');
    if (selectAll) {
        selectAll.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.inventory-checkbox');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
            updateSelectedProductsList();
        });
    }
    
    // Individual checkbox changes
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('inventory-checkbox')) {
            updateSelectedProductsList();
        }
    });
}

function updateSelectedProductsList() {
    const selectedCheckboxes = document.querySelectorAll('.inventory-checkbox:checked');
    const selectedList = document.getElementById('selectedProductsList');
    const selectedCount = document.getElementById('selectedCount');
    
    if (!selectedList || !selectedCount) return;
    
    selectedList.innerHTML = '';
    
    if (selectedCheckboxes.length === 0) {
        selectedCount.textContent = '0';
        return;
    }
    
    const products = App.getProducts();
    
    selectedCheckboxes.forEach(checkbox => {
        const productId = checkbox.value;
        const product = products.find(p => p.id == productId);
        
        if (product) {
            const selectedItem = document.createElement('div');
            selectedItem.className = 'selected-item';
            selectedItem.innerHTML = `
                <span>${product.name} (${product.sku})</span>
                <span class="text-muted">Stock: ${product.stock}</span>
            `;
            selectedList.appendChild(selectedItem);
        }
    });
    
    selectedCount.textContent = selectedCheckboxes.length;
}

function updateSelectedCount() {
    const selectedCheckboxes = document.querySelectorAll('.inventory-checkbox:checked');
    const selectedCount = document.getElementById('selectedCount');
    
    if (selectedCount) {
        selectedCount.textContent = selectedCheckboxes.length;
    }
}

function applyBulkUpdate() {
    const selectedCheckboxes = document.querySelectorAll('.inventory-checkbox:checked');
    
    if (selectedCheckboxes.length === 0) {
        showNotification('Please select at least one product to update.', 'warning');
        return;
    }
    
    const action = document.getElementById('bulkAction').value;
    const products = App.getProducts();
    let updatedCount = 0;
    
    switch (action) {
        case 'adjust':
            const adjustType = document.getElementById('bulkAdjustType').value;
            const adjustValue = parseInt(document.getElementById('bulkAdjustValue').value);
            
            if (isNaN(adjustValue) || adjustValue < 0) {
                showNotification('Please enter a valid value.', 'error');
                return;
            }
            
            selectedCheckboxes.forEach(checkbox => {
                const productId = checkbox.value;
                const productIndex = products.findIndex(p => p.id == productId);
                
                if (productIndex !== -1) {
                    const product = products[productIndex];
                    let newStock = product.stock;
                    
                    if (adjustType === 'add') {
                        newStock += adjustValue;
                    } else if (adjustType === 'set') {
                        newStock = adjustValue;
                    }
                    
                    if (newStock < 0) newStock = 0;
                    
                    products[productIndex].stock = newStock;
                    
                    // Update status
                    if (newStock === 0) {
                        products[productIndex].status = 'Out of Stock';
                    } else if (newStock <= products[productIndex].threshold) {
                        products[productIndex].status = 'Low Stock';
                    } else {
                        products[productIndex].status = 'In Stock';
                    }
                    
                    products[productIndex].lastUpdated = new Date().toISOString();
                    updatedCount++;
                    
                    // Record stock movement
                    recordStockMovement({
                        productId: product.id,
                        productName: product.name,
                        sku: product.sku,
                        movementType: 'adjustment',
                        quantityChange: newStock - product.stock,
                        newStock: newStock,
                        reason: `Bulk ${adjustType} adjustment`,
                        date: new Date().toISOString(),
                        userId: 'admin'
                    });
                }
            });
            break;
            
        case 'threshold':
            const thresholdValue = parseInt(document.getElementById('bulkThresholdValue').value);
            
            if (isNaN(thresholdValue) || thresholdValue < 1) {
                showNotification('Please enter a valid threshold (minimum 1).', 'error');
                return;
            }
            
            selectedCheckboxes.forEach(checkbox => {
                const productId = checkbox.value;
                const productIndex = products.findIndex(p => p.id == productId);
                
                if (productIndex !== -1) {
                    products[productIndex].threshold = thresholdValue;
                    
                    // Update status based on new threshold
                    if (products[productIndex].stock === 0) {
                        products[productIndex].status = 'Out of Stock';
                    } else if (products[productIndex].stock <= thresholdValue) {
                        products[productIndex].status = 'Low Stock';
                    } else {
                        products[productIndex].status = 'In Stock';
                    }
                    
                    products[productIndex].lastUpdated = new Date().toISOString();
                    updatedCount++;
                }
            });
            break;
            
        case 'status':
            const statusValue = document.getElementById('bulkStatusValue').value;
            
            selectedCheckboxes.forEach(checkbox => {
                const productId = checkbox.value;
                const productIndex = products.findIndex(p => p.id == productId);
                
                if (productIndex !== -1) {
                    products[productIndex].status = 
                        statusValue === 'in-stock' ? 'In Stock' :
                        statusValue === 'low-stock' ? 'Low Stock' : 'Out of Stock';
                    
                    products[productIndex].lastUpdated = new Date().toISOString();
                    updatedCount++;
                }
            });
            break;
    }
    
    // Save updated products
    App.saveProducts(products);
    
    // Close modal
    App.closeModal(document.getElementById('bulkUpdateModal'));
    
    // Reload data
    loadInventory();
    updateInventorySummary();
    loadStockMovements();
    
    showNotification(`${updatedCount} product(s) updated successfully!`, 'success');
}

function exportInventory() {
    const products = App.getProducts();
    
    if (products.length === 0) {
        showNotification('No inventory data to export.', 'warning');
        return;
    }
    
    // Create CSV content
    let csv = 'Product Name,SKU,Category,Brand,Current Stock,Low Stock Threshold,Status,Price,Cost,Last Updated\n';
    
    products.forEach(product => {
        const lastUpdated = product.lastUpdated ? 
            new Date(product.lastUpdated).toLocaleDateString() : 
            'Never';
        
        csv += `"${product.name}","${product.sku}","${product.category}","${product.brand || ''}",${product.stock},${product.threshold},"${product.status}",${product.price},${product.cost},"${lastUpdated}"\n`;
    });
    
    // Create download link
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    
    a.href = url;
    a.download = `inventory_export_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    
    showNotification('Inventory data exported successfully!', 'success');
}

function showNotification(message, type = 'info') {
    // Same notification function as before
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
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
    
    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.addEventListener('click', function() {
        notification.remove();
    });
    
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}