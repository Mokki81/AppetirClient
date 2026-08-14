# Repo cleanup — remove tracked junk

`.gitignore` now ignores `.gradle/`, `build/`, `run/`, `.idea/`, logs, runtime files.
**Git still tracks files that were committed before.** Remove them from the index (keeps local files):

```bash
git pull origin main

# Stop tracking generated / runtime junk
git rm -r --cached .gradle 2>/dev/null || true
git rm -r --cached build 2>/dev/null || true
git rm -r --cached run 2>/dev/null || true
git rm -r --cached logs 2>/dev/null || true
git rm -r --cached libraries 2>/dev/null || true
git rm -r --cached .idea 2>/dev/null || true
git rm --cached options.txt optionsof.txt usercache.json 2>/dev/null || true

git add .gitignore
git commit -m "Stop tracking build caches and runtime junk"
git push origin main
```

## Optional: shrink history (~600 MB → tiny)

Only if you are OK rewriting history (coordinate with anyone else using the repo):

```bash
# Requires git-filter-repo: https://github.com/newren/git-filter-repo
git filter-repo --path .gradle --invert-paths --force
git filter-repo --path run --invert-paths --force
git filter-repo --path logs --invert-paths --force
git filter-repo --path libraries --invert-paths --force
# then force-push:
git push origin --force --all
```

Or with BFG:
```bash
bfg --delete-folders .gradle
bfg --delete-folders run
git reflog expire --expire=now --all && git gc --prune=now --aggressive
```
