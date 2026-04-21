const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_REGEX = /^[0-9]{10}$/;

function isValidEmail(email) {
    return EMAIL_REGEX.test(email);
}

function isValidPhone(phone) {
    return !phone || PHONE_REGEX.test(phone);
}

module.exports = { isValidEmail, isValidPhone };
