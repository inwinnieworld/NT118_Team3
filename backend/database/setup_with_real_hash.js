/**
 * Setup database with real bcrypt hash
 * This script generates a real password hash and creates a ready-to-use SQL file
 * 
 * Usage: node backend/database/setup_with_real_hash.js
 */

const bcrypt = require('bcryptjs');
const fs = require('fs');
const path = require('path');

async function setupDatabase() {
    console.log('='.repeat(60));
    console.log('EMOTION DEBUGGING - DATABASE SETUP WITH REAL HASH');
    console.log('='.repeat(60));
    console.log('');

    // Generate real bcrypt hash
    const password = 'password123';
    console.log('Generating bcrypt hash for password:', password);
    const hash = await bcrypt.hash(password, 10);
    console.log('✓ Hash generated:', hash);
    console.log('');

    // Read the template SQL file
    const templatePath = path.join(__dirname, 'setup_all_in_one.sql');
    const outputPath = path.join(__dirname, 'setup_ready.sql');
    
    console.log('Reading template:', templatePath);
    let sqlContent = fs.readFileSync(templatePath, 'utf8');
    
    // Replace placeholder with real hash
    const placeholder = '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y';
    sqlContent = sqlContent.replace(new RegExp(placeholder, 'g'), hash);
    
    // Write the ready-to-use SQL file
    fs.writeFileSync(outputPath, sqlContent, 'utf8');
    console.log('✓ Created ready-to-use SQL file:', outputPath);
    console.log('');

    console.log('='.repeat(60));
    console.log('SETUP COMPLETE!');
    console.log('='.repeat(60));
    console.log('');
    console.log('Next steps:');
    console.log('  1. Run: mysql -u root -p < backend/database/setup_ready.sql');
    console.log('  2. Or manually:');
    console.log('     mysql -u root -p');
    console.log('     source backend/database/setup_ready.sql');
    console.log('');
    console.log('Test credentials:');
    console.log('  Admin:   admin@uit.edu.vn / password123');
    console.log('  Staff:   thangda@uit.edu.vn / password123');
    console.log('  Student: 21520001 / password123');
    console.log('');
}

setupDatabase().catch(err => {
    console.error('Error:', err);
    process.exit(1);
});
