require('dotenv').config();
const jwt = require('jsonwebtoken');

/**
 * Generate JWT token for testing
 * Usage:
 *   node generate-test-token.js
 *   node generate-test-token.js 5 student
 *   node generate-test-token.js 21 admin
 */

// Parse command line arguments
const args = process.argv.slice(2);
const userId = args[0] ? parseInt(args[0]) : null;
const role = args[1] ? args[1].toLowerCase() : null;

const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key-here';

if (!process.env.JWT_SECRET) {
    console.log('⚠️  WARNING: JWT_SECRET not found in .env, using default key\n');
}

/**
 * Generate token for specific user
 */
function generateToken(uid, email, userRole, expiresIn = '30d') {
    return jwt.sign(
        { userId: uid, email, role: userRole },
        JWT_SECRET,
        { expiresIn }
    );
}

// If specific user requested
if (userId && role) {
    const email = role === 'admin' ? `admin${userId}@uni.edu.vn` : `user${userId}@gmail.com`;
    const token = generateToken(userId, email, role);
    
    console.log(`✅ ${role.toUpperCase()} TOKEN (user_id = ${userId})`);
    console.log(`Bearer ${token}\n`);
    process.exit(0);
}

// Default: Generate common test tokens
console.log('===========================================');
console.log('🔑 GENERATE TEST TOKENS');
console.log('===========================================\n');

// Student token (user_id = 1)
const studentToken = generateToken(1, 'an.nv@gmail.com', 'student');
console.log('✅ STUDENT TOKEN (user_id = 1)');
console.log('Email: an.nv@gmail.com');
console.log('Password: password123');
console.log('Token:');
console.log('Bearer ' + studentToken);
console.log('\n');

// Admin token (user_id = 21)
const adminToken = generateToken(21, 'admin1@uni.edu.vn', 'admin');
console.log('✅ ADMIN TOKEN (user_id = 21)');
console.log('Email: admin1@uni.edu.vn');
console.log('Password: password123');
console.log('Token:');
console.log('Bearer ' + adminToken);
console.log('\n');

// Staff token (user_id = 31)
const staffToken = generateToken(31, 'hr.nguyen@uni.edu.vn', 'staff');
console.log('✅ STAFF TOKEN (user_id = 31)');
console.log('Email: hr.nguyen@uni.edu.vn');
console.log('Password: password123');
console.log('Token:');
console.log('Bearer ' + staffToken);
console.log('\n');

console.log('===========================================');
console.log('📝 USAGE:');
console.log('  Default tokens:  node generate-test-token.js');
console.log('  Custom token:    node generate-test-token.js <user_id> <role>');
console.log('  Example:         node generate-test-token.js 5 student');
console.log('\n💡 USE CASES:');
console.log('  - Testing API with Postman/curl');
console.log('  - Backend development & debugging');
console.log('  - Integration tests');
console.log('  - Onboarding new developers');
console.log('===========================================');

// Export for programmatic use
module.exports = { generateToken };