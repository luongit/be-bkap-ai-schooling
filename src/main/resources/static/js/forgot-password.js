// Toggle mat khau
function setupPasswordToggle(passwordId, toggleId) {
  document.getElementById(toggleId).addEventListener('click', function () {
    const input = document.getElementById(passwordId);
    if (input.type === 'password') {
      input.type = 'text';
      this.classList.replace('fa-eye', 'fa-eye-slash');
    } else {
      input.type = 'password';
      this.classList.replace('fa-eye-slash', 'fa-eye');
    }
  });
}
setupPasswordToggle('newPassword', 'newPasswordToggle');
setupPasswordToggle('confirmPassword', 'confirmPasswordToggle');

// Toast helper
function showToast(msg, type = 'success') {
  let bg = type === 'error'
    ? 'linear-gradient(135deg,#f43f5e,#e11d48)'
    : type === 'warning'
      ? 'linear-gradient(135deg,#f59e0b,#d97706)'
      : 'linear-gradient(135deg,#2563eb,#1d4ed8)';
  Toastify({ text: msg, duration: 3000, gravity: "top", position: "right", style: { background: bg } }).showToast();
}

function goToStep(n) { document.querySelectorAll('.step').forEach(s => s.classList.remove('active')); document.getElementById('step' + n).classList.add('active'); }

// Send OTP
document.getElementById('sendOtpBtn').addEventListener('click', async function () {
  const email = document.getElementById('email').value.trim();
  if (!email) return showToast('Vui lòng nhập email', 'warning');
  this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...'; this.disabled = true;
  try {
    const res = await fetch('/api/auth/forgot-password', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email })
    });
    if (!res.ok) throw new Error((await res.json()).message || 'Không thể gửi OTP');
    showToast('Mã xác nhận đã gửi đến email!');
    goToStep(2);
  } catch (e) { showToast(e.message, 'error'); }
  this.innerHTML = 'Gửi mã xác nhận'; this.disabled = false;
});

// Xac thuc OTP
document.getElementById('verifyOtpBtn').addEventListener('click', async function () {
  const otp = document.getElementById('otp').value.trim();
  if (!otp) return showToast('Vui lòng nhập mã', 'warning');
  this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xác nhận...'; this.disabled = true;
  try {
    const res = await fetch('/api/auth/verify-otp', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ token: otp })
    });
    if (!res.ok) throw new Error((await res.json()).message || 'OTP không hợp lệ');
    showToast('Xác nhận thành công, hãy đặt mật khẩu mới!');
    goToStep(3);
  } catch (e) { showToast(e.message, 'error'); }
  this.innerHTML = 'Xác nhận mã'; this.disabled = false;
});

// Reset pass
document.getElementById('resetPasswordBtn').addEventListener('click', async function () {
  const newPw = document.getElementById('newPassword').value, cf = document.getElementById('confirmPassword').value, otp = document.getElementById('otp').value;
  if (!newPw || !cf) return showToast('Điền đầy đủ thông tin', 'warning');
  if (newPw !== cf) return showToast('Mật khẩu không khớp', 'error');
  this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đặt lại...'; this.disabled = true;
  try {
    const res = await fetch('/api/auth/reset-password', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ token: otp, newPassword: newPw })
    });
    if (!res.ok) throw new Error((await res.json()).message || 'Không thể đặt lại mật khẩu');
    showToast('Đặt lại mật khẩu thành công! Đang chuyển hướng...');
    setTimeout(() => window.location.href = '/auth/login', 1500);
  } catch (e) { showToast(e.message, 'error'); this.innerHTML = 'Đặt lại mật khẩu'; this.disabled = false; }
});

// Enter send submit
document.addEventListener('keypress', e => {
  if (e.key === 'Enter') {
    if (document.getElementById('step1').classList.contains('active')) document.getElementById('sendOtpBtn').click();
    else if (document.getElementById('step2').classList.contains('active')) document.getElementById('verifyOtpBtn').click();
    else if (document.getElementById('step3').classList.contains('active')) document.getElementById('resetPasswordBtn').click();
  }
});