# How to work on the bot

To run the test scripts you will need Python 3 and (probably) Windows.

## Structure
* `ref_best` contains the best bot so far. Please do not modify it directly, instead make a copy.
* `dummy` contains a dummy bot that does nothing.
* To start, first copy `ref_best`.


### Copy Bot
* Open a terminal or shell in `src` (the folder that this README is in) and run
```sh
python copybot.py ref_best {your_name}
```
using `your_name` so we can better track changes.

### Test Bot
1. Make changes in the package that you copied.
2. Before updating `ref_best`, we should run tests on it to make sure that the changes are actually effective.
3. To test it, run the following in a terminal:
```sh
python compare_bots.py ref_best {your_bot}
```

4. The script will show you your bot's win rate and it will take some time to run all the maps. If your bot is good enough - use your own judgment, marginal improvements may not outweight the bytecode costs if you added a lot of code - then copy your bot to `ref_best` and commit your changes, include the bot's winrate against previous best, and changes you made.


### What to add
* splashers
* tune heuristics
* communication
* ???