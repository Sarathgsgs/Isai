import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import {
  Track,
  Album,
  Artist,
  Playlist,
  RepeatMode,
  PlayerSettings,
  EqualizerState,
  EqPreset,
} from './types';
import { INITIAL_TRACKS } from './data/demoTracks';
import { audioEngine } from './audio/AudioEngine';
import { NowPlayingView } from './components/player/NowPlayingView';
import { MiniPlayer } from './components/player/MiniPlayer';
import { QueueDrawer } from './components/player/QueueDrawer';
import { EqualizerModal } from './components/equalizer/EqualizerModal';
import { SettingsModal } from './components/dialogs/SettingsModal';
import { SleepTimerModal } from './components/dialogs/SleepTimerModal';
import { TrackInfoModal } from './components/dialogs/TrackInfoModal';
import {
  Disc,
  Music,
  FolderOpen,
  Search,
  Sliders,
  Moon,
  Settings,
  Play,
  Shuffle,
  Heart,
  MoreVertical,
  Upload,
  ArrowUpDown,
  X,
  Plus,
} from 'lucide-react';

export default function App() {
  // Library tracks state
  const [tracks, setTracks] = useState<Track[]>(() => {
    const saved = localStorage.getItem('isai_tracks');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed;
        }
      } catch {
        // Fall back
      }
    }
    return INITIAL_TRACKS;
  });

  // Player state
  const [currentTrack, setCurrentTrack] = useState<Track | null>(() => tracks[0] || null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(tracks[0]?.durationMs ? tracks[0].durationMs / 1000 : 180);
  const [volume, setVolume] = useState(0.85);
  const [repeatMode, setRepeatMode] = useState<RepeatMode>('OFF');
  const [isShuffled, setIsShuffled] = useState(false);
  const [isLifted, setIsLifted] = useState(true);

  // Playback Queue
  const [queue, setQueue] = useState<Track[]>(() => [...tracks]);
  const [currentIndex, setCurrentIndex] = useState(0);

  // Modals and views
  const [isNowPlayingOpen, setIsNowPlayingOpen] = useState(false);
  const [isQueueOpen, setIsQueueOpen] = useState(false);
  const [isEqualizerOpen, setIsEqualizerOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isSleepTimerOpen, setIsSleepTimerOpen] = useState(false);
  const [isTrackInfoOpen, setIsTrackInfoOpen] = useState(false);
  const [inspectTrack, setInspectTrack] = useState<Track | null>(null);

  // Active Samsung Music Category Tabs: Favourites | Playlists | Tracks | Albums | Artists | Folders
  type CategoryTab = 'favourites' | 'playlists' | 'tracks' | 'albums' | 'artists' | 'folders';
  const [activeTab, setActiveTab] = useState<CategoryTab>('tracks');
  const [searchQuery, setSearchQuery] = useState('');
  const [showSearchInput, setShowSearchInput] = useState(false);
  const [sortBy, setSortBy] = useState<'date' | 'title' | 'artist'>('date');
  const [selectedAlbum, setSelectedAlbum] = useState<string | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);

  // Player Settings
  const [settings, setSettings] = useState<PlayerSettings>(() => {
    const saved = localStorage.getItem('isai_settings');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // Fall back
      }
    }
    return {
      themeMode: 'dark',
      liftBehavior: 'pause',
      seekBarStyle: 'waveform_bars',
      lyricsTextSize: 'medium',
      rpm: 33,
    };
  });

  // Equalizer State
  const [equalizer, setEqualizer] = useState<EqualizerState>(() => {
    const saved = localStorage.getItem('isai_equalizer');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // Fall back
      }
    }
    return {
      enabled: true,
      presetName: 'Flat',
      bandGainsDb: [0, 0, 0, 0, 0],
      bassBoost: 15,
      virtualizer: 10,
      preampDb: 0,
      preventClipping: true,
    };
  });

  // Custom EQ Presets
  const [customPresets, setCustomPresets] = useState<EqPreset[]>(() => {
    const saved = localStorage.getItem('isai_custom_eq_presets');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch {
        // Fall back
      }
    }
    return [];
  });

  // Sleep Timer
  const [sleepTimerRemainingMs, setSleepTimerRemainingMs] = useState<number | null>(null);
  const [isStopAtEndOfTrack, setIsStopAtEndOfTrack] = useState(false);
  const sleepTimerRef = useRef<number | null>(null);

  // Hidden audio file input ref
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Persist settings & EQ
  useEffect(() => {
    localStorage.setItem('isai_settings', JSON.stringify(settings));
  }, [settings]);

  useEffect(() => {
    localStorage.setItem('isai_equalizer', JSON.stringify(equalizer));
    audioEngine.applyEqualizer(equalizer);
  }, [equalizer]);

  useEffect(() => {
    localStorage.setItem('isai_custom_eq_presets', JSON.stringify(customPresets));
  }, [customPresets]);

  useEffect(() => {
    try {
      const serializable = tracks.map((t) => ({
        ...t,
        uri: t.isCustomFile ? '' : t.uri,
      }));
      localStorage.setItem('isai_tracks', JSON.stringify(serializable));
    } catch {
      // Safe fallback
    }
  }, [tracks]);

  // Audio Engine Callbacks
  useEffect(() => {
    audioEngine.onProgress((cur, dur) => {
      setCurrentTime(cur);
      if (dur && isFinite(dur) && dur > 0) {
        setDuration(dur);
      }
    });

    audioEngine.onStateChange((playing) => {
      setIsPlaying(playing);
      if (playing) {
        setIsLifted(false);
      }
    });

    audioEngine.onEnded(() => {
      handleTrackEnd();
    });
  }, [queue, currentIndex, repeatMode, isStopAtEndOfTrack]);

  // Sleep Timer countdown
  useEffect(() => {
    if (sleepTimerRemainingMs !== null && sleepTimerRemainingMs > 0) {
      sleepTimerRef.current = window.setInterval(() => {
        setSleepTimerRemainingMs((prev) => {
          if (prev === null || prev <= 1000) {
            audioEngine.pause();
            setIsPlaying(false);
            setIsLifted(true);
            return null;
          }
          return prev - 1000;
        });
      }, 1000);
    } else {
      if (sleepTimerRef.current) {
        clearInterval(sleepTimerRef.current);
        sleepTimerRef.current = null;
      }
    }
    return () => {
      if (sleepTimerRef.current) {
        clearInterval(sleepTimerRef.current);
      }
    };
  }, [sleepTimerRemainingMs]);

  // Play track at queue index
  const playTrackAtIndex = useCallback(
    async (index: number) => {
      if (index < 0 || index >= queue.length) return;
      const track = queue[index];
      setCurrentIndex(index);
      setCurrentTrack(track);
      setCurrentTime(0);
      setDuration(track.durationMs / 1000);

      // Analog vinyl needle drop click effect
      audioEngine.playNeedleDropSound();
      setIsLifted(false);
      await audioEngine.loadTrack(track, true);
      setIsPlaying(true);
    },
    [queue]
  );

  // Play a specific track (immediately opens NowPlaying stage)
  const handlePlayTrack = useCallback(
    (track: Track, newQueue?: Track[]) => {
      const q = newQueue || tracks;
      const idx = q.findIndex((t) => t.id === track.id);
      setQueue(q);
      if (idx !== -1) {
        playTrackAtIndex(idx);
      } else {
        setQueue([track, ...q]);
        playTrackAtIndex(0);
      }
      setIsNowPlayingOpen(true);
    },
    [tracks, playTrackAtIndex]
  );

  // Handle track ending
  const handleTrackEnd = () => {
    if (isStopAtEndOfTrack) {
      audioEngine.pause();
      setIsPlaying(false);
      setIsLifted(true);
      setIsStopAtEndOfTrack(false);
      return;
    }

    if (repeatMode === 'ONE') {
      audioEngine.seek(0);
      audioEngine.play();
      return;
    }

    if (currentIndex < queue.length - 1) {
      playTrackAtIndex(currentIndex + 1);
    } else if (repeatMode === 'ALL') {
      playTrackAtIndex(0);
    } else {
      audioEngine.pause();
      setIsPlaying(false);
      setIsLifted(true);
    }
  };

  // Play / Pause toggle
  const handleTogglePlay = () => {
    if (!currentTrack) {
      if (tracks.length > 0) {
        handlePlayTrack(tracks[0]);
      }
      return;
    }

    if (isPlaying) {
      audioEngine.pause();
      setIsPlaying(false);
      if (settings.liftBehavior === 'stop') {
        audioEngine.seek(0);
        setCurrentTime(0);
      }
      setIsLifted(true);
    } else {
      audioEngine.playNeedleDropSound();
      setIsLifted(false);
      audioEngine.play();
      setIsPlaying(true);
    }
  };

  const handlePrevious = () => {
    if (currentTime > 3) {
      audioEngine.seek(0);
      setCurrentTime(0);
    } else if (currentIndex > 0) {
      playTrackAtIndex(currentIndex - 1);
    } else if (repeatMode === 'ALL') {
      playTrackAtIndex(queue.length - 1);
    }
  };

  const handleNext = () => {
    if (currentIndex < queue.length - 1) {
      playTrackAtIndex(currentIndex + 1);
    } else if (repeatMode === 'ALL') {
      playTrackAtIndex(0);
    }
  };

  const handleSeek = (seconds: number) => {
    setCurrentTime(seconds);
    audioEngine.seek(seconds);
  };

  const handleSeekProgress = (fraction: number) => {
    const target = fraction * (duration || 1);
    setCurrentTime(target);
    audioEngine.seekProgress(fraction);
  };

  const handleToggleShuffle = () => {
    if (!isShuffled) {
      const current = queue[currentIndex];
      const remaining = queue.filter((_, i) => i !== currentIndex);
      const shuffled = [...remaining].sort(() => Math.random() - 0.5);
      const newQueue = current ? [current, ...shuffled] : shuffled;
      setQueue(newQueue);
      setCurrentIndex(0);
      setIsShuffled(true);
    } else {
      setQueue([...tracks]);
      if (currentTrack) {
        const originalIndex = tracks.findIndex((t) => t.id === currentTrack.id);
        setCurrentIndex(originalIndex !== -1 ? originalIndex : 0);
      }
      setIsShuffled(false);
    }
  };

  const handleCycleRepeat = () => {
    const modes: RepeatMode[] = ['OFF', 'ALL', 'ONE'];
    const nextIdx = (modes.indexOf(repeatMode) + 1) % modes.length;
    setRepeatMode(modes[nextIdx]);
  };

  const handleToggleFavorite = (trackId?: string) => {
    const id = trackId || currentTrack?.id;
    if (!id) return;

    setTracks((prev) =>
      prev.map((t) => (t.id === id ? { ...t, isFavorite: !t.isFavorite } : t))
    );
    setQueue((prev) =>
      prev.map((t) => (t.id === id ? { ...t, isFavorite: !t.isFavorite } : t))
    );
    if (currentTrack && currentTrack.id === id) {
      setCurrentTrack((prev) => (prev ? { ...prev, isFavorite: !prev.isFavorite } : null));
    }
  };

  const handleToggleRpm = () => {
    const nextRpm = settings.rpm === 33 ? 45 : 33;
    setSettings((prev) => ({ ...prev, rpm: nextRpm }));
  };

  // Play All or Shuffle All buttons in Samsung Music subheader
  const handlePlayAll = () => {
    if (filteredTracks.length > 0) {
      handlePlayTrack(filteredTracks[0], filteredTracks);
    }
  };

  const handleShuffleAll = () => {
    if (filteredTracks.length > 0) {
      const shuffled = [...filteredTracks].sort(() => Math.random() - 0.5);
      setQueue(shuffled);
      setCurrentIndex(0);
      playTrackAtIndex(0);
      setIsShuffled(true);
      setIsNowPlayingOpen(true);
    }
  };

  // Import local audio files directly from user device storage
  const handleProcessAudioFiles = async (fileList: FileList | File[]) => {
    const files = Array.from(fileList);
    const audioFiles = files.filter(
      (f) =>
        f.type.startsWith('audio/') ||
        /\.(mp3|wav|ogg|flac|m4a|aac|opus)$/i.test(f.name)
    );

    if (audioFiles.length === 0) return;

    const newTracks: Track[] = [];

    for (const file of audioFiles) {
      const uri = URL.createObjectURL(file);
      const title = file.name.replace(/\.[^/.]+$/, '').replace(/^[0-9\s._-]+/, '');

      const waveform = Array.from({ length: 60 }, () =>
        Math.max(0.1, Math.min(0.95, 0.2 + Math.random() * 0.7))
      );

      const track: Track = {
        id: `local-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
        uri,
        title: title || 'Local Audio Track',
        artist: 'Unknown Artist',
        album: 'My Device Storage',
        durationMs: 180000,
        mimeType: file.type || 'audio/mpeg',
        sizeBytes: file.size,
        dateAdded: Date.now(),
        isFavorite: false,
        hasLyrics: false,
        waveform,
        isCustomFile: true,
      };

      newTracks.push(track);
    }

    setTracks((prev) => [...newTracks, ...prev]);
    setQueue((prev) => [...newTracks, ...prev]);

    if (!isPlaying && newTracks.length > 0) {
      handlePlayTrack(newTracks[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleProcessAudioFiles(e.dataTransfer.files);
    }
  };

  // Filter & sort tracks
  const filteredTracks = useMemo(() => {
    let list = tracks.filter((t) => {
      const matchSearch =
        !searchQuery ||
        t.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        t.artist.toLowerCase().includes(searchQuery.toLowerCase()) ||
        t.album.toLowerCase().includes(searchQuery.toLowerCase());

      if (!matchSearch) return false;

      if (activeTab === 'favourites') return t.isFavorite;
      if (activeTab === 'folders') return t.isCustomFile;
      if (selectedAlbum) return t.album === selectedAlbum;
      return true;
    });

    if (sortBy === 'title') {
      list = [...list].sort((a, b) => a.title.localeCompare(b.title));
    } else if (sortBy === 'artist') {
      list = [...list].sort((a, b) => a.artist.localeCompare(b.artist));
    } else {
      list = [...list].sort((a, b) => b.dateAdded - a.dateAdded);
    }

    return list;
  }, [tracks, searchQuery, activeTab, selectedAlbum, sortBy]);

  // Grouped albums
  const albums = useMemo<Album[]>(() => {
    const map = new Map<string, Track[]>();
    for (const t of tracks) {
      const key = t.album || 'Unknown Album';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(t);
    }
    return Array.from(map.entries()).map(([title, trks]) => ({
      id: title,
      title,
      artist: trks[0]?.artist || 'Various Artists',
      trackCount: trks.length,
      artPath: trks[0]?.artPath,
      year: trks[0]?.year,
      tracks: trks,
    }));
  }, [tracks]);

  // Grouped artists
  const artists = useMemo<Artist[]>(() => {
    const map = new Map<string, Track[]>();
    for (const t of tracks) {
      const key = t.artist || 'Unknown Artist';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(t);
    }
    return Array.from(map.entries()).map(([name, trks]) => {
      const albumsCount = new Set(trks.map((t) => t.album)).size;
      return {
        id: name,
        name,
        trackCount: trks.length,
        albumCount: albumsCount,
        artPath: trks[0]?.artPath,
      };
    });
  }, [tracks]);

  const progress = duration > 0 ? Math.max(0, Math.min(1, currentTime / duration)) : 0;

  return (
    <div
      className="h-screen max-h-screen overflow-hidden flex flex-col bg-[#000000] text-neutral-100 font-sans select-none"
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
      {/* Hidden local audio input picker */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.wav,.ogg,.flac,.m4a,.aac,.opus"
        className="hidden"
        onChange={(e) => {
          if (e.target.files) handleProcessAudioFiles(e.target.files);
        }}
      />

      {/* Drag & Drop Visual Overlay */}
      {isDragOver && (
        <div className="fixed inset-0 z-50 bg-black/85 border-4 border-dashed border-amber-500 backdrop-blur-sm flex flex-col items-center justify-center p-6 text-center pointer-events-none animate-fadeIn">
          <Upload className="w-16 h-16 text-amber-400 mb-3 animate-bounce" />
          <h3 className="text-2xl font-bold text-white">Drop your audio files here</h3>
          <p className="text-sm text-neutral-400 mt-1">Supports MP3, WAV, FLAC, OGG, M4A & AAC</p>
        </div>
      )}

      {/* 1. Samsung Music One UI Top Header (Title on left, Search & Menu on right) */}
      <header className="flex-shrink-0 bg-[#000000] px-5 pt-4 pb-2 z-30">
        <div className="max-w-xl mx-auto flex items-center justify-between gap-3">
          {showSearchInput ? (
            <div className="flex-1 flex items-center gap-2 bg-neutral-900 border border-white/10 rounded-full px-3 py-1.5 animate-fadeIn">
              <Search className="w-4 h-4 text-neutral-400 flex-shrink-0" />
              <input
                type="text"
                autoFocus
                placeholder="Search tracks, artists, albums..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full bg-transparent text-sm text-white focus:outline-none placeholder-neutral-500"
              />
              <button
                onClick={() => {
                  setSearchQuery('');
                  setShowSearchInput(false);
                }}
                className="p-1 text-neutral-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <>
              <h1 className="text-2xl font-bold tracking-tight text-white">
                Isai
              </h1>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => setShowSearchInput(true)}
                  className="p-2 rounded-full hover:bg-white/10 text-white transition-colors"
                  title="Search"
                >
                  <Search className="w-5 h-5" />
                </button>

                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="p-2 rounded-full hover:bg-white/10 text-neutral-300 hover:text-white transition-colors"
                  title="Import Local Audio"
                >
                  <FolderOpen className="w-5 h-5" />
                </button>

                <button
                  onClick={() => setIsSettingsOpen(true)}
                  className="p-2 -mr-2 rounded-full hover:bg-white/10 text-white transition-colors"
                  title="Settings"
                >
                  <MoreVertical className="w-5 h-5" />
                </button>
              </div>
            </>
          )}
        </div>
      </header>

      {/* 2. Samsung Music Category Tabs (Favourites | Playlists | Tracks | Albums | Artists | Folders) */}
      <div className="flex-shrink-0 bg-[#000000] px-5 pt-1 pb-2 z-20">
        <div className="max-w-xl mx-auto flex items-center gap-5 overflow-x-auto no-scrollbar pb-1">
          {[
            { id: 'favourites', label: 'Favourites' },
            { id: 'playlists', label: 'Playlists' },
            { id: 'tracks', label: 'Tracks' },
            { id: 'albums', label: 'Albums' },
            { id: 'artists', label: 'Artists' },
            { id: 'folders', label: 'Folders' },
          ].map((tab) => {
            const isActive = activeTab === tab.id && !selectedAlbum;
            return (
              <button
                key={tab.id}
                onClick={() => {
                  setSelectedAlbum(null);
                  setActiveTab(tab.id as CategoryTab);
                }}
                className={`whitespace-nowrap transition-all ${
                  isActive
                    ? 'text-lg font-bold text-white relative'
                    : 'text-sm font-medium text-neutral-500 hover:text-neutral-300'
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* 3. Subheader Bar (Sort Dropdown on Left, Shuffle All + Play All on Right) */}
      <div className="flex-shrink-0 bg-[#000000] px-5 py-2 z-10">
        <div className="max-w-xl mx-auto flex items-center justify-between">
          {/* Sort Dropdown Toggle */}
          <button
            onClick={() => {
              if (sortBy === 'date') setSortBy('title');
              else if (sortBy === 'title') setSortBy('artist');
              else setSortBy('date');
            }}
            className="flex items-center gap-1.5 text-xs text-neutral-400 hover:text-white font-medium"
          >
            <ArrowUpDown className="w-3.5 h-3.5" />
            <span>
              {sortBy === 'date' ? 'Date added' : sortBy === 'title' ? 'Name' : 'Artist'}
            </span>
          </button>

          {/* Shuffle All & Play All Buttons */}
          <div className="flex items-center gap-2">
            <button
              onClick={handleShuffleAll}
              className="w-9 h-9 rounded-full bg-neutral-900 border border-white/10 hover:bg-neutral-800 text-neutral-300 hover:text-white flex items-center justify-center transition-transform active:scale-90"
              title="Shuffle All"
            >
              <Shuffle className="w-4 h-4" />
            </button>

            <button
              onClick={handlePlayAll}
              className="w-9 h-9 rounded-full bg-neutral-800 hover:bg-neutral-700 text-white flex items-center justify-center transition-transform active:scale-90 shadow"
              title="Play All"
            >
              <Play className="w-4 h-4 fill-white ml-0.5" />
            </button>
          </div>
        </div>
      </div>

      {/* 4. Main Scrollable Container (Only this section scrolls when browsing tracks!) */}
      <main className="flex-1 overflow-y-auto px-5 pb-24 max-w-xl w-full mx-auto flex flex-col gap-1">
        {selectedAlbum && (
          <div className="flex items-center justify-between py-2 border-b border-white/10 mb-2">
            <div>
              <h3 className="text-base font-bold text-white">{selectedAlbum}</h3>
              <p className="text-xs text-neutral-400">
                {albums.find((a) => a.title === selectedAlbum)?.artist} · {filteredTracks.length} tracks
              </p>
            </div>
            <button
              onClick={() => setSelectedAlbum(null)}
              className="text-xs text-amber-400 hover:underline"
            >
              Back
            </button>
          </div>
        )}

        {/* Tracks List (Matching Screenshot 1) */}
        {(activeTab === 'tracks' || activeTab === 'favourites' || activeTab === 'folders' || activeTab === 'playlists' || selectedAlbum) && (
          <>
            {filteredTracks.length === 0 ? (
              <div className="py-20 text-center text-neutral-500 text-sm flex flex-col items-center gap-3">
                <Music className="w-12 h-12 text-neutral-700" />
                <p>No tracks found</p>
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="px-4 py-2 rounded-full bg-neutral-900 border border-white/10 text-xs font-semibold text-neutral-300 hover:text-white"
                >
                  Import Audio from Storage
                </button>
              </div>
            ) : (
              filteredTracks.map((track, idx) => {
                const isCurrent = currentTrack?.id === track.id;
                return (
                  <div
                    key={`${track.id}-${idx}`}
                    onClick={() => handlePlayTrack(track, filteredTracks)}
                    className="flex items-center justify-between py-2.5 px-2 rounded-2xl hover:bg-neutral-900/60 active:bg-neutral-900 transition-colors cursor-pointer group"
                  >
                    {/* Left: Album Art Cover (Rounded Square matching One UI) */}
                    <div className="flex items-center gap-3.5 min-w-0 flex-1">
                      <div className="w-12 h-12 rounded-xl overflow-hidden bg-neutral-900 border border-white/5 flex-shrink-0 flex items-center justify-center">
                        {track.artPath ? (
                          <img src={track.artPath} alt="" className="w-full h-full object-cover" />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center bg-neutral-900 text-neutral-400">
                            <Music className="w-5 h-5 text-neutral-300" />
                          </div>
                        )}
                      </div>

                      {/* Title & Artist */}
                      <div className="min-w-0 flex-1 pr-2">
                        <h3 className={`text-sm font-medium truncate ${isCurrent ? 'text-amber-400 font-bold' : 'text-white'}`}>
                          {track.title}
                        </h3>
                        <p className="text-xs text-neutral-400 truncate mt-0.5">
                          {track.artist}
                        </p>
                      </div>
                    </div>

                    {/* Right: Three Dots Action Menu */}
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setInspectTrack(track);
                        setIsTrackInfoOpen(true);
                      }}
                      className="p-2 -mr-1 rounded-full text-neutral-500 hover:text-white transition-colors"
                      title="More options"
                    >
                      <MoreVertical className="w-4 h-4" />
                    </button>
                  </div>
                );
              })
            )}
          </>
        )}

        {/* Albums Tab (2-Column Large Square Grid Matching Screenshot 2) */}
        {activeTab === 'albums' && !selectedAlbum && (
          <div className="grid grid-cols-2 gap-4 pt-2">
            {albums.map((album) => (
              <div
                key={album.id}
                onClick={() => setSelectedAlbum(album.title)}
                className="flex flex-col gap-2 cursor-pointer group"
              >
                {/* Large Square Rounded Artwork */}
                <div className="w-full aspect-square rounded-2xl overflow-hidden bg-neutral-900 border border-white/5 shadow-md">
                  {album.artPath ? (
                    <img src={album.artPath} alt="" className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center bg-neutral-900 text-neutral-400">
                      <Music className="w-10 h-10" />
                    </div>
                  )}
                </div>

                <div className="min-w-0 px-1">
                  <h4 className="text-sm font-bold text-white truncate group-hover:text-amber-400 transition-colors">
                    {album.title}
                  </h4>
                  <p className="text-xs text-neutral-400 truncate mt-0.5">
                    {album.artist} | {album.trackCount} track{album.trackCount > 1 ? 's' : ''}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Artists Tab */}
        {activeTab === 'artists' && (
          <div className="space-y-1 pt-1">
            {artists.map((artist) => (
              <div
                key={artist.id}
                onClick={() => {
                  setSearchQuery(artist.name);
                  setActiveTab('tracks');
                }}
                className="flex items-center gap-3.5 py-2.5 px-2 rounded-2xl hover:bg-neutral-900/60 cursor-pointer"
              >
                <div className="w-12 h-12 rounded-full overflow-hidden bg-neutral-900 border border-white/5 flex-shrink-0 flex items-center justify-center">
                  {artist.artPath ? (
                    <img src={artist.artPath} alt="" className="w-full h-full object-cover" />
                  ) : (
                    <Music className="w-5 h-5 text-neutral-400" />
                  )}
                </div>
                <div className="min-w-0">
                  <h4 className="text-sm font-semibold text-white truncate">{artist.name}</h4>
                  <p className="text-xs text-neutral-400">
                    {artist.trackCount} track{artist.trackCount > 1 ? 's' : ''} · {artist.albumCount} album{artist.albumCount > 1 ? 's' : ''}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Floating Pill MiniPlayer at the Bottom */}
      <MiniPlayer
        currentTrack={currentTrack}
        isPlaying={isPlaying}
        progress={progress}
        onTogglePlay={handleTogglePlay}
        onPrevious={handlePrevious}
        onNext={handleNext}
        onToggleFavorite={() => handleToggleFavorite()}
        onExpand={() => setIsNowPlayingOpen(true)}
        onOpenQueue={() => setIsQueueOpen(true)}
      />

      {/* Fullscreen Now Playing Stage View (Vinyl Turntable, Tonearm, Synced Lyrics) */}
      {isNowPlayingOpen && (
        <NowPlayingView
          currentTrack={currentTrack}
          isPlaying={isPlaying}
          progress={progress}
          currentTime={currentTime}
          duration={duration}
          volume={volume}
          repeatMode={repeatMode}
          isShuffled={isShuffled}
          isLifted={isLifted}
          settings={settings}
          sleepTimerRemainingMs={sleepTimerRemainingMs}
          isStopAtEndOfTrack={isStopAtEndOfTrack}
          onTogglePlay={handleTogglePlay}
          onPrevious={handlePrevious}
          onNext={handleNext}
          onSeek={handleSeek}
          onSeekProgress={handleSeekProgress}
          onToggleShuffle={handleToggleShuffle}
          onCycleRepeat={handleCycleRepeat}
          onToggleFavorite={() => handleToggleFavorite()}
          onSetVolume={setVolume}
          onSetPlaybackRate={(rate) => audioEngine.setPlaybackRate(rate)}
          onToggleRpm={handleToggleRpm}
          onCollapse={() => setIsNowPlayingOpen(false)}
          onOpenQueue={() => setIsQueueOpen(true)}
          onOpenEqualizer={() => setIsEqualizerOpen(true)}
          onOpenSleepTimer={() => setIsSleepTimerOpen(true)}
          onOpenTrackInfo={() => {
            setInspectTrack(currentTrack);
            setIsTrackInfoOpen(true);
          }}
          onImportLyrics={(lrcText) => {
            if (currentTrack) {
              const updated = { ...currentTrack, lyricsLrc: lrcText, hasLyrics: true };
              setCurrentTrack(updated);
              setTracks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
            }
          }}
        />
      )}

      {/* Queue Drawer Modal */}
      <QueueDrawer
        isOpen={isQueueOpen}
        onClose={() => setIsQueueOpen(false)}
        queue={queue}
        currentIndex={currentIndex}
        onSelectTrack={(idx) => playTrackAtIndex(idx)}
        onRemoveTrack={(idx) => {
          setQueue((prev) => prev.filter((_, i) => i !== idx));
          if (idx < currentIndex) {
            setCurrentIndex((prev) => prev - 1);
          }
        }}
        onMoveTrack={(from, to) => {
          setQueue((prev) => {
            const next = [...prev];
            const [item] = next.splice(from, 1);
            next.splice(to, 0, item);
            return next;
          });
          if (from === currentIndex) {
            setCurrentIndex(to);
          }
        }}
        onClearQueue={() => {
          if (currentTrack) {
            setQueue([currentTrack]);
            setCurrentIndex(0);
          } else {
            setQueue([]);
          }
        }}
      />

      {/* 5-Band Equalizer Modal */}
      <EqualizerModal
        isOpen={isEqualizerOpen}
        onClose={() => setIsEqualizerOpen(false)}
        equalizer={equalizer}
        onChange={setEqualizer}
        customPresets={customPresets}
        onSaveCustomPreset={(name, gains) => {
          const newPreset: EqPreset = {
            name,
            referenceGainsDb: [
              gains[0], gains[0], gains[1], gains[1], gains[2],
              gains[2], gains[3], gains[3], gains[4], gains[4]
            ],
            isCustom: true,
          };
          setCustomPresets((prev) => [...prev.filter((p) => p.name !== name), newPreset]);
        }}
        onDeleteCustomPreset={(name) => {
          setCustomPresets((prev) => prev.filter((p) => p.name !== name));
        }}
      />

      {/* Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        settings={settings}
        onUpdateSettings={(newSettings) => setSettings((prev) => ({ ...prev, ...newSettings }))}
      />

      {/* Sleep Timer Modal */}
      <SleepTimerModal
        isOpen={isSleepTimerOpen}
        onClose={() => setIsSleepTimerOpen(false)}
        remainingMs={sleepTimerRemainingMs}
        isStopAtEndOfTrack={isStopAtEndOfTrack}
        onSetTimer={(mins) => {
          setSleepTimerRemainingMs(mins * 60 * 1000);
          setIsStopAtEndOfTrack(false);
        }}
        onSetStopAtEnd={() => {
          setIsStopAtEndOfTrack(true);
          setSleepTimerRemainingMs(null);
        }}
        onCancelTimer={() => {
          setSleepTimerRemainingMs(null);
          setIsStopAtEndOfTrack(false);
        }}
      />

      {/* Track Metadata Info Modal */}
      <TrackInfoModal
        track={inspectTrack}
        isOpen={isTrackInfoOpen}
        onClose={() => setIsTrackInfoOpen(false)}
      />
    </div>
  );
}
