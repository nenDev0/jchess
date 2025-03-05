# jchess

## Description

This was a fun challenge to myself... I wanted to create a Chess-Bot and see, how good I could get it to be.
Turns out, not very good.

This project took me about 5 weeks full-time to get running. And I mean full-time.

## Project

### What the Project includes

- Java
- fully functional Chess-Game with reverse-function
- frontend to view and interact with the game
- a Chess-Bot, currently only set to play against another version of itself
- outdated and badly designed plantuml diagrams
- a... few bugs. :)


### What this Project does not include

- easy and intuitive customizability
- updated diagrams
- functional warranty
- promises for your eyes and brain to function after reading the code

### Requirements

- Java
- JUnit with Hamcrest-Core (included in .lib)


### Future Development

- I make no promises of continuing development on this project
- I might rewrite it into C++ for more control and therefore performance enhancements
- if I find the motivation, large parts may simply have a complete overhaul

feel free to assist me in my effort


## Sidenotes (Don't @ me about these)

- For my projects, I make my own Coding Rules. I can simply read it better that way
- Yes. Some things in the code really make no sense. If I wrote this project again, I would do a lot of things very differently.
- I attempted to care about object-orientation, however at some points, I just wished it to work at all

## Authors
@nenDev0

## Github Usage

### Git Issues

Git Issues are the way any future features, improvements, bugs, etc. are documented. As they get implemented the issues are subsequently closed.

### Branches

- developmemt:
any development, currently going on in the separate components, will be finalized here before a merge with the main branch

- dev/engine, dev/intelligence, dev/application:
selfexplanatory. The idea is to push any changes to these components via their designated branch. 

- dev/testing:
Any changes, which require thorough testing, will be pushed here.
This means that it will most likely be filled with merges etc.
It is possible, this branch will mostly be used locally, meaning little to no changes will be visible on Github.


## Code Notes

Before you start reading or messing with the code, these notes may help you understand some things.

- Exceptions are mostly not handled:
Exceptions are mostly not handled, since any errors in the code will lead to it being unplayable anyways.
They should ideally not happen in the first place, as that would point towards an issue in the code.
  
