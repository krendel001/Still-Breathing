# First-time upload guide

## 1. Create GitHub repository

1. Go to [github.com/new](https://github.com/new)
2. Repository name: `Still-Breathing`
3. Description: `Minecraft Forge mod — downed state instead of instant death`
4. Set visibility: Public
5. **Do not** initialize with README (this repo already has one)
6. Click **Create repository**

## 2. Push this project

From the mod folder:

```powershell
cd C:\Users\SystemX\Desktop\obmen\minecraft\knockout-mod

git init
git add .
git commit -m "Initial release: Still-Breathing 1.4.7"
git branch -M main
git remote add origin https://github.com/krendel001/Still-Breathing.git
git push -u origin main
```

Replace `krendel001` with your GitHub username if needed.

## 3. Create GitHub Release

1. Open **Releases → Draft a new release**
2. Tag: `v1.4.7`
3. Title: `Still-Breathing 1.4.7`
4. Paste changelog from `CHANGELOG.md`
5. Publish release

The GitHub Action will attach `knockoutmod-1.4.7.jar` automatically.

## 5. Create Modrinth project

Follow [docs/MODRINTH.md](MODRINTH.md) for copy-paste text, tags, and dependencies.

Manual jar path after build:

```
build/libs/knockoutmod-1.4.7.jar
```

## 6. Optional: auto-upload to Modrinth

In GitHub repo **Settings → Secrets and variables → Actions**, add:

| Name | Value |
|---|---|
| `MODRINTH_TOKEN` | Token from [Modrinth settings](https://modrinth.com/settings/account) |
| `MODRINTH_PROJECT_ID` | Project ID from Modrinth project settings |

Then paste the project ID into `modrinth.index.toml` and commit.

Future GitHub Releases will upload to Modrinth automatically.
