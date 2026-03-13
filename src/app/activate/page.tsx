'use client';

import { useSearchParams } from 'next/navigation';
import { useState, Suspense } from 'react';

function ActivateContent() {
    const searchParams = useSearchParams();
    const rawId = searchParams.get('d');
    const d = typeof rawId === 'string' ? rawId.trim().toLowerCase() : '';
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        if (d) {
            navigator.clipboard.writeText(d);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        }
    };

    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: 400, margin: '0 auto', textAlign: 'center' }}>
            <h2>Activate Your Device</h2>
            {d ? (
                <div style={{ marginTop: '2rem' }}>
                    <p>Your Device ID:</p>
                    <div style={{
                        background: '#f4f4f5',
                        padding: '1rem',
                        borderRadius: '8px',
                        fontFamily: 'monospace',
                        letterSpacing: '1px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between'
                    }}>
                        <strong>{d}</strong>
                        <button
                            onClick={handleCopy}
                            style={{
                                marginLeft: '1rem',
                                padding: '0.5rem 1rem',
                                background: copied ? '#22c55e' : '#ec4899',
                                color: '#fff',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer'
                            }}
                        >
                            {copied ? 'Copied!' : 'Copy'}
                        </button>
                    </div>
                    <p style={{ fontSize: '0.85rem', color: '#666', marginTop: '2rem' }}>
                        Provide this ID to your administrator to activate your service.
                    </p>
                </div>
            ) : (
                <p style={{ color: 'red', marginTop: '2rem' }}>No device ID provided in URL.</p>
            )}
        </div>
    );
}

export default function ActivatePage() {
    return (
        <Suspense fallback={<div style={{ textAlign: 'center', padding: '2rem' }}>Loading...</div>}>
            <ActivateContent />
        </Suspense>
    );
}
