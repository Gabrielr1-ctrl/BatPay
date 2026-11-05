const API_BASE_URL = 'http://localhost:8080/api';

async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' },
    };
    if (body) options.body = JSON.stringify(body);
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Erro ${response.status}`);
    }
    
    if (response.status === 204) return null;
    
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function getUsuarioLogado() {
    const data = localStorage.getItem('batpay_user');
    return data ? JSON.parse(data) : null;
}

function requireAuth() {
    const user = getUsuarioLogado();
    if (!user) {
        window.location.href = 'login.html';
        return null;
    }
    return user;
}

function logout() {
    localStorage.removeItem('batpay_user');
    window.location.href = 'login.html';
}

function showToast(message, type = 'success') {
    // Create a Bootstrap-style toast notification
    const toastContainer = document.getElementById('toastContainer') || createToastContainer();
    const toastId = 'toast-' + Date.now();
    const bgClass = type === 'success' ? 'bg-success' : type === 'error' ? 'bg-danger' : 'bg-warning';
    const icon = type === 'success' ? 'bi-check-circle' : type === 'error' ? 'bi-x-circle' : 'bi-exclamation-circle';
    
    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center text-white ${bgClass} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body"><i class="bi ${icon} me-2"></i>${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    const toastEl = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '1100';
    document.body.appendChild(container);
    return container;
}
