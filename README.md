# Welcome to the Heroes United
![Heroes United Logo](https://cdn.discordapp.com/attachments/710524543961399306/714932503944888370/Heroes_United_Logo_Pixel.png)

A Minecraft mod series for Forge 1.16+

[![](https://cf.way2muchnoise.eu/full_386012_downloads.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/heroes-united)
[![](https://cf.way2muchnoise.eu/versions/386012.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/heroes-united)
[![](https://forthebadge.com/images/badges/built-by-developers.svg)](https://forthebadge.com)
[![](https://forthebadge.com/images/badges/built-with-love.svg)](https://forthebadge.com)
[![](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)
[![](https://forthebadge.com/images/badges/powered-by-electricity.svg)](https://forthebadge.com)
[![](https://forthebadge.com/images/badges/for-you.svg)](https://forthebadge.com)

## About
Heroes United is made up of 4 mods.

These mods are:
 - Core
 - Ben 10
 - Generator Rex
 - Danny Phantom

Heroes United was made to put shows from Cartoon Network into Minecraft for the community.

Each one of the mods are programmed with quality and player experience in mind. This means there is very little lag in any of them and there will be lots of content for the player(s) to enjoy.

We also provide special players that support the project and community with access to Alphas.
These special players could be (but not limited to):

 - Patreon Supporters
 - Team Members
 - Contributors
 

## Issue Reporting
[Submit an Issue!](https://github.com/Heroes-United/HeroesUnited/issues)

Please include the following:

 - Minecraft Version
 - Version of Heroes United Core
 - Mod being used
 - Version of mod being used
 - Any screenshots are a great help
 - For Crashes
	 - Steps to reproduce
	 - Crash log from logs folder [(in a pastebin)](https://pastebin.com/)

## License
All of our mods except the core are under an "All Rights Reserved" license.
This means you cannot (but not limited to):

 - Decompile the mod
 - Modify the code
 - Repost the mod
 - Claim the mod as your own
 - Make money from the mod
 - Make modpacks and upload to a different site from CurseForge
 - Copy models that are used in the mod
 - Copy textures that are used in the mod

With the mods you can:

 - Play them privately or publicly
 - Make a CurseForge modpack
 - Make a private use modpack
 - Make texturepacks
 
The Core mod runs under an edited "MIT" license.

```
The MIT License (MIT) Copyright (c) 2020 Heroes United (BizDC, Ben, Chappie, Grillo78)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, EDITING OF EXISTING CODE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```


## Support
This project requires lots of support to keep the mods updated and maintained aswell as managing the community behind it.
This includes (but not limited to):

 - Paying Developers
 
 The best way to support the project and keep the mods maintained is to become a Patreon.
 Patreons also recieve access to the Dev Server and Alpha builds.
 
[<img src="https://cdn.shopify.com/s/files/1/0071/8107/4489/files/patreondonate_large.png?v=1542314209">](https://www.patreon.com/heroesunited)

## Developers
To use the api in your mod:
```
minecraft.runs.all {
    property 'mixin.env.remapRefMap', 'true'
    property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
}

repositories {
    maven { url 'https://maven.explodingcreeper.me/#/releases' }
}

dependencies {
    implementation "xyz.heroesunited:hu-core:*latest version*"
}
```
