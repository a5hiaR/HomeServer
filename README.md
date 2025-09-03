# HomeServer
Basic Java application for managing server connections, and running my home server.
## Plans
See below inteded features.
### Front end
- Simple log-on (guest access aloud)
  - Include info about user, such as time through x movie.
  - Guests only get access to the media server.
-  Media server
  - Display available media
  - Implement playback through browser with JellyFin
- Other servers
  - Program basic interface with servers
  - As well as access to Back End Interface
  - Efficient deploying of new servers (i.e. spinning up a minecraft server) via Docker
  - File server via Samba
### Back End
- Media Server
  - Interact with JellyFin, SQLiteDB and Back End Interface.
- Back End Interface
  - Active connections + info on what media/server
  - Easily block connections, persistent IP block through DB
  - Logging
    - Non Errors:
    - Who, What, Where, When, latency
    - Error logging ofc
    - Include information when high latency or client has to pause to await media
## Progress
I have yet to start, and this is honestly a test commit.