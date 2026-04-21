#!/bin/bash

# ============================================
# Database Setup Script
# ============================================

echo "=========================================="
echo "EMOTION DEBUGGING - DATABASE SETUP"
echo "=========================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Database credentials
DB_NAME="emotion_debugging"
DB_USER="root"

echo -e "${YELLOW}Step 1: Generate Password Hash${NC}"
echo "Running: node generate_seed_with_hash.js"
node generate_seed_with_hash.js
echo ""

echo -e "${YELLOW}⚠️  IMPORTANT: Update password_hash in complete_seed.sql with the hash above!${NC}"
echo "Press Enter when ready to continue..."
read

echo ""
echo -e "${YELLOW}Step 2: Create Database${NC}"
mysql -u $DB_USER -p -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Database created successfully${NC}"
else
    echo -e "${RED}✗ Failed to create database${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Step 3: Run Schema (DROP + CREATE tables)${NC}"
mysql -u $DB_USER -p $DB_NAME < complete_schema.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Schema created successfully${NC}"
else
    echo -e "${RED}✗ Failed to create schema${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Step 4: Insert Seed Data${NC}"
mysql -u $DB_USER -p $DB_NAME < complete_seed.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Seed data inserted successfully${NC}"
else
    echo -e "${RED}✗ Failed to insert seed data${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo -e "${GREEN}✓ DATABASE SETUP COMPLETE!${NC}"
echo "=========================================="
echo ""
echo "Test Credentials:"
echo "  Admin:   admin@uit.edu.vn / password123"
echo "  Staff:   thangda@uit.edu.vn / password123"
echo "  Student: 21520001 / password123"
echo ""
echo "Next steps:"
echo "  1. Update backend/.env with database credentials"
echo "  2. Start backend: npm start"
echo "  3. Test login with Android app"
echo ""
