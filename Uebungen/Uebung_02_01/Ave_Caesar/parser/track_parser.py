import json
from dataclasses import dataclass
from typing import List

@dataclass
class Segment:
    segment_id: str
    segment_type: str
    next_segments: List[str]

@dataclass
class Track:
    track_id: str
    segments: List[Segment]

def parse_track_file(file_path: str) -> List[Track]:
    """Liest eine JSON-Datei ein und gibt eine Liste von Track-Objekten zurück."""
    with open(file_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    tracks = []
    for track_data in data["tracks"]:
        track_id = track_data["trackId"]
        segments = [
            Segment(
                segment_id=seg["segmentId"],
                segment_type=seg["type"],
                next_segments=seg["nextSegments"]
            )
            for seg in track_data["segments"]
        ]
        tracks.append(Track(track_id=track_id, segments=segments))

    return tracks
