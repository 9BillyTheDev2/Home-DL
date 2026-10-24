"""Fetch the pinned official Python zipapp; fail the build on any mismatch."""
import hashlib
import pathlib
import urllib.request
import zipfile

VERSION = "2026.10.21"
SHA256 = "b7af77f686e95f7845ce8d09d21957718febb8b48b30555adb9dd2292ad7bf14"
target = pathlib.Path("app/src/main/assets/engine/yt-dlp")
url = f"https://github.com/yt-dlp/yt-dlp/releases/download/{VERSION}/yt-dlp"
with urllib.request.urlopen(url, timeout=30) as response:
    data = response.read(20 * 1024 * 1024 + 1)
if hashlib.sha256(data).hexdigest() != SHA256:
    raise RuntimeError("Official engine checksum mismatch")
target.parent.mkdir(parents=True, exist_ok=True)
target.write_bytes(data)
with zipfile.ZipFile(target) as archive:
    assert archive.testzip() is None
print(f"Official engine {VERSION} verified and packaged")
