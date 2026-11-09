OldTube - client that imitates the look of the first Android YouTube app.

It loads videos from an Invidious server. The server address is the constant
BASE in app/src/main/java/com/example/oldtube/Api.java (currently
http://s60tube.io.vn). Use http:// - old Android cannot do modern https.

Open this folder in Android Studio (File > Open). If the build fails, check in
app/build.gradle that compileSdkVersion / buildToolsVersion match what you have
installed in the SDK Manager.
