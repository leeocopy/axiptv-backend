import { NextResponse } from 'next/server';
import { Pool } from 'pg';

const pool = new Pool({
  connectionString: process.env.POSTGRES_URL_NON_POOLING || process.env.POSTGRES_URL || process.env.DATABASE_URL,
});

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { device_hash } = body;

    if (!device_hash || typeof device_hash !== 'string' || device_hash.length !== 64) {
      return NextResponse.json({ error: 'Invalid device hash' }, { status: 400 });
    }

    const { rows } = await pool.query('SELECT * FROM devices WHERE device_hash = $1', [device_hash]);
    const now = new Date();
    
    let device;

    if (rows.length === 0) {
      // New device: insert with trial_end_at = now + 7 days
      const trialEndAt = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
      const insertResult = await pool.query(
        `INSERT INTO devices (device_hash, first_seen_at, last_seen_at, trial_end_at) 
         VALUES ($1, $2, $3, $4) RETURNING *`,
        [device_hash, now, now, trialEndAt]
      );
      device = insertResult.rows[0];
    } else {
      // Existing device: update last_seen_at
      const updateResult = await pool.query(
        `UPDATE devices SET last_seen_at = $1 WHERE device_hash = $2 RETURNING *`,
        [now, device_hash]
      );
      device = updateResult.rows[0];
    }

    const trialEndAtDate = new Date(device.trial_end_at);
    const inTrial = trialEndAtDate > now;
    
    let isActive = device.is_active;
    let activeUntilDate = device.active_until ? new Date(device.active_until) : null;
    let hasActiveSubscription = isActive && activeUntilDate && activeUntilDate > now;

    let allowed = !device.blocked && (inTrial || hasActiveSubscription);
    
    let reason = "expired";
    if (device.blocked) reason = "blocked";
    else if (hasActiveSubscription) reason = "active";
    else if (inTrial) reason = "trial";

    return NextResponse.json({
      server_time: now.toISOString(),
      trial_end_at: device.trial_end_at,
      is_active: device.is_active,
      active_until: device.active_until,
      allowed,
      reason
    });
  } catch (err) {
    console.error('Error in /api/device/status:', err);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
