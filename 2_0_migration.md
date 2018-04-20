Ticker 2.0
==========

Here are the main changes in Ticker 2.0.


Main animation changes
----------------------

Ticker now supports automatic wrap-around animation when the animation distance for wrap-around is
shorter than the default animation distance. For example, using a default number list, the animation
from '7' to '9' will be '7' -> '8' -> '9', and the animation from '9' to '1' will be '9' -> '0' -> 
'1' because that's faster than going from 9 all the way down to 1.

When animating between two characters, if both characters do not exist in one character list, then
we will perform a default animation that directly animates between the two characters without
having to go through the entire character list.

You can now also provide any number of character lists for more customized behavior. For example,
you can provide a character list for numbers, and another character list for alphabet. This way,
when there is an animation between 'A' and '1', it'll use the default animation of directly going
from 'A' to '1' rather than going down the entire alphabet list.


Misc API changes
----------------

* Change character list API to be a simple `String` rather than `char[]`. This allows you to more
naturally define a character list, e.g. `"0123456789"`.
* Provided character lists do NOT need to include `TickerUtils.EMPTY_CHAR` anymore. The system
will handle that for you. 
* `TickerView.setCharacterList` is now called `TickerView.setCharacterLists`, and it now takes in
a vararg of character lists to allow for more custom behavior. See documentation on the method for
more information.
* All of `TickerUtils` methods are changed.
* Changed default styleable tag from `ticker_tickerView` to `TickerView`.
* Changed possible values for `ticker_defaultCharacterList` attribute.


How to migrate?
---------------

* Revisit how you define your character lists and see if you need to rewrite your custom character
list, and remove any usages of `TickerUtils.EMPTY_CHAR`.
* If you were using `TickerUtils.getDefaultNumberList` or `TickerUtils.getUSCurrencyList`, migrate
to use `TickerUtils.provideNumberList`.
* Change any calls to `TickerView.setCharacterList` to be `TickerView.setCharacterLists`.
