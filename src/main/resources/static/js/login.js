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

    if (type === 'error') {
        backgroundColor = 'linear-gradient(135deg, #f43f5e, #e11d48 100%)';
    } else if (type === 'warning') {
        backgroundColor = 'linear-gradient(135deg, #f59e0b, #d97706 100%)';
    }

    Toastify({
        text: message,
        duration: 3000,
        gravity: "top",
        position: "right",
        backgroundColor,
        stopOnFocus: true,
    }).showToast();
}


const activated = new URLSearchParams(window.location.search).get("activated");

if (activated === "success") {
    showToast("Kích hoạt tài khoản thành công! Vui lòng đăng nhập.", "success");

    // Xoá query param
    window.history.replaceState({}, document.title, window.location.pathname);
}

if (activated === "fail") {
    showToast("Kích hoạt tài khoản thất bại! Liên hệ hỗ trợ hoặc thử lại.", "error");

    // Xoá query param
    window.history.replaceState({}, document.title, window.location.pathname);
}



// ==============================
// XỬ LÝ LOGIN FORM
// ==============================
document.getElementById('loginBtn').addEventListener('click', async function (e) {
    e.preventDefault();

    const identifier = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const rememberMe = document.getElementById("remember").checked;
    const loginBtn = this;

    if (!identifier || !password) {
        showToast('Vui lòng điền đầy đủ thông tin!', 'warning');
        return;
    }

    loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng nhập...';
    loginBtn.disabled = true;

    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: "include",
            body: JSON.stringify({ identifier, password, rememberMe })
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            showToast(data.message || 'Sai tài khoản hoặc mật khẩu', 'error');
            return;
        }

        localStorage.setItem('token', data.accessToken);
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("username", data.username);
        localStorage.setItem("user", JSON.stringify(data));

        showToast('Đăng nhập thành công!');
        setTimeout(() => window.location.href = "/", 800);

    } catch (err) {
        console.error(err);
        showToast('Lỗi đăng nhập hệ thống!', 'error');
    } finally {
        loginBtn.innerHTML = 'Đăng nhập';
        loginBtn.disabled = false;
    }
});

// ==============================
// xu ly GOOGLE LOGIN DEV/PROD
// ==============================
const isLocal = window.location.hostname === "localhost";

const googleLoginUrl = isLocal
    ? "http://localhost:8080/api/auth/google"
    : "https://bkapai.vn/api/auth/google";

document.getElementById("googleLoginBtn").addEventListener("click", function () {
    window.location.href = googleLoginUrl;
});

// ==============================
// xu ly FACEBOOK LOGIN DEV/PROD  (THÊM MỚI)
// ==============================
const facebookLoginUrl = isLocal
    ? "http://localhost:8080/api/auth/facebook"
    : "https://bkapai.vn/api/auth/facebook";

document.getElementById("facebookLoginBtn").addEventListener("click", function () {
    window.location.href = facebookLoginUrl;
});



// ==============================
// AUTO LOGIN SAU KHI GOOGLE/FACEBOOK REDIRECT
// ==============================
const params = new URLSearchParams(window.location.search);
const socialToken = params.get("token");

if (socialToken) {
    localStorage.setItem("token", socialToken);

    fetch(isLocal ? "http://localhost:8080/api/auth/me" : "https://bkapai.vn/api/auth/me", {
        headers: { "Authorization": "Bearer " + socialToken }
    })
        .then(res => res.json())
        .then(user => {
            localStorage.setItem("username", user.username);
            localStorage.setItem("email", user.email);
            localStorage.setItem("userId", user.id);
            localStorage.setItem("user", JSON.stringify(user));

            showToast("Đăng nhập thành công!");
            setTimeout(() => window.location.href = "/", 800);
        })
        .catch(err => {
            console.error(err);
            showToast("Lỗi xác thực tài khoản!", "error");
        });
}
