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
I have a very basic status service and file serving service... yeah ts not locked.
## Working on
Going to use this to write out plans for what I am working on.

- Backend Interface
  - Require log-on (Admin Logon done, auth tokens and front end to test necessary.)
    - SQLite Integration
  - Incl. server performance stats
    - Disk R/W by drive
    - CPU Performance
    - Temp's
    - Errors in x time frame (start static, make variable eventually)
  - Incl. recent IPs
    - Interaction #'s by IP
    - Most recent request
  - General log access
    - Searching of server logs
  - Future
    - Samba Integration (User R/W, use, etc)