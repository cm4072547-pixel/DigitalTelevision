# DigitalTelevision Player (starter)

A lightweight Romanian-language M3U web player/PWA starter.

## Run locally
Serve this folder over HTTP (not `file://`). For example:
```bash
python -m http.server 8000
```
Then open `http://localhost:8000`.

## Publish
Upload the files to a GitHub repository and enable GitHub Pages (Settings → Pages → Deploy from a branch → `main` → `/ (root)`).

## Notes
- This starter accepts M3U text files and M3U URLs.
- URL loading may fail if the playlist host blocks browser CORS. Uploading the M3U file is an alternative.
- HLS playback depends on browser/device codec support. Some streams use formats browsers cannot play.
- The playlist is not sent to a custom backend by this app; do not use shared devices for private credentials.
- Use only playlists and streams you are authorized to access. This starter does not provide channels or subscriptions.
- This is a web/PWA starter, not yet a signed Android APK or a native Android TV app.
