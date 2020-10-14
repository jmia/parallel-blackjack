# parallel-blackjack
### Program Arguments
client: `[number of players]` (e.g. 2)
server: `[ip address] [port number]` (127.0.0.1 61013)
### Intended Program Flow
- server opens
- client connects
- welcome message is sent to client
- client goes to sleep
- server waits for second client
- second client connects
- welcome message is sent to client
- client goes to sleep
- **The Game Begins!**
- dealer gets a card
- player1 gets a card
- player2 gets a card
- dealer gets a card
- player1 gets a card
- player2 gets a card
- **All players are told the state of play**
- player 2 is told to wait
- player 1's thread wakes up
- player 1 plays a round until they stop, blackjack, or bust
- their **round** is over
- they go to sleep until the end of the game
- all threads are notified
- player 2's thread wakes up
- player2 gets to do their thing
- their **round** is over
- the **game** is flagged as over
- they go to sleep until the end of the game
- **The Game is Over!**
- the game is recognized as over **by main**
- main tallies up all the good stuff
- **all threads** print out the results
- all clients disconnect
- server shuts down
- **Everyone gets cake.**