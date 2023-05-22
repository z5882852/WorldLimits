# WorldLimits

![Author](https://img.shields.io/badge/Author-z5882852-blue) ![Version](https://img.shields.io/github/v/release/z5882852/WorldLimits?label=Version) ![](https://img.shields.io/badge/Bukkit/Spigot-1.12.2-blue.svg)

## 世界限制（WorldLimits） for Minecraft 1.12.2

该插件允许您限制玩家在家园中放置特定方块的数量，当超出限制时阻止玩家放置方块，并提供了两种处理方式：清除超过限制的方块或者清除全部限制的方块。

## 使用说明

注意：本插件需要安装前置插件 [YukiNoaAPI](https://www.mcbbs.net/thread-1214707-1-1.html)

该插件提供了以下命令：

- `/wl add <限制数量> <方块大小>`：请对准已经放置的方块使用，将指定方块类型添加到限制列表中，并设置限制数量和大小（默认为1）。

- `/wl clear`：清除所有超过限制数量的方块，（前提是在配置文件设置允许清除）。

- `/wl reload`：重载配置文件。

请注意，只有拥有适当权限的玩家可以使用这些命令。

## 配置

插件的配置文件`config.yml`位于插件目录下的`BlockLimitPlugin`文件夹中。您可以根据需要进行以下配置：

[配置文件](https://github.com/z5882852/WorldLimits/blob/mai/src/main/resources/config.yml)

## 插件限制

该插件通过获取方块的类型(type)和子id(data)来限制方块，当方块的类型和子id相同而NBT不同时仍视为一种方块。

已知方块的类型和子id相同的方块有：

- 工业2的机器

- 通用机械的各种工厂方块

- 通用机械:发电的风里发电机和太阳能发电机

- 植物魔法的各种花

请勿在限制名单中添加以上方块（目前已正常部分方块，具体请查看[MOD支持](https://github.com/z5882852/WorldLimits/tree/mai#mod%E6%94%AF%E6%8C%81)）

## MOD支持

- `通用机械(Mekanism)` : 支持不同等级的工厂方块限制(使用nbt)。

- `植物魔法(Botania)` : 支持不同功能花、产能花、浮空花的限制(使用nbt)

## 注意事项

- 插件的限制方块数据储存在`limit.yml`，家园限制方块统计储存在`data.yml`。

- 该插件不提供删除限制方块的指令，如果要删除限制方块，请到`limit.yml`中删除数据。

- 该插件为定制插件，仅适用于1.12.2版本的服务端，并且不会定期进行更新和维护。如果您需要在其他版本上使用，请参考插件源码自行构建适配版本。

- 不正确使用该插件造成的损失本人概不负责。

## BUG

目前该插件存在BUG，为了防止有人使用BUG越过限制，所以暂时不公布BUG。

当然，如果您想提供一些建议或者获取这些BUG，您可以通过以下方式联系我：

- z5882852@qq.com

- silence5882852@gmail.com
