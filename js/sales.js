// Sales Processing JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeSalesPage();
});

function initializeSalesPage() {
    // Initialize sale form
    initializeSaleForm();
    
    // Load customers for dropdown
    loadCustomers();
    
    // Load products for search
    loadProductsForSale();
    
    // Initialize modals
    initializeSalesModals();
    
    // Initialize sale history view
    initializeSalesHistory();
    
    // Set initial sale ID
    generateSaleId();
}

function initializeSaleForm() {
    // Initialize cart from localStorage or create empty
    let cart = JSON.parse(localStorage.getItem('currentCart')) || [];
    updateCartDisplay(cart);
    
    // Update sale date
    updateSaleDate();
    
    // Initialize product search
    const searchInput = document.getElementById('saleProductSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            searchProducts(this.value);
        });
    }
    
    // Browse products button
    const browseBtn = document.getElementById('browseProductsBtn');
    if (browseBtn) {
        browseBtn.addEventListener('click', function() {
            showAllProducts();
        });
    }
    
    // Customer selection
    const customerSelect = document.getElementById('customerSelect');
    if (customerSelect) {
        customerSelect.addEventListener('change', updateCustomerDetails);
    }
    
    // Discount input
    const discountInput = document.getElementById('discount');
    if (discountInput) {
        discountInput.addEventListener('input', updateTotals);
    }
    
    // Payment method change
    document.querySelectorAll('input[name="paymentMethod"]').forEach(radio => {
        radio.addEventListener('change', function() {
            updatePaymentSection(this.value);
        });
    });
    
    // Amount tendered input
    const amountTendered = document.getElementById('amountTendered');
    if (amountTendered) {
        amountTendered.addEventListener('input', updateChangeAmount);
    }
    
    // Sale actions
    const cancelBtn = document.getElementById('cancelSaleBtn');
    const completeBtn = document.getElementById('completeSaleBtn');
    
    if (cancelBtn) {
        cancelBtn.addEventListener('click', cancelSale);
    }
    
    if (completeBtn) {
        completeBtn.addEventListener('click', completeSale);
    }
    
    // Calculate initial totals
    updateTotals();
}

function generateSaleId() {
    const saleIdElement = document.getElementById('saleId');
    if (saleIdElement) {
        // Generate a random sale ID (in real app, this would come from backend)
        const randomNum = Math.floor(Math.random() * 1000);
        saleIdElement.textContent = `TRX-${String(randomNum).padStart(5, '0')}`;
    }
}

function updateSaleDate() {
    const saleDateElement = document.getElementById('saleDate');
    if (saleDateElement) {
        const now = new Date();
        saleDateElement.textContent = now.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}

function loadCustomers() {
    const customers = App.getCustomers();
    const customerSelect = document.getElementById('customerSelect');
    
    if (!customerSelect) return;
    
    // Clear existing options except first
    while (customerSelect.options.length > 1) {
        customerSelect.remove(1);
    }
    
    // Add customer options
    customers.forEach(customer => {
        const option = document.createElement('option');
        option.value = customer.id;
        option.textContent = `${customer.firstName} ${customer.lastName}${customer.type === 'vip' ? ' (VIP)' : ''}`;
        customerSelect.appendChild(option);
    });
}

function loadProductsForSale() {
    // This function loads products into memory for searching
    window.saleProducts = App.getProducts().filter(p => p.stock > 0);
}

function searchProducts(searchTerm) {
    const resultsContainer = document.getElementById('productSearchResults');
    if (!resultsContainer) return;
    
    resultsContainer.innerHTML = '';
    
    if (!searchTerm.trim()) {
        resultsContainer.style.display = 'none';
        return;
    }
    
    const searchLower = searchTerm.toLowerCase();
    const filteredProducts = window.saleProducts.filter(product => 
        product.name.toLowerCase().includes(searchLower) ||
        product.sku.toLowerCase().includes(searchLower) ||
        (product.brand && product.brand.toLowerCase().includes(searchLower))
    ).slice(0, 5); // Limit to 5 results
    
    if (filteredProducts.length === 0) {
        resultsContainer.innerHTML = `
            <div class="search-result-item">
                <div class="product-info">
                    <p class="text-muted">No products found</p>
                </div>
            </div>
        `;
        resultsContainer.style.display = 'block';
        return;
    }
    
    filteredProducts.forEach(product => {
        const resultItem = document.createElement('div');
        resultItem.className = 'search-result-item';
        resultItem.innerHTML = `
            <div class="product-info">
                <h5>${product.name}</h5>
                <p class="mb-0">SKU: ${product.sku} | Stock: ${product.stock}</p>
            </div>
            <div class="product-actions">
                <span class="product-price">${App.formatCurrency(product.price)}</span>
                <button class="btn btn-sm btn-primary add-to-cart-btn" data-id="${product.id}">
                    <i class="fas fa-plus"></i> Add
                </button>
            </div>
        `;
        
        resultsContainer.appendChild(resultItem);
    });
    
    resultsContainer.style.display = 'block';
    
    // Add event listeners to add buttons
    document.querySelectorAll('.add-to-cart-btn').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            addToCart(productId);
            document.getElementById('saleProductSearch').value = '';
            resultsContainer.style.display = 'none';
        });
    });
}

function showAllProducts() {
    const modalHtml = `
        <div class="modal active" id="browseProductsModal">
            <div class="modal-content large">
                <div class="modal-header">
                    <h3>Browse Products</h3>
                    <div class="search-box" style="flex: 1; max-width: 300px;">
                        <i class="fas fa-search"></i>
                        <input type="text" id="browseSearch" placeholder="Search products...">
                    </div>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="products-grid" id="browseProductsGrid" style="max-height: 400px; overflow-y: auto;">
                        <!-- Products will be loaded here -->
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary modal-close">Close</button>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('browseProductsModal');
    if (existingModal) existingModal.remove();
    
    // Add new modal
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Load products into modal
    loadBrowseProducts();
    
    // Initialize search in modal
    const searchInput = document.getElementById('browseSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            filterBrowseProducts(this.value);
        });
    }
    
    // Initialize modal close
    const modal = document.getElementById('browseProductsModal');
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
}

function loadBrowseProducts() {
    const products = window.saleProducts;
    const grid = document.getElementById('browseProductsGrid');
    
    if (!grid) return;
    
    grid.innerHTML = '';
    
    if (products.length === 0) {
        grid.innerHTML = '<p class="text-center">No products available</p>';
        return;
    }
    
    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <div class="product-card-body">
                <div class="product-card-header">
                    <h5>${product.name}</h5>
                    <span class="product-price">${App.formatCurrency(product.price)}</span>
                </div>
                <div class="product-card-details">
                    <p class="text-muted small mb-1">SKU: ${product.sku}</p>
                    <p class="text-muted small mb-2">Stock: ${product.stock}</p>
                    <button class="btn btn-sm btn-primary btn-block add-to-cart-modal" data-id="${product.id}">
                        <i class="fas fa-plus"></i> Add to Cart
                    </button>
                </div>
            </div>
        `;
        
        grid.appendChild(productCard);
    });
    
    // Add event listeners
    document.querySelectorAll('.add-to-cart-modal').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            addToCart(productId);
            
            // Close modal
            const modal = document.getElementById('browseProductsModal');
            if (modal) {
                modal.remove();
                document.body.style.overflow = 'auto';
            }
        });
    });
}

function filterBrowseProducts(searchTerm) {
    const products = window.saleProducts;
    const searchLower = searchTerm.toLowerCase();
    
    const filteredProducts = products.filter(product =>
        product.name.toLowerCase().includes(searchLower) ||
        product.sku.toLowerCase().includes(searchLower)
    );
    
    const grid = document.getElementById('browseProductsGrid');
    if (!grid) return;
    
    grid.innerHTML = '';
    
    filteredProducts.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <div class="product-card-body">
                <div class="product-card-header">
                    <h5>${product.name}</h5>
                    <span class="product-price">${App.formatCurrency(product.price)}</span>
                </div>
                <div class="product-card-details">
                    <p class="text-muted small mb-1">SKU: ${product.sku}</p>
                    <p class="text-muted small mb-2">Stock: ${product.stock}</p>
                    <button class="btn btn-sm btn-primary btn-block add-to-cart-modal" data-id="${product.id}">
                        <i class="fas fa-plus"></i> Add to Cart
                    </button>
                </div>
            </div>
        `;
        
        grid.appendChild(productCard);
    });
    
    // Re-add event listeners
    document.querySelectorAll('.add-to-cart-modal').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-id');
            addToCart(productId);
            
            const modal = document.getElementById('browseProductsModal');
            if (modal) {
                modal.remove();
                document.body.style.overflow = 'auto';
            }
        });
    });
}

function addToCart(productId) {
    const products = window.saleProducts;
    const product = products.find(p => p.id == productId);
    
    if (!product) {
        showNotification('Product not found!', 'error');
        return;
    }
    
    // Check stock
    if (product.stock <= 0) {
        showNotification('Product is out of stock!', 'error');
        return;
    }
    
    // Get current cart
    let cart = JSON.parse(localStorage.getItem('currentCart')) || [];
    
    // Check if product already in cart
    const existingItem = cart.find(item => item.productId == productId);
    
    if (existingItem) {
        // Check if we have enough stock
        if (existingItem.quantity >= product.stock) {
            showNotification(`Only ${product.stock} units available in stock!`, 'warning');
            return;
        }
        existingItem.quantity += 1;
    } else {
        // Add new item to cart
        cart.push({
            productId: product.id,
            name: product.name,
            price: product.price,
            quantity: 1,
            sku: product.sku
        });
    }
    
    // Save cart to localStorage
    localStorage.setItem('currentCart', JSON.stringify(cart));
    
    // Update cart display
    updateCartDisplay(cart);
    
    // Update totals
    updateTotals();
    
    showNotification(`${product.name} added to cart!`, 'success');
}

function updateCartDisplay(cart) {
    const cartItems = document.getElementById('cartItems');
    const emptyCartMessage = document.getElementById('emptyCartMessage');
    
    if (!cartItems || !emptyCartMessage) return;
    
    cartItems.innerHTML = '';
    
    if (cart.length === 0) {
        emptyCartMessage.style.display = '';
        return;
    }
    
    emptyCartMessage.style.display = 'none';
    
    cart.forEach((item, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <div>
                    <strong>${item.name}</strong>
                    <p class="mb-0 text-muted small">SKU: ${item.sku}</p>
                </div>
            </td>
            <td>${App.formatCurrency(item.price)}</td>
            <td>
                <div class="cart-item-quantity">
                    <button class="quantity-btn decrease" data-index="${index}">
                        <i class="fas fa-minus"></i>
                    </button>
                    <input type="number" class="quantity-input" value="${item.quantity}" min="1" data-index="${index}">
                    <button class="quantity-btn increase" data-index="${index}">
                        <i class="fas fa-plus"></i>
                    </button>
                </div>
            </td>
            <td>${App.formatCurrency(item.price * item.quantity)}</td>
            <td>
                <button class="remove-item" data-index="${index}">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        
        cartItems.appendChild(row);
    });
    
    // Add event listeners
    document.querySelectorAll('.decrease').forEach(button => {
        button.addEventListener('click', function() {
            const index = parseInt(this.getAttribute('data-index'));
            updateCartQuantity(index, -1);
        });
    });
    
    document.querySelectorAll('.increase').forEach(button => {
        button.addEventListener('click', function() {
            const index = parseInt(this.getAttribute('data-index'));
            updateCartQuantity(index, 1);
        });
    });
    
    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', function() {
            const index = parseInt(this.getAttribute('data-index'));
            const newQuantity = parseInt(this.value) || 1;
            updateCartQuantity(index, 0, newQuantity);
        });
    });
    
    document.querySelectorAll('.remove-item').forEach(button => {
        button.addEventListener('click', function() {
            const index = parseInt(this.getAttribute('data-index'));
            removeFromCart(index);
        });
    });
}

function updateCartQuantity(index, change, newQuantity = null) {
    let cart = JSON.parse(localStorage.getItem('currentCart')) || [];
    
    if (index < 0 || index >= cart.length) return;
    
    const item = cart[index];
    const product = window.saleProducts.find(p => p.id == item.productId);
    
    if (!product) {
        showNotification('Product not found!', 'error');
        return;
    }
    
    let newQty;
    if (newQuantity !== null) {
        newQty = newQuantity;
    } else {
        newQty = item.quantity + change;
    }
    
    // Validate quantity
    if (newQty < 1) {
        newQty = 1;
    }
    
    // Check stock
    if (newQty > product.stock) {
        showNotification(`Only ${product.stock} units available in stock!`, 'warning');
        newQty = product.stock;
    }
    
    cart[index].quantity = newQty;
    
    // Save and update
    localStorage.setItem('currentCart', JSON.stringify(cart));
    updateCartDisplay(cart);
    updateTotals();
}

function removeFromCart(index) {
    let cart = JSON.parse(localStorage.getItem('currentCart')) || [];
    
    if (index < 0 || index >= cart.length) return;
    
    const itemName = cart[index].name;
    cart.splice(index, 1);
    
    localStorage.setItem('currentCart', JSON.stringify(cart));
    updateCartDisplay(cart);
    updateTotals();
    
    showNotification(`${itemName} removed from cart!`, 'info');
}

function updateTotals() {
    const cart = JSON.parse(localStorage.getItem('currentCart')) || [];
    
    // Calculate subtotal
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    
    // Calculate tax (8%)
    const taxRate = 0.08;
    const taxAmount = subtotal * taxRate;
    
    // Calculate discount
    const discountPercent = parseFloat(document.getElementById('discount').value) || 0;
    const discountAmount = subtotal * (discountPercent / 100);
    
    // Calculate total
    const total = subtotal + taxAmount - discountAmount;
    
    // Update UI
    document.getElementById('subtotal').textContent = App.formatCurrency(subtotal);
    document.getElementById('taxAmount').textContent = App.formatCurrency(taxAmount);
    document.getElementById('discountAmount').textContent = `-${App.formatCurrency(discountAmount)}`;
    document.getElementById('totalAmount').textContent = App.formatCurrency(total);
    
    // Update change calculation if cash payment
    if (document.querySelector('input[name="paymentMethod"]:checked').value === 'cash') {
        updateChangeAmount();
    }
}

function updateCustomerDetails() {
    const customerId = document.getElementById('customerSelect').value;
    const customers = App.getCustomers();
    const customer = customers.find(c => c.id == customerId);
    
    // In a real app, you might update customer details display here
    // For now, we'll just handle discount for VIP customers
    const discountInput = document.getElementById('discount');
    
    if (customer && customer.type === 'vip') {
        discountInput.value = 10; // 10% discount for VIP
        showNotification('VIP discount applied: 10%', 'success');
    } else {
        discountInput.value = 0;
    }
    
    updateTotals();
}

function updatePaymentSection(method) {
    const cashSection = document.getElementById('cashPaymentSection');
    
    if (method === 'cash') {
        cashSection.style.display = 'block';
        updateChangeAmount();
    } else {
        cashSection.style.display = 'none';
    }
}

function updateChangeAmount() {
    const total = parseFloat(document.getElementById('totalAmount').textContent.replace(/[^0-9.-]+/g, ''));
    const amountTendered = parseFloat(document.getElementById('amountTendered').value) || 0;
    const change = amountTendered - total;
    
    document.getElementById('changeAmount').textContent = App.formatCurrency(Math.max(0, change));
}

function cancelSale() {
    if (!confirm('Are you sure you want to cancel this sale? All items in the cart will be removed.')) {
        return;
    }
    
    // Clear cart
    localStorage.removeItem('currentCart');
    
    // Reset form
    document.getElementById('customerSelect').value = '';
    document.getElementById('discount').value = 0;
    document.querySelector('input[name="paymentMethod"][value="cash"]').checked = true;
    document.getElementById('amountTendered').value = '';
    updatePaymentSection('cash');
    
    // Update display
    updateCartDisplay([]);
    updateTotals();
    
    // Generate new sale ID
    generateSaleId();
    updateSaleDate();
    
    showNotification('Sale cancelled. Cart cleared.', 'info');
}

function completeSale() {
    const cart = JSON.parse(localStorage.getItem('currentCart')) || [];
    
    if (cart.length === 0) {
        showNotification('Cart is empty. Add products before completing sale.', 'warning');
        return;
    }
    
    // Validate cash payment
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
    
    if (paymentMethod === 'cash') {
        const total = parseFloat(document.getElementById('totalAmount').textContent.replace(/[^0-9.-]+/g, ''));
        const amountTendered = parseFloat(document.getElementById('amountTendered').value) || 0;
        
        if (amountTendered < total) {
            showNotification(`Amount tendered (${App.formatCurrency(amountTendered)}) is less than total (${App.formatCurrency(total)}).`, 'error');
            return;
        }
    }
    
    // Get customer info
    const customerId = document.getElementById('customerSelect').value;
    const customers = App.getCustomers();
    const customer = customers.find(c => c.id == customerId);
    
    // Create sale record
    const sale = {
        id: document.getElementById('saleId').textContent,
        date: new Date().toISOString(),
        customerId: customerId || null,
        customerName: customer ? `${customer.firstName} ${customer.lastName}` : 'Walk-in Customer',
        items: cart.map(item => ({
            productId: item.productId,
            name: item.name,
            quantity: item.quantity,
            price: item.price,
            sku: item.sku
        })),
        subtotal: parseFloat(document.getElementById('subtotal').textContent.replace(/[^0-9.-]+/g, '')),
        tax: parseFloat(document.getElementById('taxAmount').textContent.replace(/[^0-9.-]+/g, '')),
        discount: parseFloat(document.getElementById('discountAmount').textContent.replace(/[^0-9.-]+/g, '').replace('-', '')),
        total: parseFloat(document.getElementById('totalAmount').textContent.replace(/[^0-9.-]+/g, '')),
        paymentMethod: paymentMethod,
        status: 'completed'
    };
    
    // Update inventory
    updateInventoryForSale(cart);
    
    // Update customer statistics if applicable
    if (customerId) {
        updateCustomerStatistics(customerId, sale.total);
    }
    
    // Save sale to history
    saveSaleToHistory(sale);
    
    // Show receipt
    showReceipt(sale);
    
    // Reset for next sale
    localStorage.removeItem('currentCart');
    generateSaleId();
    updateSaleDate();
    updateCartDisplay([]);
    updateTotals();
}

function updateInventoryForSale(cart) {
    const products = App.getProducts();
    
    cart.forEach(cartItem => {
        const productIndex = products.findIndex(p => p.id == cartItem.productId);
        if (productIndex !== -1) {
            products[productIndex].stock -= cartItem.quantity;
            
            // Update status based on new stock level
            if (products[productIndex].stock === 0) {
                products[productIndex].status = 'Out of Stock';
            } else if (products[productIndex].stock <= products[productIndex].threshold) {
                products[productIndex].status = 'Low Stock';
            } else {
                products[productIndex].status = 'In Stock';
            }
        }
    });
    
    App.saveProducts(products);
}

function updateCustomerStatistics(customerId, amount) {
    const customers = App.getCustomers();
    const customerIndex = customers.findIndex(c => c.id == customerId);
    
    if (customerIndex !== -1) {
        customers[customerIndex].totalSpent += amount;
        customers[customerIndex].totalOrders += 1;
        customers[customerIndex].lastPurchase = new Date().toISOString().split('T')[0];
        
        App.saveCustomers(customers);
    }
}

function saveSaleToHistory(sale) {
    const sales = App.getSales();
    sales.push(sale);
    App.saveSales(sales);
}

function showReceipt(sale) {
    // Update receipt details
    document.getElementById('receiptId').textContent = sale.id;
    document.getElementById('receiptDate').textContent = new Date(sale.date).toLocaleString();
    document.getElementById('receiptCustomer').textContent = sale.customerName;
    
    // Update receipt items
    const receiptItems = document.getElementById('receiptItems');
    receiptItems.innerHTML = '';
    
    sale.items.forEach(item => {
        const itemElement = document.createElement('div');
        itemElement.className = 'receipt-item';
        itemElement.innerHTML = `
            <span class="item-name">${item.name} x${item.quantity}</span>
            <span class="item-qty"></span>
            <span class="item-total">${App.formatCurrency(item.price * item.quantity)}</span>
        `;
        receiptItems.appendChild(itemElement);
    });
    
    // Update receipt totals
    document.getElementById('receiptSubtotal').textContent = App.formatCurrency(sale.subtotal);
    document.getElementById('receiptTax').textContent = App.formatCurrency(sale.tax);
    document.getElementById('receiptDiscount').textContent = App.formatCurrency(sale.discount);
    document.getElementById('receiptTotal').textContent = App.formatCurrency(sale.total);
    document.getElementById('receiptPayment').textContent = sale.paymentMethod.charAt(0).toUpperCase() + sale.paymentMethod.slice(1);
    
    // Calculate change for cash payments
    if (sale.paymentMethod === 'cash') {
        const amountTendered = parseFloat(document.getElementById('amountTendered').value) || sale.total;
        const change = amountTendered - sale.total;
        document.getElementById('receiptChange').textContent = App.formatCurrency(change);
    } else {
        document.getElementById('receiptChange').textContent = App.formatCurrency(0);
    }
    
    // Open receipt modal
    App.openModal('receiptModal');
    
    // Initialize print button
    const printBtn = document.getElementById('printReceiptBtn');
    if (printBtn) {
        printBtn.addEventListener('click', function() {
            window.print();
        });
    }
}

function initializeSalesModals() {
    // New Customer Modal
    const addCustomerBtn = document.getElementById('addCustomerBtn');
    const newCustomerForm = document.getElementById('newCustomerForm');
    
    if (addCustomerBtn) {
        addCustomerBtn.addEventListener('click', function() {
            App.openModal('newCustomerModal');
        });
    }
    
    if (newCustomerForm) {
        newCustomerForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addNewCustomer();
        });
    }
}

function addNewCustomer() {
    const form = document.getElementById('newCustomerForm');
    
    const customer = {
        id: App.generateId(),
        firstName: document.getElementById('customerName').value.split(' ')[0],
        lastName: document.getElementById('customerName').value.split(' ').slice(1).join(' '),
        email: document.getElementById('customerEmail').value,
        phone: document.getElementById('customerPhone').value,
        type: document.getElementById('customerType').value,
        status: 'active',
        totalSpent: 0,
        totalOrders: 0,
        joinDate: new Date().toISOString().split('T')[0],
        lastPurchase: null
    };
    
    // Save customer
    const customers = App.getCustomers();
    customers.push(customer);
    App.saveCustomers(customers);
    
    // Close modal and reset form
    App.closeModal(document.getElementById('newCustomerModal'));
    form.reset();
    
    // Reload customer dropdown
    loadCustomers();
    
    // Select the new customer
    document.getElementById('customerSelect').value = customer.id;
    updateCustomerDetails();
    
    showNotification('Customer added successfully!', 'success');
}

function initializeSalesHistory() {
    const viewHistoryBtn = document.getElementById('viewHistoryBtn');
    const saleFormContainer = document.getElementById('saleFormContainer');
    const salesHistoryContainer = document.getElementById('salesHistoryContainer');
    
    if (viewHistoryBtn && saleFormContainer && salesHistoryContainer) {
        viewHistoryBtn.addEventListener('click', function() {
            if (salesHistoryContainer.style.display === 'none') {
                // Switch to history view
                saleFormContainer.style.display = 'none';
                salesHistoryContainer.style.display = 'block';
                this.innerHTML = '<i class="fas fa-cash-register"></i> New Sale';
                loadSalesHistory();
            } else {
                // Switch to new sale view
                saleFormContainer.style.display = 'block';
                salesHistoryContainer.style.display = 'none';
                this.innerHTML = '<i class="fas fa-history"></i> View History';
            }
        });
    }
    
    // Initialize date filters
    const startDate = document.getElementById('salesStartDate');
    const endDate = document.getElementById('salesEndDate');
    
    if (startDate && endDate) {
        // Set default dates (last 30 days)
        const today = new Date();
        const lastMonth = new Date(today);
        lastMonth.setDate(today.getDate() - 30);
        
        startDate.valueAsDate = lastMonth;
        endDate.valueAsDate = today;
        
        // Add change event listeners
        startDate.addEventListener('change', loadSalesHistory);
        endDate.addEventListener('change', loadSalesHistory);
    }
    
    // Export button
    const exportBtn = document.getElementById('exportSalesBtn');
    if (exportBtn) {
        exportBtn.addEventListener('click', exportSalesHistory);
    }
}

function loadSalesHistory() {
    const sales = App.getSales();
    const tbody = document.querySelector('#salesHistoryTable tbody');
    
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    // Get date filters
    const startDate = document.getElementById('salesStartDate')?.value;
    const endDate = document.getElementById('salesEndDate')?.value;
    
    // Filter sales by date
    let filteredSales = sales;
    
    if (startDate && endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999); // End of day
        
        filteredSales = sales.filter(sale => {
            const saleDate = new Date(sale.date);
            return saleDate >= start && saleDate <= end;
        });
    }
    
    // Sort by date (newest first)
    filteredSales.sort((a, b) => new Date(b.date) - new Date(a.date));
    
    if (filteredSales.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center">
                    <div class="empty-state">
                        <i class="fas fa-receipt"></i>
                        <h4>No Sales Found</h4>
                        <p>No sales recorded for the selected period.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    filteredSales.forEach(sale => {
        const row = document.createElement('tr');
        
        // Calculate total items
        const totalItems = sale.items.reduce((sum, item) => sum + item.quantity, 0);
        
        // Format date
        const saleDate = new Date(sale.date);
        const formattedDate = saleDate.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
        
        row.innerHTML = `
            <td>${sale.id}</td>
            <td>${formattedDate}</td>
            <td>${sale.customerName}</td>
            <td>${totalItems} items</td>
            <td>${App.formatCurrency(sale.total)}</td>
            <td>${sale.paymentMethod.charAt(0).toUpperCase() + sale.paymentMethod.slice(1)}</td>
            <td>
                <span class="status-badge status-completed">Completed</span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn view" title="View Details" data-id="${sale.id}">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-btn delete" title="Delete Sale" data-id="${sale.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        
        tbody.appendChild(row);
    });
    
    // Add event listeners
    document.querySelectorAll('.action-btn.view').forEach(button => {
        button.addEventListener('click', function() {
            const saleId = this.getAttribute('data-id');
            viewSaleDetails(saleId);
        });
    });
    
    document.querySelectorAll('.action-btn.delete').forEach(button => {
        button.addEventListener('click', function() {
            const saleId = this.getAttribute('data-id');
            deleteSale(saleId);
        });
    });
}

function viewSaleDetails(saleId) {
    const sales = App.getSales();
    const sale = sales.find(s => s.id === saleId);
    
    if (!sale) {
        showNotification('Sale not found!', 'error');
        return;
    }
    
    // Create modal with sale details
    const modalHtml = `
        <div class="modal active" id="saleDetailsModal">
            <div class="modal-content large">
                <div class="modal-header">
                    <h3>Sale Details - ${sale.id}</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="sale-details">
                        <div class="sale-info-grid">
                            <div class="info-group">
                                <label>Date</label>
                                <p>${new Date(sale.date).toLocaleString()}</p>
                            </div>
                            <div class="info-group">
                                <label>Customer</label>
                                <p>${sale.customerName}</p>
                            </div>
                            <div class="info-group">
                                <label>Payment Method</label>
                                <p>${sale.paymentMethod.charAt(0).toUpperCase() + sale.paymentMethod.slice(1)}</p>
                            </div>
                            <div class="info-group">
                                <label>Status</label>
                                <p><span class="status-badge status-completed">Completed</span></p>
                            </div>
                        </div>
                        
                        <div class="sale-items">
                            <h4>Items</h4>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Product</th>
                                        <th>SKU</th>
                                        <th>Quantity</th>
                                        <th>Price</th>
                                        <th>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${sale.items.map(item => `
                                        <tr>
                                            <td>${item.name}</td>
                                            <td>${item.sku}</td>
                                            <td>${item.quantity}</td>
                                            <td>${App.formatCurrency(item.price)}</td>
                                            <td>${App.formatCurrency(item.price * item.quantity)}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                        
                        <div class="sale-totals">
                            <div class="totals-summary">
                                <div class="totals-row">
                                    <span>Subtotal:</span>
                                    <span>${App.formatCurrency(sale.subtotal)}</span>
                                </div>
                                <div class="totals-row">
                                    <span>Tax:</span>
                                    <span>${App.formatCurrency(sale.tax)}</span>
                                </div>
                                <div class="totals-row">
                                    <span>Discount:</span>
                                    <span>${App.formatCurrency(sale.discount)}</span>
                                </div>
                                <div class="totals-row total-row">
                                    <span>Total:</span>
                                    <span>${App.formatCurrency(sale.total)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-secondary modal-close">Close</button>
                    <button class="btn btn-primary" id="reprintReceiptBtn">
                        <i class="fas fa-print"></i> Reprint Receipt
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('saleDetailsModal');
    if (existingModal) existingModal.remove();
    
    // Add new modal
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Initialize modal close
    const modal = document.getElementById('saleDetailsModal');
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
    
    // Reprint receipt button
    const reprintBtn = document.getElementById('reprintReceiptBtn');
    if (reprintBtn) {
        reprintBtn.addEventListener('click', function() {
            showReceipt(sale);
            modal.remove();
            document.body.style.overflow = 'auto';
        });
    }
}

function deleteSale(saleId) {
    if (!confirm('Are you sure you want to delete this sale record? This action cannot be undone.')) {
        return;
    }
    
    const sales = App.getSales();
    const updatedSales = sales.filter(s => s.id !== saleId);
    
    App.saveSales(updatedSales);
    loadSalesHistory();
    
    showNotification('Sale record deleted successfully!', 'success');
}

function exportSalesHistory() {
    const sales = App.getSales();
    
    if (sales.length === 0) {
        showNotification('No sales data to export.', 'warning');
        return;
    }
    
    // Create CSV content
    let csv = 'Sale ID,Date,Customer,Items,Subtotal,Tax,Discount,Total,Payment Method\n';
    
    sales.forEach(sale => {
        const totalItems = sale.items.reduce((sum, item) => sum + item.quantity, 0);
        const date = new Date(sale.date).toLocaleDateString();
        
        csv += `"${sale.id}","${date}","${sale.customerName}",${totalItems},${sale.subtotal},${sale.tax},${sale.discount},${sale.total},"${sale.paymentMethod}"\n`;
    });
    
    // Create download link
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    
    a.href = url;
    a.download = `sales_export_${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    
    showNotification('Sales data exported successfully!', 'success');
}

function showNotification(message, type = 'info') {
    // Same as in products.js - you might want to move this to a shared utility
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