const gitJournalService = require('../services/gitjournal.service');

class GitJournalController {
    
    // ==================== EMOTIONS ====================
    
    /**
     * GET /api/gitjournal/emotions
     * Lấy danh sách tất cả emotions
     */
    async getAllEmotions(req, res) {
        try {
            const emotions = await gitJournalService.getAllEmotions();
            res.json({
                success: true,
                data: emotions
            });
        } catch (error) {
            console.error('Get emotions error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy danh sách cảm xúc',
                error: error.message
            });
        }
    }

    // ==================== COMMITS ====================
    
    /**
     * POST /api/gitjournal/commits
     * Tạo commit mới
     * Body: { emotion_id, branch_type, intensity_level, message }
     */
    async createCommit(req, res) {
        try {
            const userId = req.user.user_id; // Từ auth middleware
            const { emotion_id, branch_type, user_quest_id, intensity_level, message } = req.body;
            
            // Validate
            if (!emotion_id || !branch_type || intensity_level === undefined) {
                return res.status(400).json({
                    success: false,
                    message: 'Thiếu thông tin bắt buộc'
                });
            }
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            const commitData = {
                student_id: studentId,
                emotion_id,
                branch_type: branch_type || 'main',
                user_quest_id: user_quest_id || null,
                intensity_level,
                message: message || ''
            };
            
            const commit = await gitJournalService.createCommit(commitData);
            
            // Check for severity alert (async result)
            const alert = await gitJournalService.triggerSeverityScanner(
                studentId, 
                branch_type, 
                user_quest_id
            );
            
            res.status(201).json({
                success: true,
                message: 'Commit thành công',
                data: {
                    commit,
                    alert // null nếu không có alert
                }
            });
        } catch (error) {
            console.error('Create commit error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi tạo commit',
                error: error.message
            });
        }
    }

    /**
     * GET /api/gitjournal/commits
     * Lấy danh sách commits của user
     * Query: branch_type, start_date, end_date, limit, offset
     */
    async getCommits(req, res) {
        try {
            const userId = req.user.user_id;
            const { branch_type, start_date, end_date, limit, offset } = req.query;
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            const commits = await gitJournalService.getCommitsByStudent(studentId, {
                branch_type,
                start_date,
                end_date,
                limit: limit || 50,
                offset: offset || 0
            });
            
            res.json({
                success: true,
                data: commits,
                pagination: {
                    limit: parseInt(limit) || 50,
                    offset: parseInt(offset) || 0,
                    total: commits.length
                }
            });
        } catch (error) {
            console.error('Get commits error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy danh sách commits',
                error: error.message
            });
        }
    }

    /**
     * GET /api/gitjournal/commits/:id
     * Lấy chi tiết 1 commit
     */
    async getCommitById(req, res) {
        try {
            const { id } = req.params;
            const userId = req.user.user_id;
            
            const commit = await gitJournalService.getCommitById(id);
            
            if (!commit) {
                return res.status(404).json({
                    success: false,
                    message: 'Không tìm thấy commit'
                });
            }
            
            // Convert user_id to student_id for ownership check
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            // Check ownership
            if (commit.student_id !== studentId) {
                return res.status(403).json({
                    success: false,
                    message: 'Không có quyền truy cập commit này'
                });
            }
            
            res.json({
                success: true,
                data: commit
            });
        } catch (error) {
            console.error('Get commit error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy thông tin commit',
                error: error.message
            });
        }
    }

    // ==================== SEVERITY ALERTS ====================
    
    /**
     * GET /api/gitjournal/alerts
     * Lấy danh sách alerts của user
     */
    async getAlerts(req, res) {
        try {
            const userId = req.user.user_id;
            const { limit } = req.query;
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            const alerts = await gitJournalService.getAlertsByStudent(studentId, limit || 10);
            
            res.json({
                success: true,
                data: alerts
            });
        } catch (error) {
            console.error('Get alerts error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy danh sách cảnh báo',
                error: error.message
            });
        }
    }

    // ==================== DAILY MERGE ====================
    
    /**
     * POST /api/gitjournal/merge
     * Tạo daily merge (manual)
     * Body: { merge_date, user_retrospective }
     */
    async createDailyMerge(req, res) {
        try {
            const userId = req.user.user_id;
            const { merge_date, user_retrospective } = req.body;
            
            if (!merge_date) {
                return res.status(400).json({
                    success: false,
                    message: 'Thiếu thông tin ngày merge'
                });
            }
            
            // Validate time (22:00 - 23:59)
            const now = new Date();
            const currentHour = now.getHours();
            
            // Check if merge_date is today
            const today = now.toISOString().split('T')[0];
            if (merge_date === today && (currentHour < 22 || currentHour >= 24)) {
                return res.status(400).json({
                    success: false,
                    message: 'Chỉ có thể merge trong khung giờ 22:00 - 23:59'
                });
            }
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            const merge = await gitJournalService.createDailyMerge(
                studentId,
                merge_date,
                user_retrospective,
                false // manual merge
            );
            
            res.status(201).json({
                success: true,
                message: 'Daily merge thành công',
                data: merge
            });
        } catch (error) {
            console.error('Create daily merge error:', error);
            
            if (error.message === 'Daily merge already exists for this date') {
                return res.status(409).json({
                    success: false,
                    message: 'Đã merge ngày này rồi'
                });
            }
            
            if (error.message === 'No commits found for this date') {
                return res.status(400).json({
                    success: false,
                    message: 'Không có commits nào trong ngày này'
                });
            }
            
            res.status(500).json({
                success: false,
                message: 'Lỗi khi tạo daily merge',
                error: error.message
            });
        }
    }

    /**
     * GET /api/gitjournal/merges
     * Lấy danh sách daily merges
     * Query: start_date, end_date, limit, offset
     */
    async getDailyMerges(req, res) {
        try {
            const userId = req.user.user_id;
            const { start_date, end_date, limit, offset } = req.query;
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            const merges = await gitJournalService.getDailyMergesByStudent(studentId, {
                start_date,
                end_date,
                limit: limit || 30,
                offset: offset || 0
            });
            
            res.json({
                success: true,
                data: merges,
                pagination: {
                    limit: parseInt(limit) || 30,
                    offset: parseInt(offset) || 0,
                    total: merges.length
                }
            });
        } catch (error) {
            console.error('Get daily merges error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy danh sách daily merges',
                error: error.message
            });
        }
    }

    /**
     * GET /api/gitjournal/merges/:date
     * Lấy daily merge theo ngày
     */
    async getDailyMergeByDate(req, res) {
        try {
            const userId = req.user.user_id;
            const { date } = req.params;
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            const merge = await gitJournalService.getDailyMerge(studentId, date);
            
            if (!merge) {
                return res.status(404).json({
                    success: false,
                    message: 'Không tìm thấy daily merge cho ngày này'
                });
            }
            
            res.json({
                success: true,
                data: merge
            });
        } catch (error) {
            console.error('Get daily merge error:', error);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy thông tin daily merge',
                error: error.message
            });
        }
    }

    // ==================== GIT GRAPH ====================
    
    /**
     * GET /api/gitjournal/graph
     * Lấy dữ liệu để vẽ Git Graph
     * Query: start_date, end_date, limit, offset
     */
    async getGitGraphData(req, res) {
        try {
            const userId = req.user.user_id;
            const { start_date, end_date, limit, offset } = req.query;
            
            // Convert user_id to student_id
            const studentId = await gitJournalService.getStudentIdFromUserId(userId);
            
            console.log(`[GET GRAPH] student_id=${studentId}, limit=${limit}`);
            
            const graphData = await gitJournalService.getGitGraphData(studentId, {
                start_date,
                end_date,
                limit: limit || 100,
                offset: offset || 0
            });
            
            console.log(`[GET GRAPH] Got ${graphData.total_commits} commits, ${graphData.total_merges} merges`);
            
            // Ensure emotion_stats is properly serializable
            if (graphData.merges && graphData.merges.length > 0) {
                graphData.merges.forEach(merge => {
                    // If emotion_stats is already an object, it's fine
                    // Express will handle it
                    if (typeof merge.emotion_stats === 'string') {
                        try {
                            merge.emotion_stats = JSON.parse(merge.emotion_stats);
                        } catch (e) {
                            console.error('[GET GRAPH] Failed to parse emotion_stats:', e.message);
                            merge.emotion_stats = {};
                        }
                    }
                });
            }
            
            console.log('[GET GRAPH] Sending response...');
            
            res.json({
                success: true,
                data: graphData
            });
            
            console.log('[GET GRAPH] Response sent successfully');
        } catch (error) {
            console.error('[GET GRAPH] Error:', error.message);
            console.error(error.stack);
            res.status(500).json({
                success: false,
                message: 'Lỗi khi lấy dữ liệu Git Graph',
                error: error.message
            });
        }
    }
}

module.exports = new GitJournalController();
