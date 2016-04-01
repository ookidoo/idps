# Introduction #

Since I have seen some people now spreading myths or using iDPS wrong think it's time to explain slightly more detailed how the Combat Model actually works.


# Details #

The only real way of modelling a rogue dps cycle (without actually simulating) is to work with averages. There the most important averages are:

- Average CP per SS
- Average Energy Regeneration

With these values you can calculate the basic cycle: How much Sinister Strikes do i need to maintain the selected Finishers with the desired uptime. These Sinister Strikes and Finishers cost energy, whatever energy is left is used to generate additional Sinister Strikes and Eviscerates.

Since there is a reverse influence from Special Attacks -> Procs -> Crit/Haste ratings into average CP per SS and average energy regeneration iDPS is using a "tripple overpass": The cycle is calculated 3 times in a row, each time using the previous results as entry values.

From these cycle calculations iDPS now knows how many Sinister Strikes, Eviscerates etc you will be using per timeframe and as a result, using damage formulas for all kind of different attacks, your total DPS.

- to be extended -

## Rupture Cycle ##

However, the rogue cycle is, thanks to Ruthlesness, SS Glyph, 4p T10, Combat Potency and other procs extremely random and basically unpredictable. For this reason the model cannot tell you how much rupture uptime you will achieve in your gear/spec. This is what the uptime slider is for. For most combat rogues rupture uptime is in the area from 75-80%. I do not think it is physically possible to get much more then 90% due to overlapping SnD and Rupture finishers. Also a playstyle that aims for extreme rupture uptimes will most likely actually cost you dps due to energy caping and/or wasting CPs.

So always remember: iDPS can play a perfect cycle with 100% rupture and SnD uptime, not wasting any energy or CPs while still getting the odd eviscerate finisher in. Any real rogue will fail.