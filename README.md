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
a) write in app gradle:
defaultConfig {
        //...
        multiDexEnabled true
    }
    
dependencies {
  //...
    implementation 'com.android.support:multidex:1.0.3'
}

b) go to AndroidManifest and include the following in the application 
    <application
//...
        android:name="android.support.multidex.MultiDexApplication" >
        
Funktions of GeeksRun
- analyse and safe your running datas (Map, Time Laps, Graphs, speed, distance ...)
- Google Maps
- musicplayer
- geo Trigger for music player
- speech and comments during running with a included komplex commit-system (like the game : Fifa 98 for playstation)
- Challenge, run again yourself with a bot of your old runns
- overview of your rewards and performances
        
