# Game X

On startup a Menu is displayed.  The menu allows a user to choose between three options:

Live Game:

    On selecting live game, the user is allowed to choose to host, join or spectate a game.
    
    Host: User needs to enter a port number (e.g 2828).  The user then has the option of 6 maps,
    each map offers a unique racing experience.  Once selected the user enters the game.
    
    Join: User must enter a valid IP address (that of a host) and the corresponding port.
    Once selected, the user enters a game with other players (at least one) and begins the game.
    
    Spectate: User must enter a valid IP address (that of a host) and the corresponding port.
    Once selected, the user enters a game with other players (at least one) and watches the game.

Practice:

    On selecting practice the user is allowed to choose to practice start or single play.
    
    Practice start: The user enters a starting practice game, with only a start line and
    the goal is to start on time.  Pop ups are available to inform user of their progress
    
    Single Play: the user enters a map selection, with 6 courses to choose from.  The courses
    are each unique.  On entering a course, the user has the option to race against a computer
    simulated boat (easy or hard level).

Tutorial:

    Tutorial mode takes the user instantly into a game screen (no maps or marks) and teaches the
    user the keys one by one, providing valuable feedback via the popups.

General info:
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