# parallel-blackjack

### How I expect the message flow to go:

server opens

client connects

message is sent to client

client goes to sleep

server waits for second client

second client connects

message is sent to client

client goes to sleep

##### **GAME BEGINS**

dealer gets a card

player1 gets a card

player2 gets a card

dealer gets a card

player1 gets a card

player2 gets a card

**all players are told the state of play**

player 1's thread wakes up

player1 gets to do their thing

their **round** is over

they go to sleep until the end of the game

all threads are notified

player 2's thread wakes up

player2 gets to do their thing

their **round** is over

the game is flagged as over

they go to sleep until the end of the game

the game is recognized as over **by main**

main tallies up all the good stuff

**all threads** print out the results

all clients disconnect

server shuts down
