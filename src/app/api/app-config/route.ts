import { NextResponse } from "next/server";

export async function GET() {
  return NextResponse.json({
    latestVersion: "2.0.0",
    latestVersionCode: 2,
    updateUrl: "https://axiptv.com/download",
    updateMessage: "A new version of NeoPlayers (v2.0.0) is available! We've made the app faster and fixed the MATCH TODAY section. Please update to continue enjoying the app.",
    isCritical: false
  });
}
