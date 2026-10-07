# Seasonality
**Note: This mod will be updated very slowly**

Seasonality implements the four seasons (spring, summer, fall, and winter) in Minecraft 1.12.2. Although it shares a similar concept with previous alternatives, it has been completely rewritten from scratch under a modern open-source license

The core game logic has been fully replicated and works flawlessly (except for some beta bugs that are currently being fixed and will be resolved)

## Features
* **Performance comes first (at least for me):** Designed for low-end hardware. The heavy legacy ASM processes have been replaced with optimized mixins, and most of the code if not all, then certainly the vast majority has been optimized
* **Smart snow recalculation:** Handlers to calculate seasonal effects, such as winter leaving snow behind when the chunk was loaded in the middle of summer
* **Dynamic planetary rotation:** Subtle and realistic variations in the length of the day-night cycle depending on the current season, simulating Earth’s orbital tilt (though I’m no scientist I just Googled it)

**About the license**: This mod specifically the final `.jar` file and the source code does not distribute any texture files or code from Serene Seasons, since it is up to the user to download the textures to their computer from the original repository and decide whether to play without textures or with Serene Seasons textures

**Regarding compatibility with other mods**: There isn’t any yet, since this is a complete rewrite from scratch, but it will be available soon

> the source code contains 4,932 lines of code written from April through June and from early September through today (7/10/26)
