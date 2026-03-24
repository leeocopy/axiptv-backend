import { NextResponse } from "next/server";

let cachedMatches: any = null;
let lastFetchTime: number = 0;
const CACHE_TTL = 10 * 60 * 1000; // 10 minutes

export async function GET() {
  const now = Date.now();

  // Return cached data if valid
  if (cachedMatches && (now - lastFetchTime) < CACHE_TTL) {
    return NextResponse.json(cachedMatches);
  }

  try {
    const today = new Date().toISOString().split('T')[0];
    const url = `https://sportapi7.p.rapidapi.com/api/v1/sport/football/scheduled-events/${today}`;
    
    const response = await fetch(url, {
      headers: {
        "x-rapidapi-key": "c409315d3fmsh8e716c5143c4205p1769b7jsn37de79d34b1b",
        "x-rapidapi-host": "sportapi7.p.rapidapi.com"
      }
    });

    if (!response.ok) {
        if (response.status === 429 && cachedMatches) {
            return NextResponse.json(cachedMatches);
        }
        throw new Error(`API returned ${response.status}`);
    }

    const data = await response.json();
    
    // Simple verification
    if (data && data.events) {
        cachedMatches = data;
        lastFetchTime = now;
    }

    return NextResponse.json(data);
  } catch (error: any) {
    console.error("Matches API Error:", error);
    
    if (cachedMatches) {
        return NextResponse.json(cachedMatches);
    }
    
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
