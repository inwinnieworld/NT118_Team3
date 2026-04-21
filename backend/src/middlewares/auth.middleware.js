const jwt = require('jsonwebtoken');

module.exports = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    console.log(`[AUTH] ${req.method} ${req.path} - Token: ${token ? 'present' : 'missing'}`);

    if (!token) {
        console.log('[AUTH] No token provided');
        return res.status(401).json({ message: 'No token provided' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        console.log(`[AUTH] Token verified for user_id=${decoded.userId}`);
        
        // Transform camelCase to snake_case for consistency with DB
        req.user = {
            user_id: decoded.userId,
            email: decoded.email,
            role: decoded.role
        };
        
        next();
    } catch (err) {
        console.log('[AUTH] Token verification failed:', err.message);
        return res.status(403).json({ message: 'Invalid token' });
    }
};
