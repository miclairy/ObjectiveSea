# Game X


The first time the system is run, a config.txt file is generated.

The first value in the config defines a scale speed for the Game to run the race at for convenience of playing.

The next 2 fields define the address and port of the feed to connect to, which by default is the official America's Cup test feed.  
The address to connect into another game is the IP address of the computer acting as server side.  

To configure the system to connect to the Server side, set the SOURCE_ADDRESS to localhost and the SOURCE_PORT to 2828.

Once connected as a player, one is assigned a boat (ID, colour and country).  This is the boat that the connected player can control.
The given boat is shown via a circular highlight, allowing for a player to easily identify themselves.

To control a boat refer to the below key.  A boat, will have a speed based on it's current heading, wind speed and wind direction.
Wind speed is shown via the coloured and moving wind arrow, depicted in the top right hand corner of the course screen.

Keys: 
       
   PgUp: Upwind Key - The heading of the boat it slightly adjusted towards upwind
    
   PgDn: Downwind Key - The heading of the boat it slightly adjusted towards downwind
    
   Shift: Sails in/out Key - toggles the sails in or out depending on sails current status.  Sails in will result in luffing of the sail
    and the boat will slow to a gradule halt.  Conversely, moving your sails out into a powered up position will result in a sail which
    is catching all avaliable wind and the boat will now travel at maximum speed (gradually increase to it if not at it already)
    
   Enter: Tack/gybe Key - The boat will tack or gybe based on boats current heading.  If the boat's heading is in the "dead zone"
    (a zone in which the boat's heading is within the range of optimum TWA already) then this key will have no effect on the 
     boats current heading.  Else the boat will either tack or gybe to the optimum angles.
     
   SpaceBar: VMG key - This key is an autopilot key, which will take the boat to the optimum VMG angle a automatically.

   Control: Multiple selection - Holding down control allows for a user to select multiple boats. This can be
     used to toggle additional annotations such as the distance between two boats.
   
   
Collisions:  When a players boat collides with a mark or other boat, they are visually identified of this via a flashing red circle
appearing around their boat.  Collisions also incur a penalty (based on what was hit and who hit what) and a damage to the overall
boats health.

Boat Health: A boats health affects the overall maximum speed of a boat, if a boat has a health of  0, they are effectively disqualified
with an incurring penalty of a max speed of zero.
