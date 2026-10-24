"""Create public release metadata; never read or export signing secrets."""
import hashlib
import json
import pathlib
import re
import shutil

metadata = json.loads(pathlib.Path("app/build/outputs/apk/release/output-metadata.json").read_text())
assert metadata["applicationId"] == "app.homedl"
element, = metadata["elements"]
version = element["versionName"]
assert re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+", version)
apk = pathlib.Path("app/build/outputs/apk/release") / element["outputFile"]
data = apk.read_bytes()
assert 0 < len(data) <= 150 * 1024 * 1024
folder = pathlib.Path("release")
folder.mkdir(exist_ok=True)
shutil.copyfile(apk, folder / "Home-DL.apk")
manifest = {
    "versionCode": element["versionCode"],
    "versionName": version,
    "url": f"https://github.com/9BillyTheDev2/Home-DL/releases/download/v{version}/Home-DL.apk",
    "size": len(data),
    "sha256": hashlib.sha256(data).hexdigest(),
}
(folder / "update.json").write_text(json.dumps(manifest, indent=2) + "\n")
print("Release files prepared for Home-DL", version)
