// Gộp 6 số OTP thành 1 chuỗi và gọi API
    document
      .getElementById("otpForm")
      .addEventListener("submit", async (e) => {
        e.preventDefault();
        const inputs = document.querySelectorAll(".otp-input");
        let otp = "";
        inputs.forEach((input) => (otp += input.value));

        try {
          const response = await fetch(
            "http://localhost:8080/api/auth/verify-otp",
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify({ token: otp }),
            }
          );

          const result = await response.json();
          if (response.ok) {
            alert("OTP hợp lệ! Chuyển sang bước đặt lại mật khẩu.");
            // Redirect hoặc render form reset password
            window.location.href = "/reset-password?token=" + otp;
          } else {
            alert(result.message || "OTP không hợp lệ");
          }
        } catch (error) {
          alert("Lỗi: " + error.message);
        }
      });

    // Auto focus khi nhập
    const inputs = document.querySelectorAll(".otp-input");
    inputs.forEach((input, idx) => {
      input.addEventListener("keyup", (e) => {
        if (e.key >= 0 && e.key <= 9) {
          if (idx < inputs.length - 1) inputs[idx + 1].focus();
        } else if (e.key === "Backspace" && idx > 0) {
          inputs[idx - 1].focus();
        }
      });
    });

    // Đếm ngược hết hạn OTP (120 giây để đồng bộ với backend)
    let countdown = 120;
    let countdownEl = document.getElementById("countdown");
    let timer = setInterval(() => {
      countdown--;
      countdownEl.textContent = `Mã OTP sẽ hết hạn sau ${countdown} giây`;
      if (countdown <= 0) {
        clearInterval(timer);
        countdownEl.textContent =
          "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.";
      }
    }, 1000);