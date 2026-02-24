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

export async function POST(req: Request) {
    if (!checkAuth(req)) {
        return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    try {
        const body = await req.json();
        const { device_hash, days } = body;

        if (!device_hash || typeof days !== 'number') {
            return NextResponse.json({ error: 'Invalid input' }, { status: 400 });
        }

        const { rows } = await pool.query('SELECT active_until FROM devices WHERE device_hash = $1', [device_hash]);
        if (rows.length === 0) {
            return NextResponse.json({ error: 'Device not found' }, { status: 404 });
        }

        const currentActiveUntil = rows[0].active_until ? new Date(rows[0].active_until) : new Date();
        const now = new Date();
        const baseDate = currentActiveUntil > now ? currentActiveUntil : now;

        const newActiveUntil = new Date(baseDate.getTime() + days * 24 * 60 * 60 * 1000);

        const updateResult = await pool.query(
            `UPDATE devices SET is_active = true, active_until = $1 WHERE device_hash = $2 RETURNING device_hash, is_active, active_until`,
            [newActiveUntil, device_hash]
        );

        return NextResponse.json(updateResult.rows[0]);
    } catch (err) {
        console.error('Error in /api/admin/activate:', err);
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
