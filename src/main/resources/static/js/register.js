let selectedRole = null;

// CHỌN ROLE
function selectRole(role) {
    selectedRole = role;

    document.getElementById("step1").classList.add("hidden");
    document.getElementById("step2").classList.remove("hidden");

    document.getElementById("formTitle").innerText =
        role === "student" ? "Đăng ký học sinh" : "Đăng ký phụ huynh";

    // Reset lỗi
    document.querySelectorAll(".error").forEach(e => e.innerText = "");
    document.querySelectorAll("input").forEach(e => e.classList.remove("error-input"));

    document.getElementById("studentForm").classList.toggle("hidden", role !== "student");
    document.getElementById("parentForm").classList.toggle("hidden", role !== "parent");
}

function goBack() {
    selectedRole = null;
    document.getElementById("step1").classList.remove("hidden");
    document.getElementById("step2").classList.add("hidden");
}

function setError(id, errId, msg) {
    document.getElementById(id).classList.add("error-input");
    document.getElementById(errId).innerText = msg;
}

function clearError(id, errId) {
    document.getElementById(id).classList.remove("error-input");
    document.getElementById(errId).innerText = "";
}

// map lỗi
document.addEventListener("input", e => {
    const ids = {
        sUsername:"sUsernameErr", sFullName:"sFullNameErr",
        sBirthdate:"sBirthdateErr", sEmail:"sEmailErr", sPhone:"sPhoneErr",
        sPassword:"sPassErr", sConfirm:"sConfirmErr",

        pUsername:"pUsernameErr", pFullName:"pFullNameErr", pAddress:"pAddressErr",
        pEmail:"pEmailErr", pPhone:"pPhoneErr",
        pPassword:"pPassErr", pConfirm:"pConfirmErr"
    };

    if (ids[e.target.id]) clearError(e.target.id, ids[e.target.id]);
});

function validateNotEmpty(id, errId, msg) {
    const v = document.getElementById(id).value.trim();
    if (!v) { setError(id, errId, msg); return false; }
    return true;
}

function validateEmail(id, errId) {
    const v = document.getElementById(id).value.trim();
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!regex.test(v)) { setError(id, errId, "Email không hợp lệ"); return false; }
    return true;
}

function validatePhone(id, errId) {
    const v = document.getElementById(id).value.trim();
    const regex = /^(0|\+84)[0-9]{9}$/;

    if (!regex.test(v)) { setError(id, errId, "Số điện thoại không hợp lệ"); return false; }
    return true;
}

function validatePassword(prefix) {
    const pass = document.getElementById(prefix + "Password").value.trim();
    const confirm = document.getElementById(prefix + "Confirm").value.trim();

    if (!pass) { setError(prefix + "Password", prefix + "PassErr", "Mật khẩu bắt buộc"); return false; }
    if (pass !== confirm) { setError(prefix + "Confirm", prefix + "ConfirmErr", "Mật khẩu không khớp"); return false; }

    return true;
}

// ĐĂNG KÝ
document.getElementById("submitBtn").addEventListener("click", async () => {

    if (!selectedRole) return;

    const btn = document.getElementById("submitBtn");
    const msg = document.getElementById("message");
    const old = btn.innerHTML;

    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý đăng ký...';

    msg.innerText = "";
    msg.className = "form-msg";

    let ok = true;

    if (selectedRole === "student") {
        ok &= validateNotEmpty("sUsername","sUsernameErr","Không được để trống!");
        ok &= validateNotEmpty("sFullName","sFullNameErr","Không được để trống!");
        ok &= validateNotEmpty("sBirthdate","sBirthdateErr","Không được để trống!");
        ok &= validateNotEmpty("sEmail","sEmailErr","Không được để trống!") && validateEmail("sEmail","sEmailErr");
        ok &= validateNotEmpty("sPhone","sPhoneErr","Không được để trống!") && validatePhone("sPhone","sPhoneErr");
        ok &= validatePassword("s");

    } else {
        ok &= validateNotEmpty("pUsername","pUsernameErr","Không được để trống!");
        ok &= validateNotEmpty("pFullName","pFullNameErr","Không được để trống!");
        ok &= validateNotEmpty("pAddress","pAddressErr","Không được để trống!");
        ok &= validateNotEmpty("pEmail","pEmailErr","Không được để trống!") && validateEmail("pEmail","pEmailErr");
        ok &= validateNotEmpty("pPhone","pPhoneErr","Không được để trống!") && validatePhone("pPhone","pPhoneErr");
        ok &= validatePassword("p");
    }

    if (!ok) {
        btn.disabled = false;
        btn.innerHTML = old;
        return;
    }

    const base = window.location.origin.includes("localhost") ? "http://localhost:8080" : "https://bkapai.vn";

    const payload = selectedRole === "student"
        ? {
            username: sUsername.value.trim(),
            fullName: sFullName.value.trim(),
            birthdate: sBirthdate.value,
            email: sEmail.value.trim(),
            phone: sPhone.value.trim(),
            password: sPassword.value.trim(),
        }
        : {
            username: pUsername.value.trim(),
            fullName: pFullName.value.trim(),
            address: pAddress.value.trim(),
            email: pEmail.value.trim(),
            phone: pPhone.value.trim(),
            password: pPassword.value.trim(),
        };

    const url = selectedRole === "student"
        ? `${base}/api/auth/register/student`
        : `${base}/api/auth/register/parent`;

    try {
        const res = await fetch(url, {
            method:"POST",
            headers:{ "Content-Type":"application/json" },
            body: JSON.stringify(payload)
        });

        const text = await res.text();

        if (!res.ok) {
            msg.innerText = text;
            msg.classList.add("error");
            btn.disabled = false;
            btn.innerHTML = old;
            return;
        }

        msg.innerText = "Đăng ký tài khoản thành công!";
        msg.classList.add("success");

        setTimeout(()=>window.location.href="/auth/login",1000);

    } catch (err) {
        msg.innerText = "Lỗi: " + err.message;
        msg.classList.add("error");
    }

    btn.disabled = false;
    btn.innerHTML = old;
});
