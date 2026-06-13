const db = require('../config/db');

class GitJournalService {
    
    // ==================== HELPER ====================
    
    /**
     * Lấy student_id từ user_id
     */
    async getStudentIdFromUserId(userId) {
        const [students] = await db.query(
            'SELECT student_id FROM students WHERE user_id = ?',
            [userId]
        );
        
        if (students.length === 0) {
            throw new Error('Student not found for this user');
        }
        
        return students[0].student_id;
    }
    
    // ==================== EMOTIONS ====================
    
    /**
     * Lấy danh sách tất cả emotions
     */
    async getAllEmotions() {
        const [emotions] = await db.query(
            'SELECT * FROM emotions ORDER BY emotion_name ASC'
        );
        return emotions;
    }

    /**
     * Lấy emotion theo ID
     */
    async getEmotionById(emotionId) {
        const [emotions] = await db.query(
            'SELECT * FROM emotions WHERE emotion_id = ?',
            [emotionId]
        );
        return emotions[0];
    }

    // ==================== COMMITS ====================
    
    /**
     * Tạo commit mới
     * @param {Object} commitData - {student_id, emotion_id, branch_type, intensity_level, message}
     */
    async createCommit(commitData) {
        const { student_id, emotion_id, branch_type, user_quest_id, intensity_level, message } = commitData;
        
        // Validate
        if (!student_id || !emotion_id || !branch_type || intensity_level === undefined) {
            throw new Error('Missing required fields');
        }
        
        if (intensity_level < 0 || intensity_level > 100) {
            throw new Error('Intensity level must be between 0 and 100');
        }
        
        // Insert commit
        const [result] = await db.query(
            `INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message) 
             VALUES (?, ?, ?, ?, ?, ?)`,
            [student_id, emotion_id, branch_type, user_quest_id || null, intensity_level, message]
        );
        
        const commitId = result.insertId;
        
        // Trigger severity scanner (async, không chờ)
        this.triggerSeverityScanner(student_id, branch_type, user_quest_id).catch(err => {
            console.error('Severity scanner error:', err);
        });
        
        // Return created commit
        return await this.getCommitById(commitId);
    }

    /**
     * Lấy commit theo ID
     */
    async getCommitById(commitId) {
        const [commits] = await db.query(
            `SELECT c.*, e.emotion_name, e.emotion_category, e.color_hex, e.icon_url
             FROM commits c
             JOIN emotions e ON c.emotion_id = e.emotion_id
             WHERE c.commit_id = ?`,
            [commitId]
        );
        return commits[0];
    }

    /**
     * Lấy commits của student theo branch và time range
     */
    async getCommitsByStudent(studentId, options = {}) {
        const { branch_type, start_date, end_date, limit, offset } = options;
        
        let query = `
            SELECT c.*, e.emotion_name, e.emotion_category, e.color_hex, e.icon_url
            FROM commits c
            JOIN emotions e ON c.emotion_id = e.emotion_id
            WHERE c.student_id = ?
        `;
        const params = [studentId];
        
        if (branch_type) {
            query += ' AND c.branch_type = ?';
            params.push(branch_type);
        }
        
        if (start_date) {
            query += ' AND c.created_at >= ?';
            params.push(start_date);
        }
        
        if (end_date) {
            query += ' AND c.created_at <= ?';
            params.push(end_date);
        }
        
        query += ' ORDER BY c.created_at DESC';
        
        if (limit) {
            query += ' LIMIT ?';
            params.push(parseInt(limit));
            
            if (offset) {
                query += ' OFFSET ?';
                params.push(parseInt(offset));
            }
        }
        
        const [commits] = await db.query(query, params);
        return commits;
    }

    // ==================== SEVERITY SCANNER ====================
    
    /**
     * Quét severity trong 3 ngày gần nhất
     * Trigger sau mỗi commit
     */
    async triggerSeverityScanner(studentId, branchType, userQuestId = null) {
        // Lấy commits trong 3 ngày gần nhất
        const threeDaysAgo = new Date();
        threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
        
        const commits = await this.getCommitsByStudent(studentId, {
            branch_type: branchType,
            start_date: threeDaysAgo.toISOString()
        });
        
        if (commits.length === 0) return null;
        
        // Tính severity score
        const severityResult = this.calculateSeverityScore(commits);
        
        // Kiểm tra điều kiện kích hoạt alert
        if (severityResult.isHighSeverity && severityResult.isNegative) {
            // Lưu alert vào database
            const alertData = {
                student_id: studentId,
                branch_type: branchType,
                alert_type: branchType === 'main' ? 'HIGH_SEVERITY' : 'QUEST_INEFFECTIVE',
                severity_score: severityResult.score,
                alert_message: this.generateAlertMessage(branchType, severityResult)
            };
            
            await this.createSeverityAlert(alertData);
            
            return {
                shouldAlert: true,
                alertType: alertData.alert_type,
                message: alertData.alert_message,
                severityScore: severityResult.score
            };
        }
        
        return null;
    }

    /**
     * Tính severity score từ danh sách commits
     */
    calculateSeverityScore(commits) {
        if (commits.length === 0) {
            return { score: 0, isHighSeverity: false, isNegative: false };
        }
        
        // Tính trung bình intensity của các commits tiêu cực
        const negativeCommits = commits.filter(c => c.emotion_category === 'NEGATIVE');
        
        if (negativeCommits.length === 0) {
            return { score: 0, isHighSeverity: false, isNegative: false };
        }
        
        const avgIntensity = negativeCommits.reduce((sum, c) => sum + c.intensity_level, 0) / negativeCommits.length;
        const negativeRatio = negativeCommits.length / commits.length;
        
        // Severity score = avg_intensity * negative_ratio
        const score = avgIntensity * negativeRatio;
        
        // High severity nếu score >= 50 (tùy chỉnh threshold)
        const isHighSeverity = score >= 50;
        const isNegative = negativeRatio > 0.5; // Hơn 50% commits là tiêu cực
        
        return { score, isHighSeverity, isNegative, avgIntensity, negativeRatio };
    }

    /**
     * Generate alert message
     */
    generateAlertMessage(branchType, severityResult) {
        if (branchType === 'main') {
            return `Phát hiện mức độ tiêu cực cao trong 3 ngày qua (${severityResult.score.toFixed(2)}%). Khuyến nghị sử dụng chức năng Error Logs để cải thiện tâm trạng.`;
        } else {
            return `Quest hiện tại không hiệu quả (${severityResult.score.toFixed(2)}%). Hệ thống sẽ gợi ý Quest thay thế phù hợp hơn.`;
        }
    }

    /**
     * Lưu severity alert
     */
    async createSeverityAlert(alertData) {
        const [result] = await db.query(
            `INSERT INTO severity_alerts (student_id, branch_type, alert_type, severity_score, alert_message)
             VALUES (?, ?, ?, ?, ?)`,
            [alertData.student_id, alertData.branch_type, alertData.alert_type, alertData.severity_score, alertData.alert_message]
        );
        return result.insertId;
    }

    /**
     * Lấy alerts của student
     */
    async getAlertsByStudent(studentId, limit = 10) {
        const [alerts] = await db.query(
            `SELECT * FROM severity_alerts 
             WHERE student_id = ? 
             ORDER BY triggered_at DESC 
             LIMIT ?`,
            [studentId, limit]
        );
        return alerts;
    }

    // ==================== DAILY MERGE ====================
    
    /**
     * Tạo daily merge (manual hoặc auto)
     */
    async createDailyMerge(studentId, mergeDate, userRetrospective = null, isAutoMerged = false) {
        // Kiểm tra đã merge ngày này chưa
        const existing = await this.getDailyMerge(studentId, mergeDate);
        if (existing) {
            throw new Error('Daily merge already exists for this date');
        }
        
        // Lấy tất cả commits trong ngày
        const startOfDay = new Date(mergeDate);
        startOfDay.setHours(0, 0, 0, 0);
        
        const endOfDay = new Date(mergeDate);
        endOfDay.setHours(23, 59, 59, 999);
        
        const commits = await this.getCommitsByStudent(studentId, {
            start_date: startOfDay.toISOString(),
            end_date: endOfDay.toISOString()
        });
        
        if (commits.length === 0) {
            throw new Error('No commits found for this date');
        }
        
        // Chạy thuật toán WEA (Weighted Emotion Algorithm)
        const emotionStats = this.calculateEmotionStats(commits);
        const dominantEmotion = this.findDominantEmotion(emotionStats);
        
        // Insert daily merge
        const [result] = await db.query(
            `INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged)
             VALUES (?, ?, ?, ?, ?, ?)`,
            [studentId, mergeDate, dominantEmotion.emotion_id, JSON.stringify(emotionStats), userRetrospective, isAutoMerged]
        );
        
        return await this.getDailyMergeById(result.insertId);
    }

    /**
     * Thuật toán WEA: Tính thống kê cảm xúc
     * A = Frequency (Tần suất)
     * B = Average Intensity (Mức độ trung bình)
     * Impact Score = A * B
     */
    calculateEmotionStats(commits) {
        const stats = {};
        const totalCommits = commits.length;
        
        // Group by emotion
        commits.forEach(commit => {
            const emotionName = commit.emotion_name;
            if (!stats[emotionName]) {
                stats[emotionName] = {
                    emotion_id: commit.emotion_id,
                    emotion_name: emotionName,
                    emotion_category: commit.emotion_category,
                    color_hex: commit.color_hex,
                    count: 0,
                    total_intensity: 0,
                    intensities: []
                };
            }
            stats[emotionName].count++;
            stats[emotionName].total_intensity += commit.intensity_level;
            stats[emotionName].intensities.push(commit.intensity_level);
        });
        
        // Calculate frequency, avg_intensity, impact_score
        Object.keys(stats).forEach(emotionName => {
            const emotion = stats[emotionName];
            emotion.frequency = emotion.count / totalCommits; // A
            emotion.avg_intensity = emotion.total_intensity / emotion.count; // B
            emotion.impact_score = emotion.frequency * emotion.avg_intensity; // A * B
            
            // Clean up temporary fields
            delete emotion.count;
            delete emotion.total_intensity;
            delete emotion.intensities;
        });
        
        return stats;
    }

    /**
     * Tìm cảm xúc chủ đạo (highest impact score)
     */
    findDominantEmotion(emotionStats) {
        let maxScore = 0;
        let dominantEmotion = null;
        
        Object.keys(emotionStats).forEach(emotionName => {
            const emotion = emotionStats[emotionName];
            if (emotion.impact_score > maxScore) {
                maxScore = emotion.impact_score;
                dominantEmotion = emotion;
            }
        });
        
        return dominantEmotion;
    }

    /**
     * Lấy daily merge theo ID
     */
    async getDailyMergeById(mergeId) {
        const [merges] = await db.query(
            `SELECT dm.merge_id, dm.student_id, dm.merge_date, dm.dominant_emotion_id,
                    CAST(dm.emotion_stats AS CHAR) as emotion_stats,
                    dm.user_retrospective, dm.is_auto_merged, dm.created_at,
                    e.emotion_name, e.emotion_category, e.color_hex
             FROM daily_merges dm
             JOIN emotions e ON dm.dominant_emotion_id = e.emotion_id
             WHERE dm.merge_id = ?`,
            [mergeId]
        );
        
        if (merges[0]) {
            try {
                // Parse the string JSON
                merges[0].emotion_stats = JSON.parse(merges[0].emotion_stats);
            } catch (error) {
                console.error(`Failed to parse emotion_stats for merge_id ${mergeId}:`, error.message);
                merges[0].emotion_stats = {};
            }
        }
        
        return merges[0];
    }

    /**
     * Lấy daily merge theo student và date
     */
    async getDailyMerge(studentId, mergeDate) {
        const [merges] = await db.query(
            `SELECT dm.merge_id, dm.student_id, dm.merge_date, dm.dominant_emotion_id,
                    CAST(dm.emotion_stats AS CHAR) as emotion_stats,
                    dm.user_retrospective, dm.is_auto_merged, dm.created_at,
                    e.emotion_name, e.emotion_category, e.color_hex
             FROM daily_merges dm
             JOIN emotions e ON dm.dominant_emotion_id = e.emotion_id
             WHERE dm.student_id = ? AND dm.merge_date = ?`,
            [studentId, mergeDate]
        );
        
        if (merges[0]) {
            try {
                // Parse the string JSON
                merges[0].emotion_stats = JSON.parse(merges[0].emotion_stats);
            } catch (error) {
                console.error(`Failed to parse emotion_stats for student ${studentId}, date ${mergeDate}:`, error.message);
                merges[0].emotion_stats = {};
            }
        }
        
        return merges[0];
    }

    /**
     * Lấy tất cả daily merges của student
     */
    async getDailyMergesByStudent(studentId, options = {}) {
        const { start_date, end_date, limit, offset } = options;
        
        let query = `
            SELECT dm.merge_id, dm.student_id, dm.merge_date, dm.dominant_emotion_id,
                   CAST(dm.emotion_stats AS CHAR) as emotion_stats,
                   dm.user_retrospective, dm.is_auto_merged, dm.created_at,
                   e.emotion_name, e.emotion_category, e.color_hex
            FROM daily_merges dm
            JOIN emotions e ON dm.dominant_emotion_id = e.emotion_id
            WHERE dm.student_id = ?
        `;
        const params = [studentId];
        
        if (start_date) {
            query += ' AND dm.merge_date >= ?';
            params.push(start_date);
        }
        
        if (end_date) {
            query += ' AND dm.merge_date <= ?';
            params.push(end_date);
        }
        
        query += ' ORDER BY dm.merge_date DESC';
        
        if (limit) {
            query += ' LIMIT ?';
            params.push(parseInt(limit));
            
            if (offset) {
                query += ' OFFSET ?';
                params.push(parseInt(offset));
            }
        }
        
        const [merges] = await db.query(query, params);
        
        // Parse emotion_stats JSON (safely)
        merges.forEach(merge => {
            try {
                // emotion_stats is now a string from CAST
                merge.emotion_stats = JSON.parse(merge.emotion_stats);
            } catch (error) {
                console.error(`Failed to parse emotion_stats for merge_id ${merge.merge_id}:`, error.message);
                // Set to empty object on error
                merge.emotion_stats = {};
            }
        });
        
        return merges;
    }

    // ==================== GIT GRAPH DATA ====================
    
    /**
     * Lấy dữ liệu để vẽ Git Graph
     * Bao gồm commits và daily merges
     */
    async getGitGraphData(studentId, options = {}) {
        const { start_date, end_date, limit, offset } = options;
        
        // Lấy commits (không filter date nếu không có start_date/end_date)
        const commits = await this.getCommitsByStudent(studentId, {
            start_date,
            end_date,
            limit: limit || 100,
            offset: offset || 0
        });
        
        // Lấy daily merges (không filter date nếu không có start_date/end_date)
        const merges = await this.getDailyMergesByStudent(studentId, {
            start_date,
            end_date
        });
        
        return {
            commits,
            merges,
            total_commits: commits.length,
            total_merges: merges.length
        };
    }
}

module.exports = new GitJournalService();
