import { NextResponse } from "next/server";
import { Pool } from "pg";

const pool = new Pool({
  connectionString:
    process.env.POSTGRES_URL_NON_POOLING ||
    process.env.POSTGRES_URL ||
    process.env.DATABASE_URL,
});

const HASH_RE = /^(?:[a-f0-9]{16}|[a-f0-9]{64})$/;

export async function POST(req: Request) {
  try {
    const body = await req.json().catch(() => ({} as any));
    const raw = body?.device_hash;

    const device_hash =
      typeof raw === "string" ? raw.trim().toLowerCase() : "";

    if (!HASH_RE.test(device_hash)) {
      return NextResponse.json({ error: "Invalid device hash" }, { status: 400 });
    }

    const now = new Date();

    const { rows } = await pool.query(
      "SELECT * FROM devices WHERE device_hash = $1",
      [device_hash]
    );

    let device: any;

    if (rows.length === 0) {
      const trialEndAt = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);

      const insertResult = await pool.query(
        `INSERT INTO devices (device_hash, first_seen_at, last_seen_at, trial_end_at)
         VALUES ($1, $2, $3, $4)
         RETURNING *`,
        [device_hash, now, now, trialEndAt]
      );

      device = insertResult.rows[0];
    } else {
      const updateResult = await pool.query(
        `UPDATE devices
         SET last_seen_at = $1
         WHERE device_hash = $2
         RETURNING *`,
        [now, device_hash]
      );

      device = updateResult.rows[0];
    }

    const trialEndAtDate = new Date(device.trial_end_at);
    const inTrial = trialEndAtDate > now;

    const activeUntilDate = device.active_until ? new Date(device.active_until) : null;
    const hasActiveSubscription =
      device.is_active && activeUntilDate && activeUntilDate > now;

    const allowed = !device.blocked && (inTrial || hasActiveSubscription);

    let reason = "expired";
    if (device.blocked) reason = "blocked";
    else if (hasActiveSubscription) reason = "active";
    else if (inTrial) reason = "trial";

    return NextResponse.json({
      server_time: now.toISOString(),
      device_hash,
      trial_end_at: device.trial_end_at,
      is_active: device.is_active,
      active_until: device.active_until,
      allowed,
      reason,
    });
  } catch (err) {
    console.error("Error in /api/device/status:", err);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}