// Groq (api.groq.com) — OpenAI-compatible. Chỉ cần điền GROQ_API_KEY trong .env.
// Base URL + model đã set mặc định cho Groq; muốn đổi model chỉ cần đặt GROQ_MODEL.
const GROQ_BASE_URL = process.env.GROQ_BASE_URL || 'https://api.groq.com/openai/v1';
const GROQ_MODEL = process.env.GROQ_MODEL || 'llama-3.3-70b-versatile';
const GROQ_API_KEY = process.env.GROQ_API_KEY;

/**
 * Client gọi Groq (api.groq.com) qua endpoint OpenAI-compatible.
 *
 * Tách riêng làm 1 service để: đổi model / nhà cung cấp chỉ sửa 1 chỗ; các prompt
 * và orchestrator không cần biết chi tiết HTTP. Dùng fetch sẵn của Node 18+ (không SDK).
 *
 * 2 hàm chính:
 *   - chatText(): trả về chuỗi text (dùng cho RESPOND, TITLE).
 *   - chatJSON(): ép model trả JSON, parse sẵn rồi trả object (dùng cho EXTRACT, VERIFY).
 */

const DEFAULT_TIMEOUT_MS = 30000;

/**
 * Gọi Groq chat completions.
 * @param {Array<{role:string, content:string}>} messages
 * @param {object} opts - { temperature, maxTokens, jsonMode, timeoutMs }
 * @returns {Promise<string>} nội dung text của message đầu tiên
 */
async function callGroq(messages, opts = {}) {
    if (!GROQ_API_KEY) {
        throw new Error('GROQ_API_KEY chưa được cấu hình trong .env');
    }

    const {
        temperature = 0.7,
        maxTokens = 1024,
        jsonMode = false,
        timeoutMs = DEFAULT_TIMEOUT_MS
    } = opts;

    const body = {
        model: GROQ_MODEL,
        messages,
        temperature,
        max_tokens: maxTokens
    };
    if (jsonMode) {
        body.response_format = { type: 'json_object' };
    }

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), timeoutMs);

    try {
        const res = await fetch(`${GROQ_BASE_URL}/chat/completions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${GROQ_API_KEY}`
            },
            body: JSON.stringify(body),
            signal: controller.signal
        });

        if (!res.ok) {
            const errText = await res.text().catch(() => '');
            throw new Error(`Groq API ${res.status}: ${errText.slice(0, 300)}`);
        }

        const data = await res.json();
        const content = data?.choices?.[0]?.message?.content;
        if (content == null) {
            throw new Error('Groq trả về rỗng (không có choices[0].message.content)');
        }
        return content;
    } catch (err) {
        if (err.name === 'AbortError') {
            throw new Error(`Groq API timeout sau ${timeoutMs}ms`);
        }
        throw err;
    } finally {
        clearTimeout(timeout);
    }
}

/**
 * Gọi Groq trả về text thuần.
 * @param {string} systemPrompt
 * @param {Array<{role:string, content:string}>} history - các message trước (có thể rỗng)
 * @param {string} userInput - text user vừa nhập (nếu cần append riêng)
 */
async function chatText(systemPrompt, history = [], userInput = null, opts = {}) {
    const messages = [{ role: 'system', content: systemPrompt }, ...history];
    if (userInput != null) {
        messages.push({ role: 'user', content: userInput });
    }
    return callGroq(messages, { ...opts, jsonMode: false });
}

/**
 * Gọi Groq ép trả JSON, parse sẵn. Nếu parse lỗi → throw (caller xử lý fallback).
 * Temperature thấp mặc định để output ổn định cho các bước phân tích.
 */
async function chatJSON(systemPrompt, history = [], userInput = null, opts = {}) {
    const messages = [{ role: 'system', content: systemPrompt }, ...history];
    if (userInput != null) {
        messages.push({ role: 'user', content: userInput });
    }
    const raw = await callGroq(messages, {
        temperature: 0.2,
        ...opts,
        jsonMode: true
    });

    try {
        return JSON.parse(raw);
    } catch (e) {
        // Một số model bọc JSON trong ```json ... ``` dù đã bật json_mode — gỡ rồi parse lại.
        const cleaned = raw.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, '').trim();
        try {
            return JSON.parse(cleaned);
        } catch (e2) {
            throw new Error(`Groq không trả JSON hợp lệ: ${raw.slice(0, 200)}`);
        }
    }
}

/** Kiểm tra đã cấu hình key chưa (cho health-check / log khi khởi động). */
function isConfigured() {
    return Boolean(GROQ_API_KEY);
}

module.exports = { chatText, chatJSON, isConfigured, GROQ_MODEL };
