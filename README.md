## Overview

The idea of tests is to try out changes and test them against the best bot so far. We only merge changes that improve the eval of the bot (+ optimizations). This means that at every merge the bot gets better.

## How to work on the bot

To run the test scripts you will need Python 3.

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
1. Make changes in the package that you copied. Test your changes in the Battlecode client first. You can copy your bot into a package called `temp`, this will be ignored by Git.
2. Before updating `ref_best`, we should run tests on it to make sure that the changes are actually effective.
3. Git pull first. This will update `ref_best` if there are any changes. Fast-forward if necessary. If you ran `compare_bots` before pulling, the logs may be updated and you might need to merge.
4. To test it, run the following in a terminal:
```sh
python compare_bots.py ref_best {your_bot}
```
Notes:
* closing the Battlecode client may speed this up
* to stop midway, run `./gradlew --stop` in `java` folder (not `src`), try not to do this as it may cause thread/lock problems with `compare_bots.py`, but you can do it stop a match you started manually in the client
* if fails to run on macOS, try using the original: https://github.com/chenyx512/battlecode24/blob/main/compare_bots.py


5. If ran until completion, the script will show you your bot's winrate and update the logs. If your bot is good enough - use your own judgment, as marginal improvements may not outweight the bytecode costs if you added a lot of code - then copy your bot to `ref_best`, e.g. `python copybot.py {your_bot} ref_best`, optionally add a comment detailing changes in `log.txt`. Bytecode optimizations are also good, but please run the tests to make sure core functionality still works.

6. Git push. If there is a conflict that probably means that `ref_best` was updated while you were testing. You will need to git pull (and merge), and run the tests again.


### What to add

Finished items that could still be improved are ~~struckthrough~~.

* splashers
* ~~soldiers dot enemy ruins~~ done
* rework clump avoidance - current mech sucks
* tune heuristics - don't waste too much time on this, planning on making something that automatically tunes
* bytecode opts
* soldiers stop idling around the tower while waiting for it to complete (memory that goes back to tower when 1000 chips?)
* ???

### Random Ideas

These are less important.

* switch to Super-Cow-Powers-style SRP tiling on maps with large, open spaces? (current tiling is local & greedy). May require comms
* better self-destruct logic?
* ~~make paint tower reserve paint for refills?~~ done
* place defense tower in contested areas instead of using build order? - maybe make tower drop mark when health is not full?
* improve build order?
* dynamic build order depending map size and such?
