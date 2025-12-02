// Toggle hien thi mat khau
document.getElementById('passwordToggle').addEventListener('click', function () {
    const passwordInput = document.getElementById('password');
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        this.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        this.classList.replace('fa-eye-slash', 'fa-eye');
    }
});

// Toast thong bao
function showToast(message, type = 'success') {
    let backgroundColor = 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)';
    let className = 'toastify-custom';

    if (type === 'error') {
        backgroundColor = 'linear-gradient(135deg, #f43f5e 0%, #e11d48 100%)';
        className = 'toastify-custom-error';
    } else if (type === 'warning') {
        backgroundColor = 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)';
        className = 'toastify-custom-warning';
    }

    Toastify({
        text: message,
        duration: 3000,
        gravity: "top",
        position: "right",
        backgroundColor,
        className,
        stopOnFocus: true,
    }).showToast();
}

// Xu ly dang nhap
document.getElementById('loginBtn').addEventListener('click', async function (e) {
    e.preventDefault();

    const identifier = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const loginBtn = this;

    if (!identifier || !password) {
        showToast('Vui lòng điền đầy đủ thông tin đăng nhập!', 'warning');
        return;
    }

    loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
    loginBtn.disabled = true;

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier, password })
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            showToast(data.message || 'Sai tài khoản hoặc mật khẩu', 'error');
            throw new Error(data.message || 'Đăng nhập thất bại');
        }

        // Luu token
        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("username", data.username);
        localStorage.setItem("user", JSON.stringify(data));

        showToast('Đăng nhập thành công! Đang chuyển hướng...');

        setTimeout(() => {
            window.location.href = '/';
        }, 1500);

    } catch (err) {
        console.error('Lỗi đăng nhập:', err);
        showToast('Đăng nhập thất bại. Vui lòng thử lại.', 'error');
    } finally {
        loginBtn.innerHTML = 'Đăng nhập';
        loginBtn.disabled = false;
    }
});

// Bam Enter de login
document.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        document.getElementById('loginBtn').click();
    }
});

// Auto refresh token khi trang load
window.addEventListener('load', async () => {
    const accessToken = localStorage.getItem('token');
    const refreshToken = localStorage.getItem('refreshToken');

    if (accessToken && refreshToken) {
        try {
            const payload = JSON.parse(atob(accessToken.split('.')[1]));
            const exp = payload.exp * 1000;

            // Nếu access token sắp hết hạn (< 2 phút)
            if (Date.now() > exp - 2 * 60 * 1000) {
                const res = await fetch('/api/auth/refresh', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ refreshToken })
                });

                if (res.ok) {
                    const data = await res.json();
                    localStorage.setItem('token', data.accessToken);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    showToast('Phiên đăng nhập đã được làm mới tự động');
                } else {
                    showToast('Phiên đăng nhập hết hạn, vui lòng đăng nhập lại', 'warning');
                    localStorage.clear();
                    setTimeout(() => window.location.href = '/login', 2000);
                }
            }
        } catch (err) {
            console.error('Lỗi check/refresh token:', err);
            showToast('Lỗi khi kiểm tra token!', 'error');
            localStorage.clear();
            setTimeout(() => window.location.href = '/login', 2000);
        }
    }
});
