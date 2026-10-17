# Ctrl 功能菜单（tidin_ctrl_ctrl_ctrl_ctr）

Minecraft **1.20.1 / 1.20.4** 双版本 Fabric 客户端模组。按一次右 Ctrl 呼出居中的半透明黑色「功能」面板。

面板分三个标签页：

| 标签页 | 内容 |
| --- | --- |
| 工具 | 复制坐标、HUD 开关、F3 调试、打开原版设置、旋转 / 角色旋转 / 抬臂旋转、模型隐身、盘腿坐、自动钓鱼、转 R6 |
| 世界 | 时间白天 / 夜晚、天气晴 / 雨（需作弊权限） |
| 设置 | 呼出音效、信息浮层、恢复默认 |

全部为客户端表现：不改真实坐标、不改碰撞箱、不发包改朝向，别人看到的你始终正常。

---

## 一、版本矩阵

| Minecraft | Yarn | Loader | Fabric API |
| --- | --- | --- | --- |
| 1.20.1 | `1.20.1+build.10` | `0.16.14` | `0.92.12+1.20.1` |
| 1.20.4 | `1.20.4+build.3` | `0.16.14` | `0.97.2+1.20.4` |

构建工具：**Loom 1.7.4 + Gradle 8.8 + JDK 21（编译目标 Java 17）**。

整套依赖由一个开关切换，不需要手动改三处版本号：

```bash
gradle build -Pmc_version=1.20.1
gradle build -Pmc_version=1.20.4
```

产物分别落在 `build/libs/`，文件名带版本后缀，不会互相覆盖：

```
tidin_ctrl_ctrl_ctrl_ctr-1.0.0+1.20.1.jar
tidin_ctrl_ctrl_ctrl_ctr-1.0.0+1.20.4.jar
```

---

## 二、直接在 GitHub 上编译（推荐）

本机没有 Gradle 也能出 jar，全部跑在 GitHub Actions 里。

1. 在 GitHub 新建一个空仓库（不要勾选 README / .gitignore，避免冲突）。
2. **把本目录（`mod/`）作为仓库根目录**推上去 —— `.github/workflows/build.yml` 必须在仓库根，否则 Actions 不会触发：

   ```bash
   cd mod
   git init
   git add .
   git commit -m "Minecraft 1.20.1 / 1.20.4 Fabric mod"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/<仓库名>.git
   git push -u origin main
   ```

3. 打开仓库的 **Actions** 页，`Build` 工作流会自动跑起来，两个版本并行构建。
4. 跑完后在对应那次运行页面底部 **Artifacts** 下载：
   - `tidin-ctrl-menu-1.20.1`
   - `tidin-ctrl-menu-1.20.4`

   解压后就是可直接丢进 `.minecraft/mods/` 的 jar。

也可以在 Actions 页面点 **Run workflow** 手动触发。

### 工作流做了什么

- JDK 21（Temurin）
- 下载官方 Gradle 8.8 分发包（仓库里不放 `gradle-wrapper.jar` 这类二进制）
- 缓存 `~/.gradle` 加速后续构建
- 先跑 `printProfile` 打印本次使用的版本组合，再 `gradle build`
- 上传 `build/libs/*.jar`

---

## 三、本地构建

### 方式 A：命令行

需要 JDK 17 或 21（**注意别用 JDK 25，Gradle 8.8 不认识它**）。

```bash
# 先生成 wrapper（仓库里没有 gradlew，见下方说明）
gradle wrapper --gradle-version 8.8

./gradlew build -Pmc_version=1.20.1      # Windows: gradlew.bat build -Pmc_version=1.20.1
./gradlew build -Pmc_version=1.20.4
```

### 方式 B：IntelliJ IDEA

直接 `File → Open` 打开本目录，IDEA 会用 `gradle-wrapper.properties` 里的 8.8 自行下载 Gradle，然后在 Gradle 面板执行 `Tasks → build → build`（需要切版本时在 `Settings → Build Tools → Gradle` 里加 `-Pmc_version=` 参数，或命令行执行）。

### 关于 `gradlew` / `gradle-wrapper.jar`

仓库只带了 `gradle/wrapper/gradle-wrapper.properties`，**没有** `gradlew`、`gradlew.bat`、`gradle-wrapper.jar`（二进制文件不便直接生成入库）。两条路都行：

- GitHub Actions：不依赖 wrapper，直接用分发包，开箱即用；
- 本地：跑一次 `gradle wrapper --gradle-version 8.8` 自动补齐三个文件，之后就用 `./gradlew`。

---

## 四、目录结构

```
mod/
├── .github/workflows/build.yml         # GitHub Actions：双版本并行构建
├── build.gradle                        # Loom + 多版本档案表 MC_PROFILES
├── settings.gradle                     # Fabric Maven（否则找不到 loom 插件）
├── gradle.properties                   # mc_version 开关 + 模组元信息
├── gradle/wrapper/gradle-wrapper.properties
├── LICENSE
└── src/main/
    ├── java/cn/blockforge/generated/tidinctrlctrlctrlctr/
    │   ├── FunctionMenuMod.java            # 入口（ClientModInitializer）
    │   ├── FunctionMenuScreen.java         # 面板 UI
    │   ├── SpinController.java             # 旋转 / 角色旋转 / 抬臂旋转
    │   ├── ModelHideController.java        # 模型隐身
    │   ├── SitPoseController.java          # 盘腿坐
    │   ├── AutoFishController.java         # 自动钓鱼（读屏 + 音效触发）
    │   ├── R6ModelController.java          # 转 R6 比例
    │   ├── WorldInfo.java                  # 世界信息格式化
    │   └── mixin/                          # 6 个 Mixin
    └── resources/
        ├── fabric.mod.json                 # ${version} / ${minecraft_dep} 由 Gradle 注入
        ├── tidin_ctrl_ctrl_ctrl_ctr.mixins.json
        ├── pack.mcmeta
        └── assets/tidin_ctrl_ctrl_ctrl_ctr/{lang,textures/gui}
```

`fabric.mod.json` 里两处占位符在构建时自动填充：

- `${version}` ← `1.0.0+<mc_version>`
- `${minecraft_dep}` ← 1.20.1 用 `~1.20.1`，1.20.4 用 `>=1.20.2 <1.21`

---

## 五、已知风险点（1.20.4 首轮编译需要确认）

源码是按 **1.20.1** 写的，1.20.1 这一路是稳的。1.20.2~1.20.4 的 Yarn 映射对这四处的语义我最需要确认，若 CI 报错基本就在这里面：

| 位置 | 调用的东西 | 说明 |
| --- | --- | --- |
| `FunctionMenuScreen` | `renderBackground(DrawContext)` | 改签名是 1.20.5 的事，1.20.4 应仍是单参；报错就改覆写 |
| `FunctionMenuScreen` | `options.debugEnabled` | 若被改名 / 移除，改用其它方式或删掉该按钮 |
| `AutoFishController` | `Framebuffer.viewportWidth/Height`、`.fbo` | 这三个字段在 1.21.2 才改成方法，1.20.4 应仍可用 |
| 各 Mixin | `GameRenderer#render(FJZ)V`、`HeldItemFeatureRenderer#renderItem(...)` | 1.20.5 起才换 `RenderTickCounter` / 渲染状态，1.20.4 应一致 |

把 CI 的报错原文贴出来即可定位——多版本工程首轮编译碰 2~3 个 API 差异是正常的。

---

## 六、安装与使用

1. 装好 **Fabric Loader ≥ 0.16.14** 与 **Fabric API**（对应版本）。
2. 把 jar 丢进 `.minecraft/mods/`。
3. 进游戏按 **右 Ctrl**（左 Ctrl 是原版疾跑，已避开）呼出面板；再按一次或 Esc 关闭。
   可在 `选项 → 控制 → 按键绑定` 里改键。

---

## 七、常见报错

| 报错 | 原因 / 处理 |
| --- | --- |
| `Plugin [id: 'fabric-loom'] was not found` | `settings.gradle` 的 `pluginManagement` 里没加 Fabric Maven |
| `Unsupported class file major version` / Gradle 启动即崩 | JDK 版本太新（如 25）。用 JDK 17 或 21 |
| `Could not resolve net.fabricmc:yarn:...` | Yarn / API 版本号拼错，或该版本不存在 |
| `Unknown mc_version=...` | 只支持 `1.20.1` / `1.20.4`，见 `build.gradle` 的 `MC_PROFILES` |
| 游戏里 Mixin 应用失败 | 用了不匹配的 MC 版本 jar |
