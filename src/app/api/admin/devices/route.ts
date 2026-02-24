import { NextResponse } from 'next/server';
import { Pool } from 'pg';

const pool = new Pool({
    connectionString: process.env.POSTGRES_URL_NON_POOLING || process.env.POSTGRES_URL || process.env.DATABASE_URL,
});

function checkAuth(req: Request) {
    const authHeader = req.headers.get('Authorization');
    if (!authHeader || !authHeader.startsWith('Bearer ')) return false;
    const token = authHeader.split(' ')[1];
    return token === process.env.ADMIN_TOKEN;
}

export async function GET(req: Request) {
    if (!checkAuth(req)) {
        return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    try {
        const { rows } = await pool.query('SELECT * FROM devices ORDER BY last_seen_at DESC LIMIT 500');
        return NextResponse.json({ devices: rows });
    } catch (err) {
        console.error('Error in /api/admin/devices:', err);
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
