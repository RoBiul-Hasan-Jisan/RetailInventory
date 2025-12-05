// Main Application JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the login page
    if (document.body.classList.contains('login-page')) {
        initializeLoginPage();
    } else {
        initializeApp();
    }
});

// Login Page Functions
function initializeLoginPage() {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const remember = document.getElementById('remember').checked;
            
            // Simple authentication (for demo purposes)
            if (username === 'admin' && password === 'password123') {
                // Store login state in localStorage
                localStorage.setItem('isLoggedIn', 'true');
                localStorage.setItem('username', username);
                
                if (remember) {
                    localStorage.setItem('rememberMe', 'true');
                }
                
                // Redirect to dashboard
                window.location.href = 'dashboard.html';
            } else {
                alert('Invalid credentials. Use admin / password123 for demo.');
            }
        });
    }
    
    // Pre-fill demo credentials for convenience
    document.getElementById('username').value = 'admin';
    document.getElementById('password').value = 'password123';
}

// Main Application Functions
function initializeApp() {
    // Check authentication
    if (!isAuthenticated()) {
        window.location.href = 'index.html';
        return;
    }
    
    // Initialize common components
    initializeSidebar();
    initializeModals();
    initializeDateDisplay();
    
    // Set current page as active in sidebar
    setActiveNavItem();
    
    // Load sample data if needed
    initializeSampleData();
}

// Authentication check
function isAuthenticated() {
    return localStorage.getItem('isLoggedIn') === 'true';
}

// Sidebar functionality
function initializeSidebar() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.querySelector('.sidebar');
    
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
    }
    
    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', function(event) {
        if (window.innerWidth <= 992) {
            if (!sidebar.contains(event.target) && !sidebarToggle.contains(event.target)) {
                sidebar.classList.remove('active');
            }
        }
    });
}

// Modal functionality
function initializeModals() {
    // Close modals when clicking X
    document.querySelectorAll('.modal-close').forEach(button => {
        button.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) {
                closeModal(modal);
            }
        });
    });
    
    // Close modals when clicking outside
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal(this);
            }
        });
    });
    
    // Close modal with Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal').forEach(modal => {
                if (modal.classList.contains('active')) {
                    closeModal(modal);
                }
            });
        }
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modal) {
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
}

// Set active navigation item
function setActiveNavItem() {
    const currentPage = window.location.pathname.split('/').pop();
    const navItems = document.querySelectorAll('.sidebar-menu a');
    
    navItems.forEach(item => {
        const href = item.getAttribute('href');
        if (href === currentPage) {
            item.parentElement.classList.add('active');
        } else {
            item.parentElement.classList.remove('active');
        }
    });
}

// Initialize date display
function initializeDateDisplay() {
    const dateElements = document.querySelectorAll('#currentDate, #saleDate');
    
    if (dateElements.length > 0) {
        const now = new Date();
        const formattedDate = now.toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        
        dateElements.forEach(element => {
            element.textContent = formattedDate;
        });
    }
}

// Sample data initialization
function initializeSampleData() {
    // Check if sample data exists
    if (!localStorage.getItem('sampleDataInitialized')) {
        // Initialize products
        const sampleProducts = [
            {
                id: 1,
                name: "Wireless Bluetooth Headphones",
                sku: "ELEC-001",
                category: "Electronics",
                brand: "SoundMax",
                price: 89.99,
                cost: 45.00,
                stock: 42,
                threshold: 10,
                status: "In Stock",
                description: "Noise-cancelling wireless headphones with 30-hour battery life."
            },
            {
                id: 2,
                name: "Men's Running Shoes",
                sku: "CLOTH-001",
                category: "Clothing",
                brand: "RunPro",
                price: 129.99,
                cost: 65.00,
                stock: 8,
                threshold: 10,
                status: "Low Stock",
                description: "Lightweight running shoes with breathable mesh upper."
            },
            {
                id: 3,
                name: "Ceramic Coffee Mug Set",
                sku: "HOME-001",
                category: "Home & Garden",
                brand: "HomeEssentials",
                price: 24.99,
                cost: 12.00,
                stock: 0,
                threshold: 5,
                status: "Out of Stock",
                description: "Set of 4 ceramic mugs with modern design."
            },
            {
                id: 4,
                name: "JavaScript: The Definitive Guide",
                sku: "BOOK-001",
                category: "Books",
                brand: "O'Reilly",
                price: 49.99,
                cost: 25.00,
                stock: 25,
                threshold: 5,
                status: "In Stock",
                description: "Comprehensive guide to JavaScript programming."
            },
            {
                id: 5,
                name: "Robot Building Kit",
                sku: "TOY-001",
                category: "Toys",
                brand: "TechToys",
                price: 59.99,
                cost: 30.00,
                stock: 15,
                threshold: 5,
                status: "In Stock",
                description: "Educational STEM robot building kit for kids."
            }
        ];
        
        // Initialize customers
        const sampleCustomers = [
            {
                id: 1,
                firstName: "John",
                lastName: "Smith",
                email: "john.smith@email.com",
                phone: "(555) 123-4567",
                address: "123 Main Street, Anytown, USA 12345",
                type: "vip",
                status: "active",
                totalSpent: 2450.75,
                totalOrders: 24,
                joinDate: "2023-01-15",
                lastPurchase: "2023-10-28"
            },
            {
                id: 2,
                firstName: "Sarah",
                lastName: "Johnson",
                email: "sarah.j@email.com",
                phone: "(555) 234-5678",
                address: "456 Oak Avenue, Somewhere, USA 23456",
                type: "regular",
                status: "active",
                totalSpent: 875.50,
                totalOrders: 8,
                joinDate: "2023-03-22",
                lastPurchase: "2023-10-25"
            },
            {
                id: 3,
                firstName: "Mike",
                lastName: "Brown",
                email: "mike.brown@email.com",
                phone: "(555) 345-6789",
                address: "789 Pine Road, Anycity, USA 34567",
                type: "wholesale",
                status: "active",
                totalSpent: 5420.25,
                totalOrders: 12,
                joinDate: "2023-02-10",
                lastPurchase: "2023-10-20"
            }
        ];
        
        // Initialize sales
        const sampleSales = [
            {
                id: "TRX-00123",
                date: "2023-10-28",
                customerId: 1,
                customerName: "John Smith",
                items: [
                    { productId: 1, name: "Wireless Bluetooth Headphones", quantity: 1, price: 89.99 },
                    { productId: 4, name: "JavaScript: The Definitive Guide", quantity: 2, price: 49.99 }
                ],
                subtotal: 189.97,
                tax: 15.20,
                discount: 10.00,
                total: 195.17,
                paymentMethod: "card",
                status: "completed"
            },
            {
                id: "TRX-00124",
                date: "2023-10-25",
                customerId: 2,
                customerName: "Sarah Johnson",
                items: [
                    { productId: 2, name: "Men's Running Shoes", quantity: 1, price: 129.99 },
                    { productId: 5, name: "Robot Building Kit", quantity: 1, price: 59.99 }
                ],
                subtotal: 189.98,
                tax: 15.20,
                discount: 0,
                total: 205.18,
                paymentMethod: "cash",
                status: "completed"
            }
        ];
        
        // Save to localStorage
        localStorage.setItem('products', JSON.stringify(sampleProducts));
        localStorage.setItem('customers', JSON.stringify(sampleCustomers));
        localStorage.setItem('sales', JSON.stringify(sampleSales));
        localStorage.setItem('sampleDataInitialized', 'true');
    }
}

// Get data from localStorage
function getProducts() {
    const products = localStorage.getItem('products');
    return products ? JSON.parse(products) : [];
}

function getCustomers() {
    const customers = localStorage.getItem('customers');
    return customers ? JSON.parse(customers) : [];
}

function getSales() {
    const sales = localStorage.getItem('sales');
    return sales ? JSON.parse(sales) : [];
}

// Save data to localStorage
function saveProducts(products) {
    localStorage.setItem('products', JSON.stringify(products));
}

function saveCustomers(customers) {
    localStorage.setItem('customers', JSON.stringify(customers));
}

function saveSales(sales) {
    localStorage.setItem('sales', JSON.stringify(sales));
}

// Format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

// Generate unique ID
function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// Export functions for use in other files
window.App = {
    openModal,
    closeModal,
    formatCurrency,
    generateId,
    getProducts,
    getCustomers,
    getSales,
    saveProducts,
    saveCustomers,
    saveSales
};