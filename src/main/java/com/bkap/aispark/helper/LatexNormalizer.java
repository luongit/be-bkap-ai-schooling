package com.bkap.aispark.helper;

public class LatexNormalizer {
	/**
	 * Cố gắng lấy ra prefix “ổn định” từ buffer: - Đảm bảo không dừng giữa cặp
	 * \(...\), \[...\] hoặc giữa cặp $...$ / $$...$$ - Trong prefix an toàn: chuyển
	 * \(...\) -> $...$ ; \[...\] -> $$...$$ Trả về phần prefix đã chuẩn hoá và XÓA
	 * nó khỏi buffer gốc.
	 */
	public String tryNormalizeAndExtractStablePrefix(StringBuilder buf) {
		if (buf.length() == 0)
			return "";

		// Quy ước an toàn “nhẹ”: cut tại vị trí mà số lượng ký tự $ chưa đóng là chẵn
		// và không đang ở giữa \(...\) hay \[...\]
		String s = buf.toString();

		// Tránh cắt quá ngắn → cho phép grow tối thiểu
		if (s.length() < 64)
			return "";

		int cut = findSafeCutPosition(s);
		if (cut <= 0)
			return "";

		String stable = s.substring(0, cut);
		// chuẩn hoá delimiter trong phần stable
		stable = normalizeDelimiters(stable);

		// Xoá prefix đã emit khỏi buffer
		buf.delete(0, cut);
		return stable;
	}

	/**
	 * Flush toàn bộ phần còn lại (khi stream kết thúc). Chuẩn hoá nốt delimiter rồi
	 * trả ra.
	 */
	public String flushAll(StringBuilder buf) {
		if (buf.length() == 0)
			return "";
		String stable = normalizeDelimiters(buf.toString());
		buf.setLength(0);
		return stable;
	}

	// --- Helpers ---

	private int findSafeCutPosition(String s) {
		int dollars = 0;
		boolean inParenMath = false; // \(
		boolean inBracketMath = false; // \[

		// Duyệt và tìm vị trí “safe” gần đây (kết thúc câu hoặc xuống dòng) khi không ở
		// giữa toán
		int lastSafe = -1;

		for (int i = 0; i < s.length(); i++) {
			// đếm $ không escape
			if (s.charAt(i) == '$' && (i == 0 || s.charAt(i - 1) != '\\')) {
				dollars++;
			}

			// phát hiện \(
			if (s.charAt(i) == '(' && i > 0 && s.charAt(i - 1) == '\\') {
				inParenMath = !inParenMath; // toggle (đơn giản hoá, hợp lệ khi input không lỗi)
			}
			// phát hiện \[
			if (s.charAt(i) == '[' && i > 0 && s.charAt(i - 1) == '\\') {
				inBracketMath = !inBracketMath;
			}
			// phát hiện \) hoặc \]
			if (s.charAt(i) == ')' && i > 0 && s.charAt(i - 1) == '\\') {
				inParenMath = !inParenMath;
			}
			if (s.charAt(i) == ']' && i > 0 && s.charAt(i - 1) == '\\') {
				inBracketMath = !inBracketMath;
			}

			// chọn một số “điểm cắt đẹp”: khoảng trắng sau dấu chấm, xuống dòng, hoặc
			// khoảng trắng dài
			boolean punctuationBreak = (s.charAt(i) == '\n')
					|| (s.charAt(i) == '.' && i + 1 < s.length() && Character.isWhitespace(s.charAt(i + 1)));

			if (!inParenMath && !inBracketMath && (dollars % 2 == 0)) {
				if (punctuationBreak)
					lastSafe = i + 1;
				// Nếu không có dấu chấm, cho phép cắt ở khoảng trắng sau 200+ ký tự
				if (lastSafe < 0 && i > 200 && Character.isWhitespace(s.charAt(i))) {
					lastSafe = i + 1;
				}
			}
		}

		return lastSafe;
	}

	private String normalizeDelimiters(String stable) {
		// display: \[ ... \] -> $$ ... $$
		stable = stable.replaceAll("\\\\\\[\\s*([\\s\\S]*?)\\s*\\\\\\]", "\n$$\n$1\n$$\n");
		// inline: \( ... \) -> $ ... $
		stable = stable.replaceAll("\\\\\\(([^\\)]*?)\\\\\\)", "\\$$1\\$");

		return stable;
	}
}
