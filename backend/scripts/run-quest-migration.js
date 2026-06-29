require('dotenv').config();
const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

function parseStatements(sql) {
    const statements = [];
    let delimiter = ';';
    let current = '';
    for (const line of sql.split(/\r?\n/)) {
        const directive = line.trim().match(/^DELIMITER\s+(.+)$/i);
        if (directive) {
            delimiter = directive[1];
            continue;
        }
        if (!current && (!line.trim() || line.trim().startsWith('--'))) continue;
        current += `${line}\n`;
        if (!current.trimEnd().endsWith(delimiter)) continue;
        const statement = current.trimEnd().slice(0, -delimiter.length).trim();
        if (statement) statements.push(statement);
        current = '';
    }
    if (current.trim()) throw new Error('Migration contains an unterminated SQL statement');
    return statements;
}

async function main() {
    const connection = await mysql.createConnection({
        host: process.env.DB_HOST,
        port: Number(process.env.DB_PORT || 3306),
        user: process.env.DB_USER,
        password: process.env.DB_PASSWORD,
        database: process.env.DB_NAME
    });
    try {
        const migrationPath = path.join(__dirname, '../database/quest_builder_upgrade.sql');
        const statements = parseStatements(fs.readFileSync(migrationPath, 'utf8'));
        for (const statement of statements) await connection.query(statement);
        console.log(`Quest Builder migration applied (${statements.length} statements).`);
    } finally {
        await connection.end();
    }
}

main().catch((error) => {
    console.error('Quest Builder migration failed:', error.sqlMessage || error.message);
    process.exitCode = 1;
});
