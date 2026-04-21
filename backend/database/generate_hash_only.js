/**
 * Generate REAL bcrypt hash for password123
 * Copy the output and use in SQL
 */

const bcrypt = require('bcryptjs');

async function generateHash() {
    const password = 'password123';
    const hash = await bcrypt.hash(password, 10);
    
    console.log('='.repeat(70));
    console.log('REAL BCRYPT HASH FOR PASSWORD: password123');
    console.log('='.repeat(70));
    console.log('');
    console.log('Hash:', hash);
    console.log('');
    console.log('='.repeat(70));
    console.log('Copy this hash and replace in your SQL file!');
    console.log('='.repeat(70));
}

generateHash();
