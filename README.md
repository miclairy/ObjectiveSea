# Race Vision Pro


The first time the system is run, a config.txt file is generated.

The first value in the config defines a scale speed for the MockFeed to run the race at for convenience of testing.

The next 2 fields define the address and port of the feed to connect to, which by default is the official America's Cup test feed.

To configure the system to connect to the MockFeed, set the SOURCE_ADDRESS to localhost and the SOURCE_PORT to 2828.
