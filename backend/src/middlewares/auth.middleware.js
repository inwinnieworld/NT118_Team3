const jwt = require('jsonwebtoken');

module.exports = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ message: 'No token provided' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        // Transform camelCase to snake_case for consistency with DB
        req.user = {
            user_id: decoded.userId,
            email: decoded.email,
            role: decoded.role
        };
        
        next();
    } catch (err) {
        return res.status(403).json({ message: 'Invalid token' });
    }
};
