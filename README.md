# Spotify API App & Song Recommendation Engine
## Written in Scala & the Play Framework

This app allows you to:

* Authenticate with Spotify, and login to your account
* View your current top artists
* View your current top tracks
* View recommended tracks, based on your current top 5 tracks. 
  * You can preview the tracks with an embedded player, and add the tracks you like to your Spotify library from within the app.

To run, ensure you have `sbt` installed, and execute

```bash
sbt run
```

from the root of this project, and then the app should be available at `localhost:9000`