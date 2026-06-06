# HotBaaaar — 超长快捷栏（纯客户端 fork）/ Super long hotbar (client-only fork)

[中文](#中文) · [English](#english)

> 本仓库是 [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar) 的 fork，改造为**纯客户端**并扩展为多版本/多加载器矩阵。
> This repository is a fork of [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar), reworked to be **client-only** and expanded into a multi-version / multi-loader matrix.

---

## 中文

**HotBaaaar** 把快捷栏扩展成最多 4 行（你的整个 36 格背包），是一个 **纯客户端** mod。

用滚轮滚到当前行的边缘时会自动「翻行」——把那一整行通过容器点击换进真实快捷栏（第 0–8 格）。这是原版服务器本来就接受的操作，所以 **服务器无需安装本 mod**，你可以在任意原版 / 无 mod 的联机服上使用。

- 打开背包（E）时会自动把物品**复位成正常顺序**，界面看起来和原版一致；
- 关闭背包时再把你当前所在的行换回来，**手持物品保持不变**。

### 支持的版本与加载器

本仓库用**分支矩阵**管理：每个 `mc/<版本>-<加载器>` 分支都是一个独立可构建的工程，`master` 只作为放置 CI / 文档的 hub。

|  | 1.18.2 | 1.19.2 | 1.20.1 | 1.21.1 |
|--|:--:|:--:|:--:|:--:|
| **Forge** | ✅ | ✅ | ✅ | ✅ |
| **NeoForge** | — | — | ✅ | ✅ |
| **Fabric** | ✅ | ✅ | ✅ | ✅ |

下载见 [Releases](../../releases)；构建说明与如何新增目标见 [BUILDING.md](BUILDING.md)。

### 与原版（fork 前）的区别

原版 [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar) 是 **NeoForge 的两端 mod**：服务端也必须安装，通过网络包把每个玩家的屏幕宽度同步给服务端，从而决定快捷栏格数。

本 fork：
- **纯客户端**——用「翻行」技巧绕过服务端对「手持哪一格」的校验，**服务器不用装**；
- 扩展为 **Forge / NeoForge / Fabric × 1.18.2 / 1.19.2 / 1.20.1 / 1.21.1** 的矩阵。

### 致谢

感谢原作者 **USS_Shenzhou** 的创意与原版实现 ❤️。

### 许可

GPLv3，详见 [LICENSE](LICENSE)。

---

## English

**HotBaaaar** extends the hotbar to up to 4 rows (your whole 36-slot inventory). It is a **client-only** mod.

Scrolling past the edge of the current row auto-"flips" rows: that whole row is swapped into the real hotbar (slots 0–8) via container clicks — an operation vanilla servers already accept. So **the server does not need this mod**, and you can use it on any vanilla / unmodded multiplayer server.

- Opening your inventory (E) automatically **restores the normal slot order**, so the screen looks just like vanilla;
- Closing it re-applies your current row, so the **held item is preserved**.

### Supported versions & loaders

Managed as a **branch matrix**: every `mc/<version>-<loader>` branch is a standalone buildable project, while `master` is just a hub holding the CI / docs.

|  | 1.18.2 | 1.19.2 | 1.20.1 | 1.21.1 |
|--|:--:|:--:|:--:|:--:|
| **Forge** | ✅ | ✅ | ✅ | ✅ |
| **NeoForge** | — | — | ✅ | ✅ |
| **Fabric** | ✅ | ✅ | ✅ | ✅ |

Downloads: [Releases](../../releases). Building / adding targets: [BUILDING.md](BUILDING.md).

### Difference from the upstream (pre-fork) original

The original [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar) is a **both-sides NeoForge mod**: the server must also install it, and it syncs each player's screen width to the server to decide the hotbar size.

This fork:
- is **client-only** — it uses the row-flip trick to work around the server's held-slot validation, so **no server install is needed**;
- expands into a **Forge / NeoForge / Fabric × 1.18.2 / 1.19.2 / 1.20.1 / 1.21.1** matrix.

### Credits

Thanks to the original author **USS_Shenzhou** for the original idea and implementation ❤️.

### License

GPLv3 — see [LICENSE](LICENSE).
