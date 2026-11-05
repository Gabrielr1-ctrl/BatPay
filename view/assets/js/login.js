document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const loginEmail = document.getElementById('loginEmail');
    const loginSenha = document.getElementById('loginSenha');
    const loginBtn = document.getElementById('loginBtn');
    const loginError = document.getElementById('loginError');
    const toggleSenhaBtn = document.getElementById('toggleSenhaBtn') || document.querySelector('.input-group button');

    // Show message if redirected after successful registration
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('cadastro') === 'sucesso') {
        showToast('Cadastro realizado com sucesso! Faça login para continuar.', 'success');
    }

    // Toggle password visibility
    if (toggleSenhaBtn && loginSenha) {
        toggleSenhaBtn.addEventListener('click', () => {
            const isPassword = loginSenha.getAttribute('type') === 'password';
            loginSenha.setAttribute('type', isPassword ? 'text' : 'password');
            const icon = toggleSenhaBtn.querySelector('i');
            if (icon) {
                icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
            }
        });
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            // Clear previous errors
            if (loginError) {
                loginError.classList.add('d-none');
                loginError.textContent = '';
            }

            const email = loginEmail ? loginEmail.value.trim() : '';
            const senha = loginSenha ? loginSenha.value : '';

            if (!email || !senha) {
                if (loginError) {
                    loginError.textContent = 'Por favor, preencha todos os campos.';
                    loginError.classList.remove('d-none');
                }
                return;
            }

            const originalBtnText = loginBtn ? loginBtn.innerHTML : 'Entrar na Plataforma';
            if (loginBtn) {
                loginBtn.disabled = true;
                loginBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Entrando...';
            }

            try {
                const response = await apiRequest('/usuarios/login', 'POST', { email, senha });

                // If backend returns user object or token
                const userData = response || { email, nome: email.split('@')[0] };
                localStorage.setItem('batpay_user', JSON.stringify(userData));

                showToast('Login realizado com sucesso!', 'success');
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 500);
            } catch (error) {
                if (loginError) {
                    loginError.textContent = error.message || 'Falha ao autenticar. Verifique suas credenciais.';
                    loginError.classList.remove('d-none');
                }
            } finally {
                if (loginBtn) {
                    loginBtn.disabled = false;
                    loginBtn.innerHTML = originalBtnText;
                }
            }
        });
    }
});
