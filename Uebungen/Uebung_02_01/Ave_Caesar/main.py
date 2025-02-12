from parser.track_parser import parse_track_file

if __name__ == "__main__":
    tracks = parse_track_file("CuCuCo/exampe_track.json")

    for track in tracks:
        print(f"Track ID: {track.track_id}")
        for segment in track.segments:
            print(f"  Segment: {segment.segment_id}, Type: {segment.segment_type}, Next: {segment.next_segments}")
