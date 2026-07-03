require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

/**
 * Sinh backend/database/full_schema.sql từ DB live bằng mysqldump.
 *
 * File kết quả gồm:
 *   - Cấu trúc TOÀN BỘ bảng + trigger + routine.
 *   - TOÀN BỘ dữ liệu thật của mọi bảng (data mẫu đang có trên DB live).
 *
 * Teammate chỉ cần: tạo DB rỗng rồi `mysql ... < full_schema.sql` là có DB
 * giống hệt máy dev (cấu trúc + dữ liệu), không phải chạy lẻ tẻ từng migration.
 *
 * ⚠️ File này chứa DỮ LIỆU THẬT, gồm cả bảng users (email + password_hash).
 *    Chỉ commit khi chấp nhận các thông tin đó nằm trong lịch sử git.
 *
 * Chạy lại script này mỗi khi muốn cập nhật snapshot DB.
 *
 * mysqldump path: ưu tiên biến MYSQLDUMP_PATH, sau đó thử vị trí mặc định trên máy dev.
 */

const DB_HOST = process.env.DB_HOST || 'localhost';
const DB_PORT = process.env.DB_PORT || '3306';
const DB_USER = process.env.DB_USER || 'root';
const DB_PASSWORD = process.env.DB_PASSWORD || '';
const DB_NAME = process.env.DB_NAME || 'emotion_debugging';

const CANDIDATE_DUMP_PATHS = [
    process.env.MYSQLDUMP_PATH,
    'mysqldump',
    'D:\\MySQL\\Server\\bin\\mysqldump.exe',
    '/d/MySQL/Server/bin/mysqldump',
    'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe',
].filter(Boolean);

function resolveDumpBin() {
    for (const candidate of CANDIDATE_DUMP_PATHS) {
        const probe = spawnSync(candidate, ['--version'], { encoding: 'utf8' });
        if (!probe.error && probe.status === 0) return candidate;
    }
    throw new Error(
        'Không tìm thấy mysqldump. Đặt biến môi trường MYSQLDUMP_PATH trỏ tới mysqldump.exe.'
    );
}

function runDump(dumpBin, extraArgs) {
    const args = [
        `-h${DB_HOST}`,
        `-P${DB_PORT}`,
        `-u${DB_USER}`,
        '--default-character-set=utf8mb4',
        ...extraArgs,
        DB_NAME,
    ];
    // Password truyền qua env MYSQL_PWD để không lộ trong danh sách tiến trình / lịch sử shell.
    const result = spawnSync(dumpBin, args, {
        encoding: 'utf8',
        maxBuffer: 256 * 1024 * 1024,
        env: { ...process.env, MYSQL_PWD: DB_PASSWORD },
    });
    if (result.error) throw result.error;
    if (result.status !== 0) {
        throw new Error(`mysqldump lỗi (mã ${result.status}): ${result.stderr}`);
    }
    return result.stdout;
}

// Bỏ mệnh đề DEFINER=`user`@`host` khỏi trigger/routine. Nếu giữ, teammate
// import bằng user MySQL khác (không phải root@localhost) sẽ bị lỗi 1449.
function stripDefiner(sql) {
    return sql.replace(/\/\*!50017 DEFINER=`[^`]*`@`[^`]*`\*\/\s*/g, '');
}

function main() {
    const dumpBin = resolveDumpBin();

    // Cấu trúc + TOÀN BỘ data + trigger + routine của mọi bảng.
    // mysqldump đặt CREATE TRIGGER sau phần INSERT của mỗi bảng, nên khi import
    // data community_follows nạp trước lúc trigger tồn tại → count không bị đếm 2 lần.
    const dump = stripDefiner(runDump(dumpBin, [
        '--triggers',
        '--routines',
        '--skip-comments',
    ]));

    const header = [
        '-- ============================================================================',
        '-- FULL DATABASE SNAPSHOT — Emotion Debugging',
        '-- Sinh tự động bằng: npm run db:dump   (backend/scripts/generate-schema.js)',
        '-- KHÔNG sửa tay. Muốn cập nhật: chạy lại lệnh trên.',
        '--',
        '-- Gồm: cấu trúc toàn bộ bảng + trigger + TOÀN BỘ dữ liệu thật trên DB live.',
        '-- ⚠️ Chứa dữ liệu người dùng thật (email, password_hash trong bảng users).',
        '--',
        '-- Cách dùng cho teammate (DB mới tinh):',
        `--   1. CREATE DATABASE ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;`,
        `--   2. mysql -u root -p ${DB_NAME} < database/full_schema.sql`,
        '-- ============================================================================',
        '',
        'SET FOREIGN_KEY_CHECKS = 0;',
        '',
    ].join('\n');

    const footer = '\nSET FOREIGN_KEY_CHECKS = 1;\n';

    const outPath = path.join(__dirname, '..', 'database', 'full_schema.sql');
    fs.writeFileSync(outPath, header + dump + footer, 'utf8');

    console.log(`full_schema.sql đã tạo: ${outPath}`);
}

main();
