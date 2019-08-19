# GeeksRun4
GeeksRun is a gamifyed run app.
Create by David Hernandez

1) Download as zip and extract
2) Disable Instant Run (File -> Settings -> Build, Execution, Deployment -> Instant Run)
3) implement multidex*
4) clean and rebuild
5) enter your Google Maps API Key (AndroidManifest.xml)

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
        
