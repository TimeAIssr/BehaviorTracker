# 开发指南 (Contributing Guide)

## Git 工作流

### 分支策略

- **`main`** — 主分支，始终保持可发布状态
- **`fix/<description>`** — 修复 bug，如 `fix/boolean-icon-display`
- **`feat/<description>`** — 新功能，如 `feat/dark-mode-toggle`
- **`chore/<description>`** — 构建/工具链变更，如 `chore/upgrade-gradle`
- **`ci/<description>`** — CI/CD 变更，如 `ci/add-release-workflow`

### 开发流程

```bash
# 1. 从 main 创建新分支
git checkout main
git pull
git checkout -b fix/bug-description

# 2. 开发、测试、提交
git add <files>
git commit -m "fix: 描述修复内容"

# 3. 推送分支
git push -u origin fix/bug-description

# 4. 创建 Pull Request
gh pr create --title "fix: 描述" --body "## Summary\n- 变更点1\n- 变更点2"

# 5. 合并后切回 main 并拉取
git checkout main
git pull
```

### Commit Message 规范

格式：`<type>: <描述>`

| Type | 说明 | 示例 |
|------|------|------|
| `fix` | 修复 bug | `fix: 修复打卡式记录按钮图标显示异常` |
| `feat` | 新功能 | `feat: 添加数据导出为 CSV 功能` |
| `docs` | 文档变更 | `docs: 完善 README 数据库设计部分` |
| `chore` | 构建/工具/依赖 | `chore: 升级 Room 到 2.8.4` |
| `ci` | CI/CD 配置 | `ci: 配置签名 release 构建` |
| `style` | 代码格式（不影响逻辑） | `style: 统一 import 排序` |
| `refactor` | 重构（非 bug 非功能） | `refactor: 提取日期工具方法` |
| `test` | 测试相关 | `test: 添加 BehaviorRepository 单元测试` |

规则：
- 使用中文描述
- 首字母小写
- 末尾不加句号
- 保持简洁，一行内说清

## 发布流程

### 前提条件

项目已配置 APK 签名，密钥库通过 GitHub Secrets 注入 CI。

**已配置的 Secrets：**
- `KEYSTORE_BASE64` — 密钥库文件（base64 编码）
- `KEYSTORE_PASSWORD` — 密钥库密码
- `KEY_ALIAS` — 密钥别名
- `KEY_PASSWORD` — 密钥密码

### 发布步骤

```bash
# 1. 确保 main 分支是最新的
git checkout main
git pull

# 2. 推送 tag（版本号格式：vX.Y.Z）
git tag v1.0.2
git push origin v1.0.2
```

推送 tag 后，GitHub Actions 会自动：
1. 从 tag 提取版本号，更新 `build.gradle` 中的 `versionName` 和 `versionCode`
2. 解码密钥库文件
3. 构建**已签名**的 release APK（含 ProGuard 混淆）
4. 创建 GitHub Release 并上传 APK

### 版本号规则

- **主版本号**（X）— 重大架构变更或不兼容更新
- **次版本号**（Y）— 新功能、功能增强
- **修订号**（Z）— bug 修复、小调整

示例：`1.0.0` → `1.0.1`（修复 bug）→ `1.1.0`（新功能）→ `2.0.0`（重大变更）

## APK 签名

### 签名配置

- **密钥库类型**：PKCS12
- **密钥库文件**：`app/behaviortracker.jks`（不提交到 Git）
- **签名配置**：`secrets.properties`（不提交到 Git）
- **CI 签名**：通过 GitHub Secrets 在构建时动态注入

### 本地构建签名 APK

```bash
# 确保 secrets.properties 存在（包含密钥库路径和密码）
./gradlew assembleRelease
```

生成的已签名 APK 位于：`app/build/outputs/apk/release/app-release.apk`

### 密钥库管理

如需重新生成密钥库：

```bash
keytool -genkeypair -v \
  -keystore app/behaviortracker.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias behaviortracker \
  -storepass behaviortracker123 \
  -keypass behaviortracker123 \
  -dname "CN=TimeAIssr, OU=BehaviorTracker, O=Personal, L=Unknown, ST=Unknown, C=CN"
```

更新 GitHub Secret `KEYSTORE_BASE64`：

```powershell
# Windows PowerShell
$b64 = [convert]::ToBase64String([IO.File]::ReadAllBytes("app\behaviortracker.jks"))
gh secret set KEYSTORE_BASE64 --body $b64
```

> **重要**：密钥库文件丢失后无法恢复，已签名的 APK 也无法用新密钥库重新签名。请妥善保管密钥库文件和密码。
