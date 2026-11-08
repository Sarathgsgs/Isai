import React, { useEffect, useRef } from 'react';

interface VinylDiscProps {
  isPlaying: boolean;
  rpm?: number; // 33 or 45
  albumArt?: string;
  fallbackSeed?: string;
  sizePx?: number;
}

export const VinylDisc: React.FC<VinylDiscProps> = ({
  isPlaying,
  rpm = 33,
  albumArt,
  fallbackSeed = 'Isai Offline Music',
  sizePx = 360,
}) => {
  const rotationRef = useRef<number>(0);
  const discRef = useRef<HTMLDivElement>(null);
  const animFrameRef = useRef<number | null>(null);

  useEffect(() => {
    let lastTimestamp = performance.now();

    const animate = (now: number) => {
      const dt = (now - lastTimestamp) / 1000;
      lastTimestamp = now;

      if (isPlaying) {
        // 33.33 RPM = 33.33 * 360 deg / 60s = 200 deg/sec
        // 45 RPM = 45 * 360 deg / 60s = 270 deg/sec
        const degPerSec = (rpm * 360) / 60;
        rotationRef.current = (rotationRef.current + degPerSec * dt) % 360;
        if (discRef.current) {
          discRef.current.style.transform = `rotate(${rotationRef.current}deg)`;
        }
      }
      animFrameRef.current = requestAnimationFrame(animate);
    };

    animFrameRef.current = requestAnimationFrame(animate);
    return () => {
      if (animFrameRef.current) cancelAnimationFrame(animFrameRef.current);
    };
  }, [isPlaying, rpm]);

  // Generate fallback vibrant gradient if no album cover art
  const seedHash = fallbackSeed.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  const hue1 = seedHash % 360;
  const hue2 = (hue1 + 45) % 360;
  const fallbackGradient = `linear-gradient(135deg, hsl(${hue1}, 75%, 45%), hsl(${hue2}, 85%, 25%))`;

  return (
    <div
      className="relative select-none rounded-full flex items-center justify-center shadow-2xl"
      style={{
        width: `${sizePx}px`,
        height: `${sizePx}px`,
        background: '#0D0D10',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7), 0 0 40px rgba(255, 138, 61, 0.12)',
      }}
    >
      {/* Outer Platter Rim */}
      <div
        className="absolute inset-0 rounded-full pointer-events-none"
        style={{
          border: '1.5px solid rgba(255, 255, 255, 0.15)',
          background: 'radial-gradient(circle, transparent 65%, rgba(20, 20, 25, 0.9) 98%, rgba(255, 255, 255, 0.2) 100%)',
        }}
      />

      {/* Rotating Vinyl Disc Surface */}
      <div
        ref={discRef}
        className="relative w-full h-full rounded-full overflow-hidden flex items-center justify-center"
        style={{
          background: '#0c0c10',
          transform: `rotate(${rotationRef.current}deg)`,
          willChange: 'transform',
        }}
      >
        {/* Fine Concentric Vinyl Grooves */}
        <div
          className="absolute inset-0 rounded-full pointer-events-none"
          style={{
            background: `
              repeating-radial-gradient(
                circle at 50% 50%,
                #111115 0px,
                #141418 1px,
                #09090c 2px,
                #17171d 3px,
                #0b0b0f 4px
              )
            `,
            opacity: 0.88,
          }}
        />

        {/* Track Gap Separators (at ~0.4R, ~0.6R, ~0.8R) */}
        <div
          className="absolute rounded-full pointer-events-none"
          style={{
            width: '82%',
            height: '82%',
            border: '2px solid rgba(0, 0, 0, 0.65)',
            boxShadow: 'inset 0 0 2px rgba(255, 255, 255, 0.08)',
          }}
        />
        <div
          className="absolute rounded-full pointer-events-none"
          style={{
            width: '62%',
            height: '62%',
            border: '2px solid rgba(0, 0, 0, 0.7)',
            boxShadow: 'inset 0 0 2px rgba(255, 255, 255, 0.08)',
          }}
        />
        <div
          className="absolute rounded-full pointer-events-none"
          style={{
            width: '42%',
            height: '42%',
            border: '2px solid rgba(0, 0, 0, 0.75)',
            boxShadow: 'inset 0 0 2px rgba(255, 255, 255, 0.08)',
          }}
        />

        {/* Center Label Sticker (Diameter ~34% of disc) */}
        <div
          className="relative rounded-full overflow-hidden flex items-center justify-center border-2 border-amber-900/40 shadow-inner"
          style={{
            width: '35%',
            height: '35%',
            background: albumArt ? undefined : fallbackGradient,
          }}
        >
          {albumArt ? (
            <img
              src={albumArt}
              alt="Vinyl Label"
              className="w-full h-full object-cover"
              crossOrigin="anonymous"
            />
          ) : (
            <div className="flex flex-col items-center justify-center text-center p-2">
              <span className="text-[10px] uppercase tracking-widest font-bold text-amber-200/90">ISAI</span>
              <span className="text-[8px] text-white/70 font-mono mt-0.5">33⅓ RPM</span>
            </div>
          )}

          {/* Label inner concentric rings */}
          <div className="absolute inset-0 rounded-full border border-white/20 pointer-events-none" />
          <div className="absolute inset-2 rounded-full border border-black/30 pointer-events-none" />

          {/* Center Spindle Hole (Diameter ~4.5% of disc) */}
          <div
            className="absolute rounded-full bg-[#08080a] border border-neutral-400/40 flex items-center justify-center"
            style={{ width: '13%', height: '13%' }}
          >
            {/* Center Chrome Spindle Pin */}
            <div
              className="w-1.5 h-1.5 rounded-full"
              style={{
                background: 'radial-gradient(circle at 35% 35%, #ffffff 0%, #a9afba 60%, #4a4e57 100%)',
                boxShadow: '0 0 2px rgba(0,0,0,0.8)',
              }}
            />
          </div>
        </div>
      </div>

      {/* Static Vinyl Specular Reflection Sheen (Fixed light source, does not rotate) */}
      <div
        className="absolute inset-0 rounded-full pointer-events-none overflow-hidden"
        style={{
          background: `
            conic-gradient(
              from 45deg at 50% 50%,
              transparent 0deg,
              rgba(255, 255, 255, 0.12) 35deg,
              rgba(255, 255, 255, 0.02) 65deg,
              transparent 90deg,
              transparent 180deg,
              rgba(255, 255, 255, 0.12) 215deg,
              rgba(255, 255, 255, 0.02) 245deg,
              transparent 270deg
            )
          `,
          mixBlendMode: 'screen',
        }}
      />
    </div>
  );
};
