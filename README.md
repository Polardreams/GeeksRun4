# GeeksRun4
GeeksRun is a gamifyed run app.
Create by David Hernandez
I start the project at 2015 (developing) and finished it, one year later (2016).

1) Download as zip and extract
2) Disable Instant Run (File -> Settings -> Build, Execution, Deployment -> Instant Run)
3) implement multidex*
4) clean and rebuild project
5) enter your Google Maps API Key (AndroidManifest.xml)


*Multidex
modify your project gradl

a) write in defaultConfig {} ... multiDexEnabled true
    
b) write dependencies {} ... implementation 'com.android.support:multidex:1.0.3'

c) go to AndroidManifest and include the following in the application-tag ...
    android:name="android.support.multidex.MultiDexApplication"
        
Funktions of GeeksRun
- analyse and safe your running datas (Map, Time Laps, Graphs, speed, distance ...)
- Google Maps
- musicplayer
- geo Trigger for music player
- speech and comments during running with a included complex comment-system (like the game : Fifa 98 for playstation 1)
- challenge yourself with bots of your old run-datas (every file (run history) can be a bot)
- overview of your goblets and performances
- run history (simple entries)
        
