/**
 * Generate seed data with properly hashed passwords
 * Run: node backend/database/generate_seed_with_hash.js
 */

const bcrypt = require('bcryptjs');

async function generateHashedSeed() {
    const password = 'password123';
    const hash = await bcrypt.hash(password, 10);
    
    console.log('='.repeat(60));
    console.log('HASHED PASSWORD FOR SEED DATA');
    console.log('='.repeat(60));
    console.log('Plain password:', password);
    console.log('Hashed password:', hash);
    console.log('='.repeat(60));
    console.log('\nReplace all password_hash values in complete_seed.sql with:');
    console.log(`'${hash}'`);
    console.log('='.repeat(60));
}

generateHashedSeed().catch(console.error);
